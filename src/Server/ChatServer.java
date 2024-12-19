package src.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ChatServer {
	private static final int PORT = 5000;
	private static final int MAX_CLIENTS = 100;
	private static final int CONNECTION_TIMEOUT = 60000;
	private static final int MAX_LOGIN_ATTEMPTS = 3;
	private static final int LOGIN_BLOCK_DURATION = 15 * 60;

	private static final Logger SECURITY_LOGGER = Logger.getLogger("SecurityLogger");
	private static final ConcurrentMap<String, UserAccount> userAccounts = new ConcurrentHashMap<>();
	private static final ConcurrentMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();
	private static final ExecutorService clientExecutor = Executors.newFixedThreadPool(MAX_CLIENTS);
	private static final Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

	// Chat room management
	private static class ChatRoom {
		private final String name;
		private final Set<ClientHandler> members = ConcurrentHashMap.newKeySet();

		ChatRoom(String name) {
			this.name = name;
		}

		void addMember(ClientHandler client) {
			members.add(client);
		}

		void removeMember(ClientHandler client) {
			members.remove(client);
		}

		void broadcast(String sender, String message) {
			for (ClientHandler member : members) {
				member.sendMessage(name, sender, message);
			}
		}
	}

	private static class UserAccount {
		final String hashedPassword;
		final byte[] salt;
		final AtomicInteger loginAttempts = new AtomicInteger(0);
		volatile Instant lockoutTime;
		final Set<String> joinedRooms = ConcurrentHashMap.newKeySet();

		UserAccount(String hashedPassword, byte[] salt) {
			this.hashedPassword = hashedPassword;
			this.salt = salt;
		}

		boolean canAttemptLogin() {
			return lockoutTime == null || Instant.now().isAfter(lockoutTime);
		}

		void lockAccount() {
			if (loginAttempts.incrementAndGet() >= MAX_LOGIN_ATTEMPTS) {
				lockoutTime = Instant.now().plusSeconds(LOGIN_BLOCK_DURATION);
			}
		}

		void resetAttempts() {
			loginAttempts.set(0);
			lockoutTime = null;
		}
	}

	// Sanitize input to prevent injection and trim whitespace
	private static String sanitizeInput(String input) {
		if (input == null) return null;
		return input.replaceAll("[\\p{Cntrl}]", "").trim();
	}

	// Password complexity validation
	private static boolean isValidPassword(String password) {
		if (password == null || password.length() < 12) return false;

		boolean hasUppercase = false;
		boolean hasLowercase = false;
		boolean hasDigit = false;
		boolean hasSpecialChar = false;

		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c)) hasUppercase = true;
			if (Character.isLowerCase(c)) hasLowercase = true;
			if (Character.isDigit(c)) hasDigit = true;
			if (!Character.isLetterOrDigit(c)) hasSpecialChar = true;
		}

		return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
	}

	private static String hashPassword(String password, byte[] salt) {
		try {
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			byte[] hash = factory.generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			SECURITY_LOGGER.severe("Password hashing failed: " + e.getMessage());
			return null;
		}
	}

	private static boolean authenticateUser(String username, String password) {
		UserAccount account = userAccounts.get(username);

		if (account == null || !account.canAttemptLogin()) {
			return false;
		}

		String hashedInputPassword = hashPassword(password, account.salt);
		boolean passwordMatch = MessageDigest.isEqual(
				hashedInputPassword.getBytes(StandardCharsets.UTF_8),
				account.hashedPassword.getBytes(StandardCharsets.UTF_8)
		);

		if (passwordMatch) {
			account.resetAttempts();
			return true;
		} else {
			account.lockAccount();
			return false;
		}
	}

	private static String registerNewUser(BufferedReader input, PrintWriter output) throws IOException {
		output.println("Enter username (min 3 characters):");
		String username = sanitizeInput(input.readLine());

		if (username == null || !username.matches("^[a-zA-Z0-9._-]{3,}$") || userAccounts.containsKey(username)) {
			output.println("Invalid username. Registration failed.");
			return null;
		}

		output.println("Enter password (min 12 chars, mix of upper/lower/number/special chars):");
		String password = sanitizeInput(input.readLine());

		if (!isValidPassword(password)) {
			output.println("Password does not meet complexity requirements.");
			return null;
		}

		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		String hashedPassword = hashPassword(password, salt);

		if (hashedPassword == null) {
			output.println("Password hashing failed.");
			return null;
		}

		UserAccount newAccount = new UserAccount(hashedPassword, salt);
		userAccounts.put(username, newAccount);
		output.println("Registration successful!");
		return username;
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private BufferedReader input;
		private PrintWriter output;
		private String username;
		private String currentRoom = "GENERAL";

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		private String loginUser() throws IOException {
			output.println("Enter username:");
			String username = sanitizeInput(input.readLine());
			output.println("Enter password:");
			String password = sanitizeInput(input.readLine());

			if (authenticateUser(username, password)) {
				output.println("Login successful!");
				return username;
			}
			output.println("Login failed.");
			return null;
		}

		private void sendMessage(String sender, String message) {
			output.println(String.format("[GENERAL] %s: %s", sender, message));
		}

		private void sendMessage(String room, String sender, String message) {
			output.println(String.format("[%s] %s: %s", room, sender, message));
		}

		private void processCommand(String command) {
			String[] parts = command.split("\\s+", 3);
			if (parts.length == 0) return;

			String cmd = parts[0].toUpperCase();
			switch (cmd) {
				case "MESSAGE":
					if (parts.length >= 3) {
						String room = parts[1];
						String message = parts[2];
						ChatRoom chatRoom = chatRooms.get(room);
						if (chatRoom != null) {
							chatRoom.broadcast(username, message);
						} else if (room.equals("GENERAL")) {
							// Broadcast to all active clients
							for (ClientHandler client : activeClients.values()) {
								client.sendMessage(username, message);
							}
						}
					}
					break;

				case "JOIN":
					if (parts.length >= 2) {
						String roomName = parts[1];
						ChatRoom room = chatRooms.computeIfAbsent(roomName, ChatRoom::new);
						room.addMember(this);
						currentRoom = roomName;
						UserAccount account = userAccounts.get(username);
						if (account != null) {
							account.joinedRooms.add(roomName);
						}
						sendMessage("SERVER", "Joined room: " + roomName);
					}
					break;

				case "LEAVE":
					if (parts.length >= 2) {
						String roomName = parts[1];
						ChatRoom room = chatRooms.get(roomName);
						if (room != null) {
							room.removeMember(this);
							UserAccount account = userAccounts.get(username);
							if (account != null) {
								account.joinedRooms.remove(roomName);
							}
						}
						currentRoom = "GENERAL";
					}
					break;

				case "PM":
					if (parts.length >= 3) {
						String recipient = parts[1];
						String message = parts[2];
						ClientHandler targetClient = activeClients.get(recipient);
						if (targetClient != null) {
							targetClient.output.println(String.format("[PM] %s: %s", username, message));
							this.output.println(String.format("[PM to %s]: %s", recipient, message));
						} else {
							sendMessage("SERVER", "User " + recipient + " is not online.");
						}
					}
					break;

				case "LOGOUT":
					// Handle cleanup before logout
					for (ChatRoom room : chatRooms.values()) {
						room.removeMember(this);
					}
					activeClients.remove(username);
					break;
			}
		}

		@Override
		public void run() {
			try {
				input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				output = new PrintWriter(clientSocket.getOutputStream(), true);

				while (username == null) {
					output.println("1. Login\n2. Register\n3. Exit");
					String choice = sanitizeInput(input.readLine());

					switch (choice) {
						case "1":
							username = loginUser();
							break;
						case "2":
							username = registerNewUser(input, output);
							break;
						case "3":
							return;
						default:
							output.println("Invalid choice.");
					}
				}

				activeClients.put(username, this);
				output.println("Welcome to the chat server!");

				// Join the general chat room by default
				ChatRoom generalRoom = chatRooms.computeIfAbsent("GENERAL", ChatRoom::new);
				generalRoom.addMember(this);

				// Main message processing loop
				String clientMessage;
				while ((clientMessage = input.readLine()) != null) {
					processCommand(clientMessage);
				}

			} catch (IOException e) {
				SECURITY_LOGGER.warning("Client connection error: " + e.getMessage());
			} finally {
				try {
					if (username != null) {
						// Clean up when client disconnects
						for (ChatRoom room : chatRooms.values()) {
							room.removeMember(this);
						}
						activeClients.remove(username);
					}
					clientSocket.close();
				} catch (IOException e) {
					SECURITY_LOGGER.severe("Error closing socket: " + e.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				clientExecutor.shutdownNow();
				SECURITY_LOGGER.info("Server shut down gracefully.");
			} catch (Exception e) {
				SECURITY_LOGGER.severe("Error during shutdown: " + e.getMessage());
			}
		}));

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			SECURITY_LOGGER.info("Secure Chat Server started on port " + PORT);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				clientExecutor.submit(new ClientHandler(clientSocket));
			}
		} catch (IOException e) {
			SECURITY_LOGGER.severe("Server startup failed: " + e.getMessage());
		}
	}
}

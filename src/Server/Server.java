package Server;

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
import javax.swing.*;

public class Server {
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
	private static final String USER_DATA_FILE = "user_accounts.dat";

	static {
		loadUserAccounts();
	}

	private static void loadUserAccounts() {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
			@SuppressWarnings("unchecked")
			Map<String, UserAccount> loadedAccounts = (Map<String, UserAccount>) ois.readObject();
			userAccounts.putAll(loadedAccounts);
			SECURITY_LOGGER.info("Loaded " + loadedAccounts.size() + " user accounts");
		} catch (IOException | ClassNotFoundException e) {
			SECURITY_LOGGER.info("No existing user accounts found or error loading accounts");
		}
	}

	private static void saveUserAccounts() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
			oos.writeObject(new HashMap<>(userAccounts));
			SECURITY_LOGGER.info("Saved " + userAccounts.size() + " user accounts");
		} catch (IOException e) {
			SECURITY_LOGGER.severe("Error saving user accounts: " + e.getMessage());
		}
	}

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

		void broadcastImage(String sender, String fileName) {
			File imageFile = new File(fileName + ".png");
			byte[] imageData = new byte[(int) imageFile.length()];
			for (ClientHandler member : members) {
				member.sendImage(name, sender, imageData, fileName);
			}
		}
	}

	// Make UserAccount serializable
	private static class UserAccount implements Serializable {
		private static final long serialVersionUID = 1L;
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
		String username = sanitizeInput(input.readLine());
		String password = sanitizeInput(input.readLine());

		if (username == null || !username.matches("^[a-zA-Z0-9._-]{3,}$")) {
			output.println("Registration failed: Invalid username");
			return null;
		}

		if (userAccounts.containsKey(username)) {
			output.println("Registration failed: Username already exists");
			return null;
		}

		if (!isValidPassword(password)) {
			output.println("Registration failed: Password does not meet requirements");
			return null;
		}

		byte[] salt = new byte[16];
		new SecureRandom().nextBytes(salt);
		String hashedPassword = hashPassword(password, salt);

		if (hashedPassword == null) {
			output.println("Registration failed: Internal error");
			return null;
		}

		UserAccount newAccount = new UserAccount(hashedPassword, salt);
		userAccounts.put(username, newAccount);
		saveUserAccounts(); // Save after registration
		output.println("Registration successful!");
		return username;
	}

	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private BufferedReader input;
		private PrintWriter output;
		private DataInputStream inputStream;
		private DataOutputStream outputStream;
		private String username;
		private String currentRoom = "GENERAL";
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
			try {
				this.inputStream = new DataInputStream(clientSocket.getInputStream());
				this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
				startImageReceiver();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

		private void sendImage(String room, String sender, byte[] data, String fileName) {
			try {
				outputStream.writeUTF(fileName);
				outputStream.writeInt(data.length);
				outputStream.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void sendImage(String room, String sender, String fileName) {
			File imageFile = new File(fileName + ".png");
			byte[] imageData = new byte[(int) imageFile.length()];
			try {
				outputStream.writeUTF(fileName);
				outputStream.writeInt(imageData.length);
				outputStream.write(imageData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void receiveImage(InputStream inputStream, String imageName) throws IOException {
			FileOutputStream fileOutputStream = new FileOutputStream(imageName);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, bytesRead);
			}

			fileOutputStream.close();
		}

		private void startImageReceiver() throws IOException {
			executor.execute(() -> {
				try {
					inputStream = new DataInputStream(clientSocket.getInputStream());
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					while (username != null) {
						String fileName = inputStream.readUTF();
						if (!fileName.equals("SavedMSG")) {
							int length = inputStream.readInt();
							byte[] imageData = new byte[length];
							inputStream.readFully(imageData, 0, length);
							File imageFile = new File(fileName + ".png");
							try (FileOutputStream fos = new FileOutputStream(imageFile)) {
								fos.write(imageData);
							}
						} else {
							int length = inputStream.readInt();
							byte[] imageData = new byte[length];
							inputStream.readFully(imageData, 0, length);
							File imageFile = new File(fileName + ".txt");
							try (FileOutputStream fos = new FileOutputStream(imageFile)) {
								fos.write(imageData);
							}
						}
					}
				} catch (IOException e) {

				}
			});
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

				case "IMG":
					if (parts.length >= 3) {
						String recipient = parts[1];
						String imageName = parts[2];
						ClientHandler targetClient = activeClients.get(recipient);
						if (targetClient != null) {
							sendImage("", "", imageName);
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
				//InputStream inputStream = clientSocket.getInputStream();
				//OutputStream outputStream = clientSocket.getOutputStream();

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
		// Add shutdown hook to save user accounts
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			saveUserAccounts();
			clientExecutor.shutdownNow();
			SECURITY_LOGGER.info("Server shut down gracefully.");
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

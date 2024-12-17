package src.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
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

	private static SecretKey encryptionKey;

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
		// Remove non-printable characters and trim
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
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt);
			byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

			// Convert to hexadecimal representation
			StringBuilder sb = new StringBuilder();
			for (byte b : hashedBytes) {
				sb.append(Integer.toHexString(Byte.toUnsignedInt(b)));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			SECURITY_LOGGER.severe("Password hashing failed: " + e.getMessage());
			return null;
		}
	}

	// User authentication method
	private static boolean authenticateUser(String username, String password) {
		UserAccount account = userAccounts.get(username);

		// Check if account exists and can attempt login
		if (account == null || !account.canAttemptLogin()) {
			return false;
		}

		// Hash the provided password with the stored salt
		String hashedInputPassword = hashPassword(password, account.salt);

		// Constant-time comparison to prevent timing attacks
		boolean passwordMatch = MessageDigest.isEqual(
				hashedInputPassword.getBytes(StandardCharsets.UTF_8),
				account.hashedPassword.getBytes(StandardCharsets.UTF_8)
		);

		if (passwordMatch) {
			// Reset login attempts on successful login
			account.resetAttempts();
			return true;
		} else {
			// Increment failed login attempts
			account.lockAccount();
			return false;
		}
	}

	// Registration method
	private static String registerNewUser(BufferedReader input, PrintWriter output) throws IOException {
		output.println("Enter username (min 3 characters):");
		String username = sanitizeInput(input.readLine());

		// Validate username
		if (username == null || username.length() < 3 || userAccounts.containsKey(username)) {
			output.println("Invalid username. Registration failed.");
			return null;
		}

		output.println("Enter password (min 12 chars, mix of upper/lower/number/special chars):");
		String password = sanitizeInput(input.readLine());

		// Validate password complexity
		if (!isValidPassword(password)) {
			output.println("Password does not meet complexity requirements.");
			return null;
		}

		// Generate salt and hash password
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

	// Nested ClientHandler class (stub for demonstration)
	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private BufferedReader input;
		private PrintWriter output;
		private String username;

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

		@Override
		public void run() {
			try {
				// Initialize input and output streams
				input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				output = new PrintWriter(clientSocket.getOutputStream(), true);

				// Authentication flow
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

				// Additional client handling logic would go here
			} catch (IOException e) {
				SECURITY_LOGGER.warning("Client connection error: " + e.getMessage());
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
					SECURITY_LOGGER.severe("Error closing socket: " + e.getMessage());
				}
			}
		}
	}

	// Main server startup method (stub)
	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			SECURITY_LOGGER.info("Secure Chat Server started on port " + PORT);

			while (true) {
				Socket clientSocket = serverSocket.accept();
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				clientExecutor.submit(clientHandler);
			}
		} catch (IOException e) {
			SECURITY_LOGGER.severe("Server startup failed: " + e.getMessage());
		} finally {
			clientExecutor.shutdown();
		}
	}
}
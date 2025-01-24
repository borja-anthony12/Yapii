package Client;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Client {
	//Stefs Branch
	private static final String HOST = "localhost";
	private static final int PORT = 5000;
	private Socket socket;
	private BufferedReader serverInput;
	private PrintWriter serverOutput;
	private final ClientDisplay display;
	private volatile boolean isRunning = true;
	private String currentRoom = "GENERAL";
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final ExecutorService executor2 = Executors.newSingleThreadExecutor();

	public Client(ClientDisplay display) {
		this.display = display;
		setupNetworking();
		if (socket != null && socket.isConnected()) {
			startMessageReceiver();
			startImageReceiver();
		}
	}

	private void setupNetworking() {
		try {
			socket = new Socket(HOST, PORT);
			serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			serverOutput = new PrintWriter(socket.getOutputStream(), true);
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			SwingUtilities.invokeLater(() ->
					display.showError("Could not connect to server: " + e.getMessage())
			);
			shutdown(); // Ensure resources are cleaned up
		}
	}

	private void startMessageReceiver() {
		executor.execute(() -> {
			try {
				String message;
				while (isRunning && (message = serverInput.readLine()) != null) {
					final String finalMessage = message;
					SwingUtilities.invokeLater(() -> processServerMessage(finalMessage));
				}
			} catch (IOException e) {
				if (isRunning) {
					SwingUtilities.invokeLater(() ->
							display.showError("Lost connection to server")
					);
				}
			}
		});
	}

	private void startImageReceiver() {
		executor2.execute(() -> {
			try {
				inputStream = new DataInputStream(socket.getInputStream());
				outputStream = new DataOutputStream(socket.getOutputStream());
				while (isRunning) {
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
				if (isRunning) {
					SwingUtilities.invokeLater(() ->
							display.showError("Lost connection to server")
					);
				}
			}
		});
	}

	private void processServerMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			if (message.contains("Login successful")) {
				display.showPage("MAIN");
				display.appendMessage("\u001B[32m" + message + "\u001B[0m");
			} else if (message.contains("Registration successful")) {
				display.showPage("LOGIN");
				display.appendMessage("\u001B[32m" + message + "\u001B[0m");
			} else if (message.contains("Login failed") || message.contains("Registration failed")) {
				display.appendMessage("\u001B[31m" + message + "\u001B[0m");
			} else {
				display.appendMessage(message);
			}
		});
	}

	public void sendMessage(String text) {
	    if (text.startsWith("/")) {
	        handleCommand(text);
	    } else {
	        serverOutput.println("MESSAGE " + currentRoom + " " + text);
	    }
	}

	private void sendImage(byte[] data, String fileName) {
		try {
			outputStream.writeUTF(fileName);
			outputStream.writeInt(data.length);
			outputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendImage(String fileName) {
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

	public void authenticate(String username, String password, boolean isRegistration) {
		if (socket == null || !socket.isConnected()) {
			setupNetworking();
		}

		try {
			// Send authentication type
			serverOutput.println(isRegistration ? "2" : "1");
			// Send credentials
			serverOutput.println(username);
			serverOutput.println(password);

			// Store username for later use
			if (!isRegistration) {
				display.setName(username);
			}

		} catch (Exception e) {
			display.showError("Authentication error: " + e.getMessage());
		}
	}

	private void handleCommand(String command) {
		String[] parts = command.split("\\s+", 3);
		String cmd = parts[0].toLowerCase();

		switch (cmd) {
			case "/exit" -> handleExit();
			case "/join" -> handleJoin(parts);
			case "/pm" -> handlePrivateMessage(parts);
			case "/leave" -> handleLeave();
			case "/img" -> handleImg(parts);
			default -> display.appendMessage("Unknown command. Available: /join, /leave, /pm, /exit");
		}
	}

	private void handleImg(String[] parts) {
		//part 1 currently useless
		sendImage(parts[2]);
	}

	private void handleExit() {
		serverOutput.println("LOGOUT");
		shutdown();
		System.exit(0);
	}

	private void handleJoin(String[] parts) {
		if (parts.length >= 2) {
			currentRoom = parts[1].toUpperCase();
			serverOutput.println("JOIN " + currentRoom);
		}
	}

	private void handlePrivateMessage(String[] parts) {
		if (parts.length >= 3) {
			serverOutput.println("PM " + parts[1] + " " + parts[2]);
		}
	}

	private void handleLeave() {
		if (!currentRoom.equals("GENERAL")) {
			serverOutput.println("LEAVE " + currentRoom);
			currentRoom = "GENERAL";
		}
	}

	public void shutdown() {
		isRunning = false;
		executor.shutdownNow();
		try {
			if (socket != null && !socket.isClosed()) {
				serverOutput.println("LOGOUT");
				socket.close();
			}
		} catch (IOException e) {
			SwingUtilities.invokeLater(() ->
					display.showError("Error during shutdown: " + e.getMessage())
			);
		} finally {
			if (!executor.isTerminated()) {
				try {
					executor.awaitTermination(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new ClientDisplay();
	}
}
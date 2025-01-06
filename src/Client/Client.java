package Client;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Client {
	private static final String HOST = "localhost";
	private static final int PORT = 5000;
	private Socket socket;
	private BufferedReader serverInput;
	private PrintWriter serverOutput;
	private ClientDisplay display;
	private volatile boolean isRunning = true;
	private String currentRoom = "GENERAL";

	public Client(ClientDisplay display) {
		this.display = display;
		setupNetworking();
		startMessageReceiver();
	}

	private void setupNetworking() {
		try {
			socket = new Socket(HOST, PORT);
			serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			serverOutput = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			display.showError("Could not connect to server");
		}
	}

	private void startMessageReceiver() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			try {
				String message;
				while (isRunning && (message = serverInput.readLine()) != null) {
					final String finalMessage = message;
					javax.swing.SwingUtilities.invokeLater(() -> processServerMessage(finalMessage));
				}
			} catch (IOException e) {
				if (isRunning) {
					display.showError("Lost connection to server");
				}
			}
		});
		executor.shutdown();
	}

	private void processServerMessage(String message) {
		if (message.contains("Login successful")) {
			display.showMainPanel();
			display.appendMessage("\u001B[32m" + message + "\u001B[0m");
		} else if (message.contains("Registration successful")) {
			display.appendMessage("\u001B[32m" + message + "\u001B[0m");
		} else if (message.contains("Login failed")) {
			display.appendMessage("\u001B[31m" + message + "\u001B[0m");
		} else {
			display.appendMessage(message);
		}
	}

	public void sendMessage(String text) {
		if (text.startsWith("/")) {
			handleCommand(text);
		} else {
			serverOutput.println("MESSAGE " + currentRoom + " " + text);
		}
	}

	public void authenticate(String username, String password, boolean isRegistration) {
		serverOutput.println(isRegistration ? "2" : "1");
		serverOutput.println(username);
		serverOutput.println(password);
	}

	private void handleCommand(String command) {
		String[] parts = command.split("\\s+", 3);
		String cmd = parts[0].toLowerCase();

		switch (cmd) {
			case "/exit":
				serverOutput.println("LOGOUT");
				System.exit(0);
				break;
			case "/join":
				if (parts.length >= 2) {
					currentRoom = parts[1].toUpperCase();
					serverOutput.println("JOIN " + currentRoom);
				}
				break;
			case "/pm":
				if (parts.length >= 3) {
					serverOutput.println("PM " + parts[1] + " " + parts[2]);
				}
				break;
			case "/leave":
				if (!currentRoom.equals("GENERAL")) {
					serverOutput.println("LEAVE " + currentRoom);
					currentRoom = "GENERAL";
				}
				break;
			default:
				display.appendMessage("Unknown command. Type /help for available commands.");
		}
	}

	public void shutdown() {
		isRunning = false;
		if (serverOutput != null) {
			serverOutput.println("LOGOUT");
		}
	}
}
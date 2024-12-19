package src.Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class Client {
	private static final String HOST = "localhost";
	private static final int PORT = 5000;
	private static volatile boolean isRunning = true;
	private static volatile boolean isLoggedIn = false;
	private static String currentRoom = "GENERAL";

	public static void main(String[] args) {
		try (Socket socket = new Socket(HOST, PORT);
			 BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
			 Scanner userScanner = new Scanner(System.in)) {

			Object menuLock = new Object();
			ExecutorService executor = Executors.newFixedThreadPool(2);

			Future<?> receiveTask = executor.submit(() -> receiveMessages(serverInput));
			Future<?> inputTask = executor.submit(() -> handleUserInput(userScanner, serverOutput, menuLock));

			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);

		} catch (IOException | InterruptedException e) {
			System.out.println("Error connecting to server: " + e.getMessage());
		}
	}

	private static void receiveMessages(BufferedReader serverInput) {
		try {
			String serverMessage;
			while (isRunning && (serverMessage = serverInput.readLine()) != null) {
				processServerMessage(serverMessage);
			}
		} catch (IOException e) {
			System.out.println("Connection lost.");
			isRunning = false;
		}
	}

	private static void handleUserInput(Scanner scanner, PrintWriter serverOutput, Object menuLock) {
		while (isRunning) {
			synchronized (menuLock) {
				if (!isLoggedIn) {
					displayMainMenu(scanner, serverOutput);
				} else {
					handleLoggedInMenu(scanner, serverOutput);
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private static void displayMainMenu(Scanner scanner, PrintWriter serverOutput) {
		System.out.println("\n--- Main Menu ---");
		System.out.println("1. Login");
		System.out.println("2. Register");
		System.out.println("3. Exit");
		System.out.print("Choose an option: ");

		String choice = scanner.nextLine().trim();
		switch (choice) {
			case "1":
				performLogin(scanner, serverOutput);
				break;
			case "2":
				performRegistration(scanner, serverOutput);
				break;
			case "3":
				isRunning = false;
				serverOutput.println("3"); // Send exit signal to server
				break;
			default:
				System.out.println("Invalid option. Please try again.");
		}
	}

	private static void performLogin(Scanner scanner, PrintWriter serverOutput) {
		System.out.print("Enter username: ");
		String username = scanner.nextLine().trim();
		System.out.print("Enter password: ");
		String password = scanner.nextLine().trim();

		serverOutput.println("1");
		serverOutput.println(username);
		serverOutput.println(password);
	}

	private static void performRegistration(Scanner scanner, PrintWriter serverOutput) {
		System.out.print("Enter desired username (min 3 characters): ");
		String username = scanner.nextLine().trim();
		System.out.print("Enter password (min 12 chars, mix of upper/lower/number/special chars): ");
		String password = scanner.nextLine().trim();

		serverOutput.println("2");
		serverOutput.println(username);
		serverOutput.println(password);
	}

	private static void processServerMessage(String message) {
		if (message == null) return;

		if (message.contains("Login successful")) {
			isLoggedIn = true;
			System.out.println("\u001B[32m" + message + "\u001B[0m"); // Green text
		} else if (message.contains("Registration successful")) {
			System.out.println("\u001B[32m" + message + "\u001B[0m"); // Green text
		} else if (message.contains("Login failed")) {
			System.out.println("\u001B[31m" + message + "\u001B[0m"); // Red text
			isLoggedIn = false;
		} else if (message.startsWith("[GENERAL]")) {
			System.out.println("\u001B[36m" + message + "\u001B[0m"); // Cyan text
		} else if (message.startsWith("[") && message.contains("]")) {
			// Room messages or private messages
			System.out.println("\u001B[35m" + message + "\u001B[0m"); // Magenta text
		} else {
			System.out.println(message);
		}
	}

	private static void handleLoggedInMenu(Scanner scanner, PrintWriter serverOutput) {
		System.out.print("\nEnter command or message (type /help for commands): ");
		String userInput = scanner.nextLine().trim();

		if (userInput.isEmpty()) {
			return;
		}

		if (userInput.startsWith("/")) {
			handleCommand(userInput, serverOutput);
		} else {
			// Send message to current room or general chat
			String messageCommand = String.format("MESSAGE %s %s", currentRoom, userInput);
			serverOutput.println(messageCommand);
		}
	}

	private static void handleCommand(String command, PrintWriter serverOutput) {
		String[] parts = command.split("\\s+", 3);
		String cmd = parts[0].toLowerCase();

		switch (cmd) {
			case "/exit":
				serverOutput.println("LOGOUT");
				isLoggedIn = false;
				System.out.println("Logged out successfully.");
				break;

			case "/help":
				displayHelpMenu();
				break;

			case "/join":
				if (parts.length >= 2) {
					String roomName = parts[1].toUpperCase();
					serverOutput.println("JOIN " + roomName);
					currentRoom = roomName;
				} else {
					System.out.println("Usage: /join <roomname>");
				}
				break;

			case "/pm":
				if (parts.length >= 3) {
					String recipient = parts[1];
					String message = parts[2];
					serverOutput.println("PM " + recipient + " " + message);
				} else {
					System.out.println("Usage: /pm <username> <message>");
				}
				break;

			case "/leave":
				if (currentRoom.equals("GENERAL")) {
					System.out.println("Cannot leave the general chat.");
				} else {
					serverOutput.println("LEAVE " + currentRoom);
					currentRoom = "GENERAL";
					System.out.println("Returned to general chat.");
				}
				break;

			default:
				System.out.println("Unknown command. Type /help for available commands.");
				break;
		}
	}

	private static void displayHelpMenu() {
		System.out.println("\n--- Available Commands ---");
		System.out.println("/exit - Logout and return to main menu");
		System.out.println("/help - Show this help menu");
		System.out.println("/pm <username> <message> - Send private message");
		System.out.println("/join <roomname> - Join/create chat room");
		System.out.println("/leave - Leave current room and return to general chat");
		System.out.println("Just type your message to chat in the current room");
		System.out.println("Current room: " + currentRoom);
	}
}
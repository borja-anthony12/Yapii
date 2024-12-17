package src.Client;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private Socket socket;
	private BufferedReader br;
	private BufferedWriter bw;
	private String username;

	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.username = username;
			this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			closeEverything(socket, br, bw);
		}
	}

	public void sendMessage() {
		try {
			bw.write(username);
			bw.newLine();
			bw.flush();

			Scanner scanner = new Scanner(System.in);
			while (socket.isConnected()) {
				String message = scanner.nextLine();
				bw.write("\u001B[32m" + username + "\u001B[0m" + ": " + message + "\u001B[0m");
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			closeEverything(socket, br, bw);
		}
	}

	public void listenForMessage() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String message;
				while (socket.isConnected()) {
					try {
						message = br.readLine();
						System.out.println(message);
					} catch (IOException e) {
						closeEverything(socket, br, bw);
					}
				}
			}
		}).start();
	}

	private void closeEverything(Socket socket, BufferedReader br, BufferedWriter bw) {
		try {
			if (socket != null)
				socket.close();
			if (br != null)
				br.close();
			if (bw != null)
				bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter your username: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 5000);
		Client client = new Client(socket, username);
		client.listenForMessage();
		client.sendMessage();
	}
}

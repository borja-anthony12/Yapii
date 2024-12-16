package main;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	private Socket socket;
	private BufferedReader br;
	private BufferedWriter bw;
	private String clientUsername;

	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = br.readLine();
			clientHandlers.add(this);
			broadcastMessage("\u001B[33m" + "Server: " + clientUsername + " has entered the chat");
		} catch (IOException e) {
			closeEverything(socket, br, bw);
		}
	}

	public void broadcastMessage(String message) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if (!clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.bw.write(message);
					clientHandler.bw.newLine();
					clientHandler.bw.flush();
				}
			} catch (IOException e) {
				closeEverything(socket, br, bw);
			}
		}
	}

	private void closeEverything(Socket socket, BufferedReader br, BufferedWriter bw) {
		clientHandlers.remove(this);
		broadcastMessage("\u001B[33m" + "Server: " + clientUsername + " has left the chat");
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

	@Override
	public void run() {
		String message;
		while (socket.isConnected()) {
			try {
				message = br.readLine();
				broadcastMessage(message);
			} catch (IOException e) {
				closeEverything(socket, br, bw);
				break;
			}
		}
	}
}
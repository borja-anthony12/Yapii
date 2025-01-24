package game;

import Client.ClientDisplay;
import javax.swing.*;
import java.io.PrintWriter;
import java.net.Socket;

public class GameLauncher extends SpleefGame {
    private ClientDisplay parentDisplay;
    private PrintWriter serverOutput;
    private Socket clientSocket;
    private String username;

    public GameLauncher(ClientDisplay display, PrintWriter serverOutput, Socket clientSocket, String username) {
        super();
        this.parentDisplay = display;
        this.serverOutput = serverOutput;
        this.clientSocket = clientSocket;
        this.username = username;
        
        // Customize game window
        setTitle("Spleef Game - " + username);
        
        // Add a way to return to chat
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (serverOutput != null) {
                    serverOutput.println("GAME_LEAVE");
                }
                dispose();
                parentDisplay.setVisible(true);
            }
        });
    }
    
    // Optional: Override main game methods to integrate with server
    @Override
    public void update() {
        super.update();
        // Potential: Add network sync logic for multiplayer
    }
    
    // Static method to launch game from client
    public static void launchGame(ClientDisplay display, PrintWriter serverOutput, Socket clientSocket, String username) {
        SwingUtilities.invokeLater(() -> {
            GameLauncher game = new GameLauncher(display, serverOutput, clientSocket, username);
            display.setVisible(false);
            game.setVisible(true);
        });
    }
}
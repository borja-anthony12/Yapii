package src.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientDisplay extends JFrame {
	private LoginPage loginPage;
	private RegisterPage registerPage;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel messagePanel;
    private JPanel inputPanel;
    private JTextField sendMessage;
    private JButton send;
    private JButton exitButton;
    private JLabel nameLabel;
    
    public ClientDisplay() {
        // Set up the main frame
        setBounds(0, 0, 800, 600);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Initialize components
        loginPage = new LoginPage(this);
        registerPage = new RegisterPage();
        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new BorderLayout());
        messagePanel = new JPanel();
        inputPanel = new JPanel(new BorderLayout());
        sendMessage = new JTextField(30);
        send = new JButton("Send");
        exitButton = new JButton("Exit");
        nameLabel = new JLabel("User Name", SwingConstants.CENTER);
        
        // Style the name label
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Set up exit button functionality
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // Create a panel for the exit button
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exitPanel.add(exitButton);
        
        // Style the top panel
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(exitPanel, BorderLayout.EAST);
        topPanel.add(nameLabel, BorderLayout.CENTER);
        
        // Style the message panel
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Set up the input panel with text field and button
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(sendMessage, BorderLayout.CENTER);
        inputPanel.add(send, BorderLayout.EAST);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);       
        
        add(loginPage);
        
        setVisible(true);
    }
    
    public void showRegistration() {
    	
    }
    
    // Method to update the name label
    public void setName(String name) {
        nameLabel.setText(name);
    }
}
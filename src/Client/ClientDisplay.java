package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ClientDisplay extends JFrame {
    public final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private LoginPage loginPage;
    private RegisterPage registerPage;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel messagePanel;
    private JPanel inputPanel;
    private JTextField messageText;
    private JButton sendButton;
    private JButton exitButton;
    private JButton importFile;
    private JLabel nameLabel;
    private File selectedFile;  // To store the selected file

    public ClientDisplay() {
        // Set up the main frame
        setSize(screenSize);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize components
        loginPage = new LoginPage(this);
        registerPage = new RegisterPage(this);
        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new BorderLayout());
        messagePanel = new JPanel();
        inputPanel = new JPanel(new BorderLayout());
        messageText = new JTextField(30);
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        importFile = new JButton("Import File");
        nameLabel = new JLabel("User Name", SwingConstants.CENTER);

        // Style the name label
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Set up exit button functionality
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Set up send button functionality
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messageText.setText("");
            }
        });

        // Set up import file button functionality
        importFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create file chooser using the native Windows look and feel
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
                FileDialog fileDialog = new FileDialog(ClientDisplay.this, "Choose a file", FileDialog.LOAD);
                fileDialog.setVisible(true);
                
                if (fileDialog.getFile() != null) {
                    selectedFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
                    // Update message text field to show selected file name
                    messageText.setText("Selected file: " + selectedFile.getName());
                }
            }
        });

        // Create a panel for the exit button
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        iconPanel.add(exitButton);

        // Style the top panel
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(iconPanel, BorderLayout.EAST);
        topPanel.add(nameLabel, BorderLayout.WEST);

        // Style the message panel
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a panel for send and import buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(importFile);
        buttonPanel.add(sendButton);

        // Set up the input panel with text field and buttons
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(messageText, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setVisible(true);
    }

    // Getter for selected file
    public File getSelectedFile() {
        return selectedFile;
    }

    public void showPage(String str) {
        switch (str) {
            case "REG":
                setContentPane(registerPage);
                break;
            case "LOGIN":
                setContentPane(loginPage);
                break;
            case "MAIN":
                setContentPane(mainPanel);
                break;
            default:
                System.out.println("Menu string loader invalid");
        }
        revalidate();
        repaint();
    }

    // Method to update the name label
    public void setName(String name) {
        nameLabel.setText(name);
    }
}
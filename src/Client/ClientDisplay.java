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
    private File selectedFile;
    private JLayeredPane layeredPane;
    private Timer animationTimer;
    private boolean isAnimating = false;

    public ClientDisplay() {
    	setSize(screenSize);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize components
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(screenSize);
        
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

        // Position the pages
        loginPage.setBounds(0, 0, screenSize.width, screenSize.height);
        registerPage.setBounds(screenSize.width, 0, screenSize.width, screenSize.height);
        
        // Add pages to layered pane
        layeredPane.add(loginPage, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(registerPage, JLayeredPane.DEFAULT_LAYER);

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

        setContentPane(layeredPane);
        setVisible(true);
    }

    // Getter for selected file
    public File getSelectedFile() {
        return selectedFile;
    }

    public void showPage(String str) {
        if (isAnimating) return;
        
        switch (str) {
            case "REG":
                slideTransition(loginPage, registerPage, true);
                break;
            case "LOGIN":
                slideTransition(registerPage, loginPage, false);
                break;
            case "MAIN":
                layeredPane.removeAll();
                setContentPane(mainPanel);
                break;
        }
        revalidate();
        repaint();
    }
    
    private void slideTransition(JComponent fromPage, JComponent toPage, boolean slideLeft) {
        isAnimating = true;
        int steps = 15;
        int delay = 1;
        
        fromPage.setVisible(true);
        toPage.setVisible(true);
        
        // Initialize positions
        int startFrom = 0;
        int startTo = slideLeft ? screenSize.width : -screenSize.width;
        int endFrom = slideLeft ? -screenSize.width : screenSize.width;
        int endTo = 0;
        
        toPage.setLocation(startTo, 0);
        
        animationTimer = new Timer(delay, null);
        final int[] step = {0};
        
        animationTimer.addActionListener(e -> {
            step[0]++;
            float progress = (float)Math.pow(step[0] / (double)steps, 2);
            
            int currentFromX = startFrom + (int)((endFrom - startFrom) * progress);
            int currentToX = startTo + (int)((endTo - startTo) * progress);
            
            fromPage.setLocation(currentFromX, 0);
            toPage.setLocation(currentToX, 0);
            
            if (step[0] >= steps) {
                animationTimer.stop();
                isAnimating = false;
                fromPage.setVisible(false);
            }
        });
        
        animationTimer.start();
    }

    // Method to update the name label
    public void setName(String name) {
        nameLabel.setText(name);
    }
}
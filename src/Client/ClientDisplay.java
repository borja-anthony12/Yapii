package Client;

import javax.swing.*;
import java.awt.*;
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
    private JTextArea messageArea;
    private JScrollPane scrollPane;
    private Client client;

    public ClientDisplay() {
        setSize(screenSize);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(screenSize);
        initializeComponents();
        client = new Client(this);
    }

    private void initializeComponents() {
        loginPage = new LoginPage(this);
        registerPage = new RegisterPage(this);
        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new BorderLayout());
        messagePanel = new JPanel(new BorderLayout());
        inputPanel = new JPanel(new BorderLayout());
        messageText = new JTextField(30);
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        importFile = new JButton("Import File");
        nameLabel = new JLabel("User Name", SwingConstants.CENTER);
        messageArea = new JTextArea();
        scrollPane = new JScrollPane(messageArea);

        setupComponents();
        setupLayeredPane();
        styleComponents();
        layoutComponents();

        setContentPane(layeredPane);
        setVisible(true);
    }

    private void setupComponents() {
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageArea.setEditable(false);

        exitButton.addActionListener(e -> {
            client.shutdown();
            System.exit(0);
        });

        sendButton.addActionListener(e -> sendMessage());
        messageText.addActionListener(e -> sendMessage());

        importFile.addActionListener(e -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            FileDialog fileDialog = new FileDialog(this, "Choose a file", FileDialog.LOAD);
            fileDialog.setVisible(true);
            
            if (fileDialog.getFile() != null) {
                selectedFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
                messageText.setText("Selected file: " + selectedFile.getName());
            }
        });
    }

    private void setupLayeredPane() {
        loginPage.setBounds(0, 0, screenSize.width, screenSize.height);
        registerPage.setBounds(screenSize.width, 0, screenSize.width, screenSize.height);
        
        layeredPane.add(loginPage, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(registerPage, JLayeredPane.DEFAULT_LAYER);
    }

    private void styleComponents() {
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void layoutComponents() {
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        iconPanel.add(exitButton);

        topPanel.add(iconPanel, BorderLayout.EAST);
        topPanel.add(nameLabel, BorderLayout.WEST);

        messagePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(importFile);
        buttonPanel.add(sendButton);

        inputPanel.add(messageText, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String text = messageText.getText().trim();
        if (!text.isEmpty()) {
            client.sendMessage(text);
            messageText.setText("");
        }
    }

    public void authenticate(String username, String password, boolean isRegistration) {
        client.authenticate(username, password, isRegistration);
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

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void appendMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientDisplay());
    }
}
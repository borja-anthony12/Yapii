package Client;

import javax.swing.*;
import java.awt.*;

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
    private JTextArea messageArea;
    private JScrollPane scrollPane;
    private Client client;

    public ClientDisplay() {
        initializeComponents();
        client = new Client(this);
    }

    private void initializeComponents() {
        setBounds(0, 0, 800, 600);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginPage = new LoginPage(this);
        registerPage = new RegisterPage(this);
        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new BorderLayout());
        messagePanel = new JPanel(new BorderLayout());
        inputPanel = new JPanel(new BorderLayout());
        sendMessage = new JTextField(30);
        send = new JButton("Send");
        exitButton = new JButton("Exit");
        nameLabel = new JLabel("User Name", SwingConstants.CENTER);
        messageArea = new JTextArea();
        scrollPane = new JScrollPane(messageArea);

        setupComponents();
        styleComponents();
        layoutComponents();

        add(loginPage);
        setVisible(true);
    }

    private void setupComponents() {
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageArea.setEditable(false);

        exitButton.addActionListener(e -> {
            client.shutdown();
            System.exit(0);
        });

        send.addActionListener(e -> sendMessage());
        sendMessage.addActionListener(e -> sendMessage());
    }

    private void styleComponents() {
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void layoutComponents() {
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exitPanel.add(exitButton);

        topPanel.add(exitPanel, BorderLayout.EAST);
        topPanel.add(nameLabel, BorderLayout.CENTER);

        messagePanel.add(scrollPane, BorderLayout.CENTER);

        inputPanel.add(sendMessage, BorderLayout.CENTER);
        inputPanel.add(send, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendMessage() {
        String text = sendMessage.getText().trim();
        if (!text.isEmpty()) {
            client.sendMessage(text);
            sendMessage.setText("");
        }
    }

    public void authenticate(String username, String password, boolean isRegistration) {
        client.authenticate(username, password, isRegistration);
    }

    public void showMainPanel() {
        getContentPane().removeAll();
        getContentPane().add(mainPanel);
        revalidate();
        repaint();
    }

    public void showRegistration() {
        getContentPane().removeAll();
        getContentPane().add(registerPage);
        revalidate();
        repaint();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientDisplay());
    }
    class LoginPage extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton;
        private JButton registerButton;
        private ClientDisplay parent;

        public LoginPage(ClientDisplay parent) {
            this.parent = parent;
            setLayout(new GridBagLayout());
            initializeComponents();
        }

        private void initializeComponents() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);
            loginButton = new JButton("Login");
            registerButton = new JButton("Register");

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Username:"), gbc);

            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Password:"), gbc);

            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(loginButton);
            buttonPanel.add(registerButton);
            add(buttonPanel, gbc);

            loginButton.addActionListener(e -> {
                parent.authenticate(usernameField.getText(), new String(passwordField.getPassword()), false);
                parent.setName(usernameField.getText());
            });

            registerButton.addActionListener(e -> parent.showRegistration());
        }
    }

    class RegisterPage extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton registerButton;
        private JButton backButton;
        private ClientDisplay parent;

        public RegisterPage(ClientDisplay parent) {
            this.parent = parent;
            setLayout(new GridBagLayout());
            initializeComponents();
        }

        private void initializeComponents() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);
            registerButton = new JButton("Register");
            backButton = new JButton("Back to Login");

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Desired Username:"), gbc);

            gbc.gridx = 1;
            add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Password:"), gbc);

            gbc.gridx = 1;
            add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(registerButton);
            buttonPanel.add(backButton);
            add(buttonPanel, gbc);

            registerButton.addActionListener(e -> {
                parent.authenticate(usernameField.getText(), new String(passwordField.getPassword()), true);
                parent.setName(usernameField.getText());
            });

            backButton.addActionListener(e -> {
                Container container = getParent();
                if (container != null) {
                    container.remove(this);
                    container.add(new LoginPage(parent));
                    container.revalidate();
                    container.repaint();
                }
            });
        }
    }
}
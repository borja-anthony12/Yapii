package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;

public class RegisterPage extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    
    public RegisterPage(ClientDisplay display) {
        setLayout(new BorderLayout());
        setPreferredSize(ClientDisplay.screenSize);
        
        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(178, 230, 210));  // Mint green
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 30, 5, 30);
        
        // Register header
        JLabel registerHeader = new JLabel("REGISTER");
        registerHeader.setFont(new Font("Arial", Font.BOLD, 96));
        registerHeader.setForeground(Color.WHITE);
        registerHeader.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.insets = new Insets(0, 30, 20, 30);
        mainPanel.add(registerHeader, gbc);
        
        // Username field
        addFormField(mainPanel, "USERNAME", usernameField = new JTextField(30), gbc);
        
        // Email field
        addFormField(mainPanel, "EMAIL", emailField = new JTextField(30), gbc);
        
        // Password field
        addFormField(mainPanel, "PASSWORD", passwordField = new JPasswordField(30), gbc);
        
        // Confirm Password field
        addFormField(mainPanel, "CONFIRM PASSWORD", confirmPasswordField = new JPasswordField(30), gbc);
        
        // Back to login link panel
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loginPanel.setOpaque(false);
        JLabel existingUserLabel = new JLabel("Already have an account? ");
        existingUserLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        existingUserLabel.setForeground(Color.WHITE);
        JLabel loginLabel = new JLabel("Login here");
        loginLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        loginLabel.setForeground(new Color(51, 122, 183));
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                display.showPage("LOGIN");
            }
        });
        loginPanel.add(existingUserLabel);
        loginPanel.add(loginLabel);
        gbc.insets = new Insets(10, 30, 20, 30);
        mainPanel.add(loginPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        setOpaque(false);
    }
    
    private void addFormField(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 20));
        label.setForeground(Color.WHITE);
        gbc.insets = new Insets(5, 30, 5, 30);
        panel.add(label, gbc);
        
        styleField(field);
        panel.add(field, gbc);
    }
    
    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 55));
        field.setFont(new Font("Arial", Font.PLAIN, 20));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (validateInputs(username, email, password, confirmPassword)) {
            showMessage(username + " is registered");
        }
    }
    
    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        if (username.trim().isEmpty()) {
            showError("Username cannot be empty");
            return false;
        }
        
        if (email.trim().isEmpty() || !email.contains("@")) {
            showError("Please enter a valid email address");
            return false;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Successful Registration", JOptionPane.INFORMATION_MESSAGE);
    }
}
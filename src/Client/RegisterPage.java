package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPage extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton backToLoginButton;

    public RegisterPage(ClientDisplay display) {
        
        // Create main panel with padding
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        usernameField = new JTextField(20);
        add(usernameField, gbc);
        
        // Email label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        emailField = new JTextField(20);
        add(emailField, gbc);
        
        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);
        
        // Confirm password label and field
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        confirmPasswordField = new JPasswordField(20);
        add(confirmPasswordField, gbc);
        
        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // Register button
        registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                
                if (validateInputs(username, email, password, confirmPassword)) {
                    // Add your registration logic here
                    System.out.println("Registration attempt - Username: " + username + ", Email: " + email);
                    showMessage(username + " is registered");
                }
            }
        });
        buttonPanel.add(registerButton);
        
        // Back to login button
        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.showPage("LOGIN");
            }
        });
        buttonPanel.add(backToLoginButton);
        
        // Add button panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);
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
    	JOptionPane.showMessageDialog(this, message, "Succesful Registration", JOptionPane.INFORMATION_MESSAGE);
    }
}
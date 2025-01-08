package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;

public class LoginPage extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private static final int CORNER_RADIUS = 30;

    public LoginPage(ClientDisplay display) {
        setLayout(new BorderLayout());
        setPreferredSize(ClientDisplay.screenSize);

        // Welcome Panel (Left side - Purple, 1/3 width)
        JPanel welcomePanel = new RoundedPanel(
                CORNER_RADIUS,
                false, true, false, true,
                new Color(188, 130, 205)  // Light purple
        );
        welcomePanel.setLayout(new GridBagLayout());
        welcomePanel.setPreferredSize(new Dimension(ClientDisplay.screenSize.width / 3, ClientDisplay.screenSize.height));

        JLabel welcomeLabel = new JLabel("Welcome");
        JLabel backLabel = new JLabel("Back!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 64));
        backLabel.setFont(new Font("Arial", Font.BOLD, 64));
        welcomeLabel.setForeground(Color.WHITE);
        backLabel.setForeground(Color.WHITE);

        JPanel welcomeTextPanel = new JPanel();
        welcomeTextPanel.setLayout(new BoxLayout(welcomeTextPanel, BoxLayout.Y_AXIS));
        welcomeTextPanel.setOpaque(false);
        welcomeTextPanel.add(welcomeLabel);
        welcomeTextPanel.add(backLabel);
        welcomePanel.add(welcomeTextPanel);

        // Login Panel (Right side - Mint Green, 2/3 width)
        JPanel loginPanel = new RoundedPanel(
                CORNER_RADIUS,
                true, false, true, false,
                new Color(178, 230, 210)  // Mint green
        );
        loginPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 30, 5, 30);

        // Login header
        JLabel loginHeader = new JLabel("LOGIN");
        loginHeader.setFont(new Font("Arial", Font.BOLD, 96));
        loginHeader.setForeground(Color.WHITE);
        loginHeader.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.insets = new Insets(0, 30, 20, 30);
        loginPanel.add(loginHeader, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("USERNAME");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        usernameLabel.setForeground(Color.WHITE);
        gbc.insets = new Insets(5, 30, 5, 30);
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(30);
        styleField(usernameField);
        loginPanel.add(usernameField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordLabel.setForeground(Color.WHITE);
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(30);
        styleField(passwordField);
        loginPanel.add(passwordField, gbc);

        // Login button
        loginButton = new JButton("LOGIN");
        loginButton.setPreferredSize(new Dimension(usernameField.getPreferredSize().width, 55));
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(new Color(178, 230, 210));
        loginButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setFocusPainted(false);
        gbc.insets = new Insets(20, 30, 10, 30);
        loginPanel.add(loginButton, gbc);

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(178, 230, 210));
                loginButton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(Color.WHITE);
                loginButton.setForeground(new Color(178, 230, 210));
            }
        });
        
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginPage.this, 
                    "Username and password cannot be empty", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            display.authenticate(username, password, false);
        });

        // Register link panel
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        registerPanel.setOpaque(false);
        JLabel newUserLabel = new JLabel("New User? ");
        newUserLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        newUserLabel.setForeground(Color.WHITE);
        JLabel registerLabel = new JLabel("Create an account");
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        registerLabel.setForeground(new Color(51, 122, 183));
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                display.showPage("REG");
            }
        });
        registerPanel.add(newUserLabel);
        registerPanel.add(registerLabel);
        gbc.insets = new Insets(10, 30, 20, 30);
        loginPanel.add(registerPanel, gbc);

        // Add panels to main panel
        add(welcomePanel, BorderLayout.WEST);
        add(loginPanel, BorderLayout.CENTER);

        setOpaque(false);
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
}

class RoundedPanel extends JPanel {
    private final int radius;
    private final boolean roundTopLeft;
    private final boolean roundTopRight;
    private final boolean roundBottomLeft;
    private final boolean roundBottomRight;
    private final Color backgroundColor;

    public RoundedPanel(int radius, boolean roundTopLeft, boolean roundTopRight, boolean roundBottomLeft, boolean roundBottomRight, Color backgroundColor) {
        this.radius = radius;
        this.roundTopLeft = roundTopLeft;
        this.roundTopRight = roundTopRight;
        this.roundBottomLeft = roundBottomLeft;
        this.roundBottomRight = roundBottomRight;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(backgroundColor);

        int width = getWidth();
        int height = getHeight();

        Path2D.Float path = new Path2D.Float();

        if (roundTopLeft) {
            path.moveTo(0, radius);
            path.quadTo(0, 0, radius, 0);
        } else {
            path.moveTo(0, 0);
        }

        path.lineTo(width - (roundTopRight ? radius : 0), 0);
        if (roundTopRight) {
            path.quadTo(width, 0, width, radius);
        }

        path.lineTo(width, height - (roundBottomRight ? radius : 0));
        if (roundBottomRight) {
            path.quadTo(width, height, width - radius, height);
        }

        path.lineTo(roundBottomLeft ? radius : 0, height);
        if (roundBottomLeft) {
            path.quadTo(0, height, 0, height - radius);
        }

        path.lineTo(0, roundTopLeft ? radius : 0);

        path.closePath();
        g2.fill(path);
        g2.dispose();
    }
}
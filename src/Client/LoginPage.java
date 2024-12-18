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
        setPreferredSize(new Dimension(800, 400));
        
        // Welcome Panel (Left side - Purple, 1/3 width)
        JPanel welcomePanel = new RoundedPanel(
            CORNER_RADIUS,
            true, true, true, true, // all corners rounded
            new Color(198, 156, 209)  // Light purple
        );
        welcomePanel.setLayout(new GridBagLayout());
        welcomePanel.setPreferredSize(new Dimension(266, 400)); // 1/3 of 800px
        
        JLabel welcomeLabel = new JLabel("Welcome");
        JLabel backLabel = new JLabel("Back!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        backLabel.setFont(new Font("Arial", Font.BOLD, 36));
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
            true, true, true, true, // all corners rounded
            new Color(202, 231, 223)  // Mint green
        );
        loginPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 30, 5, 30);
        
        // Login header
        JLabel loginHeader = new JLabel("LOGIN");
        loginHeader.setFont(new Font("Arial", Font.BOLD, 24));
        loginHeader.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.insets = new Insets(0, 30, 20, 30);
        loginPanel.add(loginHeader, gbc);
        
        // Username field
        JLabel usernameLabel = new JLabel("USERNAME:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.insets = new Insets(5, 30, 5, 30);
        loginPanel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        styleField(usernameField);
        loginPanel.add(usernameField, gbc);
        
        // Password field
        JLabel passwordLabel = new JLabel("PASSWORD:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        loginPanel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        styleField(passwordField);
        loginPanel.add(passwordField, gbc);
        
        // Register link panel
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        registerPanel.setOpaque(false);
        JLabel newUserLabel = new JLabel("New User? ");
        JLabel registerLabel = new JLabel("Create an account");
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
        
        // Set main panel properties
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    
    private void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE),
            new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(Color.WHITE);
    }
}

// Custom panel class for rounded corners
class RoundedPanel extends JPanel {
    private final int radius;
    private final boolean roundTopLeft;
    private final boolean roundTopRight;
    private final boolean roundBottomLeft;
    private final boolean roundBottomRight;
    private final Color backgroundColor;

    public RoundedPanel(int radius, boolean roundTopLeft, boolean roundTopRight, 
                       boolean roundBottomLeft, boolean roundBottomRight, Color backgroundColor) {
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
        
        // Create path for rounded rectangle
        Path2D.Float path = new Path2D.Float();
        
        // Top left corner
        if (roundTopLeft) {
            path.moveTo(0, radius);
            path.quadTo(0, 0, radius, 0);
        } else {
            path.moveTo(0, 0);
        }
        
        // Top edge and top right corner
        path.lineTo(width - (roundTopRight ? radius : 0), 0);
        if (roundTopRight) {
            path.quadTo(width, 0, width, radius);
        }
        
        // Right edge and bottom right corner
        path.lineTo(width, height - (roundBottomRight ? radius : 0));
        if (roundBottomRight) {
            path.quadTo(width, height, width - radius, height);
        }
        
        // Bottom edge and bottom left corner
        path.lineTo(roundBottomLeft ? radius : 0, height);
        if (roundBottomLeft) {
            path.quadTo(0, height, 0, height - radius);
        }
        
        // Left edge
        path.lineTo(0, roundTopLeft ? radius : 0);
        
        path.closePath();
        g2.fill(path);
        g2.dispose();
    }
}
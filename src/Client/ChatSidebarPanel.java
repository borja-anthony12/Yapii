package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ChatSidebarPanel extends JPanel {
    private final ArrayList<FriendChatPanel> friendChats = new ArrayList<>();

    public ChatSidebarPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        //setBorder(BorderFactory.createMatteBorder(0, 0, 0, 5, Color.LIGHT_GRAY));

        // Create header panel that will contain the username
        JPanel headerPanel = createHeaderPanel();

        // Friends list
        JPanel friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.setBackground(new Color(178, 226, 215));

        // Add sample friends
        addFriend(friendsPanel, "Alice", true, "Hey there!");
        addFriend(friendsPanel, "Bob", false, "See you tomorrow");
        addFriend(friendsPanel, "Charlie", true, "Got it, thanks!");

        JScrollPane scrollPane = new JScrollPane(friendsPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(178, 226, 215));

        // Main content panel to hold both header and friends list
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(178, 226, 215));
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(178, 226, 215));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create user name label with black background
        JLabel userLabel = new JLabel("ACCOUNT");
        userLabel.setForeground(Color.BLACK);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Create a container for the username with black background
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(178, 226,215));
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        userPanel.add(userLabel);

        panel.add(userPanel, BorderLayout.NORTH);

        return panel;
    }

    private void addFriend(JPanel container, String name, boolean isOnline, String lastMessage) {
        FriendChatPanel friendPanel = new FriendChatPanel(name, isOnline, lastMessage);
        friendChats.add(friendPanel);
        container.add(friendPanel);
    }
}

class FriendChatPanel extends JPanel {
    private boolean isOnline;

    public FriendChatPanel(String name, boolean isOnline, String lastMessage) {
        this.isOnline = isOnline;
        setLayout(new BorderLayout(10, 0));
        setBackground(new Color(178, 226,215));
        setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 15));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Avatar panel with status indicator
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw avatar circle
                g2.setColor(new Color(243, 238, 255));
                g2.fillOval(0, 0, 40, 40);

                // Draw status indicator
                g2.setColor(isOnline ? new Color(34, 197, 94) : Color.GRAY);
                g2.fillOval(28, 28, 12, 12);
                g2.setColor(Color.WHITE);
                g2.drawOval(28, 28, 12, 12);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40, 40);
            }
        };

        // Text panel for name and last message
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(178, 226,215));
        textPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel messageLabel = new JLabel(lastMessage);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.GRAY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(messageLabel);

        add(avatarPanel, BorderLayout.WEST);
        add(textPanel, BorderLayout.CENTER);

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(218, 146, 224));
                textPanel.setBackground(new Color(218, 146, 224));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(new Color(178, 226,215));
                textPanel.setBackground(new Color(178, 226,215));
            }
        });
    }

    // Now these overrides are correctly outside the constructor
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 45); // 45 pixels total height
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(super.getMaximumSize().width, 45);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width, 45);
    }
}

package Client;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.awt.event.ActionEvent;

public class ClientDisplay extends JFrame {
    final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final JLayeredPane layeredPane;
    private final LoginPage loginPage;
    private final RegisterPage registerPage;
    private final JPanel mainPanel;
    private final JTextArea messageArea;
    private final JTextField messageText;
    private final JLabel nameLabel;
    private File selectedFile;
    private boolean isAnimating;
    private Timer animationTimer;
    private final Client client;

    public ClientDisplay() {
        setSize(screenSize);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(screenSize);

        messageArea = new JTextArea();
        messageText = new JTextField(30);
        nameLabel = new JLabel("User Name", SwingConstants.CENTER);
        loginPage = new LoginPage(this);
        registerPage = new RegisterPage(this);
        mainPanel = createMainPanel();
        

        initializeUI();
        client = new Client(this);

        setContentPane(layeredPane);
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createTopPanel(), BorderLayout.NORTH);
        panel.add(createMessagePanel(), BorderLayout.CENTER);
        panel.add(createInputPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            client.shutdown();
            System.exit(0);
        });

        iconPanel.add(exitButton);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(iconPanel, BorderLayout.EAST);
        panel.add(nameLabel, BorderLayout.WEST);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return panel;
    }

    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton sendButton = new JButton("Send");
        JButton importFile = new JButton("Import File");

        sendButton.addActionListener(e -> sendMessage());
        messageText.addActionListener(e -> sendMessage());
        importFile.addActionListener(e -> handleFileImport());

        buttonPanel.add(importFile);
        buttonPanel.add(sendButton);

        panel.add(messageText, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return panel;
    }

    private void initializeUI() {
        setupLayeredPane();
        setContentPane(layeredPane);
    }

    private void setupLayeredPane() {
        loginPage.setBounds(0, 0, screenSize.width, screenSize.height);
        registerPage.setBounds(screenSize.width, 0, screenSize.width, screenSize.height);
        layeredPane.add(loginPage, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(registerPage, JLayeredPane.DEFAULT_LAYER);
    }

    private void handleFileImport() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            FileDialog fileDialog = new FileDialog(this, "Choose a file", FileDialog.LOAD);
            fileDialog.setVisible(true);

            if (fileDialog.getFile() != null) {
                selectedFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
                messageText.setText("Selected file: " + selectedFile.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public void showPage(String page) {
        if (isAnimating) return;

        switch (page) {
            case "REG" -> slideTransition(loginPage, registerPage, true);
            case "LOGIN" -> slideTransition(registerPage, loginPage, false);
            case "MAIN" -> {
                layeredPane.removeAll();
                setContentPane(mainPanel);
            }
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

        animationTimer.addActionListener((ActionEvent e) -> {
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
}
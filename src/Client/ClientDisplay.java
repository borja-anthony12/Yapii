package Client;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.io.File;
import java.util.function.Function;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class ClientDisplay extends JFrame {
	final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//	private MessageDisplayPanel messageDisplay;
	private final JLayeredPane layeredPane;
	private final LoginPage loginPage;
	private final RegisterPage registerPage;
	private final JPanel mainPanel;
	private JTextArea messageArea;
	private JTextField messageText;
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

		setContentPane(mainPanel);
		setVisible(true);
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.setBackground(Color.WHITE);

		// Top panel with profile info
		JPanel topPanel = createTopPanel();
		topPanel.setBackground(Color.WHITE);
		topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

		// Left sidebar
		JPanel sidebarPanel = createSidebarPanel();

		// Main chat area
		JPanel chatPanel = createMessagePanel();
		chatPanel.setBackground(Color.WHITE);

		// Message display area with custom background
		messageArea = new JTextArea();
		messageArea.setEditable(false);
		messageArea.setBackground(new Color(245, 245, 245));
		messageArea.setMargin(new Insets(10, 10, 10, 10));
		messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		JScrollPane scrollPane = new JScrollPane(messageArea);
		scrollPane.setBorder(null);

		// Bottom input panel
		JPanel inputPanel = createInputPanel();
		inputPanel.setBackground(Color.WHITE);

		chatPanel.add(scrollPane, BorderLayout.CENTER);
		chatPanel.add(inputPanel, BorderLayout.SOUTH);

		// Add components to main panel
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(sidebarPanel, BorderLayout.WEST);
		panel.add(chatPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createSidebarPanel() {
		JPanel sidebar = new JPanel();
		sidebar.setPreferredSize(new Dimension(250, 0));
		sidebar.setBackground(Color.WHITE);
		sidebar.setLayout(new BorderLayout());

		// Logo panel at top
		JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
		logoPanel.setBackground(Color.WHITE);
		JLabel logoLabel = new JLabel("Yapii");
		logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
		logoLabel.setForeground(new Color(138, 43, 226)); // Purple color
		logoPanel.add(logoLabel);

		// Sidebar content
		JPanel sidebarContent = new JPanel();
		sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
		sidebarContent.setBackground(Color.WHITE);

		JLabel descLabel = new JLabel("Create spots for different");
		JLabel descLabel2 = new JLabel("direct messages with other");
		JLabel descLabel3 = new JLabel("groups/people");

		descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		descLabel3.setAlignmentX(Component.LEFT_ALIGNMENT);

		sidebarContent.add(Box.createVerticalStrut(10));
		sidebarContent.add(descLabel);
		sidebarContent.add(descLabel2);
		sidebarContent.add(descLabel3);
		sidebarContent.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		sidebar.add(logoPanel, BorderLayout.NORTH);
		sidebar.add(sidebarContent, BorderLayout.CENTER);

		// Add right border
		sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

		return sidebar;
	}

	private JPanel createTopPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);

		// Profile section
		JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		profilePanel.setBackground(Color.WHITE);

		// Create circular avatar
		JLabel avatarLabel = new CircularAvatar(32);

		// Name and other details
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
		namePanel.setBackground(Color.WHITE);

		nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		namePanel.add(nameLabel);

		profilePanel.add(avatarLabel);
		profilePanel.add(namePanel);

		panel.add(profilePanel, BorderLayout.WEST);

		// Add some padding
		panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		return panel;
	}

	// Update the createMainPanel method to use the new components
	private JPanel createMessagePanel() {
	    JPanel panel = new JPanel(new BorderLayout());
//	    messageDisplay = new MessageDisplayPanel(); // Initialize field
	    JScrollPane scrollPane = new JScrollPane();
	    scrollPane.setBorder(null);
	    panel.add(scrollPane, BorderLayout.CENTER);
	    return panel;
	}

	private JPanel createInputPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 0));
		panel.setBackground(Color.WHITE);

		// Custom rounded panel for input area
		JPanel inputWrapper = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(245, 245, 245));
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
				g2.dispose();
			}
		};
		inputWrapper.setOpaque(false);

		// Update message text field
		messageText = new JTextField() {
			@Override
			public void setBorder(Border border) {
				// Remove border
			}
		};
		messageText.setOpaque(false);
		messageText.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		// Create button panel with icons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		buttonPanel.setOpaque(false);

		// Attachment button
		JButton attachButton = new JButton();
		attachButton.setIcon(createAttachmentIcon());
		styleButton(attachButton);
		attachButton.addActionListener(e -> handleFileImport());

		// Send button
		JButton sendButton = new JButton();
		sendButton.setIcon(createSendIcon());
		styleButton(sendButton);
		sendButton.addActionListener(e -> sendMessage());

		buttonPanel.add(attachButton);
		buttonPanel.add(sendButton);

		// Add components to input wrapper
		inputWrapper.add(messageText, BorderLayout.CENTER);
		inputWrapper.add(buttonPanel, BorderLayout.EAST);
		inputWrapper.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));

		panel.add(inputWrapper, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		return panel;
	}

	// Helper method to style buttons
	private void styleButton(JButton button) {
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setPreferredSize(new Dimension(32, 32));
	}

	private static class CircularAvatar extends JLabel {
		private final int size;

		public CircularAvatar(int size) {
			this.size = size;
			setPreferredSize(new Dimension(size, size));
			setBackground(new Color(200, 200, 200));
			setOpaque(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			g2.fillOval(0, 0, size - 1, size - 1);
			g2.dispose();
		}
	}

	// Create simple vector icons since we can't load image files
	private ImageIcon createSendIcon() {
		return createIcon(size -> {
			BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = bi.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(138, 43, 226));
			int[] xPoints = { 2, size - 2, 2 };
			int[] yPoints = { 2, size / 2, size - 2 };
			g2.fillPolygon(xPoints, yPoints, 3);
			g2.dispose();
			return bi;
		}, 16);
	}

	private ImageIcon createAttachmentIcon() {
		return createIcon(size -> {
			BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = bi.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.GRAY);
			g2.setStroke(new BasicStroke(2));
			g2.drawArc(4, 4, size - 8, size - 8, 45, 180);
			g2.drawLine(size / 2, size / 2, size / 2, size - 4);
			g2.dispose();
			return bi;
		}, 16);
	}

	private ImageIcon createIcon(Function<Integer, BufferedImage> drawer, int size) {
		return new ImageIcon(drawer.apply(size));
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
		if (isAnimating)
			return;

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
		final int[] step = { 0 };

		animationTimer.addActionListener((ActionEvent e) -> {
			step[0]++;
			float progress = (float) Math.pow(step[0] / (double) steps, 2);

			int currentFromX = startFrom + (int) ((endFrom - startFrom) * progress);
			int currentToX = startTo + (int) ((endTo - startTo) * progress);

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

	// Update the appendMessage method to use the new message display
	public void appendMessage(String message) {
		if (messageArea != null) {
			messageArea.append(message + "\n");
			// Auto-scroll to bottom
			messageArea.setCaretPosition(messageArea.getDocument().getLength());
		}
	}

	public void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	public void joinRoom(String roomName) {
		appendMessage("Joined room: " + roomName);
		// Update UI to show current room
		// Could add a label showing current room
	}

	public void leaveRoom(String roomName) {
		appendMessage("Left room: " + roomName);
	}
	public File getSelectedFile() {
		return selectedFile;
	}
}
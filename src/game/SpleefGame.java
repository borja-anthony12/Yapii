package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

public class SpleefGame extends JFrame {
	private GamePanel gamePanel;
	private PowerUpManager powerUpManager;
	private ArrayList<Projectile> projectiles = new ArrayList<>();
	private ArrayList<Player> players;
	private ArrayList<Block> blocks;
	private boolean[] keys;
	private final int BLOCK_SIZE = 40;
	private Timer gameTimer;

	public SpleefGame() {
		setTitle("Spleef Game");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		blocks = new ArrayList<>();
		powerUpManager = new PowerUpManager(800, blocks);
		keys = new boolean[256];
		players = new ArrayList<>();
		players.add(new Player(400, 40, Color.RED));

		// Initialize blocks in a grid pattern
		for (int x = 40; x <= 720; x += BLOCK_SIZE) {
			for (int y = 120; y <= 500; y += BLOCK_SIZE) {
				y += BLOCK_SIZE;
				blocks.add(new Block(x, y));
			}
		}

		gamePanel = new GamePanel();
		add(gamePanel);
		pack();
		setLocationRelativeTo(null);

		// Add both key and mouse listeners
		addKeyListener(new KeyHandler());
		addMouseListener(new MouseHandler());
		setFocusable(true);

		gameTimer = new Timer(16, e -> {
			update();
			gamePanel.repaint();
		});
		gameTimer.start();
	}
	
	public void addPlayer(String username, Color color) {
	    players.add(new Player(400, 40, color));
	    // Optional: Add username display or tracking
	}

	public class Player {
		double x, y;
		double velocityX = 0;
		double velocityY = 0;
		boolean isJumping = false;
		boolean canJump = false;
		int size = 20;
		Color color;
		Rectangle bounds;

		protected PowerUp currentPowerUp = null;
		protected double jumpForce = -10;
		protected double knockbackForce = 10.0;

		public Player(int x, int y, Color color) {
			this.x = x;
			this.y = y;
			this.color = color;
			this.bounds = new Rectangle(x - size / 2, y - size / 2, size, size);
		}

		public void setPowerUp(PowerUp powerUp) {
			currentPowerUp = powerUp;
		}

		public void activatePowerUp() {
			if (currentPowerUp != null) {
				currentPowerUp.activate(this);
			}
		}

		public void updatePowerUp() {
			if (currentPowerUp != null) {
				currentPowerUp.update(this);
			}
		}

		public Rectangle getBounds() {
			bounds.setLocation((int) x - size / 2, (int) y - size / 2);
			return bounds;
		}

		public void handleCollision(Player other) {
			if (getBounds().intersects(other.getBounds())) {
				// Calculate direction vector between players
				double dx = x - other.x;
				double dy = y - other.y;
				double distance = Math.sqrt(dx * dx + dy * dy);

				if (distance == 0)
					distance = 1;

				// Normalize direction vector
				dx /= distance;
				dy /= distance;

				double defaultKnockbackForce = 10.0;

				// Apply velocities instead of direct position changes
				velocityX = dx * defaultKnockbackForce;
				other.velocityX = -dx * knockbackForce;

				// Add slight vertical boost to make collisions more dynamic
				velocityY = dy * defaultKnockbackForce * 0.5;
				other.velocityY = -dy * knockbackForce * 0.5;

				// Set both players to jumping state since they're knocked back
				isJumping = true;
				other.isJumping = true;
			}
		}
	}

	public class Block {
		int x, y;
		boolean isActive = true;
		int durability = 8;
		Color baseColor = new Color(100, 100, 100);
		Color color;
		private long lastDamageTime = 0;
		private final long DAMAGE_COOLDOWN = 500;

		public Block(int x, int y) {
			this.x = x;
			this.y = y;
			updateColor();
		}

		public void updateColor() {
			float factor = durability / 3.0f;
			if (factor > 1 || factor < 0)
				factor = 1.0f;
			color = new Color((int) (baseColor.getRed() * factor), (int) (baseColor.getGreen() * factor),
					(int) (baseColor.getBlue() * factor));
		}

		public Rectangle getBounds() {
			return new Rectangle(x, y, BLOCK_SIZE, BLOCK_SIZE);
		}

		public boolean canBeDamaged() {
			long currentTime = System.currentTimeMillis();
			return currentTime - lastDamageTime >= DAMAGE_COOLDOWN;
		}

		public void damage(int damage) {
			if (canBeDamaged()) {
				durability -= damage;
				lastDamageTime = System.currentTimeMillis();
				updateColor();
				if (durability <= 0) {
					isActive = false;
				}
			}
		}

		public void draw(Graphics2D g2d) {
			if (!isActive)
				return;

			g2d.setColor(color);
			g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);

			g2d.setColor(color.brighter());
			g2d.fillRect(x, y, BLOCK_SIZE, 5);
			g2d.fillRect(x, y, 5, BLOCK_SIZE);

			g2d.setColor(color.darker());
			g2d.fillRect(x, y + BLOCK_SIZE - 5, BLOCK_SIZE, 5);
			g2d.fillRect(x + BLOCK_SIZE - 5, y, 5, BLOCK_SIZE);

			g2d.setColor(Color.WHITE);
			g2d.drawString(String.valueOf(durability), x + BLOCK_SIZE / 2 - 5, y + BLOCK_SIZE / 2 + 5);

			for (Projectile projectile : projectiles) {
				projectile.draw(g2d);
			}
		}
	}

	private class GamePanel extends JPanel {
		public GamePanel() {
			setPreferredSize(new Dimension(800, 600));
			setBackground(Color.BLACK);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			powerUpManager.draw(g2d);

			for (Block block : blocks) {
				block.draw(g2d);
			}

			for (Player player : players) {
				// Draw player
				g2d.setColor(player.color.darker());
				g2d.fillOval((int) player.x - player.size / 2 + 2, (int) player.y - player.size / 2 + 2, player.size,
						player.size);
				g2d.setColor(player.color);
				g2d.fillOval((int) player.x - player.size / 2, (int) player.y - player.size / 2, player.size,
						player.size);

				// Draw power-up timer bar if power-up is active
				if (player.currentPowerUp != null && player.currentPowerUp.activationTime > 0) {
					// Calculate remaining time percentage
					float remainingTime = player.currentPowerUp.getRemainingDuration();
					float totalDuration = player.currentPowerUp.duration;
					float percentage = remainingTime / totalDuration;

					// Bar dimensions - now full width
					int barWidth = getWidth();
					int barHeight = 25;
					int x = 0; // Start from left edge
					int y = getHeight() - barHeight; // Bottom of screen

					// Draw background (empty bar)
					g2d.setColor(new Color(50, 50, 50));
					g2d.fillRect(x, y, barWidth, barHeight);

					// Draw filled portion with smooth color transition
					if (percentage > 0) {
						// Create smooth color transition from green to red
						float hue = percentage * 0.3f; // 0.3 is green, 0.0 is red in HSB color space
						Color fillColor = Color.getHSBColor(hue, 0.8f, 0.8f);

						g2d.setColor(fillColor);
						g2d.fillRect(x, y, (int) (barWidth * percentage), barHeight);
					}
				}
			}
		}
	}

	public class Projectile {
		private double x, y;
		private double velocityX, velocityY;
		private static final double SPEED = 15.0;
		private static final int SIZE = 8;
		private boolean active = true;
		private static final double EXPLOSION_RADIUS = 80.0;
		private static final double EXPLOSION_FORCE = 15.0;
		private static final int BLOCK_DAMAGE = 3;

		public Projectile(double startX, double startY, double targetX, double targetY) {
			this.x = startX;
			this.y = startY;

			// Calculate direction vector
			double dx = targetX - startX;
			double dy = targetY - startY;
			double length = Math.sqrt(dx * dx + dy * dy);

			// Normalize and scale by speed
			if (length > 0) {
				velocityX = (dx / length) * SPEED;
				velocityY = (dy / length) * SPEED;
			}
		}

		public void update(ArrayList<Block> blocks, ArrayList<Player> players) {
			if (!active)
				return;

			x += velocityX;
			y += velocityY;

			// Check for collisions with blocks
			for (Block block : blocks) {
				if (block.isActive && checkCollision(block)) {
					explode(blocks, players);
					return;
				}
			}

			// Check if projectile is out of bounds
			if (x < 0 || x > 800 || y < 0 || y > 600) {
				active = false;
			}
		}

		private boolean checkCollision(Block block) {
			return new Rectangle((int) x - SIZE / 2, (int) y - SIZE / 2, SIZE, SIZE).intersects(block.getBounds());
		}

		private void explode(ArrayList<Block> blocks, ArrayList<Player> players) {
			// Damage nearby blocks regardless of cooldown
			for (Block block : blocks) {
				if (!block.isActive)
					continue;

				double distance = getDistance(block.x + block.getBounds().width / 2,
						block.y + block.getBounds().height / 2);

				if (distance <= EXPLOSION_RADIUS) {
					// Apply more damage to closer blocks
					int damage = (int) (BLOCK_DAMAGE * (1 - distance / EXPLOSION_RADIUS));
					// Save current time
					long currentTime = block.lastDamageTime;
					// Temporarily set lastDamageTime to allow damage
					block.lastDamageTime = 0;
					block.damage(damage);
					// Restore original time
					block.lastDamageTime = currentTime;
				}
			}

			// Push players away from explosion
			for (Player player : players) {
				double dx = player.x - x;
				double dy = player.y - y;
				double distance = Math.sqrt(dx * dx + dy * dy);

				if (distance <= EXPLOSION_RADIUS) {
					double force = EXPLOSION_FORCE * (1 - distance / EXPLOSION_RADIUS);
					if (distance > 0) {
						player.velocityX += (dx / distance) * force;
						player.velocityY += (dy / distance) * force;
						player.isJumping = true;
					}
				}
			}

			active = false;
		}

		private double getDistance(double targetX, double targetY) {
			double dx = x - targetX;
			double dy = y - targetY;
			return Math.sqrt(dx * dx + dy * dy);
		}

		public void draw(Graphics2D g2d) {
			if (!active)
				return;

			g2d.setColor(Color.ORANGE);
			g2d.fillOval((int) x - SIZE / 2, (int) y - SIZE / 2, SIZE, SIZE);
			g2d.setColor(Color.RED);
			g2d.drawOval((int) x - SIZE / 2, (int) y - SIZE / 2, SIZE, SIZE);
		}

		public boolean isActive() {
			return active;
		}
	}

	private class MouseHandler implements MouseListener {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				for (Player player : players) {
					if (player.currentPowerUp != null) {
						player.activatePowerUp();
					}
				}
			}
			if (e.getButton() == MouseEvent.BUTTON3) {
				for (Player player : players) {
					if (player.currentPowerUp instanceof ProjectilePowerUp && player.currentPowerUp.isActiveInPlayer
							&& System.currentTimeMillis()
									- player.currentPowerUp.activationTime <= player.currentPowerUp.duration) {
						Point mousePoint = e.getPoint();
						projectiles.add(new Projectile(player.x, player.y, mousePoint.x, mousePoint.y));
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	private class KeyHandler implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			keys[e.getKeyCode()] = true;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keys[e.getKeyCode()] = false;
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	protected void update() {

		powerUpManager.update(players);

		Iterator<Projectile> iterator = projectiles.iterator();
		while (iterator.hasNext()) {
			Projectile projectile = iterator.next();
			projectile.update(blocks, players);
			if (!projectile.isActive()) {
				iterator.remove();
			}
		}

		for (Player player : players) {
			// Store original position
			double originalX = player.x;
			double originalY = player.y;

			// Apply velocities from collisions
			player.x += player.velocityX;
			player.velocityX *= 0.8; // Decay horizontal velocity

			// Regular movement
			if (keys[KeyEvent.VK_A])
				player.x -= 5.0;
			if (keys[KeyEvent.VK_D])
				player.x += 5.0;

			// Handle horizontal collisions first
			Rectangle horizontalBounds = new Rectangle((int) player.x - player.size / 2,
					(int) originalY - player.size / 2, player.size, player.size);

			for (Block block : blocks) {
				if (!block.isActive)
					continue;
				if (horizontalBounds.intersects(block.getBounds())) {
					// Restore X position and adjust to block edge
					if (originalX < block.x) {
						player.x = block.x - player.size / 2;
					} else {
						player.x = block.x + BLOCK_SIZE + player.size / 2;
					}
					player.velocityX = 0; // Stop horizontal velocity on collision
				}
			}

			// Jumping - only allow if canJump is true
			if (keys[KeyEvent.VK_SPACE] && player.canJump) {
				player.velocityY = player.jumpForce;
				player.isJumping = true;
				player.canJump = false;
			}

			if (player.currentPowerUp != null) {
				player.updatePowerUp();
			}

			// Apply gravity
			player.velocityY += 0.5;
			player.y += player.velocityY;

			// Handle vertical collisions
			boolean onGround = false;
			Rectangle verticalBounds = new Rectangle((int) player.x - player.size / 2, (int) player.y - player.size / 2,
					player.size, player.size);

			for (Block block : blocks) {
				if (!block.isActive)
					continue;
				if (verticalBounds.intersects(block.getBounds())) {
					if (originalY < block.y) {
						block.damage(1);
					}

					if (player.velocityY > 0) {
						player.y = block.y - player.size / 2;
						player.velocityY = 0;
						onGround = true;
					} else if (player.velocityY < 0) {
						player.y = block.y + BLOCK_SIZE + player.size / 2;
						player.velocityY = 0;
					}
				}
			}

			// Update jumping state and ability to jump
			if (onGround) {
				player.isJumping = false;
				player.canJump = true;
			}

			// Check player collisions
			for (int i = 0; i < players.size(); i++) {
				for (int j = i + 1; j < players.size(); j++) {
					players.get(i).handleCollision(players.get(j));
				}
			}

			// Keep player within screen bounds
			if (player.x < player.size / 2) {
				player.x = player.size / 2;
				player.velocityX = 0;
			}
			if (player.x > 800 - player.size / 2) {
				player.x = 800 - player.size / 2;
				player.velocityX = 0;
			}
			if (player.y < player.size / 2) {
				player.y = player.size / 2;
				player.velocityY = 0;
			}

			// Delete player if fallen
			if (player.y > 600) {
				players.remove(player);
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new SpleefGame().setVisible(true);
		});
	}
}
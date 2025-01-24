package game;

import java.awt.*;
import game.SpleefGame.Player;

abstract class PowerUp {
	protected double x, y;
	protected int size = 20;
	protected Color color;
	protected boolean isActive = true;
	protected boolean isFalling = true;
	protected boolean isActiveInPlayer = false;
	protected long activationTime = -1;
	protected long duration; // Duration in milliseconds

	public PowerUp(double x, double y, long duration) {
		this.x = x;
		this.y = y;
		this.duration = duration;
	}

	public Rectangle getBounds() {
		return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
	}

	public float getRemainingDuration() {
		if (activationTime == -1)
			return duration;
		long remainingTime = duration - (System.currentTimeMillis() - activationTime);
		return Math.max(0, remainingTime);
	}

	public void update(Player player) {
		if (activationTime > 0 && System.currentTimeMillis() - activationTime > duration) {
			deactivate(player);
			player.setPowerUp(null);
		}
	}

	public void activate(Player player) {
		// Only activate if not already active
		if (!isActiveInPlayer) {
			isActiveInPlayer = true;
			activationTime = System.currentTimeMillis();
			applyEffect(player);
		}
	}

	protected abstract void applyEffect(Player player);

	public abstract void deactivate(Player player);

	public abstract void draw(Graphics2D g2d);
}

// SuperJump
class SuperJumpPowerUp extends PowerUp {
	final static int weight = 12;
	public SuperJumpPowerUp(double x, double y) {
		super(x, y, 5000);
		this.color = Color.GREEN;
	}

	@Override
	protected void applyEffect(Player player) {
		player.jumpForce = -15;
	}

	@Override
	public void deactivate(Player player) {
		player.jumpForce = -10;
		isActiveInPlayer = false;
		activationTime = -1;
	}

	@Override
	public void draw(Graphics2D g2d) {
		if (!isActive)
			return;

		g2d.setColor(color);
		g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
		g2d.setColor(color.darker());
		g2d.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
		g2d.drawString("J", (int) x - 4, (int) y + 4);
	}
}

class ForcePowerUp extends PowerUp {
	final static int weight = 12;
	public ForcePowerUp(double x, double y) {
		super(x, y, 5000);
		this.color = Color.RED;
	}

	@Override
	protected void applyEffect(Player player) {
		player.knockbackForce = 25;
	}

	@Override
	public void deactivate(Player player) {
		player.knockbackForce = 10.0;
		isActiveInPlayer = false;
		activationTime = -1;
	}

	@Override
	public void draw(Graphics2D g2d) {
		if (!isActive)
			return;

		g2d.setColor(color);
		g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
		g2d.setColor(color.darker());
		g2d.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
		g2d.drawString("F", (int) x - 4, (int) y + 4);
	}
}

class ProjectilePowerUp extends PowerUp {
	final static int weight = 6;
	public ProjectilePowerUp(double x, double y) {
		super(x, y, 750);
		this.color = Color.ORANGE;
	}

	@Override
	protected void applyEffect(Player player) {

	}

	@Override
	public void deactivate(Player player) {
		isActiveInPlayer = false;
		activationTime = -1;
	}

	@Override
	public void draw(Graphics2D g2d) {
		if (!isActive)
			return;

		g2d.setColor(color);
		g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
		g2d.setColor(color.darker());
		g2d.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
		g2d.drawString("P", (int) x - 4, (int) y + 4);
	}
}

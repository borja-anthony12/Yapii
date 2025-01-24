package game;

import java.util.ArrayList;
import java.util.Random;

import game.SpleefGame.*;

import java.awt.*;

class PowerUpManager {
    private ArrayList<PowerUp> activePowerUps;
    private long lastSpawnTime;
    private static final long SPAWN_INTERVAL = 7500;
    private static final double FALL_SPEED = 3.0;
    private Random random;
    private int screenWidth;
    private ArrayList<Block> blocks;

    public PowerUpManager(int screenWidth, ArrayList<Block> blocks) {
        this.activePowerUps = new ArrayList<>();
        this.lastSpawnTime = System.currentTimeMillis();
        this.random = new Random();
        this.screenWidth = screenWidth;
        this.blocks = blocks;
    }

    public void update(ArrayList<Player> players) {
        long currentTime = System.currentTimeMillis();

        // Spawn new power-up if it's time
        if (currentTime - lastSpawnTime >= SPAWN_INTERVAL) {
            spawnNewPowerUp();
            lastSpawnTime = currentTime;
        }

        // Update existing power-ups
        for (int i = activePowerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = activePowerUps.get(i);
            
            if (!powerUp.isActive) {
                activePowerUps.remove(i);
                continue;
            }

            // Check if powerup should fall
            boolean hasSupport = false;
            for (Block block : blocks) {
                if (block.isActive && isOnBlock(powerUp, block)) {
                    hasSupport = true;
                    powerUp.y = block.y - powerUp.size/2; // Ensure correct positioning
                    break;
                }
            }

            // If no supporting block found, powerup should fall
            powerUp.isFalling = !hasSupport;

            // Apply gravity to falling power-ups
            if (powerUp.isFalling) {
                powerUp.y += FALL_SPEED;
            }

            // Check for player collection
            for (Player player : players) {
                if (powerUp.isActive && powerUp.getBounds().intersects(player.getBounds()) && player.currentPowerUp == null) {
                    player.setPowerUp(powerUp);
                    powerUp.isActive = false;
                    break;
                }
            }

            // Remove if fallen off screen
            if (powerUp.y > 600) {
                activePowerUps.remove(i);
            }
        }
    }

    private boolean isOnBlock(PowerUp powerUp, Block block) {
        // Check if the powerup is directly above the block and close enough to be considered "on" it
        Rectangle powerUpBounds = powerUp.getBounds();
        Rectangle blockBounds = block.getBounds();
        
        // The power-up should be within a small vertical distance of the block's top surface
        double verticalDistance = Math.abs((powerUpBounds.y + powerUpBounds.height) - blockBounds.y);
        
        return powerUpBounds.x + powerUpBounds.width > blockBounds.x && 
               powerUpBounds.x < blockBounds.x + blockBounds.width &&
               verticalDistance < 5; // Small threshold for vertical alignment
    }

    private void spawnNewPowerUp() {
        // Random x position between 40 and screenWidth-40 to avoid spawning at edges
        double x = 40 + random.nextDouble() * (screenWidth - 80);
        PowerUp newPowerUp = PowerUpFactory.createRandomPowerUp(x, 0);
        activePowerUps.add(newPowerUp);
    }

    public void draw(Graphics2D g2d) {
        for (PowerUp powerUp : activePowerUps) {
            if (powerUp.isActive) {
                powerUp.draw(g2d);
            }
        }
    }
}

class PowerUpFactory {
	final static int totalWeight = 
			SuperJumpPowerUp.weight +
			ForcePowerUp.weight +
			ProjectilePowerUp.weight;
	
    private static Random random = new Random();
    
    public static PowerUp createRandomPowerUp(double x, double y) {
        // Add more cases as new power-ups are implemented
        switch(random.nextInt(totalWeight)) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            	return new SuperJumpPowerUp(x, y);
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            	return new ForcePowerUp(x, y);
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            	return new ProjectilePowerUp(x, y);
            default:
                return null;
        }
    }
}
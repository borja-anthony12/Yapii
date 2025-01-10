package Client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class ChatMessagePanel extends JPanel {
    private final String message;
    private final boolean sentByMe;
    private final Color SENT_COLOR = new Color(230, 210, 240);
    private final Color RECEIVED_COLOR = new Color(240, 240, 240);
    private static final int PADDING = 15;
    private static final int BUBBLE_RADIUS = 20;
    private static final int MAX_WIDTH = 400;
    
    public ChatMessagePanel(String message, boolean sentByMe) {
        this.message = message;
        this.sentByMe = sentByMe;
        setOpaque(false);
        
        // Set minimum size
        Dimension size = calculateSize();
        setMinimumSize(size);
        setPreferredSize(size);
    }
    
    private Dimension calculateSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int textWidth = fm.stringWidth(message);
        int bubbleWidth = Math.min(textWidth + (2 * PADDING), MAX_WIDTH);
        int height = fm.getHeight() + (2 * PADDING);
        
        // Add extra space for positioning
        return new Dimension(bubbleWidth + 40, height);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int bubbleWidth = Math.min(textWidth + (2 * PADDING), MAX_WIDTH - 40);
        int bubbleHeight = fm.getHeight() + (2 * PADDING);
        
        // Calculate x position based on alignment
        int x = sentByMe ? getWidth() - bubbleWidth - 20 : 20;
        int y = 5;
        
        // Draw bubble
        g2.setColor(sentByMe ? SENT_COLOR : RECEIVED_COLOR);
        g2.fillRoundRect(x, y, bubbleWidth, bubbleHeight, BUBBLE_RADIUS, BUBBLE_RADIUS);
        
        // Draw text
        g2.setColor(Color.BLACK);
        int textX = x + PADDING;
        int textY = y + PADDING + fm.getAscent();
        g2.drawString(message, textX, textY);
        
        g2.dispose();
    }
}
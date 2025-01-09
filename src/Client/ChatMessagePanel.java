package Client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ChatMessagePanel extends JPanel {
    private final String message;
    private final boolean sentByMe;
    private final Color SENT_COLOR = new Color(230, 210, 240); // Light purple for sent messages
    private final Color RECEIVED_COLOR = new Color(240, 240, 240); // Light gray for received
    
    public ChatMessagePanel(String message, boolean sentByMe) {
        this.message = message;
        this.sentByMe = sentByMe;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate text metrics
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int padding = 15;
        int bubbleWidth = Math.min(textWidth + 2 * padding, getWidth() - 100);
        int bubbleHeight = fm.getHeight() + padding;
        
        // Draw bubble
        g2.setColor(sentByMe ? SENT_COLOR : RECEIVED_COLOR);
        int x = sentByMe ? getWidth() - bubbleWidth - 20 : 20;
        int y = 5;
        g2.fillRoundRect(x, y, bubbleWidth, bubbleHeight, 20, 20);
        
        // Draw text
        g2.setColor(Color.BLACK);
        int textX = x + padding;
        int textY = y + (bubbleHeight - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(message, textX, textY);
        
        g2.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int height = fm.getHeight() + 20;
        return new Dimension(getWidth(), height);
    }
}
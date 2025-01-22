package Client;

import java.awt.*;
import javax.swing.*;


public class ChatMessagePanel extends JPanel {
    private final String message;
    private final boolean sentByMe;
    private final Color SENT_COLOR = new Color(230, 210, 12);
    private final Color RECEIVED_COLOR = new Color(12, 12, 123);

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

        FontMetrics fm = g2.getFontMetrics();
        int padding = 15;
        int maxWidth = getWidth() - 100;

        // Calculate wrapped text height
        int lineHeight = fm.getHeight();
        String[] words = message.split("\\s+");
        int currentLineWidth = 0;
        int totalHeight = lineHeight;

        for (String word : words) {
            int wordWidth = fm.stringWidth(word + " ");
            if (currentLineWidth + wordWidth > maxWidth) {
                totalHeight += lineHeight;
                currentLineWidth = wordWidth;
            } else {
                currentLineWidth += wordWidth;
            }
        }

        // Draw bubble
        int bubbleWidth = Math.min(fm.stringWidth(message) + 2 * padding, maxWidth);
        int bubbleHeight = totalHeight + padding;

        g2.setColor(sentByMe ? SENT_COLOR : RECEIVED_COLOR);
        int x = sentByMe ? getWidth() - bubbleWidth - 20 : 20;
        int y = 5;
        g2.fillRoundRect(x, y, bubbleWidth, bubbleHeight, 20, 20);

        // Draw wrapped text
        g2.setColor(Color.BLACK);
        int currentX = x + padding;
        int currentY = y + fm.getAscent() + (padding / 2);
        currentLineWidth = 0;

        for (String word : words) {
            int wordWidth = fm.stringWidth(word + " ");
            if (currentLineWidth + wordWidth > maxWidth) {
                currentY += lineHeight;
                currentX = x + padding;
                currentLineWidth = 0;
            }
            g2.drawString(word + " ", currentX, currentY);
            currentX += wordWidth;
            currentLineWidth += wordWidth;
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int maxWidth = getWidth() - 100;
        int lineHeight = fm.getHeight();
        int padding = 15;

        // Calculate height based on word wrapping
        String[] words = message.split("\\s+");
        int currentLineWidth = 0;
        int totalHeight = lineHeight;

        for (String word : words) {
            int wordWidth = fm.stringWidth(word + " ");
            if (currentLineWidth + wordWidth > maxWidth) {
                totalHeight += lineHeight;
                currentLineWidth = wordWidth;
            } else {
                currentLineWidth += wordWidth;
            }
        }

        return new Dimension(getWidth(), totalHeight + padding + 10);
    }
}
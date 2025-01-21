package Client;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

public class MessageDisplayPanel extends JPanel {
    public MessageDisplayPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    public void addMessage(String message, boolean sentByMe) {
        ChatMessagePanel messagePanel = new ChatMessagePanel(message, sentByMe);
        add(messagePanel);
        add(Box.createVerticalStrut(5));
        messagePanel.setAlignmentX(sentByMe ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        
        revalidate();
        repaint();
        scrollToBottom();
    }
    
    private void scrollToBottom() {
        if (getParent() instanceof JViewport) {
            JViewport viewport = (JViewport) getParent();
            if (viewport.getParent() instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) viewport.getParent();
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMaximum());
            }
        }
    }
}
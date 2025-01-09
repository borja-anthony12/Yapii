package Client;

import java.awt.Color;
import java.awt.List;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class MessageDisplayPanel extends JPanel {
	private final ArrayList<ChatMessagePanel> messages = new ArrayList<ChatMessagePanel>();
    
    public MessageDisplayPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
    }
    
    public void addMessage(String message, boolean sentByMe) {
        ChatMessagePanel messagePanel = new ChatMessagePanel(message, sentByMe);
        messages.add(messagePanel);
        add(messagePanel);
        add(Box.createVerticalStrut(5));
        revalidate();
        repaint();
    }
}
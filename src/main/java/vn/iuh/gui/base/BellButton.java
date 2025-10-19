package vn.iuh.gui.base;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BellButton extends JButton {
    private final List<String> notifications;
    private boolean hasNewNotification = false;
    private Color defaultColor = CustomUI.mine;
    private Color alertColor = CustomUI.yellow;
    private final int dotSize = 10;
    private JLabel emptyLabel;
    private JPanel panel;
    private JPopupMenu popup;

    public BellButton() {
        super("\uD83D\uDD14");
        this.notifications = new ArrayList<>();
        setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setBackground(CustomUI.darkBlue);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Khi click vào chuông → reset trạng thái

        addActionListener(e -> {
            if (hasNewNotification) {
                hasNewNotification = false;
                setBackground(defaultColor);
                repaint();
                showNotificationPopup();
            } else {
                showNotificationPopup();
            }
        });
    }

    public void addNotification(String message) {
        this.notifications.add(message);
        hasNewNotification = true;
        setBackground(alertColor);
        repaint();
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public void clearNotifications() {
        notifications.clear();
        hasNewNotification = false;
        setBackground(defaultColor);
        repaint();
    }

    private void showNotificationPopup() {
        popup = new JPopupMenu();
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        if(notifications.isEmpty()){
            emptyLabel = new JLabel("Không có thông báo nào!");
            emptyLabel.setFont(CustomUI.verySmallFont);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 10));
            panel.add(emptyLabel);
            return;
        }

        for (String msg : notifications) {
            JLabel label = new JLabel(msg);
            label.setFont(CustomUI.verySmallFont);
            label.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 10));
            panel.add(label);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        popup.add(scrollPane);
        popup.show(this, -250, getHeight() + 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hasNewNotification) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.RED);
            int x = getWidth() - dotSize - 12;
            int y = 6;
            g2.fillOval(x, y, dotSize, dotSize);
            g2.dispose();
        }
    }
}
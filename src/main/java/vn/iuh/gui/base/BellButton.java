package vn.iuh.gui.base;

import vn.iuh.entity.ThongBao;
import vn.iuh.util.IconUtil;
import vn.iuh.util.TimeFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static vn.iuh.util.TimeFormat.formatTime;

public class BellButton extends JButton {
    private final List<ThongBao> notifications;
    private boolean hasNewNotification = false;
    private final int dotSize = 10;
    private JPopupMenu popup;
    private static final int DEFAULT_ITEM_HEIGHT = 60;
    private ImageIcon hasNewNotiIcon = IconUtil.createMenuIcon("/icons/notification.png");
    private ImageIcon defaultIcon = IconUtil.createMenuIcon("/icons/notification-bell.png");

    public BellButton() {
        super(IconUtil.createMenuIcon("/icons/notification-bell.png"));
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
                setIcon(defaultIcon);
                repaint();
                showNotificationPopup();
            } else {
                showNotificationPopup();
            }
        });
    }

    public void addNotification(ThongBao noti) {
        this.notifications.add(noti);
        hasNewNotification = true;
        setIcon(hasNewNotiIcon);
        setBackground(CustomUI.darkBlue);
        repaint();
    }


    public void clearNotifications() {
        notifications.clear();
        hasNewNotification = false;
        setIcon(defaultIcon);
        repaint();
    }

    private void showNotificationPopup() {
        popup = new JPopupMenu();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.WHITE);

        if (notifications.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có thông báo nào!");
            emptyLabel.setFont(CustomUI.verySmallFont);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(emptyLabel);
        } else {
            for (ThongBao msg : notifications) {
                JPanel item = createNotificationItem(msg);
                container.add(item);
                container.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setPreferredSize(new Dimension(310, 220));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        // ÉP width item = viewport (CỐT LÕI – CÁCH DUY NHẤT)
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = scrollPane.getViewport().getWidth();
                for (Component c : container.getComponents()) {
                    if (c instanceof JPanel) {
                        c.setMaximumSize(new Dimension(width - 16, Integer.MAX_VALUE));
                    }
                }
                container.revalidate();
            }
        });

        popup.add(scrollPane);
        popup.show(this, -260, getHeight() + 4);
    }

    private JPanel createNotificationItem(ThongBao msg) {
        RoundedPanel item = new RoundedPanel(12);
        item.setLayout(new BorderLayout(0, 6));
        item.setBackground(CustomUI.mine);

        // margin trong
        item.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // BoxLayout bắt buộc
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setPreferredSize(new Dimension(0, DEFAULT_ITEM_HEIGHT));
        item.setMinimumSize(new Dimension(0, DEFAULT_ITEM_HEIGHT));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lblContent = new JLabel();
        lblContent.setFont(CustomUI.verySmallFont);


        // HTML wrap theo width thật (KHÔNG hard-code)
        lblContent.setText(
                "<html>" +
                        msg.getNoiDung() +
                        "</html>"
        );

        JLabel lblTime = new JLabel(TimeFormat.formatTime(msg.getThoiGianTao()));
        lblTime.setFont(CustomUI.verySmallFont);
        lblTime.setForeground(Color.GRAY);

        item.add(lblContent, BorderLayout.CENTER);
        item.add(lblTime, BorderLayout.SOUTH);

        return item;
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
package vn.iuh.gui.base;

import javax.swing.*;
import java.awt.*;

public class UserInfoPanel extends JPanel {
    private JLabel lblAvatar;
    private JLabel lblRole;
    private JLabel lblName;
    private JLabel lblDetail;

    public UserInfoPanel(String role, String name) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(200, 160));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setIcon(resizeIcon(new ImageIcon("C:\\Users\\ANH DUC\\IdeaProjects\\BTTH_OOP\\src\\main\\resources\\meme.jpg"), 60, 60));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setOpaque(false);

        lblRole = new JLabel(role, SwingConstants.CENTER);
        lblRole.setForeground(Color.WHITE);

        lblName = new JLabel(name, SwingConstants.CENTER);
        lblName.setForeground(Color.WHITE);

        lblDetail = new JLabel("<Xem chi tiết>", SwingConstants.CENTER);
        lblDetail.setForeground(Color.LIGHT_GRAY);

        infoPanel.add(lblRole);
        infoPanel.add(lblName);
        infoPanel.add(lblDetail);

        add(lblAvatar, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(50, 100, 155));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2.dispose();
    }

    private Icon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resized);
    }

    public JLabel getLblDetail() {
        return lblDetail;
    }
}

package vn.iuh.gui.base;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedWrapperPanel extends JPanel {
    private int cornerRadius = 15;
    private int borderWidth = 5;

    public RoundedWrapperPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Vẽ background trắng bo tròn
        g2.setColor(new Color(230, 230, 230));
        g2.fill(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));

        // Vẽ border bo tròn
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.draw(new RoundRectangle2D.Double(borderWidth/1.5, borderWidth/1.5,
                width - borderWidth, height - borderWidth, cornerRadius, cornerRadius));
        g2.dispose();
        super.paintComponent(g);
    }
}

package vn.iuh.gui.base;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class HeadingTop extends JPanel {
    private final JPanel pnlCounting;
    private final JLabel countingLabel;

    public HeadingTop() {
        setBackground(CustomUI.darkBlue);
        pnlCounting = new JPanel();
        countingLabel = new JLabel();
        countingLabel.setForeground(CustomUI.white);
        countingLabel.setFont(CustomUI.normalFont);


        SimpleDateFormat countingSecondSdf = new SimpleDateFormat("HH:mm:ss");
        countingSecondSdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        Timer updateSecondTimer = new Timer(1000, e -> {
            Date now = new Date();
            countingLabel.setText(countingSecondSdf.format(now));
        });

        updateSecondTimer.start();
        pnlCounting.setOpaque(false);
        pnlCounting.add(countingLabel);
        add(Box.createHorizontalStrut(50));
        add(pnlCounting);
    }

    @Override
    public void paintComponents(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(50, 100, 155));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        g2.dispose();
        super.paintComponent(g);
    }
}

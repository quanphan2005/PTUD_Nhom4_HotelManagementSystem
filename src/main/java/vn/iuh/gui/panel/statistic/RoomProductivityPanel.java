package vn.iuh.gui.panel.statistic;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.gui.base.CustomUI;

import javax.swing.*;
import java.awt.*;

public class RoomProductivityPanel extends JPanel {
    private JPanel pnlTop;
    private JLabel lblTop;

    private void init(){
        createTopPanel();
        createCenterPanel();
    }

    public RoomProductivityPanel() {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        init();
    }
    private void createTopPanel(){
        pnlTop = new JPanel();
        lblTop = new JLabel("Thống kê hiệu suất phòng");
        lblTop.setForeground(CustomUI.white);
        pnlTop.setBackground(CustomUI.lightBlue);
        pnlTop.add(lblTop);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        add(pnlTop);
    }

    private void createCenterPanel(){
        
    }
}

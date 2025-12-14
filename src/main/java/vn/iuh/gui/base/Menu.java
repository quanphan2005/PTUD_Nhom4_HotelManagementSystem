package vn.iuh.gui.base;

import net.miginfocom.swing.MigLayout;
import vn.iuh.util.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class Menu extends JComponent {
    private MigLayout migLayout;
    private final String[][] menuItems = new String[][]{
            {"Đơn đặt phòng",  "Quản lý đơn đặt phòng"},
            {"Phòng",  "Quản lý đặt phòng","Tìm phòng","Quản lý phòng", "Quản lý loại phòng", "Thống kê hiệu suất"},
            {"Dịch vụ", "Tìm dịch vụ","Quản lý dịch vụ", "Quản lý loại dịch vụ"},
            {"Hóa đơn", "Tìm hóa đơn", "Thống kê doanh thu"},
            {"Nhân viên", "Quản lý nhân viên", "Quản lý tài khoản"},
            {"Khách hàng", "Tìm khách hàng", "Quản lý khách hàng"},
            {"Hệ thống", "Quản lý phụ phí", "Thiết lập hệ thống", "Trợ giúp"}
    };

    private final ImageIcon[] menuIcons = {
            IconUtil.createMenuIcon("/icons/booking2.png"),
            IconUtil.createMenuIcon("/icons/bed.png"),
            IconUtil.createMenuIcon("/icons/call_service.png"),
            IconUtil.createMenuIcon("/icons/bill.png"),
            IconUtil.createMenuIcon("/icons/receptionist.png"),
            IconUtil.createMenuIcon("/icons/man.png"),
            IconUtil.createMenuIcon("/icons/content-management-system.png"),

    };

    public Menu(Main mainFrame) {
        init(mainFrame);
        setOpaque(false);
    }

    private void init(Main mainFrame){
        migLayout = new MigLayout("wrap 1, fillx, gapy 0, inset 2", "fill");
        setLayout(migLayout);
        for(int i = 0 ; i < menuItems.length; i++){
            addMenu(menuItems[i][0], i, mainFrame);
        }
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addMenu(String menuName, int index, Main mainFrame){
        int length = menuItems[index].length;
        MenuItem item = new MenuItem(menuName, index, length > 1);

        item.setIcon(menuIcons[index]);
        item.setIconTextGap(10);
        item.setFont(CustomUI.smallFont);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (length > 1) {
                    if (!item.isSelected()) {
                        item.setSelected (true);
                        addSubMenu(item, index, length, getComponentZOrder(item),mainFrame);
                    } else {
                        hideMenu(item, index);
                        item.setSelected(false);
                    }
                }
            }
        });
        add(item);
        revalidate();
        repaint();
    }

    private void addSubMenu(MenuItem item, int index, int length, int indexZOrder, Main mainFrame){
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, inset 0, gapy 0", "fill"));
        panel.setName(index + "");
        panel.setBackground(Color.red);
        panel.setBackground(new Color(70, 130, 180));
        for (int i = 1; i < length; i++) {
            String pageName = menuItems[index][i];
            MenuItem subItem = new MenuItem(pageName, i, false);
            subItem.addActionListener( (e) -> mainFrame.showCard(pageName));
            subItem.initSubMenu(i, length);
            panel.add(subItem);
        }
        add(panel, "h 0!", indexZOrder + 1);
        MenuAnimation.showMenu(panel, item, migLayout, true);
        revalidate();
        repaint();
    }

    private void hideMenu(MenuItem item, int index) {
        for (Component com : getComponents()) {
            if (com instanceof JPanel && com.getName() != null && com.getName().equals(index + "")) {
                com.setName(null);
                MenuAnimation.showMenu(com, item, migLayout, false);
                break;
            }
        }
        revalidate();
        repaint();
    }
    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(50, 100, 155));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(grphcs);
    }
}

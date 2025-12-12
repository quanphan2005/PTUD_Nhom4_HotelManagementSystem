package vn.iuh.gui.base;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MenuItem extends JButton{
    private int index;
    private boolean subMenuAble;
    private boolean isOpen;
    //submenu
    private int subMenuIndex;
    private int length;

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public MenuItem(String name, int index, boolean subMenuAble) {
        super(name);
        this.index = index;
        this.subMenuAble = subMenuAble;
        this.isOpen = false;
        setContentAreaFilled(false);
        setForeground(new Color(230, 230, 230));
        setPreferredSize(new Dimension(0, 50));
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setBorderPainted(false);
        setFocusable(false);
        setBorder(new EmptyBorder(0,10, 0,0));
        setFont(CustomUI.subMenuFont);
    }

    public void initSubMenu(int subMenuIndex, int length){
        this.subMenuIndex = subMenuIndex;
        this.length = length;
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(0,30,0, 0));
        setBackground(new Color(18, 99, 63));
        setOpaque(true);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSubMenuAble() {
        return subMenuAble;
    }

    public void setSubMenuAble(boolean subMenuAble) {
        this.subMenuAble = subMenuAble;
    }

    public int getSubMenuIndex() {
        return subMenuIndex;
    }

    public void setSubMenuIndex(int subMenuIndex) {
        this.subMenuIndex = subMenuIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    @Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (length  != 0) {
            g2.setColor(new Color(230, 230, 230));
            if (subMenuIndex == 1) {
                g2.drawLine(18, 0, 18, getHeight());
                g2.drawLine(18, getHeight() / 2, 26, getHeight() / 2);
            } else if (subMenuIndex == length - 1) {
                g2.drawLine(18, 0, 18, getHeight() / 2);
                g2.drawLine(18, getHeight() / 2, 26, getHeight() / 2);
            } else {
                g2.drawLine(18, 0, 18, getHeight());
                g2.drawLine(18, getHeight() / 2, 26, getHeight() / 2);
            }
        }
        g2.dispose();
    }

}

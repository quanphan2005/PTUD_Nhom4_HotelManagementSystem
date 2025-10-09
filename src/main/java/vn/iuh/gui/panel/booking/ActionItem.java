package vn.iuh.gui.panel.booking;

import javax.swing.*;
import java.awt.*;

public class ActionItem {
    private String text;
    private ImageIcon icon;
    private Color backgroundColor;
    private Runnable action;

    ActionItem(String text, ImageIcon icon, Color backgroundColor, Runnable action) {
        this.text = text;
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }
}

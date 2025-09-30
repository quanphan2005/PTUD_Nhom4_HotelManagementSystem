package vn.iuh.gui.panel;

import javax.swing.*;
import java.awt.*;

public class ActionItem {
    final String text;
    final ImageIcon icon;
    final Color backgroundColor;
    final Runnable action;

    ActionItem(String text, ImageIcon icon, Color backgroundColor, Runnable action) {
        this.text = text;
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.action = action;
    }
}

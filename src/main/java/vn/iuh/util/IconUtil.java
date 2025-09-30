package vn.iuh.util;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class IconUtil {
    public static ImageIcon createServiceIcon() {
        // Try to load from resources first, fallback to creating a colored icon
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/call_service.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(255, 165, 0), "🍽️"); // Orange for service
        }
    }

    public static ImageIcon createBookingIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/reservation.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(76, 175, 80), "🏨"); // Green for booking
        }
    }

    public static ImageIcon createCheckOutIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/checkout.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(244, 67, 54), "🚪"); // Red for checkout
        }
    }

    public static ImageIcon createCheckInIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/checkin.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(76, 175, 80), "🗝️"); // Green for check-in
        }
    }

    public static ImageIcon createTransferIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/transfer.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(33, 150, 243), "🔄"); // Blue for transfer
        }
    }

    public static ImageIcon createExtendIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/extend.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(255, 193, 7), "📋"); // Amber for extend
        }
    }

    public static ImageIcon createCancelIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/error.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(244, 67, 54), "❌"); // Red for cancel
        }
    }

    public static ImageIcon createCompleteIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/error.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(244, 67, 54), "❌"); // Red for cancel
        }
    }

    // TODO - fix duplicate method
    public static ImageIcon createProgressIcon() {
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(IconUtil.class.getResource("/icons/error.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(128, 128, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(new Color(244, 67, 54), "❌"); // Red for cancel
        }
    }

    public static ImageIcon createColoredIcon(Color bgColor, String emoji) {
        int size = 48;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Create circular background
        g2d.setColor(bgColor);
        g2d.fillOval(2, 2, size - 4, size - 4);

        // Add border
        g2d.setColor(bgColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(2, 2, size - 4, size - 4);

        // Add emoji text in center
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (size - fm.stringWidth(emoji)) / 2;
        int textY = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(emoji, textX, textY);

        g2d.dispose();
        return new ImageIcon(image);
    }


}

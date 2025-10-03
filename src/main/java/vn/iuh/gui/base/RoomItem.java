package vn.iuh.gui.base;

import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.panel.BookingFormPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class RoomItem extends JPanel {
    private BookingResponse bookingResponse;

    public BookingResponse getBookingResponse() {
        return bookingResponse;
    }

    public RoomItem(BookingResponse bookingResponse) {
        this.bookingResponse = bookingResponse;

        createUI();
        createActionListener();
    }

    private void createActionListener() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String cardName = "Đặt phòng";
                BookingFormPanel bookingFormPanel = new BookingFormPanel(bookingResponse);

                Main.addCard(bookingFormPanel, cardName);
                Main.showCard(cardName);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                // Add subtle hover effect
                setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            }
        });
    }

    private void createUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 120));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        setBackground(Color.WHITE);

        // Left blue panel
        JPanel leftPanel = createLeftPanel();

        // Right panel with room details
        JPanel rightPanel = createRightPanel();

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(100, 120));
        leftPanel.setBackground(new Color(30, 144, 255)); // Blue color
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Room name
        JLabel lblRoomName = new JLabel("PHÒNG");
        lblRoomName.setFont(CustomUI.smallFont);
        lblRoomName.setForeground(Color.WHITE);
        lblRoomName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRoomNumber = new JLabel(bookingResponse.getRoomName().substring(6));
        lblRoomNumber.setFont(CustomUI.smallFont);
        lblRoomNumber.setForeground(Color.WHITE);
        lblRoomNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Capacity with icon
        JPanel capacityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        capacityPanel.setOpaque(false);

        JLabel lblCapacity = new JLabel(String.valueOf(bookingResponse.getNumberOfCustomers()));
        lblCapacity.setFont(CustomUI.normalFont);
        lblCapacity.setForeground(Color.WHITE);

        // Create person icon using ImageIcon
        ImageIcon personIcon = createPersonIcon();
        JLabel personIconLabel = new JLabel(personIcon);

        capacityPanel.add(lblCapacity);
        capacityPanel.add(personIconLabel);

        // Status icon at bottom
        JLabel statusIcon = createStatusIcon();
        statusIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components with proper spacing
        leftPanel.add(lblRoomName);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(lblRoomNumber);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(capacityPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(statusIcon);

        return leftPanel;
    }

    private ImageIcon createPersonIcon() {
        // Try to load from resources first, fallback to creating a colored icon
        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/capacity.png")));
            return new ImageIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return createColoredIcon(Color.WHITE, "👤", 32);
        }
    }

    private JLabel createStatusIcon() {
        String status = bookingResponse.getRoomStatus().toLowerCase();
        ImageIcon statusIcon = getStatusImageIcon(status);

        JLabel iconLabel = new JLabel(statusIcon);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        return iconLabel;
    }

    private ImageIcon getStatusImageIcon(String status) {
        // Try to load appropriate icon from resources first
        String iconPath = getIconPathForStatus(status);

        try {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(iconPath)));
            return new ImageIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            // Fallback to emoji-based colored icons
            return createFallbackStatusIcon(status);
        }
    }

    private String getIconPathForStatus(String status) {

        RoomStatus roomStatus = null;
        try {
            roomStatus = RoomStatus.fromString(status);
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown room status: " + status);
        }

        if (Objects.isNull(roomStatus)) {
            roomStatus = RoomStatus.ROOM_AVAILABLE_STATUS; // Default to available if unknown
        }

        return switch (roomStatus) {
            case ROOM_AVAILABLE_STATUS -> "/icons/checked.png";
            case ROOM_USING_STATUS -> "/icons/checkin.png";
            case ROOM_BOOKED_STATUS -> "/icons/calendar.png";
            case ROOM_CHECKING_STATUS -> "/icons/room.png";
            case ROOM_CHECKOUT_LATE_STATUS -> "/icons/error.png";
            case ROOM_MAINTENANCE_STATUS -> "/icons/transfer.png";
            case ROOM_CLEANING_STATUS -> "/icons/room.png";
        };
    }

    private ImageIcon createFallbackStatusIcon(String status) {

        RoomStatus roomStatus;
        try {
            roomStatus = RoomStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown room status: " + status);
            roomStatus = RoomStatus.ROOM_AVAILABLE_STATUS; // Default to available if unknown
        }


        String emoji = switch (roomStatus) {
            case ROOM_AVAILABLE_STATUS -> "✅";
            case ROOM_USING_STATUS -> "🏠";
            case ROOM_BOOKED_STATUS -> "📅";
            case ROOM_CHECKING_STATUS -> "🛏️";
            case ROOM_CHECKOUT_LATE_STATUS -> "⚠️";
            case ROOM_MAINTENANCE_STATUS -> "🔧";
            case ROOM_CLEANING_STATUS -> "🧹";
        };

        return createColoredIcon(Color.WHITE, emoji, 24);
    }

    // Helper method to create colored icons with emoji text when image files are not available
    private ImageIcon createColoredIcon(Color textColor, String emoji, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Add emoji text
        g2d.setColor(textColor);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, size - 4));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (size - fm.stringWidth(emoji)) / 2;
        int textY = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(emoji, textX, textY);

        g2d.dispose();
        return new ImageIcon(image);
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Determine background color and content based on status
        String status = bookingResponse.getRoomStatus().toLowerCase();

        if (isEmptyRoom(status)) {
            return createEmptyRoomPanel();
        } else if (isOccupiedRoom(status)) {
            return createOccupiedRoomPanel();
        } else {
            return createEmptyRoomPanel();
        }
    }

    private boolean isEmptyRoom(String status) {
        return status.equals("còn trống") || status.equals("đang hoạt động");
    }

    private boolean isOccupiedRoom(String status) {
        return status.equals("đang sử dụng") || status.equals("đặt trước") ||
                status.equals("đang kiểm tra") || status.equals("trả phòng trễ");
    }

    private JPanel createEmptyRoomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(144, 238, 144)); // Light green background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Room type at top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblRoomType = new JLabel(bookingResponse.getRoomType().toUpperCase());
        lblRoomType.setFont(new Font("Arial", Font.BOLD, 12));
        lblRoomType.setForeground(Color.BLACK);
        panel.add(lblRoomType, gbc);

        // Status in center (large)
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel lblStatus = new JLabel("TRỐNG");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 24));
        lblStatus.setForeground(Color.BLACK);
        panel.add(lblStatus, gbc);

        // Price information using GridBagLayout
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 10, 2, 5);

        // Hourly price row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel lblHourlyLabel = new JLabel("Giá theo giờ:");
        lblHourlyLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHourlyLabel.setForeground(Color.BLACK);
        panel.add(lblHourlyLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblHourlyPrice = new JLabel(String.format("%.0f vnđ", bookingResponse.getHourlyPrice()));
        lblHourlyPrice.setFont(new Font("Arial", Font.PLAIN, 11));
        lblHourlyPrice.setForeground(Color.BLACK);
        panel.add(lblHourlyPrice, gbc);

        // Daily price row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblDailyLabel = new JLabel("Giá theo ngày:");
        lblDailyLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lblDailyLabel.setForeground(Color.BLACK);
        panel.add(lblDailyLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblDailyPrice = new JLabel(String.format("%.0f vnđ", bookingResponse.getDailyPrice()));
        lblDailyPrice.setFont(new Font("Arial", Font.PLAIN, 11));
        lblDailyPrice.setForeground(Color.BLACK);
        panel.add(lblDailyPrice, gbc);

        return panel;
    }

    private JPanel createOccupiedRoomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(17, 216, 230)); // Light blue background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Room type at top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblRoomType = new JLabel(bookingResponse.getRoomType().toUpperCase());
        lblRoomType.setFont(new Font("Arial", Font.BOLD, 12));
        lblRoomType.setForeground(Color.BLACK);
        panel.add(lblRoomType, gbc);

        // Customer name in center
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        String customerName = bookingResponse.getCustomerName();
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "Khách hàng";
        }
        JLabel lblCustomer = new JLabel(customerName);
        lblCustomer.setFont(CustomUI.smallFont);
        lblCustomer.setForeground(Color.BLACK);
        panel.add(lblCustomer, gbc);

        // Time information using GridBagLayout
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 10, 2, 5);

        String checkInTime = bookingResponse.getTimeIn() != null ? bookingResponse.getTimeIn().toString() : "30/10/2025";
        String checkOutTime = bookingResponse.getTimeOut() != null ? bookingResponse.getTimeOut().toString() : "01/11/2025";

        // Check-in row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel lblCheckInLabel = new JLabel("Checkin:");
        lblCheckInLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCheckInLabel.setForeground(Color.BLACK);
        panel.add(lblCheckInLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblCheckIn = new JLabel(checkInTime);
        lblCheckIn.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCheckIn.setForeground(Color.BLACK);
        panel.add(lblCheckIn, gbc);

        // Check-out row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblCheckOutLabel = new JLabel("Checkout:");
        lblCheckOutLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCheckOutLabel.setForeground(Color.BLACK);
        panel.add(lblCheckOutLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblCheckOut = new JLabel(checkOutTime);
        lblCheckOut.setFont(new Font("Arial", Font.PLAIN, 11));
        lblCheckOut.setForeground(Color.BLACK);
        panel.add(lblCheckOut, gbc);

        return panel;
    }

    private JPanel createDefaultRoomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 224)); // Light yellow background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Room type at top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel lblRoomType = new JLabel(bookingResponse.getRoomType().toUpperCase());
        lblRoomType.setFont(CustomUI.smallFont);
        lblRoomType.setForeground(Color.WHITE);
        panel.add(lblRoomType, gbc);

        // Status in center
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel lblStatus = new JLabel(bookingResponse.getRoomStatus().toUpperCase());
        lblStatus.setFont(CustomUI.smallFont);
        lblStatus.setForeground(Color.WHITE);
        panel.add(lblStatus, gbc);

        // Additional info at bottom
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 10);
        JLabel lblInfo = new JLabel("Đang xử lý...");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        panel.add(lblInfo, gbc);

        return panel;
    }

    public String getRoomId() {
        return bookingResponse.getRoomId();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof String)
            return Objects.equals(bookingResponse.getRoomId(), o);


        if (!(o instanceof RoomItem roomItem)) return false;
        return Objects.equals(bookingResponse, roomItem.bookingResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bookingResponse);
    }

    public Object getRoomCategoryName() {
        return bookingResponse.getRoomType();
    }

    public Object getCapacity() {
        return bookingResponse.getNumberOfCustomers();
    }
}

package vn.iuh.gui.base;

import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.panel.booking.BookingFormPanel;
import vn.iuh.gui.panel.booking.RoomUsageFormPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public class RoomItem extends JPanel {
    private BookingResponse bookingResponse;
    private boolean isSelected = false;
    private boolean isMultiBookingMode = false;
    private MultiRoomSelectionCallback selectionCallback;
    private JPanel overlayPanel;

    private DecimalFormat priceFormatter = new DecimalFormat("#,###");

    // Interface for multi-room selection callback
    public interface MultiRoomSelectionCallback {
        void onRoomSelectionChanged(BookingResponse room, boolean selected);
    }

    public BookingResponse getBookingResponse() {
        return bookingResponse;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateVisualState();
    }

    public void setMultiBookingMode(boolean multiBookingMode) {
        this.isMultiBookingMode = multiBookingMode;
        updateVisualState();
    }

    public void setSelectionCallback(MultiRoomSelectionCallback callback) {
        this.selectionCallback = callback;
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
                if (isMultiBookingMode) {
                    // Toggle selection in multi-booking mode
                    isSelected = !isSelected;
                    updateVisualState();

                    if (selectionCallback != null) {
                        selectionCallback.onRoomSelectionChanged(bookingResponse, isSelected);
                    }
                } else {
                    // Original single room booking behavior
                    String cardName = PanelName.BOOKING.getName();
                    if (bookingResponse.getRoomStatus().equalsIgnoreCase(RoomStatus.ROOM_EMPTY_STATUS.getStatus())) {
                        Main.addCard(new BookingFormPanel(bookingResponse, PanelName.RESERVATION_MANAGEMENT.getName()), cardName);
                    } else if (
                                bookingResponse.getRoomStatus()
                                               .equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus())
                                || bookingResponse.getRoomStatus()
                                                  .equalsIgnoreCase(RoomStatus.ROOM_CHECKING_STATUS.getStatus())
                                || bookingResponse.getRoomStatus()
                                                  .equalsIgnoreCase(RoomStatus.ROOM_USING_STATUS.getStatus())
                                || bookingResponse.getRoomStatus()
                                                  .equalsIgnoreCase(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus())
                                || bookingResponse.getRoomStatus().equalsIgnoreCase(RoomStatus.ROOM_CLEANING_STATUS.getStatus())
                        ) {
                        cardName = PanelName.ROOM_USING.getName();
                        System.out.println(bookingResponse.getTimeIn());
                        Main.addCard(new RoomUsageFormPanel(bookingResponse), cardName);
                        }
                    Main.showCard(cardName);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                if (!isMultiBookingMode || !isSelected) {
                    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (!isMultiBookingMode || !isSelected) {
                    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                }
            }
        });
    }

    private void updateVisualState() {
        if (isMultiBookingMode && isSelected) {
            // Apply blur effect and show check icon
            applySelectionEffect();
        } else {
            // Remove selection effects
            removeSelectionEffect();
        }
        repaint();
    }

    private void applySelectionEffect() {
        // Create overlay panel if it doesn't exist
        if (overlayPanel == null) {
            overlayPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    // Create blur effect
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2d.setColor(new Color(100, 149, 237, 150)); // Semi-transparent blue
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            };
            overlayPanel.setOpaque(false);
            overlayPanel.setLayout(new BorderLayout());

            // Add big check icon
            JLabel checkIcon = createBigCheckIcon();
            overlayPanel.add(checkIcon, BorderLayout.CENTER);
        }

        // Add overlay if not already added (preserve original BorderLayout)
        if (overlayPanel.getParent() != this) {
            // Add overlay panel to this component
            add(overlayPanel, BorderLayout.CENTER, 0); // Add as first component (on top)

            // Ensure proper layering by validating the component hierarchy
//            validate();
        }

        // Set overlay bounds to cover the entire component area
        SwingUtilities.invokeLater(() -> {
            overlayPanel.setBounds(0, 0, getWidth(), getHeight());
            overlayPanel.setVisible(true);

            // Ensure the overlay is on top
            if (overlayPanel.getParent() == this) {
                setComponentZOrder(overlayPanel, 0);
            }

            repaint();
        });

        setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 3)); // Blue selection border
    }

    private void removeSelectionEffect() {
        if (overlayPanel != null) {
            SwingUtilities.invokeLater(() -> {
                overlayPanel.setVisible(false);
                // Remove overlay from parent to avoid layout issues
                if (overlayPanel.getParent() == this) {
                    remove(overlayPanel);
                    revalidate();
                    repaint();
                }
            });
        }
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    }

    private JLabel createBigCheckIcon() {
        // Try to load check icon from resources first
        try {
            ImageIcon checkIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/checked.png")));
            checkIcon = new ImageIcon(checkIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));

            JLabel iconLabel = new JLabel(checkIcon, SwingConstants.CENTER);
            iconLabel.setOpaque(false);
            return iconLabel;
        } catch (Exception e) {
            // Fallback to text-based check icon
            return createTextCheckIcon();
        }
    }

    private JLabel createTextCheckIcon() {
        JLabel checkLabel = new JLabel("✓", SwingConstants.CENTER);
        checkLabel.setFont(new Font("Arial", Font.BOLD, 48));
        checkLabel.setForeground(Color.WHITE);
        checkLabel.setOpaque(false);

        // Add shadow effect
        checkLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        return checkLabel;
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
            roomStatus = RoomStatus.ROOM_EMPTY_STATUS; // Default to available if unknown
        }

        return switch (roomStatus) {
            case ROOM_EMPTY_STATUS -> "/icons/checked.png";
            case ROOM_USING_STATUS -> "/icons/using.png";
            case ROOM_BOOKED_STATUS -> "/icons/calendar.png";
            case ROOM_CHECKING_STATUS -> "/icons/checking.png";
            case ROOM_CHECKOUT_LATE_STATUS -> "/icons/warning.png";
            case ROOM_MAINTENANCE_STATUS -> "/icons/maintainance.png";
            case ROOM_CLEANING_STATUS -> "/icons/cleaning.png";
        };
    }

    private ImageIcon createFallbackStatusIcon(String status) {

        RoomStatus roomStatus;
        try {
            roomStatus = RoomStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown room status: " + status);
            roomStatus = RoomStatus.ROOM_EMPTY_STATUS; // Default to available if unknown
        }


        String emoji = switch (roomStatus) {
            case ROOM_EMPTY_STATUS -> "✅";
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
        return status.equalsIgnoreCase(RoomStatus.ROOM_EMPTY_STATUS.getStatus());
    }

    private boolean isOccupiedRoom(String status) {
        return status.equalsIgnoreCase(RoomStatus.ROOM_USING_STATUS.getStatus())
               || status.equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus())
               || status.equalsIgnoreCase(RoomStatus.ROOM_CHECKING_STATUS.getStatus())
               || status.equalsIgnoreCase(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
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
        lblHourlyLabel.setFont(CustomUI.supperSmallFont);
        lblHourlyLabel.setForeground(Color.BLACK);
        panel.add(lblHourlyLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblHourlyPrice = new JLabel(String.format("%.0f vnđ", bookingResponse.getHourlyPrice()));
        lblHourlyPrice.setText(priceFormatter.format(bookingResponse.getHourlyPrice()) + " VNĐ");
        lblHourlyPrice.setFont(CustomUI.supperSmallFont);
        lblHourlyPrice.setForeground(Color.BLACK);
        panel.add(lblHourlyPrice, gbc);

        // Daily price row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblDailyLabel = new JLabel("Giá theo ngày:");
        lblDailyLabel.setFont(CustomUI.supperSmallFont);
        lblDailyLabel.setForeground(Color.BLACK);
        panel.add(lblDailyLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblDailyPrice = new JLabel(String.format("%.0f vnđ", bookingResponse.getDailyPrice()));
        lblDailyPrice.setText(priceFormatter.format(bookingResponse.getDailyPrice()) + " VNĐ");
        lblDailyPrice.setFont(CustomUI.supperSmallFont);
        lblDailyPrice.setForeground(Color.BLACK);
        panel.add(lblDailyPrice, gbc);

        return panel;
    }

    private JPanel createOccupiedRoomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        if (RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(bookingResponse.getRoomStatus())) {
            panel.setBackground(CustomUI.cyan);
        } else if(RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(bookingResponse.getRoomStatus())){
            panel.setBackground(CustomUI.lightBlue);
        } else if (RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(bookingResponse.getRoomStatus())) {
            panel.setBackground(CustomUI.orange);
        } else if(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus().equalsIgnoreCase(bookingResponse.getRoomStatus())){
            panel.setBackground(CustomUI.red);
        } else if (RoomStatus.ROOM_CLEANING_STATUS.getStatus().equalsIgnoreCase(bookingResponse.getRoomStatus())) {
            panel.setBackground(CustomUI.lightGreen);
        } else {
            panel.setBackground(new Color(255, 255, 224)); // Default light yellow
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);

        // Room type at top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel roomStatus = new JLabel(bookingResponse.getRoomStatus().toUpperCase());
        roomStatus.setFont(new Font("Arial", Font.BOLD, 12));
        roomStatus.setForeground(Color.BLACK);
        panel.add(roomStatus, gbc);

        // Customer name in center
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        String customerName = bookingResponse.getCustomerName();
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "Khách hàng";
        }
        JLabel lblCustomer = new JLabel(customerName);
        lblCustomer.setFont(CustomUI.normalFont);
        lblCustomer.setForeground(Color.BLACK);
        panel.add(lblCustomer, gbc);

        // Time information using GridBagLayout
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 10, 2, 5);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");


        Date now = new Date();
        String checkInTime = bookingResponse.getTimeIn() != null
                ? bookingResponse.getTimeIn().toLocalDateTime().format(formatter)
                : now.toString();
        String checkOutTime = bookingResponse.getTimeOut() != null
                ? bookingResponse.getTimeOut().toLocalDateTime().format(formatter)
                : Date.from(now.toInstant().plus(1, ChronoUnit.DAYS)).toString();

        // Check-in row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel lblCheckInLabel = new JLabel("Checkin:");
        lblCheckInLabel.setFont(CustomUI.supperSmallFont);
        lblCheckInLabel.setForeground(Color.BLACK);
        panel.add(lblCheckInLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblCheckIn = new JLabel(checkInTime);
        lblCheckIn.setFont(CustomUI.supperSmallFont);
        lblCheckIn.setForeground(Color.BLACK);
        panel.add(lblCheckIn, gbc);

        // Check-out row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblCheckOutLabel = new JLabel("Checkout:");
        lblCheckOutLabel.setFont(CustomUI.supperSmallFont);
        lblCheckOutLabel.setForeground(Color.BLACK);
        panel.add(lblCheckOutLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel lblCheckOut = new JLabel(checkOutTime);
        lblCheckOut.setFont(CustomUI.supperSmallFont);
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

    public String getRoomStatus() {
        return bookingResponse.getRoomStatus();
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

    public boolean setBookingResponseStatus(String status){
        if(!status.equalsIgnoreCase(this.getBookingResponse().getRoomStatus())){
            this.bookingResponse.setRoomStatus(status);
            return true;
        }
        return false;
    }
}

package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.servcie.BookingService;
import vn.iuh.servcie.impl.BookingServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReservationManagementPanel extends JPanel {
    private GridRoomPanel gridRoomPanels;
    private final BookingService bookingService;

    private List<RoomItem> allRoomItems;
    private List<RoomItem> filteredRooms;

    // Search panel components
    private JComboBox<String> cmbRoomType;
    private JComboBox<Integer> cmbCapacity;
    private JSpinner spnCheckInDate;
    private JSpinner spnCheckOutDate;

    // Filter state
    private RoomFilter roomFilter;

    // Multi-booking mode components
    private boolean isMultiBookingMode = false;
    private JToggleButton btnMultiBookingToggle;
    private JLabel lblSelectedRooms;
    private JButton btnConfirmSelection;
    private List<BookingResponse> selectedRooms = new ArrayList<>();

    public ReservationManagementPanel() {
        bookingService = new BookingServiceImpl();
        roomFilter = new RoomFilter(null, null, null, null, null);

        List<RoomItem> roomItems = new ArrayList<>();

        List<BookingResponse> bookingResponses = bookingService.getAllBookingInfo();
        for (BookingResponse bookingResponse : bookingResponses) {
            roomItems.add(new RoomItem(bookingResponse));
        }

        allRoomItems = roomItems;
        filteredRooms = new ArrayList<>(allRoomItems);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        init();
        setupMultiBookingCallbacks();
    }

    private void setupMultiBookingCallbacks() {
        // Set selection callback for all room items
        for (RoomItem roomItem : allRoomItems) {
            roomItem.setSelectionCallback(this::handleRoomSelectionChanged);
        }
    }

    private void handleRoomSelectionChanged(BookingResponse room, boolean selected) {
        if (selected) {
            if (!selectedRooms.contains(room)) {
                selectedRooms.add(room);
            }
        } else {
            selectedRooms.remove(room);
        }
        updateSelectionDisplay();
    }

    private void updateSelectionDisplay() {
        lblSelectedRooms.setText("Đã chọn: " + selectedRooms.size() + " phòng");
        btnConfirmSelection.setEnabled(selectedRooms.size() > 0);
    }

    private void init() {
        createTopPanel();
        createSearchAndStatusPanel();
        createModeTogglePanel();
        createCenterPanel();
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("Quản lý đặt phòng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);

        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMinimumSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        add(pnlTop);
    }

    private void createSearchAndStatusPanel() {
        // Main container panel for search and status sections (50/50 split)
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        mainContainer.setPreferredSize(new Dimension(0, 200));
        mainContainer.setMinimumSize(new Dimension(0, 200));
        mainContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Left side - Search panel (50% width)
        JPanel leftPanel = createSearchPanel();

        // Right side - Status panel (50% width)
        JPanel rightPanel = createStatusPanel();

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        add(mainContainer);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize components
        initializeSearchComponents();

        // Add form rows following BookingFormPanel style
        addFormRow(panel, gbc, 0, 0, "Loại phòng:", cmbRoomType);
        addFormRow(panel, gbc, 1, 0, "Số người:", cmbCapacity);
        addFormRow(panel, gbc, 2, 0, "Checkin dự kiến:", spnCheckInDate);
        addFormRow(panel, gbc, 3, 0, "Checkout dự kiến:", spnCheckOutDate);

        return panel;
    }

    private void initializeSearchComponents() {
        // Room type dropdown - populate with actual room categories
        cmbRoomType = new JComboBox<>();
        cmbRoomType.addItem("TẤT CẢ");
        cmbRoomType.addItem("VIP");
        cmbRoomType.addItem("THƯỜNG");
        cmbRoomType.setPreferredSize(new Dimension(200, 35));
        cmbRoomType.setFont(CustomUI.smallFont);

        cmbRoomType.setSelectedIndex(0); // Default to "TẤT CẢ"
        cmbRoomType.addActionListener(e -> {
            roomFilter.roomType = (String) cmbRoomType.getSelectedItem();
            search();
        });

        // Capacity dropdown
        cmbCapacity = new JComboBox<>();
        cmbCapacity.addItem(1);
        cmbCapacity.addItem(2);
        cmbCapacity.addItem(4);
        cmbCapacity.setPreferredSize(new Dimension(200, 35));
        cmbCapacity.setFont(CustomUI.smallFont);

        cmbCapacity.setSelectedIndex(0); // Default to 1
        cmbCapacity.addActionListener(e -> {
            roomFilter.capacity = (Integer) cmbCapacity.getSelectedItem();
            search();
        });

        // Check-in date spinner following BookingFormPanel style
        spnCheckInDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(spnCheckInDate, "dd/MM/yyyy");
        spnCheckInDate.setEditor(checkInEditor);
        spnCheckInDate.setPreferredSize(new Dimension(200, 35));
        spnCheckInDate.setFont(CustomUI.smallFont);

        spnCheckInDate.addChangeListener(e -> {
            roomFilter.checkinDate = (java.util.Date) spnCheckInDate.getValue();
            search();
        });

        // Check-out date spinner following BookingFormPanel style
        spnCheckOutDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkOutEditor = new JSpinner.DateEditor(spnCheckOutDate, "dd/MM/yyyy");
        spnCheckOutDate.setEditor(checkOutEditor);
        spnCheckOutDate.setPreferredSize(new Dimension(200, 35));
        spnCheckOutDate.setFont(CustomUI.smallFont);

        spnCheckOutDate.addChangeListener(e -> {
            roomFilter.checkoutDate = (java.util.Date) spnCheckOutDate.getValue();
            search();
        });
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.lightGreen, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(600, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Calculate actual quantities for each status
        int totalRooms = allRoomItems != null ? allRoomItems.size() : 0;
        int availableCount = getStatusCount(RoomStatus.ROOM_EMPTY_STATUS.getStatus());
        int bookedCount = getStatusCount(RoomStatus.ROOM_BOOKED_STATUS.getStatus());
        int checkingCount = getStatusCount(RoomStatus.ROOM_CHECKING_STATUS.getStatus());
        int usingCount = getStatusCount(RoomStatus.ROOM_USING_STATUS.getStatus());
        int lateCount = getStatusCount(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
        int cleaningCount = getStatusCount(RoomStatus.ROOM_CLEANING_STATUS.getStatus());
        int maintenanceCount = getStatusCount(RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus());

        // Create status buttons with actual quantities and proper colors
        createStatusButton(panel, gbc, 0, 0, "Tất cả (" + totalRooms + ")", CustomUI.lightGreen, "Tất cả");
        createStatusButton(panel, gbc, 1, 0, RoomStatus.ROOM_EMPTY_STATUS.getStatus() + " (" + availableCount + ")", CustomUI.lightGreen, RoomStatus.ROOM_EMPTY_STATUS.getStatus());
        createStatusButton(panel, gbc, 2, 0, RoomStatus.ROOM_BOOKED_STATUS.getStatus() + " (" + bookedCount + ")", CustomUI.cyan, RoomStatus.ROOM_BOOKED_STATUS.getStatus());

        createStatusButton(panel, gbc, 0, 1, RoomStatus.ROOM_CHECKING_STATUS.getStatus() + " (" + checkingCount + ")", CustomUI.lightBlue, RoomStatus.ROOM_CHECKING_STATUS.getStatus());
        createStatusButton(panel, gbc, 1, 1, RoomStatus.ROOM_USING_STATUS.getStatus() + " (" + usingCount + ")", Color.ORANGE, RoomStatus.ROOM_USING_STATUS.getStatus());
        createStatusButton(panel, gbc, 2, 1, RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus() + " (" + lateCount + ")", Color.RED, RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());

        createStatusButton(panel, gbc, 0, 2, RoomStatus.ROOM_CLEANING_STATUS.getStatus() + " (" + cleaningCount + ")", new Color(144, 238, 144), RoomStatus.ROOM_CLEANING_STATUS.getStatus());
        createStatusButton(panel, gbc, 1, 2, RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus() + " (" + maintenanceCount + ")", Color.LIGHT_GRAY, RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus());

        return panel;
    }

    private int getStatusCount(String status) {
        if (allRoomItems == null) return 0;

        int count = 0;
        for (RoomItem roomItem : allRoomItems) {
            if (roomItem.getBookingResponse().getRoomStatus().equalsIgnoreCase(status)) {
                count++;
            }
        }
        return count;
    }

    private void createStatusButton(JPanel parent, GridBagConstraints gbc, int x, int y, String text, Color color, String statusValue) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton statusBtn = new JButton(text);
        statusBtn.setBackground(color);
        statusBtn.setForeground(Color.BLACK);
        statusBtn.setFont(CustomUI.verySmallFont);
        statusBtn.setPreferredSize(new Dimension(160, 40));
        statusBtn.setMinimumSize(new Dimension(160, 40));
        statusBtn.setMaximumSize(new Dimension(160, 40));
        statusBtn.setFocusPainted(false);
        statusBtn.putClientProperty(FlatClientProperties.STYLE,
                                    "arc: 10; " +
                                    "borderWidth: 1; " +
                                    "borderColor: #808080; " +
                                    "margin: 10,10,10,10"
        );

        statusBtn.addActionListener(e -> handleStatusFilter(statusValue));
        parent.add(statusBtn, gbc);
    }

    private void createModeTogglePanel() {
        JPanel modePanel = new JPanel(new BorderLayout());
        modePanel.setBackground(Color.WHITE);
        modePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.lightGray, 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15
        )));

        // Left panel with toggle button
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(Color.WHITE);

        // Multi-booking toggle button with icon and shorter text
        btnMultiBookingToggle = new JToggleButton();
        updateToggleButtonAppearance(false);
        btnMultiBookingToggle.setFont(CustomUI.normalFont);
        btnMultiBookingToggle.setPreferredSize(new Dimension(250, 35));
        btnMultiBookingToggle.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnMultiBookingToggle.addActionListener(e -> toggleMultiBookingMode());

        leftPanel.add(btnMultiBookingToggle);

        // Right panel with selection info and confirm button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setBackground(Color.WHITE);

        // Selected rooms label
        lblSelectedRooms = new JLabel("Đã chọn: 0 phòng");
        lblSelectedRooms.setFont(CustomUI.normalFont);
        lblSelectedRooms.setForeground(new Color(0, 123, 255));
        lblSelectedRooms.setVisible(false);

        // Confirm selection button
        btnConfirmSelection = new JButton("Xác nhận");
        btnConfirmSelection.setFont(CustomUI.normalFont);
        btnConfirmSelection.setBackground(CustomUI.darkGreen);
        btnConfirmSelection.setForeground(Color.WHITE);
        btnConfirmSelection.setPreferredSize(new Dimension(150, 35));
        btnConfirmSelection.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnConfirmSelection.setEnabled(false);
        btnConfirmSelection.setVisible(false);
        btnConfirmSelection.addActionListener(e -> confirmMultiRoomSelection());

        rightPanel.add(lblSelectedRooms);
        rightPanel.add(btnConfirmSelection);

        modePanel.add(leftPanel, BorderLayout.WEST);
        modePanel.add(rightPanel, BorderLayout.EAST);

        // Set fixed height for mode panel
        modePanel.setPreferredSize(new Dimension(0, 50));
        modePanel.setMinimumSize(new Dimension(0, 50));
        modePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        add(modePanel);
    }

    private void updateToggleButtonAppearance(boolean isMultiMode) {
        if (isMultiMode) {
            // Exit multi-booking mode - use error icon
            try {
                ImageIcon errorIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/error.png")));
                errorIcon = new ImageIcon(errorIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                btnMultiBookingToggle.setIcon(errorIcon);
            } catch (Exception e) {
                // Fallback without icon
            }
            btnMultiBookingToggle.setText("Thoát đặt nhiều");
            btnMultiBookingToggle.setBackground(new Color(220, 53, 69));
            btnMultiBookingToggle.setForeground(Color.WHITE);
        } else {
            btnMultiBookingToggle.setIcon(null);
            btnMultiBookingToggle.setText("Đặt nhiều phòng");
            btnMultiBookingToggle.setBackground(new Color(108, 117, 125));
        }
    }

    private void toggleMultiBookingMode() {
        isMultiBookingMode = btnMultiBookingToggle.isSelected();

        // Update button appearance
        updateToggleButtonAppearance(isMultiBookingMode);

        if (isMultiBookingMode) {
            lblSelectedRooms.setVisible(true);
            btnConfirmSelection.setVisible(true);
        } else {
            lblSelectedRooms.setVisible(false);
            btnConfirmSelection.setVisible(false);

            // Clear all selections when exiting multi-booking mode
            clearAllSelections();
        }

        // Update all room items with new mode
        updateAllRoomItemsMode();

        revalidate();
        repaint();
    }

    private void clearAllSelections() {
        selectedRooms.clear();
        for (RoomItem roomItem : allRoomItems) {
            roomItem.setSelected(false);
        }
        updateSelectionDisplay();
    }

    private void updateAllRoomItemsMode() {
        for (RoomItem roomItem : allRoomItems) {
            roomItem.setMultiBookingMode(isMultiBookingMode);
            roomItem.setSelectionCallback(this::handleRoomSelectionChanged);
        }
    }

    private void confirmMultiRoomSelection() {
        if (selectedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn ít nhất một phòng!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show confirmation dialog
        int result = JOptionPane.showConfirmDialog(this,
            "Bạn đã chọn " + selectedRooms.size() + " phòng. Tiếp tục đặt phòng?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            String cardName = PanelName.MULTI_BOOKING.getName();
            MultiRoomBookingFormPanel multiBookingPanel = new MultiRoomBookingFormPanel(selectedRooms);

            Main.addCard(multiBookingPanel, cardName);
            Main.showCard(cardName);
        }
    }

    private void createCenterPanel() {
        gridRoomPanels = new GridRoomPanel(allRoomItems);
        gridRoomPanels.setBackground(Color.WHITE);
        gridRoomPanels.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gridRoomPanels,
                                                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setOpaque(true);
        add(scrollPane);
    }

    private void handleStatusFilter(String status) {
        roomFilter.roomStatus = status;
        search();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, int col, String labelText, JComponent component) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = col;
        gbc.weightx = 0.0;
        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        component.setFont(CustomUI.smallFont);
        switch (component) {
            case JTextField jTextField -> {
                component.setPreferredSize(new Dimension(200, 35));
                component.setMinimumSize(new Dimension(180, 35));
            }
            case JSpinner jSpinner -> {
                component.setPreferredSize(new Dimension(200, 30));
                component.setMinimumSize(new Dimension(180, 30));
            }
            case JComboBox jComboBox -> {
                component.setPreferredSize(new Dimension(200, 30));
                component.setMinimumSize(new Dimension(180, 30));
            }
            default -> {
            }
        }
        panel.add(component, gbc);
    }

    private void search() {
        filteredRooms = new ArrayList<>();

        for (RoomItem roomItem : allRoomItems) {
            BookingResponse bookingResponse = roomItem.getBookingResponse();

            // Apply all filters
            if (passesAllFilters(bookingResponse)) {
                filteredRooms.add(roomItem);
            }
        }

        gridRoomPanels.setRoomItems(filteredRooms);
        gridRoomPanels.revalidate();
        gridRoomPanels.repaint();
    }

    // Consolidated filter method - all filtering logic in one place
    private boolean passesAllFilters(BookingResponse bookingResponse) {
        // Room type filter
        if (roomFilter.roomType != null && !roomFilter.roomType.equals("TẤT CẢ")) {
            if (!bookingResponse.getRoomType().equalsIgnoreCase(roomFilter.roomType)) {
                return false;
            }
        }

        // Capacity filter
        if (roomFilter.capacity != null) {
            try {
                int roomCapacity = Integer.parseInt(bookingResponse.getNumberOfCustomers());
                if (roomCapacity < roomFilter.capacity) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // If parsing fails, don't filter out
            }
        }

        // Date filters
        if (roomFilter.checkinDate != null || roomFilter.checkoutDate != null) {
            java.sql.Timestamp roomTimeIn = bookingResponse.getTimeIn();
            java.sql.Timestamp roomTimeOut = bookingResponse.getTimeOut();

            // If room has no booking dates, it's available for any date
            if (roomTimeIn != null && roomTimeOut != null) {
                java.sql.Timestamp filterCheckin = roomFilter.checkinDate != null ?
                        new java.sql.Timestamp(roomFilter.checkinDate.getTime()) : null;
                java.sql.Timestamp filterCheckout = roomFilter.checkoutDate != null ?
                        new java.sql.Timestamp(roomFilter.checkoutDate.getTime()) : null;

                // Check if the requested dates overlap with existing booking
                // Room is available if: requested checkout <= room checkin OR requested checkin >= room checkout
                if (filterCheckin != null && filterCheckout != null) {
                    if (!(filterCheckout.compareTo(roomTimeIn) <= 0 || filterCheckin.compareTo(roomTimeOut) >= 0)) {
                        return false;
                    }
                } else if (filterCheckin != null) {
                    if (filterCheckin.compareTo(roomTimeOut) < 0) {
                        return false;
                    }
                } else if (filterCheckout != null) {
                    if (filterCheckout.compareTo(roomTimeIn) > 0) {
                        return false;
                    }
                }
            }
        }

        // Status filter
        if (roomFilter.roomStatus != null && !roomFilter.roomStatus.equals("Tất cả")) {
            if (!bookingResponse.getRoomStatus().equalsIgnoreCase(roomFilter.roomStatus)) {
                return false;
            }
        }

        return true;
    }

    private void reset() {
        cmbRoomType.setSelectedIndex(0);
        cmbCapacity.setSelectedIndex(0);
        spnCheckInDate.setValue(new java.util.Date());
        spnCheckOutDate.setValue(new java.util.Date());

        // Reset filter state
        roomFilter = new RoomFilter(null, null, null, null, null);

        gridRoomPanels.setRoomItems(allRoomItems);
        gridRoomPanels.revalidate();
        gridRoomPanels.repaint();
    }

    private class RoomFilter {
        String roomType;
        Integer capacity;
        java.util.Date checkinDate;
        java.util.Date checkoutDate;
        String roomStatus;

        public RoomFilter(String roomType, Integer capacity, Date checkinDate, Date checkoutDate,
                          String roomStatus) {
            this.roomType = roomType;
            this.capacity = capacity;
            this.checkinDate = checkinDate;
            this.checkoutDate = checkoutDate;
            this.roomStatus = roomStatus;
        }
    }
}

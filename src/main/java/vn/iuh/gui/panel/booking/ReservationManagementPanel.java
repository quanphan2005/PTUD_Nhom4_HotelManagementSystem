package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.schedule.RoomStatusHandler;
import vn.iuh.service.BookingService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.util.RefreshManager;

import javax.swing.*;
import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReservationManagementPanel extends JPanel {
    public static GridRoomPanel gridRoomPanels;
    private BookingService bookingService;

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

    // Status buttons for refresh functionality
    private JButton[] statusButtons;

    public ReservationManagementPanel() {
        init();
        setupMultiBookingCallbacks();
    }

    public void initData() {
        this.bookingService = new BookingServiceImpl();
        List<BookingResponse> allBookingInfo = bookingService.getAllBookingInfo();

        allRoomItems = new ArrayList<>();
        for (BookingResponse bookingResponse : allBookingInfo) {
            allRoomItems.add(new RoomItem(bookingResponse));
        }

        filteredRooms = new ArrayList<>(allRoomItems);
        roomFilter = new RoomFilter(null, null, null, null, null);

        // Register this panel for refresh events
        RefreshManager.setReservationManagementPanel(this);
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
        initData();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(spnCheckInDate, "dd/MM/yyyy HH:mm");
        spnCheckInDate.setEditor(checkInEditor);

        // Initialize with today's date
        Date today = new Date();
        spnCheckInDate.setValue(today);
        roomFilter.checkInDate = today; // Initialize filter too

        spnCheckInDate.setPreferredSize(new Dimension(200, 35));
        spnCheckInDate.setFont(CustomUI.smallFont);
        spnCheckInDate.addChangeListener(e -> handleCheckinDateChange());

        // Check-out date spinner following BookingFormPanel style
        spnCheckOutDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkOutEditor = new JSpinner.DateEditor(spnCheckOutDate, "dd/MM/yyyy HH:mm");
        spnCheckOutDate.setEditor(checkOutEditor);

        // Initialize with tomorrow's date
        Date tomorrow = Date.from(today.toInstant().plus(1, ChronoUnit.DAYS));
        spnCheckOutDate.setValue(tomorrow);
        roomFilter.checkOutDate = tomorrow; // Initialize filter too

        spnCheckOutDate.setPreferredSize(new Dimension(200, 35));
        spnCheckOutDate.setFont(CustomUI.smallFont);
        spnCheckOutDate.addChangeListener(e -> handleCheckoutDateChange());
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
        JButton btnAll = createStatusButton("Tất cả (" + totalRooms + ")", CustomUI.lightGreen, "Tất cả");
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(btnAll, gbc);

        JButton btnAvailable = createStatusButton(RoomStatus.ROOM_EMPTY_STATUS.getStatus() + " (" + availableCount + ")", CustomUI.lightGreen, RoomStatus.ROOM_EMPTY_STATUS.getStatus());
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(btnAvailable, gbc);

        JButton btnBooked = createStatusButton(RoomStatus.ROOM_BOOKED_STATUS.getStatus() + " (" + bookedCount + ")", CustomUI.cyan, RoomStatus.ROOM_BOOKED_STATUS.getStatus());
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(btnBooked, gbc);

        JButton btnChecking = createStatusButton(RoomStatus.ROOM_CHECKING_STATUS.getStatus() + " (" + checkingCount + ")", CustomUI.lightBlue, RoomStatus.ROOM_CHECKING_STATUS.getStatus());
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(btnChecking, gbc);

        JButton btnUsing = createStatusButton(RoomStatus.ROOM_USING_STATUS.getStatus() + " (" + usingCount + ")", Color.ORANGE, RoomStatus.ROOM_USING_STATUS.getStatus());
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(btnUsing, gbc);

        JButton btnLate = createStatusButton(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus() + " (" + lateCount + ")", Color.RED, RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(btnLate, gbc);

        JButton btnCleaning = createStatusButton(RoomStatus.ROOM_CLEANING_STATUS.getStatus() + " (" + cleaningCount + ")", new Color(144, 238, 144), RoomStatus.ROOM_CLEANING_STATUS.getStatus());
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(btnCleaning, gbc);

        JButton btnMaintenance = createStatusButton(RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus() + " (" + maintenanceCount + ")", Color.LIGHT_GRAY, RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus());
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(btnMaintenance, gbc);

        JButton btnReset = createStatusButton("LÀM MỚI", CustomUI.lightGray, null);
        btnReset.removeActionListener(btnReset.getActionListeners()[0]); // Remove existing listener
        btnReset.addActionListener(e -> refreshPanel());
        gbc.gridx = 2; gbc.gridy = 2;
        panel.add(btnReset, gbc);
        // Store references to status buttons for later updates
        statusButtons = new JButton[] {btnAll, btnAvailable, btnBooked, btnChecking, btnUsing, btnLate, btnCleaning, btnMaintenance, btnReset};

        return panel;
    }

    private int getStatusCount(String status) {
        if (filteredRooms == null) return 0;

        int count = 0;
        for (RoomItem roomItem : filteredRooms) {
            if (roomItem.getBookingResponse().getRoomStatus().equalsIgnoreCase(status)) {
                count++;
            }
        }
        return count;
    }

    private JButton createStatusButton(String text, Color color, String statusValue) {
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
        return statusBtn;
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

        createUpdateRoomStatusSchedule(gridRoomPanels);
    }

    private void handleStatusFilter(String status) {
        roomFilter.roomStatus = status;
        search();
    }

    private void handleCheckinDateChange() {
        roomFilter.checkInDate = (java.util.Date) spnCheckInDate.getValue();
        if (!validateDateRangeWithMinimumHours()) {
            JOptionPane.showMessageDialog(this,
                                          "Ngày check-out phải sau ngày check-in ít nhất 1 giờ!",
                                          "Lỗi ngày tháng",
                                          JOptionPane.ERROR_MESSAGE);

            roomFilter.checkInDate = Date.from(roomFilter.checkOutDate.toInstant().minus(1, ChronoUnit.DAYS));
            spnCheckInDate.setValue(roomFilter.checkInDate);
            return;
        }
        search();
    }

    private void handleCheckoutDateChange() {
        roomFilter.checkOutDate = (java.util.Date) spnCheckOutDate.getValue();
        if (!validateDateRangeWithMinimumHours()) {
            JOptionPane.showMessageDialog(this,
                                          "Ngày check-out phải sau ngày check-in ít nhất 1 giờ!",
                                          "Lỗi ngày tháng",
                                          JOptionPane.ERROR_MESSAGE);

            roomFilter.checkOutDate = Date.from(roomFilter.checkInDate.toInstant().plus(1, ChronoUnit.DAYS));
            spnCheckOutDate.setValue(roomFilter.checkOutDate);
            return;
        }
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

        refreshFilterBtn();
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
        if (roomFilter.checkInDate != null || roomFilter.checkOutDate != null) {
            java.sql.Timestamp roomTimeIn = bookingResponse.getTimeIn();
            java.sql.Timestamp roomTimeOut = bookingResponse.getTimeOut();

            // If room has no booking dates, it's available for any date
            if (roomTimeIn != null && roomTimeOut != null) {
                java.sql.Timestamp filterCheckin = roomFilter.checkInDate != null ?
                        new java.sql.Timestamp(roomFilter.checkInDate.getTime()) : null;
                java.sql.Timestamp filterCheckout = roomFilter.checkOutDate != null ?
                        new java.sql.Timestamp(roomFilter.checkOutDate.getTime()) : null;

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

    private boolean validateDateRangeWithMinimumHours() {
        if (roomFilter.checkInDate == null)
            roomFilter.checkInDate = new Date();
        if (roomFilter.checkOutDate == null)
            roomFilter.checkOutDate = Date.from(roomFilter.checkInDate.toInstant().plus(1, ChronoUnit.DAYS));

        // Allow check-in to be in the past, but ensure check-out is at least 1 hour after check-in
        long diffInMillis = roomFilter.checkOutDate.getTime() - roomFilter.checkInDate.getTime();
        long diffInHours = diffInMillis / (1000 * 60 * 60);

        return diffInHours >= 1;
    }

    public void refreshPanel() {
        // Reset filter form to defaults
        Date today = new Date();
        Date tomorrow = Date.from(today.toInstant().plus(1, ChronoUnit.DAYS));
        cmbRoomType.setSelectedIndex(0);
        cmbCapacity.setSelectedIndex(0);
        spnCheckInDate.setValue(today);
        spnCheckOutDate.setValue(tomorrow);

        // Reset room filter
        roomFilter = new RoomFilter(null, null, today, tomorrow, null);

        // Exit multi-booking mode if active
        if (isMultiBookingMode) {
            btnMultiBookingToggle.setSelected(false);
            toggleMultiBookingMode();
        }

        // Reset RoomItem data
        initData();
        gridRoomPanels.setRoomItems(allRoomItems);

        // Update status button counts
        refreshFilterBtn();
    }

    private void refreshFilterBtn() {
        if (statusButtons == null) return;

        int availableCount = getStatusCount(RoomStatus.ROOM_EMPTY_STATUS.getStatus());
        int bookedCount = getStatusCount(RoomStatus.ROOM_BOOKED_STATUS.getStatus());
        int checkingCount = getStatusCount(RoomStatus.ROOM_CHECKING_STATUS.getStatus());
        int usingCount = getStatusCount(RoomStatus.ROOM_USING_STATUS.getStatus());
        int lateCount = getStatusCount(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
        int cleaningCount = getStatusCount(RoomStatus.ROOM_CLEANING_STATUS.getStatus());
        int maintenanceCount = getStatusCount(RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus());

        statusButtons[1].setText(RoomStatus.ROOM_EMPTY_STATUS.getStatus() + " (" + availableCount + ")");
        statusButtons[2].setText(RoomStatus.ROOM_BOOKED_STATUS.getStatus() + " (" + bookedCount + ")");
        statusButtons[3].setText(RoomStatus.ROOM_CHECKING_STATUS.getStatus() + " (" + checkingCount + ")");
        statusButtons[4].setText(RoomStatus.ROOM_USING_STATUS.getStatus() + " (" + usingCount + ")");
        statusButtons[5].setText(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus() + " (" + lateCount + ")");
        statusButtons[6].setText(RoomStatus.ROOM_CLEANING_STATUS.getStatus() + " (" + cleaningCount + ")");
        statusButtons[7].setText(RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus() + " (" + maintenanceCount + ")");
    }

    // Internal class to hold current filter state
    private static class RoomFilter {
        String roomType;
        Integer capacity;
        Date checkInDate;
        Date checkOutDate;
        String roomStatus;

        public RoomFilter(String roomType, Integer capacity, Date checkInDate, Date checkOutDate, String roomStatus) {
            this.roomType = roomType;
            this.capacity = capacity;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.roomStatus = roomStatus;
        }
    }

    private void createUpdateRoomStatusSchedule(GridRoomPanel gridRoomPanel) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("gridRoomPanel", gridRoomPanel);

            JobDetail jobDetail = JobBuilder.newJob(RoomStatusHandler.class)
                    .withIdentity("roomStatusUpdateJob", "group1")
                    .usingJobData(jobDataMap)
                    .build();

            Trigger trigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("roomStatusUpdateTrigger", "group1")
                    .withSchedule(org.quartz.SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(30)
                            .repeatForever())
                    .build();

            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            System.out.println("Error creating schedule: " + e.getMessage());
        }
    }
}

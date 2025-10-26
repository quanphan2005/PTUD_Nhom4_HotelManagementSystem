package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import org.quartz.*;
import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.schedule.RoomStatusHandler;
import vn.iuh.service.BookingService;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.util.RefreshManager;
import vn.iuh.util.SchedulerUtil;
import vn.iuh.util.TimeFilterHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BookingManagementPanel extends JPanel {
    public static GridRoomPanel gridRoomPanels;
    private BookingService bookingService;
    private LoaiPhongService loaiPhongService;

    private List<RoomItem> allRoomItems;
    private List<RoomItem> filteredRooms;
    private Map<String, RoomItem> emptyRoomMap;
    private List<RoomCategoryResponse> roomCategories;

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

    private final String ALL_STATUS = "TẤT CẢ";

    public BookingManagementPanel() {
        init();
        setupMultiBookingCallbacks();
    }

    public void initData() {
        this.bookingService = new BookingServiceImpl();
        this.loaiPhongService = new LoaiPhongServiceImpl();
        List<BookingResponse> allBookingInfo = bookingService.getAllBookingInfo();
        emptyRoomMap = new HashMap<>();
        roomCategories = loaiPhongService.getAllRoomCategories();

        allRoomItems = new ArrayList<>();
        for (BookingResponse bookingResponse : allBookingInfo) {
            allRoomItems.add(new RoomItem(bookingResponse));
            emptyRoomMap.put(bookingResponse.getRoomId(), new RoomItem(bookingResponse, true));
        }

        filteredRooms = new ArrayList<>(allRoomItems);
        roomFilter = new RoomFilter(null, null, null, null, null);

        // Register this panel for refresh events
        RefreshManager.setBookingManagementPanel(this);
    }

    private void setupMultiBookingCallbacks() {
        // Set selection callback for all room items
        for (RoomItem roomItem : filteredRooms) {
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
        JLabel lblTop = new JLabel("QUẢN LÝ ĐẶT PHÒNG", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);

        pnlTop.setPreferredSize(new Dimension(0, 40));
        pnlTop.setMinimumSize(new Dimension(0, 40));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
        cmbRoomType.addItem(ALL_STATUS);

        // map this to set of types
        Set<String> types = roomCategories.stream().map(RoomCategoryResponse::getPhanLoai).collect(Collectors.toSet());
        for (String type : types) {
            cmbRoomType.addItem(type);
        }

        cmbRoomType.setPreferredSize(new Dimension(200, 35));
        cmbRoomType.setFont(CustomUI.smallFont);

        cmbRoomType.setSelectedIndex(0); // Default to ALL_STATUS
        cmbRoomType.addActionListener(e -> handleCmbRoomTypeChangeEvent());

        // Capacity dropdown
        cmbCapacity = new JComboBox<>();
        Set<Integer> capacities = roomCategories.stream().map(RoomCategoryResponse::getSoLuongKhach).collect(Collectors.toSet());
        for (Integer capacity : capacities) {
            cmbCapacity.addItem(capacity);
        }

        cmbCapacity.setPreferredSize(new Dimension(200, 35));
        cmbCapacity.setFont(CustomUI.smallFont);

        cmbCapacity.setSelectedIndex(0); // Default to 1
        cmbCapacity.addActionListener(e -> handleCmbRoomCapacityChangeEvent());

        // Check-in date spinner following BookingFormPanel style
        spnCheckInDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(spnCheckInDate, "dd/MM/yyyy HH:mm");
        spnCheckInDate.setEditor(checkInEditor);

        // Initialize with today's date
        Date today = new Date();
        spnCheckInDate.setValue(today);

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

        spnCheckOutDate.setPreferredSize(new Dimension(200, 35));
        spnCheckOutDate.setFont(CustomUI.smallFont);
        spnCheckOutDate.addChangeListener(e -> handleCheckoutDateChange());
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.green, 2),
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
        JButton btnAll = createStatusButton(ALL_STATUS + " (" + totalRooms + ")", CustomUI.green, ALL_STATUS);
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(btnAll, gbc);

        JButton btnAvailable = createStatusButton(RoomStatus.ROOM_EMPTY_STATUS.getStatus() + " (" + availableCount + ")", CustomUI.green, RoomStatus.ROOM_EMPTY_STATUS.getStatus());
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(btnAvailable, gbc);

        JButton btnBooked = createStatusButton(RoomStatus.ROOM_BOOKED_STATUS.getStatus() + " (" + bookedCount + ")", CustomUI.cyan, RoomStatus.ROOM_BOOKED_STATUS.getStatus());
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(btnBooked, gbc);

        JButton btnChecking = createStatusButton(RoomStatus.ROOM_CHECKING_STATUS.getStatus() + " (" + checkingCount + ")", CustomUI.lightBlue, RoomStatus.ROOM_CHECKING_STATUS.getStatus());
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(btnChecking, gbc);

        JButton btnUsing = createStatusButton(RoomStatus.ROOM_USING_STATUS.getStatus() + " (" + usingCount + ")", CustomUI.orange, RoomStatus.ROOM_USING_STATUS.getStatus());
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(btnUsing, gbc);

        JButton btnLate = createStatusButton(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus() + " (" + lateCount + ")", CustomUI.red, RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(btnLate, gbc);

        JButton btnCleaning = createStatusButton(RoomStatus.ROOM_CLEANING_STATUS.getStatus() + " (" + cleaningCount + ")", CustomUI.lightGreen, RoomStatus.ROOM_CLEANING_STATUS.getStatus());
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

    // HandleUpdateStatusCount
    private void updateStatusCount() {
        int availableCount = 0;
        int bookedCount = 0;
        int checkingCount = 0;
        int usingCount = 0;
        int lateCount = 0;
        int cleaningCount = 0;
        int maintenanceCount = 0;

        if (roomFilter.checkInDate != null && roomFilter.checkOutDate != null) {
            availableCount = filteredRooms.size();
        } else {
            for (RoomItem roomItem : filteredRooms) {
                // Switch case base on room status enums RoomStatus
                switch (RoomStatus.fromStatus(roomItem.getRoomStatus())) {
                    case ROOM_EMPTY_STATUS -> availableCount++;
                    case ROOM_BOOKED_STATUS -> bookedCount++;
                    case ROOM_CHECKING_STATUS -> checkingCount++;
                    case ROOM_USING_STATUS -> usingCount++;
                    case ROOM_CHECKOUT_LATE_STATUS -> lateCount++;
                    case ROOM_CLEANING_STATUS -> cleaningCount++;
                    case ROOM_MAINTENANCE_STATUS -> maintenanceCount++;
                    case null -> {
                        // Unknown status, do nothing
                    }
                }
            }
        }

        statusButtons[1].setText(RoomStatus.ROOM_EMPTY_STATUS.getStatus() + " (" + availableCount + ")");
        statusButtons[2].setText(RoomStatus.ROOM_BOOKED_STATUS.getStatus() + " (" + bookedCount + ")");
        statusButtons[3].setText(RoomStatus.ROOM_CHECKING_STATUS.getStatus() + " (" + checkingCount + ")");
        statusButtons[4].setText(RoomStatus.ROOM_USING_STATUS.getStatus() + " (" + usingCount + ")");
        statusButtons[5].setText(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus() + " (" + lateCount + ")");
        statusButtons[6].setText(RoomStatus.ROOM_CLEANING_STATUS.getStatus() + " (" + cleaningCount + ")");
        statusButtons[7].setText(RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus() + " (" + maintenanceCount + ")");
    }

    private int getStatusCount(String status) {
        if (filteredRooms == null) return 0;

        int count = 0;
        for (RoomItem roomItem : allRoomItems) {
            if (roomItem.getRoomStatus().equalsIgnoreCase(status)) {
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

        // Add hover effect
        btnMultiBookingToggle.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btnMultiBookingToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnMultiBookingToggle.setBackground(btnMultiBookingToggle.isSelected() ? CustomUI.darkRed : CustomUI.blue);
            }

            public void mouseExited(MouseEvent evt) {
                btnMultiBookingToggle.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                updateToggleButtonAppearance(btnMultiBookingToggle.isSelected());
            }
        });

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
        // Search all empty room when entering multi-booking mode
        boolean select = btnMultiBookingToggle.isSelected();
        if (select) {
            setDefaultValueForEmptyCondition();
            search();
        }

        isMultiBookingMode = select;
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
        updateMultibookingMode();

        revalidate();
        repaint();
    }

    private void setDefaultValueForEmptyCondition() {
        Date checkin = spnCheckInDate.getValue() != null ? (Date) spnCheckInDate.getValue() : new Date();
        Date checkout = spnCheckOutDate.getValue() != null ? (Date) spnCheckOutDate.getValue() : Date.from(checkin.toInstant().plus(1, ChronoUnit.DAYS));
        roomFilter.checkOutDate = checkout;
        roomFilter.checkInDate = checkin;
        roomFilter.roomType = roomFilter.roomType == null ? ALL_STATUS : roomFilter.roomType;
        roomFilter.capacity = roomFilter.capacity == null ? 1 : roomFilter.capacity;
        roomFilter.roomStatus = roomFilter.roomStatus == null ? RoomStatus.ROOM_EMPTY_STATUS.getStatus() : roomFilter.roomStatus;
    }

    private void clearAllSelections() {
        selectedRooms.clear();
        for (RoomItem roomItem : filteredRooms) {
            roomItem.setSelected(false);
        }
        updateSelectionDisplay();
    }

    private void updateMultibookingMode() {
        for (RoomItem roomItem : filteredRooms) {
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
        if (status == null || status.equals(ALL_STATUS)) {
            roomFilter.roomStatus = null;
        } else {
            roomFilter.roomStatus = status;
        }

        search();
    }

    private void handleCmbRoomTypeChangeEvent() {
        roomFilter.roomType = (String) cmbRoomType.getSelectedItem();
        search();
        refreshFilterBtn();
    }

    private void handleCmbRoomCapacityChangeEvent() {
        roomFilter.capacity = (Integer) cmbCapacity.getSelectedItem();
        search();
        refreshFilterBtn();
    }

    private void handleCheckinDateChange() {
        Date now = new Date();
        Date checkInDate = (Date) spnCheckInDate.getValue();
        Date currentCheckOutDate = (Date) spnCheckOutDate.getValue();

        // Handle past check-in date
        if (checkInDate.before(Date.from(now.toInstant().minus(1, ChronoUnit.MINUTES)))) {
            JOptionPane.showMessageDialog(this,
                                          "Ngày check-in không được trước ngày hiện tại!",
                                          "Lỗi ngày tháng",
                                          JOptionPane.ERROR_MESSAGE);
            spnCheckInDate.setValue(now);
            spnCheckOutDate.setValue(Date.from(now.toInstant().plus(1, ChronoUnit.DAYS)));
            roomFilter.checkInDate = now;
            roomFilter.checkOutDate = Date.from(now.toInstant().plus(1, ChronoUnit.DAYS));
            search();
            refreshFilterBtn();
            return;
        }

        // Auto increase check-out date if it's not after check-in or less than 1 hour after check-in
        if (!currentCheckOutDate.after(checkInDate) ||
            (currentCheckOutDate.getTime() - checkInDate.getTime()) < (60 * 60 * 1000)) {
            // Set checkout to be 1 day after checkin
            Date newCheckOutDate = Date.from(checkInDate.toInstant().plus(1, ChronoUnit.DAYS));
            spnCheckOutDate.setValue(newCheckOutDate);
            roomFilter.checkOutDate = newCheckOutDate;
        }

        roomFilter.checkInDate = checkInDate;
        if (roomFilter.checkOutDate == null) {
            roomFilter.checkOutDate = (Date) spnCheckOutDate.getValue();
        }
        search();
        refreshFilterBtn();

        // Update global time filters
        TimeFilterHelper.setCheckinTime(checkInDate);
        TimeFilterHelper.setCheckoutTime(roomFilter.checkOutDate);
    }

    private void handleCheckoutDateChange() {
        Date checkInDate = (Date) spnCheckInDate.getValue();
        Date checkOutDate = (Date) spnCheckOutDate.getValue();

        // Auto increase check-out date if it's not after check-in or less than 1 hour after check-in
        if (!checkOutDate.after(checkInDate) ||
            (checkOutDate.getTime() - checkInDate.getTime()) < (60 * 60 * 1000)) {

            JOptionPane.showMessageDialog(this,
                                          "Ngày check-out phải sau ngày check-in ít nhất 1 giờ!",
                                          "Lỗi ngày tháng",
                                          JOptionPane.ERROR_MESSAGE);

            // Set checkout to be 1 day after checkin
            Date now = new Date();
            if (checkInDate.before(Date.from(now.toInstant().minus(1, ChronoUnit.MINUTES)))) {
                // If check-in is in the past, reset both to now and tomorrow
                spnCheckInDate.setValue(now);
                roomFilter.checkInDate = now;
            }
            Date newCheckOutDate = Date.from(roomFilter.checkInDate.toInstant().plus(1, ChronoUnit.DAYS));
            spnCheckOutDate.setValue(newCheckOutDate);
            roomFilter.checkOutDate = newCheckOutDate;
            search();
            refreshFilterBtn();
            return;
        }

        roomFilter.checkOutDate = checkOutDate;
        search();
        refreshFilterBtn();

        // Update global time filters
        TimeFilterHelper.setCheckinTime(roomFilter.checkInDate);
        TimeFilterHelper.setCheckoutTime(checkOutDate);
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
        // Toggle off multi-booking mode if active
        boolean wasMultiBookingMode = isMultiBookingMode;
        if (isMultiBookingMode) {
            // Reset by set off / on
            btnMultiBookingToggle.setSelected(false);
            toggleMultiBookingMode();
        }

        filteredRooms = new ArrayList<>();
        // Search all empty room if both dates are set
        if (roomFilter.checkInDate != null && roomFilter.checkOutDate != null) {
            List<String> nonEmptyRoomIds = bookingService.getAllNonEmptyRoomInRange(
                    new Timestamp(roomFilter.checkInDate.getTime()),
                    new Timestamp(roomFilter.checkOutDate.getTime())
            );
            System.out.println("Non-empty rooms in range: " + nonEmptyRoomIds.size());

            // Loop through all empty rooms (sorted) and apply filters
            for (String roomId : emptyRoomMap.keySet().stream().sorted().toList()) {
                RoomItem roomItem = emptyRoomMap.get(roomId);
                if (!nonEmptyRoomIds.contains(roomId) && passesAllFilters(roomItem.getBookingResponse())) {
                    filteredRooms.add(roomItem);
                }
            }
        } else {
            // If dates are not set, filter from all current rooms
            for (RoomItem roomItem : allRoomItems) {
                if (passesAllFilters(roomItem.getBookingResponse())) {
                    filteredRooms.add(roomItem);
                }
            }
        }

        if (wasMultiBookingMode) {
            // Re-enable multi-booking mode if it was active
            btnMultiBookingToggle.setSelected(true);
            toggleMultiBookingMode();
        }

        // Update grid panel with filtered results
        gridRoomPanels.setRoomItems(filteredRooms);
        gridRoomPanels.revalidate();
        gridRoomPanels.repaint();
    }

    // Consolidated filter method - all filtering logic in one place
    private boolean passesAllFilters(BookingResponse bookingResponse) {
        if (bookingResponse == null) return false;

        // Room type filter
        if (roomFilter.roomType != null && !roomFilter.roomType.equals(ALL_STATUS)) {
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

        // Status filter
        if (roomFilter.roomStatus != null && !roomFilter.roomStatus.equals(ALL_STATUS)) {
            if (!bookingResponse.getRoomStatus().equalsIgnoreCase(roomFilter.roomStatus)) {
                return false;
            }
        }

        return true;
    }

    public void refreshPanel() {
        // Reset filter form to defaults
        Date today = new Date();
        Date pivot = Date.from(today.toInstant().plus(99999999, ChronoUnit.DAYS)); // Far future pivot
        Date tomorrow = Date.from(today.toInstant().plus(1, ChronoUnit.DAYS));

        // remove existing listeners to avoid triggering during reset
        cmbRoomType.removeActionListener(cmbRoomType.getActionListeners()[0]);
        cmbCapacity.removeActionListener(cmbCapacity.getActionListeners()[0]);
        spnCheckInDate.removeChangeListener(spnCheckInDate.getChangeListeners()[0]);
        spnCheckOutDate.removeChangeListener(spnCheckOutDate.getChangeListeners()[0]);

        cmbRoomType.setSelectedIndex(0);
        cmbCapacity.setSelectedIndex(0);
        spnCheckOutDate.setValue(pivot);
        spnCheckInDate.setValue(today);
        spnCheckOutDate.setValue(tomorrow);

        // Reset TimeFilterHelper
        TimeFilterHelper.setCheckinTime(null);
        TimeFilterHelper.setCheckoutTime(null);

        // Re-attach listeners
        cmbRoomType.addActionListener(e -> handleCmbRoomTypeChangeEvent());
        cmbCapacity.addActionListener(e -> handleCmbRoomCapacityChangeEvent());
        spnCheckInDate.addChangeListener(e -> handleCheckinDateChange());
        spnCheckOutDate.addChangeListener(e -> handleCheckoutDateChange());

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
        updateStatusCount();
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
            Scheduler scheduler = SchedulerUtil.getInstance();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("gridRoomPanel", gridRoomPanel);

            JobDetail jobDetail = JobBuilder.newJob(RoomStatusHandler.class)
                                            .withIdentity("roomStatusUpdateJob", "group1")
                                            .usingJobData(jobDataMap)
                                            .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                                                       .withIdentity("roomStatusUpdateTrigger", "group1")
                                                       .withSchedule(SimpleScheduleBuilder.simpleSchedule()
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

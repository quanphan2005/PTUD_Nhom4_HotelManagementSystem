package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.service.BookingService;
import vn.iuh.service.CheckOutService;
import vn.iuh.service.impl.CheckOutServiceImpl;
import vn.iuh.util.IconUtil;
import vn.iuh.util.RefreshManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

import static vn.iuh.constraint.PanelName.SERVICE_ORDER;

public class RoomUsageFormPanel extends JPanel {
    private BookingResponse selectedRoom;
    private BookingService bookingService;
    private CheckOutService checkOutService;
    // Formatters
    private DecimalFormat priceFormatter = new DecimalFormat("#,###");

    // Room Information Components
    private JLabel lblRoomNumber;
    private JLabel lblRoomType;
    private JLabel lblRoomCapacity;
    private JLabel lblHourlyPrice;
    private JLabel lblDailyPrice;
    private JLabel lblRoomStatus;

    // Customer Information Components
    private JTextField txtCustomerName;
    private JTextField txtPhoneNumber;
    private JTextField txtCCCD;

    // Booking Information Components
    private JSpinner spnCheckInDate;
    private JSpinner spnCheckOutDate;
    private JSpinner spnCreateAt;
    private JTextArea txtNote;
    private JTextField txtInitialPrice;
    private JTextField txtTotalServicePrice;
    private JTextField txtDepositPrice;
    private JButton btnCreateReservationForm;

    // Service Components - simplified to use dialog
    private List<DonGoiDichVu> serviceOrdered = new ArrayList<>();

    // Action Buttons
    private JButton btnCancel;
    private JButton btnCalculatePrice;

    // Main content components
    private JPanel mainContentPanel;

    // Dropdown panel states
    private boolean isRoomInfoCollapsed = false;
    private boolean isCustomerInfoCollapsed = false;
    private boolean isBookingInfoCollapsed = false;
    private boolean isActionMenuCollapsed = false;

    // Dropdown content panels
    private JPanel roomInfoContent;
    private JPanel customerInfoContent;
    private JPanel bookingInfoContent;
    private JPanel actionMenuContent;

    // Close button
    private JButton closeButton;

    public RoomUsageFormPanel(BookingResponse roomInfo) {
        this.selectedRoom = roomInfo;
        this.bookingService = new vn.iuh.service.impl.BookingServiceImpl();
        this.checkOutService = new CheckOutServiceImpl();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateRoomInformation();
        setDefaultValues();
    }

    private void initializeComponents() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));


        ServiceSelectionPanel servicePanel =
                new ServiceSelectionPanel(PanelName.ROOM_USING.getName(), 1, selectedRoom.getMaChiTietDatPhong(), (services) -> {
                    serviceOrdered.clear();
                    serviceOrdered.addAll(services);
                    updateTotalServicePrice(); // Update service price when services are selected
                });
        // Initialize service selection


        Main.addCard(servicePanel, SERVICE_ORDER.getName());

        // Room Information Labels
        lblRoomNumber = new JLabel();
        lblRoomType = new JLabel();
        lblRoomCapacity = new JLabel();
        lblHourlyPrice = new JLabel();
        lblDailyPrice = new JLabel();
        lblRoomStatus = new JLabel();

        // Customer Information Fields - increased width
        txtCustomerName = new JTextField(12);
        txtPhoneNumber = new JTextField(12);
        txtCCCD = new JTextField(12);

        // Booking Information Fields
        spnCheckInDate = new JSpinner(new SpinnerDateModel());
        spnCheckOutDate = new JSpinner(new SpinnerDateModel());
        spnCreateAt = new JSpinner(new SpinnerDateModel());

        txtInitialPrice = new JTextField(15);
        txtTotalServicePrice = new JTextField(15);
        txtDepositPrice = new JTextField(15);

        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(spnCheckInDate, "dd/MM/yyyy HH:mm");
        JSpinner.DateEditor checkOutEditor = new JSpinner.DateEditor(spnCheckOutDate, "dd/MM/yyyy HH:mm");
        JSpinner.DateEditor createAtEditor = new JSpinner.DateEditor(spnCreateAt, "dd/MM/yyyy HH:mm");
        spnCheckInDate.setEditor(checkInEditor);
        spnCheckOutDate.setEditor(checkOutEditor);
        spnCheckOutDate.setValue(Date.from(((Date) spnCheckInDate.getValue()).toInstant().plus(1, ChronoUnit.DAYS)));
        spnCreateAt.setEditor(createAtEditor);

        txtNote = new JTextArea(4, 25);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        btnCreateReservationForm = new JButton("Tạo đơn đặt phòng mới");

        // Buttons
        btnCancel = new JButton("Hủy");
        btnCalculatePrice = new JButton("Tính giá");

        // Style buttons
        styleButton(btnCancel, CustomUI.red);
        styleButton(btnCalculatePrice, CustomUI.lightBlue);
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(CustomUI.normalFont);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
    }

    private void setupLayout() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 50));
        headerPanel.putClientProperty(FlatClientProperties.STYLE, " arc: 20");
        headerPanel.setBackground(CustomUI.blue);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);

        // Title with room name
        JLabel titleLabel = new JLabel("THÔNG TIN PHÒNG " + selectedRoom.getRoomName(), SwingConstants.CENTER);
        titleLabel.setFont(CustomUI.veryBigFont);
        titleLabel.setForeground(CustomUI.white);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));

        // Check-in and Check-out icons
        ImageIcon checkinIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/checkin.png")));
        checkinIcon = new ImageIcon(checkinIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        JButton checkInButton = new JButton(checkinIcon);

        ImageIcon checkoutIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/leaving.png")));
        checkoutIcon = new ImageIcon(checkoutIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
        JButton checkoutButton = new JButton(checkoutIcon);

        titlePanel.add(checkInButton);
        titlePanel.add(titleLabel);
        titlePanel.add(checkoutButton);

        closeButton = new JButton("x");
        closeButton.setFont(CustomUI.veryBigFont);
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(60, 20));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> Main.showCard("Quản lý đặt phòng"));
        closeButton.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(closeButton, BorderLayout.EAST);

        // Create main content panel with overlay capability for service panel
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new OverlayLayout(mainContentPanel));
        mainContentPanel.setBackground(Color.white);

        // Base content panel
        JPanel basePanel = createBaseContentPanel();

        mainContentPanel.add(basePanel); // Base content underneath

        // Create scroll pane for the entire main content
        JScrollPane mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setBorder(null);

        // Set scroll speed
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(40);
        mainScrollPane.getViewport().setBackground(Color.WHITE);

        // Add to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createBaseContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();

        // LEFT COLUMN - Row 0: Booking info panel (WHITE background) - SWAPPED TO TOP
        JPanel bookingPanel = createBookingInfoPanel();
        bookingPanel.setBackground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.weighty = 0.6; // More height for booking info
        gbc.insets = new Insets(0, 0, 5, 5);
        contentPanel.add(bookingPanel, gbc);

        // Right Column - Row 0: Action menu panel (WHITE background)
        JPanel actionMenu = createActionMenuPanel();
        bookingPanel.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4;
        gbc.weighty = 0.6; // More height for booking info
        gbc.insets = new Insets(0, 5, 5, 5);
        contentPanel.add(actionMenu, gbc);

        // LEFT COLUMN - Row 1: Room info panel (WHITE background)
        JPanel rightRoomPanel = createRoomInfoPanel();
        rightRoomPanel.setBackground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1; // Reset to single row
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.weighty = 0.4; // Less height for customer info
        gbc.insets = new Insets(5, 0, 10, 5);
        contentPanel.add(rightRoomPanel, gbc);

        // RIGHT COLUMN - Row 1: Customer info panel (WHITE background)
        JPanel customerPanel = createCustomerInfoPanel();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(5, 5, 10, 5);
        contentPanel.add(customerPanel, gbc);

        return contentPanel;
    }

    private JPanel createCustomerInfoPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon customerIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/customer.png")));
        customerIcon = new ImageIcon(customerIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        // Create collapsible header
        JPanel headerPanel = createCollapsibleHeader(customerIcon, "THÔNG TIN KHÁCH HÀNG",
                                                     new Color(70, 130, 180), CustomUI.white, () -> {
                    isCustomerInfoCollapsed = !isCustomerInfoCollapsed;
                    togglePanelVisibility(customerInfoContent, isCustomerInfoCollapsed);
                });

        // Create content panel
        customerInfoContent = new JPanel(new GridBagLayout());
        customerInfoContent.setBackground(Color.WHITE);
        customerInfoContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        txtCustomerName.setEditable(false);
        txtPhoneNumber.setEditable(false);
        txtCCCD.setEditable(false);

        addFormRow(customerInfoContent, gbc, 0, "Tên khách hàng:", txtCustomerName);
        addFormRow(customerInfoContent, gbc, 1, "Số điện thoại:", txtPhoneNumber);
        addFormRow(customerInfoContent, gbc, 2, "CCCD/CMND:", txtCCCD);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(customerInfoContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createActionMenuPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon menuIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/action.png")));
        menuIcon = new ImageIcon(menuIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        // Create collapsible header
        JPanel headerPanel = createCollapsibleHeader(menuIcon, "BẢNG THAO TÁC",
                                                     new Color(70, 130, 180), CustomUI.white, () -> {
                    isActionMenuCollapsed = !isActionMenuCollapsed;
                    togglePanelVisibility(actionMenuContent, isActionMenuCollapsed);
                });

        // Create content panel - flexible grid based on number of actions
        actionMenuContent = new JPanel();
        actionMenuContent.setBackground(Color.WHITE);
        actionMenuContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Populate action items based on room status
        populateActionItems();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(actionMenuContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createBookingInfoPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon bookingIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/booking.png")));
        bookingIcon = new ImageIcon(bookingIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        // Create collapsible header
        JPanel headerPanel = createCollapsibleHeader(bookingIcon, "THÔNG TIN ĐẶT PHÒNG",
                                                     CustomUI.darkGreen, Color.WHITE, () -> {
                    isBookingInfoCollapsed = !isBookingInfoCollapsed;
                    togglePanelVisibility(bookingInfoContent, isBookingInfoCollapsed);
                });

        // Create content panel
        bookingInfoContent = new JPanel(new GridBagLayout());
        bookingInfoContent.setBackground(Color.WHITE);
        bookingInfoContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.darkGreen, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

//         Remove arrow buttons from check-in and check-out date spinners
        ((JSpinner.DefaultEditor) spnCheckInDate.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor) spnCheckOutDate.getEditor()).getTextField().setEditable(false);
        Component[] checkInComponents = spnCheckInDate.getComponents();
        for (Component comp : checkInComponents) {
            if (comp instanceof JButton) {
                comp.setVisible(false);
            }
        }
        Component[] checkOutComponents = spnCheckOutDate.getComponents();
        for (Component comp : checkOutComponents) {
            if (comp instanceof JButton) {
                comp.setVisible(false);
            }
        }
        txtInitialPrice.setEditable(false);
        txtTotalServicePrice.setEditable(false);
        txtDepositPrice.setEditable(false);

        // Single column layout as requested
        addFormRow(bookingInfoContent, gbc, 0, "Ngày nhận phòng:", spnCheckInDate);
        addFormRow(bookingInfoContent, gbc, 1, "Ngày trả phòng:", spnCheckOutDate);
        addFormRow(bookingInfoContent, gbc, 2, "Giá ban đầu:", txtInitialPrice);
        addFormRow(bookingInfoContent, gbc, 3, "Tổng tiền dịch vụ:", txtTotalServicePrice);
        addFormRow(bookingInfoContent, gbc, 4, "Tiền đặt cọc:", txtDepositPrice);

        // Advanced booking checkbox
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        btnCreateReservationForm.setFont(CustomUI.smallFont);
        btnCreateReservationForm.setForeground(CustomUI.white);
        btnCreateReservationForm.setBackground(CustomUI.purple);

        // Add calendar icon to reservation button
        ImageIcon calendar = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/calendar.png")));
        calendar = new ImageIcon(calendar.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        btnCreateReservationForm.setIcon(calendar);

        bookingInfoContent.add(btnCreateReservationForm, gbc);

        // Note area
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JLabel lblNote = new JLabel("Ghi chú:");
        lblNote.setFont(CustomUI.smallFont);
        bookingInfoContent.add(lblNote, gbc);

        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea txtNote = new JTextArea();
        txtNote.setPreferredSize(new Dimension(300, 100));
        txtNote.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        txtNote.setEditable(false);
        bookingInfoContent.add(txtNote, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(bookingInfoContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createRoomInfoPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon roomIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/room.png")));
        roomIcon = new ImageIcon(roomIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        // Create collapsible header
        JPanel headerPanel = createCollapsibleHeader(roomIcon, "CHI TIẾT PHÒNG",
                                                     CustomUI.orange, Color.WHITE, () -> {
                    isRoomInfoCollapsed = !isRoomInfoCollapsed;
                    togglePanelVisibility(roomInfoContent, isRoomInfoCollapsed);
                });

        // Create content panel with proper overflow handling
        roomInfoContent = new JPanel(new GridBagLayout());
        roomInfoContent.setBackground(Color.WHITE);
        roomInfoContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.orange, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Essential room information using addFormRow for consistency
        addFormRow(roomInfoContent, gbc, 0, "Số phòng:", lblRoomNumber);
        addFormRow(roomInfoContent, gbc, 1, "Loại phòng:", lblRoomType);
        addFormRow(roomInfoContent, gbc, 2, "Sức chứa:", lblRoomCapacity);
        addFormRow(roomInfoContent, gbc, 3, "Trạng thái:", lblRoomStatus);
        addFormRow(roomInfoContent, gbc, 4, "Giá theo giờ:", lblHourlyPrice);
        addFormRow(roomInfoContent, gbc, 5, "Giá theo ngày:", lblDailyPrice);

        // Additional info section with separator - flexible content based on room status
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 8, 10);
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        roomInfoContent.add(separator, gbc);

        // Reset for additional fields
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridwidth = 1;

        // Create additional info labels - these will be flexible based on room status
        JLabel lblRoomId = new JLabel(selectedRoom.getRoomId());
        JLabel lblAmenities = new JLabel(getAmenitiesForRoom());
        JLabel lblArea = new JLabel(getAreaForRoom());
        JLabel lblFloor = new JLabel("Tầng " + extractFloorFromRoomName());

        // Add flexible additional information using addFormRow
        addFormRow(roomInfoContent, gbc, 7, "Mã phòng:", lblRoomId);
        addFormRow(roomInfoContent, gbc, 8, "Tiện nghi:", lblAmenities);
        addFormRow(roomInfoContent, gbc, 9, "Diện tích:", lblArea);
        addFormRow(roomInfoContent, gbc, 10, "Tầng:", lblFloor);

        // Add status-specific information if needed
        addStatusSpecificInfo(roomInfoContent, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(roomInfoContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JButton createActionButton(ActionItem item) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(item.getBackgroundColor());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon label - now using ImageIcon instead of emoji string
        JLabel iconLabel = new JLabel(item.getIcon(), SwingConstants.CENTER);
        iconLabel.setOpaque(false);

        // Text label
        JLabel textLabel = new JLabel(item.getText(), SwingConstants.CENTER);
        textLabel.setFont(CustomUI.normalFont);
        textLabel.setForeground(Color.WHITE);
        textLabel.setOpaque(false);

        button.add(iconLabel, BorderLayout.CENTER);
        button.add(textLabel, BorderLayout.SOUTH);

        // Add hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(item.getBackgroundColor().brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(item.getBackgroundColor());
            }
        });

        // Add click action
        button.addActionListener(e -> {
            if (item.getAction() != null)
                item.getAction().run();
        });

        return button;
    }

    private void populateActionItems() {
        actionMenuContent.removeAll(); // Clear existing items

        String roomStatus = selectedRoom.getRoomStatus();
        List<ActionItem> actionItems = getActionItemsForStatus(roomStatus);

        // Set grid layout based on number of items
        int itemCount = actionItems.size();
        int cols = Math.min(itemCount, 2); // Max 2 columns
        int rows = Math.max((int) Math.ceil((double) itemCount / 2), 2);
        actionMenuContent.setLayout(new GridLayout(rows, cols, 10, 10));

        // Create and add action buttons
        for (ActionItem item : actionItems) {
            JButton actionButton = createActionButton(item);
            actionMenuContent.add(actionButton);
        }

        // Refresh the panel
        actionMenuContent.revalidate();
        actionMenuContent.repaint();
    }

    // Get action items based on room status
    private List<ActionItem> getActionItemsForStatus(String roomStatus) {
        List<ActionItem> items = new ArrayList<>();

        ActionItem callServiceItem = new ActionItem("Gọi Dịch Vụ", IconUtil.createServiceIcon(), CustomUI.bluePurple,
                                                    this::handleCallService);
        ActionItem checkOutItem =
                new ActionItem("Trả Phòng", IconUtil.createCheckOutIcon(), CustomUI.bluePurple, this::handleCheckOut);
        ActionItem transferRoomItem = new ActionItem("Chuyển Phòng", IconUtil.createTransferIcon(), CustomUI.bluePurple,
                                                     this::handleTransferRoom);
        ActionItem extendBookingItem = new ActionItem("Book Thêm Giờ", IconUtil.createExtendIcon(), CustomUI.bluePurple,
                                                      this::handleExtendBooking);
        ActionItem cancelReservationItem = new ActionItem("Hủy Đặt Phòng", IconUtil.createCancelIcon(), CustomUI.red,
                                                          this::handleCancelReservation);
        ActionItem checkInItem =
                new ActionItem("Nhận Phòng", IconUtil.createCheckInIcon(), CustomUI.bluePurple, this::handleCheckIn);

        ActionItem completeItem = new ActionItem("Hoàn Tất Dọn Phòng", IconUtil.createCompleteIcon(), CustomUI.green,
                                                     this::handleCompleteCleaning);

        if (roomStatus.equals(RoomStatus.ROOM_BOOKED_STATUS.getStatus())) {
            items.add(callServiceItem);
            items.add(cancelReservationItem);
            items.add(transferRoomItem);
            items.add(extendBookingItem);
        } else
            if (roomStatus.equals(RoomStatus.ROOM_CHECKING_STATUS.getStatus())
                || roomStatus.equals(RoomStatus.ROOM_USING_STATUS.getStatus())) {
                items.add(callServiceItem);
                items.add(checkOutItem);
                items.add(transferRoomItem);
                items.add(extendBookingItem);
            } else
                if (roomStatus.equals(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus())) {
                    callServiceItem.setBackgroundColor(CustomUI.gray);
                    callServiceItem.setAction(null);
                    transferRoomItem.setBackgroundColor(CustomUI.gray);
                    transferRoomItem.setAction(null);
                    extendBookingItem.setBackgroundColor(CustomUI.gray);
                    extendBookingItem.setAction(null);

                    items.add(callServiceItem);
                    items.add(checkOutItem);
                    items.add(transferRoomItem);
                    items.add(extendBookingItem);

                    // Disable some event when status is late

                } else
                    if (roomStatus.equals(RoomStatus.ROOM_CLEANING_STATUS.getStatus())) {
                        callServiceItem.setBackgroundColor(CustomUI.gray);
                        callServiceItem.setAction(null);
                        transferRoomItem.setBackgroundColor(CustomUI.gray);
                        transferRoomItem.setAction(null);
                        extendBookingItem.setBackgroundColor(CustomUI.gray);
                        extendBookingItem.setAction(null);

                        items.add(callServiceItem);
                        items.add(completeItem);
                        items.add(transferRoomItem);
                        items.add(extendBookingItem);
                    }

        return items;
    }

    // Helper class add row to form panels
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        component.setFont(CustomUI.smallFont);
        if (component instanceof JTextField) {
            component.setPreferredSize(new Dimension(300, 35));
            component.setMinimumSize(new Dimension(250, 35));
        } else
            if (component instanceof JSpinner) {
                component.setPreferredSize(new Dimension(300, 35));
                component.setMinimumSize(new Dimension(250, 35));
            }
        panel.add(component, gbc);
    }

    // Create a collapsible header panel
    private JPanel createCollapsibleHeader(ImageIcon icon, String title, Color backgroundColor, Color textColor,
                                           Runnable toggleAction) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor.darker(), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));


        // Title label with text-based icon for better compatibility
        JLabel titleLabel = new JLabel(title, icon, SwingConstants.LEFT);
        titleLabel.setFont(CustomUI.normalFont);
        titleLabel.setForeground(textColor);

        // Dropdown arrow button
        JButton dropdownButton = new JButton("▼");
        dropdownButton.setFont(new Font("Arial", Font.BOLD, 12));
        dropdownButton.setForeground(textColor);
        dropdownButton.setBackground(backgroundColor);
        dropdownButton.setBorder(BorderFactory.createEmptyBorder());
        dropdownButton.setFocusPainted(false);
        dropdownButton.setPreferredSize(new Dimension(30, 25));

        // Add hover effect
        dropdownButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                dropdownButton.setBackground(backgroundColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                dropdownButton.setBackground(backgroundColor);
            }
        });

        // Toggle action with specific panel tracking
        ActionListener toggleListener = e -> {
            toggleAction.run();
            // Update arrow direction based on the specific panel's collapse state
            if (title.contains("KHÁCH HÀNG")) {
                dropdownButton.setText(isCustomerInfoCollapsed ? "►" : "▼");
            } else
                if (title.contains("ĐẶT PHÒNG")) {
                    dropdownButton.setText(isBookingInfoCollapsed ? "►" : "▼");
                } else
                    if (title.contains("THÔNG TIN PHÒNG")) {
                        dropdownButton.setText(isRoomInfoCollapsed ? "►" : "▼");
                    } else
                        if (title.contains("BẢNG THAO TÁC")) {
                            dropdownButton.setText(isActionMenuCollapsed ? "►" : "▼");
                        }
        };

        dropdownButton.addActionListener(toggleListener);

        // Make the entire header clickable
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleListener.actionPerformed(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                headerPanel.setBackground(backgroundColor.brighter());
                dropdownButton.setBackground(backgroundColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                headerPanel.setBackground(backgroundColor);
                dropdownButton.setBackground(backgroundColor);
            }
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(dropdownButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void togglePanelVisibility(JPanel contentPanel, boolean isCollapsed) {
        if (contentPanel != null) {
            contentPanel.setVisible(!isCollapsed);
            // Animate the collapse/expand
            SwingUtilities.invokeLater(() -> {
                mainContentPanel.revalidate();
                mainContentPanel.repaint();
            });
        }
    }

    // Helper methods for additional room info
    private String extractFloorFromRoomName() {
        String roomName = selectedRoom.getRoomName();
        // Assuming room name format is like "101", "202A", etc.
        if (roomName.length() >= 3 && Character.isDigit(roomName.charAt(0))) {
            return String.valueOf(roomName.charAt(0));
        }
        return "N/A";
    }

    private String getAmenitiesForRoom() {
        // This can be made flexible based on room type or category
        String roomType = selectedRoom.getRoomType();
        if (roomType.toLowerCase().contains("vip") || roomType.toLowerCase().contains("deluxe")) {
            return "Điều hòa, TV, WiFi, Minibar, Jacuzzi";
        } else
            if (roomType.toLowerCase().contains("standard")) {
                return "Điều hòa, TV, WiFi";
            } else {
                return "Điều hòa, TV, WiFi, Tủ lạnh";
            }
    }

    private String getAreaForRoom() {
        // This can be made flexible based on room type or actual room data
        String roomType = selectedRoom.getRoomType();
        if (roomType.toLowerCase().contains("vip") || roomType.toLowerCase().contains("deluxe")) {
            return "45m²";
        } else
            if (roomType.toLowerCase().contains("standard")) {
                return "25m²";
            } else {
                return "30m²";
            }
    }

    private String getCurrentTimeString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy");
        return sdf.format(new Date());
    }

    // Method to add status-specific information dynamically
    private void addStatusSpecificInfo(JPanel panel, GridBagConstraints gbc) {
        String roomStatus = selectedRoom.getRoomStatus();
        int currentRow = 11;

        if ("OCCUPIED".equalsIgnoreCase(roomStatus)) {
            // Add check-in time, expected checkout, etc.
            JLabel lblCheckInTime = new JLabel("14:00 - 25/09/2024");
            JLabel lblExpectedCheckout = new JLabel("12:00 - 27/09/2024");

            addFormRow(panel, gbc, currentRow++, "Giờ nhận phòng:", lblCheckInTime);
            addFormRow(panel, gbc, currentRow++, "Dự kiến trả:", lblExpectedCheckout);

        } else
            if ("MAINTENANCE".equalsIgnoreCase(roomStatus)) {
                // Add maintenance information
                JLabel lblMaintenanceType = new JLabel("Bảo trì định kỳ");
                JLabel lblEstimatedCompletion = new JLabel("28/09/2024");

                addFormRow(panel, gbc, currentRow++, "Loại bảo trì:", lblMaintenanceType);
                addFormRow(panel, gbc, currentRow++, "Dự kiến xong:", lblEstimatedCompletion);

            } else
                if ("RESERVED".equalsIgnoreCase(roomStatus)) {
                    // Add reservation information
                    JLabel lblReservationTime = new JLabel("15:00 - 27/09/2024");
                    JLabel lblCustomerName = new JLabel("Đã đặt trước");

                    addFormRow(panel, gbc, currentRow++, "Giờ nhận phòng:", lblReservationTime);
                    addFormRow(panel, gbc, currentRow++, "Trạng thái:", lblCustomerName);

                } else
                    if ("CLEANING".equalsIgnoreCase(roomStatus)) {
                        // Add cleaning information
                        JLabel lblCleaningStatus = new JLabel("Đang dọn dẹp");
                        JLabel lblEstimatedReady = new JLabel("30 phút");

                        addFormRow(panel, gbc, currentRow++, "Trạng thái:", lblCleaningStatus);
                        addFormRow(panel, gbc, currentRow++, "Còn lại:", lblEstimatedReady);
                    }

        // Add last updated time for all statuses
        JLabel lblLastUpdated = new JLabel(getCurrentTimeString());
        addFormRow(panel, gbc, currentRow, "Cập nhật lúc:", lblLastUpdated);
    }

    // Populate room information
    private void populateRoomInformation() {
        lblRoomNumber.setText(selectedRoom.getRoomName());
        lblRoomType.setText(selectedRoom.getRoomType());
        lblRoomCapacity.setText(String.valueOf(selectedRoom.getNumberOfCustomers()));
        lblHourlyPrice.setText(String.format("%.0f VNĐ", selectedRoom.getHourlyPrice()));
        lblDailyPrice.setText(String.format("%.0f VNĐ", selectedRoom.getDailyPrice()));
        lblRoomStatus.setText(selectedRoom.getRoomStatus());
    }

    private void setDefaultValues() {
        // Set initial price based on daily rate

        spnCheckOutDate.setValue(selectedRoom.getTimeOut());
        spnCheckInDate.setValue(selectedRoom.getTimeIn());

        txtInitialPrice.setText(priceFormatter.format(selectedRoom.getDailyPrice()) + " VNĐ");
        txtTotalServicePrice.setText(priceFormatter.format(0) + " VNĐ");

        txtCustomerName.setText(selectedRoom.getCustomerName());
        txtPhoneNumber.setText("TEST");
        txtCCCD.setText("TEST");
    }

    // Method to update total service price from ServiceSelectionPanel
    private void updateTotalServicePrice() {
        double totalServicePrice = 0.0;
        for (DonGoiDichVu service : serviceOrdered) {
            if (!service.isDuocTang()) { // Only count non-gift services
                totalServicePrice += service.getGiaThoiDiemDo() * service.getSoLuong();
            }
        }
        txtTotalServicePrice.setText(priceFormatter.format(totalServicePrice) + " VNĐ");
    }

    // Setup event handlers for buttons
    private void setupEventHandlers() {
        btnCancel.addActionListener(e -> handleCancel());
        closeButton.addActionListener(e -> Main.showCard(PanelName.RESERVATION_MANAGEMENT.getName()));
        btnCreateReservationForm.addActionListener(e -> handleCreateReservationForm());
    }

    // Handler methods for action buttons
    private void handleCallService() {
        Main.showCard(SERVICE_ORDER.getName());
    }

    private void handleCheckOut() {
        System.out.println("Check out");
        int result = JOptionPane.showConfirmDialog(null,
                "Xác nhận trả phòng " + selectedRoom.getRoomName() + "?",
                "Trả phòng", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            System.out.println("Trả phòng...");
            boolean success = checkOutService.checkOutByReservationDetail(selectedRoom.getMaChiTietDatPhong());

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Đã hoàn thành trả phòng " + selectedRoom.getRoomName(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                RefreshManager.refreshAfterBooking();
                Main.showCard(PanelName.RESERVATION_MANAGEMENT.getName());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Trả phòng thất bại cho " + selectedRoom.getRoomName(),
                        "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleTransferRoom() {
        String newRoom = JOptionPane.showInputDialog(this,
                                                     "Nhập số phòng muốn chuyển đến:",
                                                     "Chuyển phòng", JOptionPane.QUESTION_MESSAGE);

        if (newRoom != null && !newRoom.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                                          "Đã chuyển khách từ phòng " + selectedRoom.getRoomName() + " đến phòng " +
                                          newRoom,
                                          "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleExtendBooking() {
        String[] options = {"1 giờ", "2 giờ", "3 giờ", "Tùy chỉnh"};
        String choice = (String) JOptionPane.showInputDialog(this,
                                                             "Chọn thời gian gia hạn:",
                                                             "Book thêm giờ",
                                                             JOptionPane.QUESTION_MESSAGE,
                                                             null, options, options[0]);

        if (choice != null) {
            JOptionPane.showMessageDialog(this,
                                          "Đã gia hạn phòng " + selectedRoom.getRoomName() + " thêm " + choice,
                                          "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleCheckIn() {
        int result = JOptionPane.showConfirmDialog(this,
                                                   "Xác nhận nhận phòng " + selectedRoom.getRoomName() + "?",
                                                   "Nhận phòng", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                                          "Khách đã nhận phòng " + selectedRoom.getRoomName(),
                                          "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleCancelReservation() {
        int result = JOptionPane.showConfirmDialog(this,
                                                   "Xác nhận hủy đặt phòng " + selectedRoom.getRoomName() + "?",
                                                   "Hủy đặt phòng", JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                                          "Đã hủy đặt phòng " + selectedRoom.getRoomName(),
                                          "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleCompleteCleaning() {
        int result = JOptionPane.showConfirmDialog(this,
                                                   "Xác nhận hoàn tất dọn phòng " + selectedRoom.getRoomName() + "?",
                                                   "Hoàn tất dọn phòng", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                                          "Đã hoàn tất dọn phòng " + selectedRoom.getRoomName(),
                                          "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleCreateReservationForm() {
        BookingResponse bookingResponse = new BookingResponse(
                selectedRoom.getRoomId(),
                selectedRoom.getRoomName(),
                selectedRoom.isActive(),
                RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
                selectedRoom.getRoomType(),
                selectedRoom.getNumberOfCustomers(),
                selectedRoom.getDailyPrice(),
                selectedRoom.getHourlyPrice(),
                selectedRoom.getCustomerName(),
                null,
                selectedRoom.getTimeIn(),
                selectedRoom.getTimeOut()
        );

        BookingFormPanel bookingFormPanel = new BookingFormPanel(bookingResponse, PanelName.ROOM_USING.getName());
        String panelName = PanelName.BOOKING.getName();
        Main.addCard(bookingFormPanel, panelName);
        Main.showCard(panelName);
    }

    private void handleCancel() {
        Main.showCard(PanelName.RESERVATION_MANAGEMENT.getName());
    }
}

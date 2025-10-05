package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.service.BookingService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.util.IconUtil;
import vn.iuh.util.RefreshManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static vn.iuh.constraint.PanelName.SERVICE_ORDER;

public class MultiRoomBookingFormPanel extends JPanel {
    private List<BookingResponse> selectedRooms;
    private BookingService bookingService;

    // Customer Information Components
    private JTextField txtCustomerName;
    private JTextField txtPhoneNumber;
    private JTextField txtCCCD;

    // Booking Information Components
    private JSpinner spnCheckInDate;
    private JSpinner spnCheckOutDate;
    private JSpinner spnCreateAt;
    private JTextArea txtNote;
    private JTextField txtTotalInitialPrice;
    private JTextField txtTotalServicePrice;
    private JTextField txtDepositPrice;
    private JCheckBox chkIsAdvanced;
    private JButton reservationButton;

    // Service Components
    private List<DonGoiDichVu> serviceOrdered = new ArrayList<>();

    // Room list table
    private JTable roomListTable;
    private DefaultTableModel roomListTableModel;

    // Main content components
    private JPanel mainContentPanel;

    // Dropdown panel states
    private boolean isRoomListCollapsed = false;
    private boolean isCustomerInfoCollapsed = false;
    private boolean isBookingInfoCollapsed = false;
    private boolean isActionMenuCollapsed = false;

    // Dropdown content panels
    private JPanel roomListContent;
    private JPanel customerInfoContent;
    private JPanel bookingInfoContent;
    private JPanel actionMenuContent;

    // Close button
    private JButton closeButton;

    // Formatters
    private DecimalFormat priceFormatter = new DecimalFormat("#,###");

    public MultiRoomBookingFormPanel(List<BookingResponse> selectedRooms) {
        this.selectedRooms = selectedRooms;
        this.bookingService = new BookingServiceImpl();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateRoomList();
        setDefaultValues();
    }

    private void initializeComponents() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Initialize service selection
        ServiceSelectionPanel servicePanel = new ServiceSelectionPanel(selectedRooms.size(), (services) -> {
            serviceOrdered.clear();
            serviceOrdered.addAll(services);
            updateTotalServicePrice(); // Update service price when services are selected
        });
        Main.addCard(servicePanel, SERVICE_ORDER.getName());

        // Customer Information Fields
        txtCustomerName = new JTextField(12);
        txtPhoneNumber = new JTextField(12);
        txtCCCD = new JTextField(12);

        // Booking Information Fields
        spnCheckInDate = new JSpinner(new SpinnerDateModel());
        spnCheckOutDate = new JSpinner(new SpinnerDateModel());
        spnCreateAt = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(spnCheckInDate, "dd/MM/yyyy HH:mm");
        JSpinner.DateEditor checkOutEditor = new JSpinner.DateEditor(spnCheckOutDate, "dd/MM/yyyy HH:mm");
        JSpinner.DateEditor createAtEditor = new JSpinner.DateEditor(spnCreateAt, "dd/MM/yyyy HH:mm");
        spnCheckInDate.setEditor(checkInEditor);
        spnCheckOutDate.setEditor(checkOutEditor);
        spnCheckOutDate.setValue(Date.from(((Date) spnCheckInDate.getValue()).toInstant().plus(1, ChronoUnit.DAYS)));
        spnCheckInDate.addChangeListener(e -> handleCheckinDateChange());
        spnCheckOutDate.addChangeListener(e -> handleCheckoutDateChange());
        spnCreateAt.setEditor(createAtEditor);

        txtNote = new JTextArea(4, 25);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        txtTotalInitialPrice = new JTextField(15);
        txtTotalServicePrice = new JTextField(15);
        txtDepositPrice = new JTextField(15);
        chkIsAdvanced = new JCheckBox("Đặt phòng trước");
        reservationButton = new JButton(" Xem lịch đặt phòng");

        // Room list table
        String[] roomColumns = {"Phòng", "Loại", "Giá/ngày", "Sức chứa"};
        roomListTableModel = new DefaultTableModel(roomColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomListTable = new JTable(roomListTableModel);
        roomListTable.setFont(CustomUI.smallFont);
        roomListTable.setRowHeight(35);
        roomListTable.getTableHeader().setFont(CustomUI.smallFont);
        roomListTable.getTableHeader().setBackground(Color.LIGHT_GRAY);

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

        // Title for multi-room booking
        JLabel titleLabel = new JLabel("Đặt nhiều phòng (" + selectedRooms.size() + " phòng)", SwingConstants.CENTER);
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

        // Create main content panel
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new OverlayLayout(mainContentPanel));
        mainContentPanel.setBackground(Color.white);

        // Base content panel
        JPanel basePanel = createBaseContentPanel();
        mainContentPanel.add(basePanel);

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

        // LEFT COLUMN - Row 0: Booking info panel
        JPanel bookingPanel = createBookingInfoPanel();
        bookingPanel.setBackground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 0.6;
        gbc.insets = new Insets(0, 0, 5, 5);
        contentPanel.add(bookingPanel, gbc);

        // Right Column - Row 0: Action menu panel
        JPanel actionMenu = createActionMenuPanel();
        actionMenu.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4; gbc.weighty = 0.6;
        gbc.insets = new Insets(0, 5, 5, 5);
        contentPanel.add(actionMenu, gbc);

        // LEFT COLUMN - Row 1: Selected rooms panel (replaces room info panel)
        JPanel roomListPanel = createRoomListPanel();
        roomListPanel.setBackground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 0.4;
        gbc.insets = new Insets(5, 0, 10, 5);
        contentPanel.add(roomListPanel, gbc);

        // RIGHT COLUMN - Row 1: Customer info panel
        JPanel customerPanel = createCustomerInfoPanel();
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4; gbc.weighty = 0.4;
        gbc.insets = new Insets(5, 5, 10, 5);
        contentPanel.add(customerPanel, gbc);

        return contentPanel;
    }

    private JPanel createRoomListPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon roomIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/room.png")));
        roomIcon = new ImageIcon(roomIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        // Create collapsible header
        JPanel headerPanel = createCollapsibleHeader(roomIcon, "DANH SÁCH PHÒNG ĐÃ CHỌN",
                                                     CustomUI.orange, Color.WHITE, () -> {
                    isRoomListCollapsed = !isRoomListCollapsed;
                    togglePanelVisibility(roomListContent, isRoomListCollapsed);
                });

        // Create content panel with room list table
        roomListContent = new JPanel(new BorderLayout());
        roomListContent.setBackground(Color.WHITE);
        roomListContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.orange, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Add summary info at top
        JPanel summaryPanel = createRoomSummaryPanel();
        roomListContent.add(summaryPanel, BorderLayout.NORTH);

        // Add table with selected rooms
        JScrollPane tableScrollPane = new JScrollPane(roomListTable);
        tableScrollPane.setPreferredSize(new Dimension(350, 200));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Chi tiết phòng"));
        roomListContent.add(tableScrollPane, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(roomListContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createRoomSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Total rooms
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblTotalRoomsTitle = new JLabel("Tổng số phòng:");
        lblTotalRoomsTitle.setFont(CustomUI.smallFont);
        summaryPanel.add(lblTotalRoomsTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel lblTotalRooms = new JLabel(String.valueOf(selectedRooms.size()));
        lblTotalRooms.setFont(CustomUI.smallFont);
        lblTotalRooms.setForeground(CustomUI.blue);
        summaryPanel.add(lblTotalRooms, gbc);

        // Total capacity
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel lblTotalCapacityTitle = new JLabel("Tổng sức chứa:");
        lblTotalCapacityTitle.setFont(CustomUI.smallFont);
        summaryPanel.add(lblTotalCapacityTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        int totalCapacity = selectedRooms.stream()
                .mapToInt(room -> Integer.parseInt(room.getNumberOfCustomers()))
                .sum();
        JLabel lblTotalCapacity = new JLabel(totalCapacity + " người");
        lblTotalCapacity.setFont(CustomUI.smallFont);
        lblTotalCapacity.setForeground(CustomUI.blue);
        summaryPanel.add(lblTotalCapacity, gbc);

        // Estimated total price
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel lblEstimatedPriceTitle = new JLabel("Tổng giá dự tính:");
        lblEstimatedPriceTitle.setFont(CustomUI.smallFont);
        summaryPanel.add(lblEstimatedPriceTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        double totalEstimatedPrice = selectedRooms.stream()
                .mapToDouble(BookingResponse::getDailyPrice)
                .sum();
        JLabel lblEstimatedPrice = new JLabel(priceFormatter.format(totalEstimatedPrice) + " VNĐ");
        lblEstimatedPrice.setFont(CustomUI.smallFont);
        lblEstimatedPrice.setForeground(CustomUI.orange);
        summaryPanel.add(lblEstimatedPrice, gbc);

        return summaryPanel;
    }

    private void populateRoomList() {
        roomListTableModel.setRowCount(0);

        for (BookingResponse room : selectedRooms) {
            Object[] row = {
                room.getRoomName(),
                room.getRoomType(),
                priceFormatter.format(room.getDailyPrice()) + " VNĐ",
                room.getNumberOfCustomers(),
            };
            roomListTableModel.addRow(row);
        }
    }

    // Reuse the same methods from BookingFormPanel for customer info, booking info, and action menu
    private JPanel createCustomerInfoPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon customerIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/customer.png")));
        customerIcon = new ImageIcon(customerIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        JPanel headerPanel = createCollapsibleHeader(customerIcon, "THÔNG TIN KHÁCH HÀNG",
            new Color(70, 130, 180), CustomUI.white, () -> {
                isCustomerInfoCollapsed = !isCustomerInfoCollapsed;
                togglePanelVisibility(customerInfoContent, isCustomerInfoCollapsed);
            });

        customerInfoContent = new JPanel(new GridBagLayout());
        customerInfoContent.setBackground(Color.WHITE);
        customerInfoContent.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(customerInfoContent, gbc, 0, "Tên khách hàng:", txtCustomerName);
        addFormRow(customerInfoContent, gbc, 1, "Số điện thoại:", txtPhoneNumber);
        addFormRow(customerInfoContent, gbc, 2, "CCCD/CMND:", txtCCCD);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(customerInfoContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createBookingInfoPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon bookingIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/booking.png")));
        bookingIcon = new ImageIcon(bookingIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        JPanel headerPanel = createCollapsibleHeader(bookingIcon, "THÔNG TIN ĐẶT PHÒNG",
                                                     CustomUI.darkGreen, Color.WHITE, () -> {
                    isBookingInfoCollapsed = !isBookingInfoCollapsed;
                    togglePanelVisibility(bookingInfoContent, isBookingInfoCollapsed);
                });

        bookingInfoContent = new JPanel(new GridBagLayout());
        bookingInfoContent.setBackground(Color.WHITE);
        bookingInfoContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.darkGreen, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(bookingInfoContent, gbc, 0, "Ngày nhận phòng:", spnCheckInDate);
        addFormRow(bookingInfoContent, gbc, 1, "Ngày trả phòng:", spnCheckOutDate);
        addFormRow(bookingInfoContent, gbc, 2, "Tổng giá ban đầu:", txtTotalInitialPrice);
        addFormRow(bookingInfoContent, gbc, 3, "Tổng giá dịch vụ:", txtTotalServicePrice);
        addFormRow(bookingInfoContent, gbc, 4, "Tiền đặt cọc:", txtDepositPrice);

        txtTotalInitialPrice.setEditable(false);
        txtDepositPrice.setEditable(false);
        txtTotalServicePrice.setEditable(false);

        // Advanced booking checkbox
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 1;
        chkIsAdvanced.setFont(CustomUI.smallFont);
        chkIsAdvanced.setBackground(Color.WHITE);
        bookingInfoContent.add(chkIsAdvanced, gbc);

        // Reservation button
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        reservationButton.setFont(CustomUI.smallFont);
        reservationButton.setForeground(CustomUI.white);
        reservationButton.setBackground(CustomUI.purple);
        reservationButton.setEnabled(false);

        ImageIcon calendar = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/calendar.png")));
        calendar = new ImageIcon(calendar.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        reservationButton.setIcon(calendar);

        bookingInfoContent.add(reservationButton, gbc);

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
        txtNote.setPreferredSize(new Dimension(300, 100));
        txtNote.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        bookingInfoContent.add(txtNote, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(bookingInfoContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createActionMenuPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        ImageIcon menuIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/action.png")));
        menuIcon = new ImageIcon(menuIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        JPanel headerPanel = createCollapsibleHeader(menuIcon, "BẢNG THAO TÁC",
                                                     new Color(70, 130, 180), CustomUI.white, () -> {
                    isActionMenuCollapsed = !isActionMenuCollapsed;
                    togglePanelVisibility(actionMenuContent, isActionMenuCollapsed);
                });

        actionMenuContent = new JPanel(new GridLayout(2, 1, 10, 10));
        actionMenuContent.setBackground(Color.WHITE);
        actionMenuContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Create action buttons for multi-room booking
        JButton callServiceButton = createActionButton("Gọi Dịch Vụ", IconUtil.createServiceIcon(), CustomUI.bluePurple, this::handleCallService);
        JButton confirmBookingButton = createActionButton("Xác Nhận Đặt", IconUtil.createBookingIcon(), CustomUI.darkGreen, this::handleConfirmBooking);

        actionMenuContent.add(callServiceButton);
        actionMenuContent.add(confirmBookingButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(actionMenuContent, BorderLayout.CENTER);

        return mainPanel;
    }

    // Helper methods (similar to BookingFormPanel)
    private JButton createActionButton(String text, ImageIcon icon, Color backgroundColor, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setOpaque(false);

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(CustomUI.normalFont);
        textLabel.setForeground(Color.WHITE);
        textLabel.setOpaque(false);

        button.add(iconLabel, BorderLayout.CENTER);
        button.add(textLabel, BorderLayout.SOUTH);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        button.addActionListener(e -> action.run());

        return button;
    }

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
        } else if (component instanceof JSpinner) {
            component.setPreferredSize(new Dimension(300, 35));
            component.setMinimumSize(new Dimension(250, 35));
        }
        panel.add(component, gbc);
    }

    private JPanel createCollapsibleHeader(ImageIcon icon, String title, Color backgroundColor, Color textColor, Runnable toggleAction) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JLabel titleLabel = new JLabel(title, icon, SwingConstants.LEFT);
        titleLabel.setFont(CustomUI.normalFont);
        titleLabel.setForeground(textColor);

        JButton dropdownButton = new JButton("▼");
        dropdownButton.setFont(new Font("Arial", Font.BOLD, 12));
        dropdownButton.setForeground(textColor);
        dropdownButton.setBackground(backgroundColor);
        dropdownButton.setBorder(BorderFactory.createEmptyBorder());
        dropdownButton.setFocusPainted(false);
        dropdownButton.setPreferredSize(new Dimension(30, 25));

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

        dropdownButton.addActionListener(e -> {
            toggleAction.run();
            if (title.contains("KHÁCH HÀNG")) {
                dropdownButton.setText(isCustomerInfoCollapsed ? "►" : "▼");
            } else if (title.contains("ĐẶT PHÒNG")) {
                dropdownButton.setText(isBookingInfoCollapsed ? "►" : "▼");
            } else if (title.contains("PHÒNG ĐÃ CHỌN")) {
                dropdownButton.setText(isRoomListCollapsed ? "►" : "▼");
            } else if (title.contains("BẢNG THAO TÁC")) {
                dropdownButton.setText(isActionMenuCollapsed ? "►" : "▼");
            }
        });

        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dropdownButton.doClick();
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
            SwingUtilities.invokeLater(() -> {
                mainContentPanel.revalidate();
                mainContentPanel.repaint();
            });
        }
    }

    private void setDefaultValues() {
        // Calculate and set total initial price
        double totalPrice = selectedRooms.stream()
                                         .mapToDouble(BookingResponse::getDailyPrice)
                                         .sum();
        txtTotalInitialPrice.setText(priceFormatter.format(totalPrice) + " VNĐ");
        txtTotalServicePrice.setText(priceFormatter.format(0) + " VNĐ");
        calculateTotalInitialPrice();

        // Set default check-in date to today
        java.util.Date today = new java.util.Date();
        spnCheckInDate.setValue(today);

        // Set default check-out date to tomorrow
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(today);
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        spnCheckOutDate.setValue(cal.getTime());
    }

    private void setupEventHandlers() {
        closeButton.addActionListener(e -> Main.showCard("Quản lý đặt phòng"));
        chkIsAdvanced.addActionListener(e -> handleCalculateDeposit());
    }

    private void handleCalculateDeposit() {
        boolean isSelected = chkIsAdvanced.isSelected();
        txtDepositPrice.setEnabled(isSelected);
        if (!isSelected) {
            txtDepositPrice.setText("0");
        }
        calculateDepositPrice();
    }


    // Action handlers
    private void handleCallService() {
        Main.showCard(SERVICE_ORDER.getName());
    }

    private void updateTotalServicePrice() {
        double totalServicePrice = 0.0;
        for (DonGoiDichVu service : serviceOrdered) {
            if (!service.isDuocTang()) { // Only count non-gift services
                totalServicePrice += service.getGiaThoiDiemDo() * service.getSoLuong();
            }
        }
        txtTotalServicePrice.setText(priceFormatter.format(totalServicePrice) + " VNĐ");
        calculateDepositPrice(); // Recalculate deposit when service price changes
    }

    private void calculateDepositPrice() {
        try {
            // Parse prices by removing formatting
            String initialPriceText = txtTotalInitialPrice.getText().replace(" VNĐ", "").replace(",", "");
            String servicePriceText = txtTotalServicePrice.getText().replace(" VNĐ", "").replace(",", "");

            double initialPrice = Double.parseDouble(initialPriceText);
            double servicePrice = Double.parseDouble(servicePriceText);
            double totalPrice = initialPrice + servicePrice;

            if (chkIsAdvanced.isSelected()) {
                // If advanced booking, deposit is 30% of total price
                double depositPrice = totalPrice * 0.3;
                txtDepositPrice.setText(priceFormatter.format(depositPrice) + " VNĐ");
            } else {
                // If not advanced booking, no deposit required
                txtDepositPrice.setText(priceFormatter.format(0) + " VNĐ");
            }
        } catch (NumberFormatException e) {
            txtDepositPrice.setText(priceFormatter.format(0) + " VNĐ");
        }
    }

    private void handleConfirmBooking() {
        // Implement multi-room booking logic here
        try {
            if (!validateInput()) {
                return;
            }

            // Create booking events for each room
            List<String> roomIds = selectedRooms.stream()
                    .map(BookingResponse::getRoomId)
                    .toList();

            BookingCreationEvent bookingEvent = createMultiRoomBookingEvent(roomIds);
            boolean success = bookingService.createBooking(bookingEvent);

            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Đặt " + selectedRooms.size() + " phòng thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

                RefreshManager.refreshAfterBooking();
                handleCancel();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Đặt phòng thất bại! Vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("Error during multi-room booking: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Lỗi khi đặt phòng: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancel() {
        Main.showCard("Quản lý đặt phòng");
    }

    private void handleCheckinDateChange() {
        if (!validateDateRangeWithMinimumHours()) {
            JOptionPane.showMessageDialog(this,
                                          "Ngày check-out phải sau ngày check-in ít nhất 1 giờ!",
                                          "Lỗi ngày tháng",
                                          JOptionPane.ERROR_MESSAGE);

            Date checkOutDate = (Date) spnCheckOutDate.getValue();
            spnCheckInDate.setValue(Date.from(checkOutDate.toInstant().minus(1, ChronoUnit.DAYS)));
            return;
        }
        
        calculateTotalInitialPrice();
    }

    private void handleCheckoutDateChange() {
        if (!validateDateRangeWithMinimumHours()) {
            JOptionPane.showMessageDialog(this,
                                          "Ngày check-out phải sau ngày check-in ít nhất 1 giờ!",
                                          "Lỗi ngày tháng",
                                          JOptionPane.ERROR_MESSAGE);

            Date checkInDate = (Date) spnCheckInDate.getValue();
            spnCheckOutDate.setValue(Date.from(checkInDate.toInstant().plus(1, ChronoUnit.DAYS)));
            return;
        }

        calculateTotalInitialPrice();
    }

    private boolean validateDateRangeWithMinimumHours() {
        Date checkIn = (Date) spnCheckInDate.getValue();
        Date checkOut = (Date) spnCheckOutDate.getValue();

        // Allow check-in to be in the past, but ensure check-out is at least 1 hour after check-in
        long diffInMillis = checkOut.getTime() - checkIn.getTime();
        long diffInHours = diffInMillis / (1000 * 60 * 60);

        return diffInHours >= 1;
    }

    private boolean validateInput() {
        if (txtCustomerName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtCustomerName.requestFocus();
            return false;
        }

        if (txtPhoneNumber.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtPhoneNumber.requestFocus();
            return false;
        }

        if (txtCCCD.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD/CMND!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }

        try {
            Double.parseDouble(txtTotalInitialPrice.getText().replace(" VNĐ", "").replace(",", ""));
            Double.parseDouble(txtDepositPrice.getText().replace(" VNĐ", "").replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá phòng và tiền đặt cọc phải là số!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Date checkIn = (Date) spnCheckInDate.getValue();
        Date checkOut = (Date) spnCheckOutDate.getValue();

        if (checkOut.before(checkIn)) {
            JOptionPane.showMessageDialog(this, "Ngày trả phòng phải sau ngày nhận phòng!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void calculateTotalInitialPrice() {
        Date checkIn = (Date) spnCheckInDate.getValue();
        Date checkOut = (Date) spnCheckOutDate.getValue();

        long diffInMillies = checkOut.getTime() - checkIn.getTime();
        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillies);
        long days = diffInHours / 24;
        long hours = diffInHours % 24;

        double totalDailyPrice = selectedRooms.stream()
                .mapToDouble(BookingResponse::getDailyPrice)
                .sum() * days;

        double totalHourlyPrice = selectedRooms.stream()
                .mapToDouble(BookingResponse::getDailyPrice)
                .sum() * hours;
        txtTotalInitialPrice.setText(priceFormatter.format(totalDailyPrice + totalHourlyPrice) + " VNĐ");

        calculateDepositPrice();
    }

    private BookingCreationEvent createMultiRoomBookingEvent(List<String> roomIds) {
        String tenKhachHang = txtCustomerName.getText().trim();
        String soDienThoai = txtPhoneNumber.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String moTa = txtNote.getText().trim();
        Timestamp ngayNhanPhong = new Timestamp(((Date) spnCheckInDate.getValue()).getTime());
        Timestamp ngayTraPhong = new Timestamp(((Date) spnCheckOutDate.getValue()).getTime());
        Timestamp thoiGianTao = new Timestamp(System.currentTimeMillis());
        double tongTienDuTinh = Double.parseDouble(txtTotalInitialPrice.getText().replace(" VNĐ", "").replace(",", ""));
        double tienDatCoc = Double.parseDouble(txtDepositPrice.getText().replace(" VNĐ", "").replace(",", ""));
        boolean daDatTruoc = chkIsAdvanced.isSelected();

        String maPhienDangNhap = "PN00000002"; // TODO - get actual shift assignment ID

        return new BookingCreationEvent(tenKhachHang, soDienThoai, cccd, moTa,
                                        ngayNhanPhong, ngayTraPhong, tongTienDuTinh, tienDatCoc, daDatTruoc,
                                        roomIds, serviceOrdered, maPhienDangNhap, thoiGianTao);
    }
}

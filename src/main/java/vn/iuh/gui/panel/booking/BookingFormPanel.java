package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.Fee;
import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.ResponseType;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.DepositInvoiceResponse;
import vn.iuh.dto.response.EventResponse;
import vn.iuh.entity.KhachHang;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.dialog.DepositInvoiceDialog;
import vn.iuh.service.BookingService;
import vn.iuh.service.CustomerService;
import vn.iuh.service.RoomService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.service.impl.CustomerServiceImpl;
import vn.iuh.service.impl.RoomServiceImpl;
import vn.iuh.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

import static vn.iuh.constraint.PanelName.SERVICE_ORDER;

public class BookingFormPanel extends JPanel {
    private String parentName;

    private BookingResponse selectedRoom;
    private BookingService bookingService;
    private CustomerService customerService;
    private RoomService roomService;

    // Formatters
    private DecimalFormat priceFormatter = PriceFormat.getPriceFormatter();

    // Room Information Components
    private JLabel lblRoomNumber;
    private JLabel lblRoomType;
    private JLabel lblRoomCapacity;
    private JLabel lblHourlyPrice;
    private JLabel lblDailyPrice;
    private JLabel lblRoomStatus;
    private List<RoomFurnitureItem> RoomFurnitureItems;

    // Customer Information Components
    private JTextField txtCustomerName;
    private JTextField txtPhoneNumber;
    private JTextField txtCCCD;
    private JButton btnFindCustomer;

    // Booking Information Components
    private JSpinner spnCheckInDate;
    private JSpinner spnCheckOutDate;
    private JSpinner spnCreateAt;
    private JTextArea txtNote;
    private JTextField txtInitialPrice;
    private JTextField txtTotalServicePrice;
    private JTextField txtDepositPrice;
    private JCheckBox chkIsAdvanced;
    private JButton reservationButton;

    // Service Components - simplified to use dialog
    private List<DonGoiDichVu> serviceOrdered = new ArrayList<>();

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

    // Buttons on bottom navbar
    private JButton btnGoDichVu;
    private JButton btnDatPhong;

    // Close button
    private JButton closeButton;

    public BookingFormPanel(BookingResponse roomInfo, String parentName) {
        this.parentName = parentName;

        this.selectedRoom = roomInfo;
        this.bookingService = new BookingServiceImpl();
        this.customerService = new CustomerServiceImpl();
        this.roomService = new RoomServiceImpl();

        initializeComponents();
        setupLayout();
        populateRoomInformation();
        setDefaultValues();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        ServiceSelectionPanel servicePanel = new ServiceSelectionPanel(PanelName.BOOKING.getName(), 1,
                                                                       Collections.singletonList(
                                                                               selectedRoom.getRoomName()),
                                                                       selectedRoom.getMaChiTietDatPhong(),
                                                                       (services) -> {
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
        btnFindCustomer = new JButton("Tìm kiếm bằng CCCD");

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
        spnCreateAt.setEditor(createAtEditor);

        chkIsAdvanced = new JCheckBox("Đặt phòng trước");
        reservationButton = new JButton(" Xem lịch đặt phòng");

        txtNote = new JTextArea(4, 25);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        // Initialize navbar buttons
        btnGoDichVu = new JButton("Gọi dịch vụ");
        btnDatPhong = new JButton("Đặt phòng");
    }

    private void setupLayout() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 40));
        headerPanel.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        headerPanel.setBackground(CustomUI.blue);

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);

        // Title with room name
        JLabel titleLabel = new JLabel("ĐẶT PHÒNG " + selectedRoom.getRoomName(), SwingConstants.CENTER);
        titleLabel.setFont(CustomUI.bigFont);
        titleLabel.setForeground(CustomUI.white);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 150, 0, 150));

        titlePanel.add(titleLabel);

        closeButton = new JButton("x");
        closeButton.setFont(CustomUI.bigFont);
        closeButton.setBackground(Color.RED);
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(50, 20));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> handleCloseReservation());
        closeButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

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

        // Create footer navbar panel
        JPanel footerPanel = createFooterNavbar();

        // Add to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(mainScrollPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createFooterNavbar() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setPreferredSize(new Dimension(0, 50));
        footerPanel.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        footerPanel.setBackground(CustomUI.lightGray);

        // Button panel with horizontal layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setBackground(CustomUI.lightGray);
        buttonPanel.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        buttonPanel.setOpaque(true);

        // Style and configure buttons
        btnGoDichVu.setFont(CustomUI.bigFont);
        btnGoDichVu.setBackground(CustomUI.blue);
        btnGoDichVu.setForeground(Color.WHITE);
        btnGoDichVu.setPreferredSize(new Dimension(300, 45));
        btnGoDichVu.setFocusPainted(false);
        btnGoDichVu.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnDatPhong.setFont(CustomUI.bigFont);
        btnDatPhong.setBackground(CustomUI.darkGreen);
        btnDatPhong.setForeground(Color.WHITE);
        btnDatPhong.setPreferredSize(new Dimension(300, 45));
        btnDatPhong.setFocusPainted(false);
        btnDatPhong.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Add hover effects
        btnGoDichVu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnGoDichVu.setBackground(CustomUI.blue.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnGoDichVu.setBackground(CustomUI.blue);
            }
        });

        btnDatPhong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnDatPhong.setBackground(CustomUI.darkGreen.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnDatPhong.setBackground(CustomUI.darkGreen);
            }
        });

        buttonPanel.add(btnGoDichVu);
        buttonPanel.add(Box.createHorizontalStrut(80));
        buttonPanel.add(btnDatPhong);

        footerPanel.add(buttonPanel, BorderLayout.CENTER);
        return footerPanel;
    }

    private JPanel createBaseContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();

        // LEFT COLUMN - Row 0: Booking info panel (WHITE background) - SWAPPED TO TOP
        JPanel bookingPanel = createBookingInfoPanel();
        bookingPanel.setBackground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 0.6; // More height for booking info
        gbc.insets = new Insets(0, 0, 5, 5);
        contentPanel.add(bookingPanel, gbc);

        // Right Column - Row 0: Action menu panel (WHITE background)
        JPanel customerPanel = createCustomerInfoPanel();
        bookingPanel.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4; gbc.weighty = 0.6; // More height for booking info
        gbc.insets = new Insets(0, 5, 5, 0);
        contentPanel.add(customerPanel, gbc);

        // LEFT COLUMN - Row 1: Room info panel (WHITE background)
        JPanel rightRoomPanel = createRoomInfoPanel();
        rightRoomPanel.setBackground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1; // Reset to single row
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 0.4; // Less height for customer info
        gbc.insets = new Insets(5, 0, 10, 0);
        contentPanel.add(rightRoomPanel, gbc);

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

        // Add form rows
        addFormRow(customerInfoContent, gbc, 0, "CCCD/CMND:", txtCCCD);
        addFormRow(customerInfoContent, gbc, 1, "Tên khách hàng:", txtCustomerName);
        addFormRow(customerInfoContent, gbc, 2, "Số điện thoại:", txtPhoneNumber);

        // Add search customer by CCCD button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnFindCustomer.setFont(CustomUI.smallFont);
        btnFindCustomer.setBackground(CustomUI.blue);
        btnFindCustomer.setForeground(Color.WHITE);
        btnFindCustomer.setFocusPainted(false);
        btnFindCustomer.setPreferredSize(new Dimension(80, 35));

        customerInfoContent.add(btnFindCustomer, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(customerInfoContent, BorderLayout.CENTER);

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

        // Single column layout as requested
        addFormRow(bookingInfoContent, gbc, 0, "Ngày nhận phòng:", spnCheckInDate);
        addFormRow(bookingInfoContent, gbc, 1, "Ngày trả phòng:", spnCheckOutDate);
        addFormRow(bookingInfoContent, gbc, 2, "Giá ban đầu:", txtInitialPrice);
        addFormRow(bookingInfoContent, gbc, 3, "Tổng tiền dịch vụ:", txtTotalServicePrice);
        addFormRow(bookingInfoContent, gbc, 4, "Tiền đặt cọc:", txtDepositPrice);

        txtInitialPrice.setEditable(false);
        txtTotalServicePrice.setEditable(false);
        txtDepositPrice.setEditable(false);

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

        // Add calendar icon to reservation button
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
        JTextArea txtNote = new JTextArea();
        txtNote.setPreferredSize(new Dimension(300, 100));
        txtNote.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        bookingInfoContent.add(txtNote, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(bookingInfoContent, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createRoomInfoPanel() {
        RoomFurnitureItems = roomService.getAllFurnitureInRoom(selectedRoom.getRoomId());

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
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        lblHourlyPrice.setText(priceFormatter.format(selectedRoom.getHourlyPrice()) + " VND");
        lblDailyPrice.setText(priceFormatter.format(selectedRoom.getDailyPrice()) + " VND");

        // Essential room information using addFormRow for consistency
        addFormRow(roomInfoContent, gbc, 0, 0,"Số phòng:", lblRoomNumber);
        addFormRow(roomInfoContent, gbc, 1, 0, "Loại phòng:", lblRoomType);
        addFormRow(roomInfoContent, gbc, 2, 0, "Sức chứa:", lblRoomCapacity);
        addFormRow(roomInfoContent, gbc, 0, 1, "Trạng thái:", lblRoomStatus);
        addFormRow(roomInfoContent, gbc, 1, 1, "Giá theo giờ:", lblHourlyPrice);
        addFormRow(roomInfoContent, gbc, 2, 1, "Giá theo ngày:", lblDailyPrice);

        // Additional info section with separator - flexible content based on room status
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 10, 8, 10);
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        roomInfoContent.add(separator, gbc);

        // Reset for additional fields
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel furnitureItems = new JLabel("Nội thất phòng: ");
        furnitureItems.setFont(CustomUI.smallFont);
        gbc.insets = new Insets(15, 10, 8, 10);
        roomInfoContent.add(furnitureItems, gbc);

        // Reset insets
        gbc.insets = new Insets(5, 10, 5, 10);

        // Add furniture items
        // It will separate into 2 column - Don`t use addFormRow for this part
        int pivot = (RoomFurnitureItems.size() + 1) / 2;
        for (int i = 0; i < RoomFurnitureItems.size(); i++) {
            RoomFurnitureItem item = RoomFurnitureItems.get(i);
            JLabel itemLabel = new JLabel(item.getName());
            JLabel quantityLabel = new JLabel("Số lượng: " + item.getQuantity());

            itemLabel.setFont(CustomUI.smallFont);
            quantityLabel.setFont(CustomUI.smallFont);

            if (i < pivot) {
                // Left column
                gbc.gridx = 0;
                gbc.gridy = 5 + i;
            } else {
                // Right column
                gbc.gridx = 2;
                gbc.gridy = 5 + (i - pivot);
            }

            roomInfoContent.add(itemLabel, gbc);
            gbc.gridx += 1;
            roomInfoContent.add(quantityLabel, gbc);
        }

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
        button.addActionListener(e -> item.getAction().run());

        return button;
    }

    private void populateActionItems() {
        List<ActionItem> actionItems = getActionItems();

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
    private List<ActionItem> getActionItems() {
        List<ActionItem> items = new ArrayList<>();

        ActionItem callServiceItem = new ActionItem("Gọi Dịch Vụ", IconUtil.createServiceIcon(), CustomUI.bluePurple, this::handleCallService);
        ActionItem bookRoomItem = new ActionItem("Đặt Phòng", IconUtil.createBookingIcon(), CustomUI.bluePurple, this::handleBookRoom);

        items.add(callServiceItem);
        items.add(bookRoomItem);

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
        } else if (component instanceof JSpinner) {
            component.setPreferredSize(new Dimension(300, 35));
            component.setMinimumSize(new Dimension(250, 35));
        }
        panel.add(component, gbc);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, int col, String labelText, JComponent component) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = col * 2;
        gbc.weightx = 0.0;
        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        component.setFont(CustomUI.smallFont);
        panel.add(component, gbc);
    }

    // Create a collapsible header panel
    private JPanel createCollapsibleHeader(ImageIcon icon, String title, Color backgroundColor, Color textColor, Runnable toggleAction) {
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
            }
            else if (title.contains("ĐẶT PHÒNG")) {
                dropdownButton.setText(isBookingInfoCollapsed ? "►" : "▼");
            }
            else if (title.contains("THÔNG TIN PHÒNG")) {
                dropdownButton.setText(isRoomInfoCollapsed ? "►" : "▼");
            } else if (title.contains("BẢNG THAO TÁC")) {
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
        txtInitialPrice.setText(priceFormatter.format(selectedRoom.getDailyPrice()) + " VNĐ");
        txtTotalServicePrice.setText(priceFormatter.format(0) + " VNĐ");

        // Set default check-in date to today
        if (TimeFilterHelper.getCheckinTime() != null
            && TimeFilterHelper.getCheckoutTime() != null
            && TimeFilterHelper.getCheckinTime().after(new Date())) {
            spnCheckOutDate.setValue(TimeFilterHelper.getCheckoutTime());
            spnCheckInDate.setValue(TimeFilterHelper.getCheckinTime());
            chkIsAdvanced.setSelected(true);
        } else {
            java.util.Date today = new Date();
            spnCheckOutDate.setValue(Date.from(today.toInstant().plus(1, ChronoUnit.DAYS)));
            spnCheckInDate.setValue(today);
            chkIsAdvanced.setSelected(false);
        }

        calculatePrice();
    }

    // Method to update total service price from ServiceSelectionPanel
    private void updateTotalServicePrice() {
        double totalServicePrice = 0.0;
        for (DonGoiDichVu service : serviceOrdered) {
            totalServicePrice += service.getGiaThoiDiemDo() * service.getSoLuong();
        }
        txtTotalServicePrice.setText(priceFormatter.format(totalServicePrice) + " VNĐ");
        calculateDepositPrice(); // Recalculate deposit when service price changes
    }

    // Method to calculate deposit price based on advanced booking status
    private void calculateDepositPrice() {
        try {
            // Parse prices by removing formatting
            String initialPriceText = txtInitialPrice.getText().replace(" VNĐ", "").replace(",", "");
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

    private void calculatePrice() {
        try {
            java.util.Date checkIn = (java.util.Date) spnCheckInDate.getValue();
            java.util.Date checkOut = (java.util.Date) spnCheckOutDate.getValue();

            if (checkOut.before(checkIn)) {
                JOptionPane.showMessageDialog(this, "Ngày trả phòng phải sau ngày nhận phòng!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long diffInMillis = checkOut.getTime() - checkIn.getTime();

            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
            long diffInHours = (diffInMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);

            // If hours exceed 12, round up to next day
            if (diffInHours > 12) {
                diffInDays += 1;
                diffInHours = 0;
            } else if (diffInHours > 0) {
                // Round up to next hour if there are remaining minutes
                diffInHours += 1;
            }

            // Total = days * dailyPrice + hours * hourlyPrice
            double totalPrice = (diffInDays * selectedRoom.getDailyPrice()) +
                                (diffInHours * selectedRoom.getHourlyPrice());

            txtInitialPrice.setText(priceFormatter.format(totalPrice) + " VNĐ");
            calculateDepositPrice(); // Recalculate deposit when initial price changes
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tính giá: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleConfirmBooking() {
        try {
            // Validate input
            if (!validateInput()) {
                return;
            }

            // Create booking event
            BookingCreationEvent bookingEvent = createBookingEvent();

            // Call booking service
            EventResponse<DepositInvoiceResponse> response = bookingService.createBooking(bookingEvent);
            if (response.getType().equals(ResponseType.SUCCESS)) {
                if (chkIsAdvanced.isSelected()) {
                    if (response.getData() != null) {
                        SwingUtilities.invokeLater(() -> {
                            DepositInvoiceDialog dialog =
                                    new DepositInvoiceDialog(response.getData());
                            dialog.setVisible(true);
                        });
                    } else {
                        throw new IllegalStateException("Expected DepositInvoiceResponse in response data");
                    }
                }

                JOptionPane.showMessageDialog(this,  response.getMessage(),
                                              "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // Refresh reservation management panel
                RefreshManager.refreshAfterBooking();
                handleCloseReservation(); // Return to previous screen
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("Error during booking: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi đặt phòng: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BookingResponse collectBookingResponse(String customerName, Timestamp checkInDate, Timestamp checkOutDate) {
        return new BookingResponse(selectedRoom.getRoomId(),
                selectedRoom.getRoomName(),
                selectedRoom.isActive(),
                RoomStatus.ROOM_CHECKING_STATUS.getStatus(),
                selectedRoom.getRoomType(),
                selectedRoom.getNumberOfCustomers(),
                selectedRoom.getDailyPrice(),
                selectedRoom.getHourlyPrice());
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
            return;
        }

        // Auto increase check-out date if it's not after check-in or less than 1 hour after check-in
        if (!currentCheckOutDate.after(checkInDate) ||
            (currentCheckOutDate.getTime() - checkInDate.getTime()) < (60 * 60 * 1000)) {
            // Set checkout to be 1 day after checkin
            spnCheckOutDate.setValue(Date.from(checkInDate.toInstant().plus(1, ChronoUnit.DAYS)));
        }

        // Auto on isAdvanced if check-in is in the future
        chkIsAdvanced.setSelected(checkInDate.after(now));

        calculatePrice();
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
            spnCheckOutDate.setValue(Date.from(checkInDate.toInstant().plus(1, ChronoUnit.DAYS)));
            return;
        }

        calculatePrice();
    }


    private boolean validateInput() {
        if (txtCCCD.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD/CMND!",
                                          "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }

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

//        // Regex check for CCCD/CMND format (12 digits)
//        String cccdPattern = "^[0-9]{12}$";
//        if (!txtCCCD.getText().trim().matches(cccdPattern)) {
//            JOptionPane.showMessageDialog(this, "CCCD/CMND không hợp lệ! Vui lòng nhập đúng định dạng 12 chữ số.",
//                                          "Lỗi định dạng CCCD/CMND", JOptionPane.WARNING_MESSAGE);
//            txtCCCD.requestFocus();
//            return false;
//        }
//
//        // Simple name validation (Last name & first name, letters and spaces only)
//        String namePattern = "^[A-ZÀ-ỹ][a-zà-ỹ]*(\\s[A-ZÀ-ỹ][a-zà-ỹ]*)+$";
//        if (!txtCustomerName.getText().trim().matches(namePattern)) {
//            JOptionPane.showMessageDialog(this, "Tên khách hàng không hợp lệ! Tên chỉ chứa ký tự và khoảng trắng.\nVui lòng nhập đầy đủ họ và tên.",
//                                          "Lỗi định dạng tên khách hàng", JOptionPane.WARNING_MESSAGE);
//            txtCustomerName.requestFocus();
//            return false;
//        }
//
//        // Simple phone number validation (digits only, length 10-15)
//        String phonePattern = "^[0-9]{10,15}$";
//        if (!txtPhoneNumber.getText().trim().matches(phonePattern)) {
//            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! Vui lòng nhập đúng định dạng từ 10 đến 15 chữ số.",
//                                          "Lỗi định dạng số điện thoại", JOptionPane.WARNING_MESSAGE);
//            txtPhoneNumber.requestFocus();
//            return false;
//        }

        // Check if customer with CCCD exists but have different name/phone
        KhachHang kh = customerService.getCustomerByCCCD(txtCCCD.getText().trim());
        if (kh != null) {
            if (!kh.getTenKhachHang().equalsIgnoreCase(txtCustomerName.getText().trim())
                || !kh.getSoDienThoai().equalsIgnoreCase(txtPhoneNumber.getText().trim())) {
                // Show dialog deny changing name/phone for existing CCCD
                JOptionPane.showMessageDialog(this,
                                              "CCCD/CMND đã tồn tại với tên hoặc số điện thoại khác!\n" +
                                              "Vui lòng kiểm tra lại thông tin khách hàng.",
                                              "Lỗi thông tin khách hàng",
                                              JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        try {
            Double.parseDouble(txtInitialPrice.getText().replace(" VNĐ", "").replace(",", ""));
            Double.parseDouble(txtDepositPrice.getText().replace(" VNĐ", "").replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá phòng và tiền đặt cọc phải là số!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        java.util.Date checkIn = (java.util.Date) spnCheckInDate.getValue();
        java.util.Date checkOut = (java.util.Date) spnCheckOutDate.getValue();

        if (checkOut.before(checkIn)) {
            JOptionPane.showMessageDialog(this, "Ngày trả phòng phải sau ngày nhận phòng!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Ensure pre-booking at least 1 hour in advance
        if (chkIsAdvanced.isSelected()) {
            Date now = new Date();
            if (checkIn.before(Date.from(now.toInstant().plus(1, ChronoUnit.HOURS)))) {
                JOptionPane.showMessageDialog(this,
                                              "Thời gian đặt trước phải ít nhất 1 giờ so với hiện tại!",
                                              "Lỗi thời gian",
                                              JOptionPane.WARNING_MESSAGE);
                spnCheckInDate.setValue(Date.from(now.toInstant().plus(1, ChronoUnit.HOURS).plus(1, ChronoUnit.MINUTES)));
                return false;
            }
        }

        return true;
    }

    // Create booking event from form data
    private BookingCreationEvent createBookingEvent() {

        String tenKhachHang = txtCustomerName.getText().trim();
        String soDienThoai = txtPhoneNumber.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String moTa = txtNote.getText().trim();
        java.sql.Timestamp ngayNhanPhong = new java.sql.Timestamp(((java.util.Date) spnCheckInDate.getValue()).getTime());
        java.sql.Timestamp ngayTraPhong = new java.sql.Timestamp(((java.util.Date) spnCheckOutDate.getValue()).getTime());
        java.sql.Timestamp thoiGianTao = new java.sql.Timestamp(System.currentTimeMillis());
        double tongTienDuTinh = Double.parseDouble(txtInitialPrice.getText().replace(" VNĐ", "").replace(",", ""))
                + Double.parseDouble(txtTotalServicePrice.getText().replace(" VNĐ", "").replace(",", ""));
        double tienDatCoc = Double.parseDouble(txtDepositPrice.getText().replace(" VNĐ", "").replace(",", ""));
        boolean daDatTruoc = chkIsAdvanced.isSelected();
        List<String> danhSachMaPhong = java.util.Arrays.asList(selectedRoom.getRoomId());

        // Assign roomId for each service ordered
        for (DonGoiDichVu service : serviceOrdered) {
            service.setMaPhong(selectedRoom.getRoomId());
        }

        String maPhienDangNhap = Main.getCurrentLoginSession();

        return new BookingCreationEvent(tenKhachHang, soDienThoai, cccd, moTa,
                                        ngayNhanPhong, ngayTraPhong, tongTienDuTinh, tienDatCoc, daDatTruoc,
                                        danhSachMaPhong, serviceOrdered, maPhienDangNhap, thoiGianTao);
    }

    // Setup event handlers for buttons
    private void setupEventHandlers() {
        spnCheckInDate.addChangeListener(e -> handleCheckinDateChange());
        spnCheckOutDate.addChangeListener(e -> handleCheckoutDateChange());

        btnFindCustomer.addActionListener(e -> handleFindCustomer());
        closeButton.addActionListener(e -> handleCloseReservation());
        chkIsAdvanced.addActionListener(e -> handleCalculateDeposit());
        reservationButton.addActionListener(e -> handleShowReservationManagement());

        btnGoDichVu.addActionListener(e -> handleCallService());
        btnDatPhong.addActionListener(e -> handleConfirmBooking());
    }

    private void handleFindCustomer() {
        String cccd = txtCCCD.getText().trim();
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD/CMND để tìm kiếm!",
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return;
        }

        KhachHang khachHang = customerService.getCustomerByCCCD(cccd);
        if (khachHang != null) {
            txtCustomerName.setText(khachHang.getTenKhachHang());
            txtPhoneNumber.setText(khachHang.getSoDienThoai());
            JOptionPane.showMessageDialog(this, "Tìm thấy khách hàng: " + khachHang.getTenKhachHang(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng với CCCD/CMND: " + cccd,
                "Không tìm thấy", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleShowReservationManagement() {
        PreReservationSearchPanel reservationFormManagementPanel =
                new PreReservationSearchPanel(PanelName.BOOKING.getName(), selectedRoom.getRoomName(), selectedRoom.getRoomId());

        Main.addCard(reservationFormManagementPanel, PanelName.PRE_RESERVATION_SEARCH.getName());
        Main.showCard(PanelName.PRE_RESERVATION_SEARCH.getName());
    }

    private void handleCalculateDeposit() {
        boolean isSelected = chkIsAdvanced.isSelected();
        txtDepositPrice.setEnabled(isSelected);

        if (!isSelected) {
            // If unchecked, reset deposit to 0 and check-in date to today
            spnCheckInDate.setValue(new Date());
            txtDepositPrice.setText("0");
        }

        calculateDepositPrice();
    }

    // Handler methods for action buttons
    private void handleCallService() {
        Main.showCard(SERVICE_ORDER.getName());
    }

    private void handleBookRoom() {
        handleConfirmBooking();
    }

    private void handleCloseReservation() {
        Main.showCard(parentName);
    }
}

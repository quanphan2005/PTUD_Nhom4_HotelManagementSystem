package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.ReservationResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.BookingService;
import vn.iuh.service.impl.BookingServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservationManagementPanel extends JPanel {
    private final BookingService bookingService;

    // Filter components
    private JTextField txtMaDon;
    private JTextField txtCCCD;
    private JTextField txtTenPhong;
    private JSpinner spnStartDate;
    private JSpinner spnEndDate;
    private JCheckBox chkCurrentReservations;
    private JButton btnReset;

    // Table components
    private JTable reservationTable;
    private DefaultTableModel tableModel;

    // Data
    private List<ReservationResponse> allReservations;
    private List<ReservationResponse> filteredReservations;

    public ReservationManagementPanel() {
        bookingService = new BookingServiceImpl();

        // Load data
        loadReservationData();

        setLayout(new BorderLayout());
        init();
    }

    private void loadReservationData() {
        allReservations = bookingService.getAllReservationsWithStatus();
        filteredReservations = new ArrayList<>(allReservations);
    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTopPanel();
        createFilterPanel();
        createTablePanel();
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("QUẢN LÝ ĐƠN ĐẶT PHÒNG", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMinimumSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        pnlTop.add(lblTop, BorderLayout.CENTER);

        // Create wrapper panel with spacing
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(pnlTop, BorderLayout.CENTER);
//        topWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Add bottom margin

        add(topWrapper, BorderLayout.NORTH);
    }

    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize filter components
        initializeFilterComponents();

        // Row 1: Mã đơn và Số CCCD
        addFilterRow(filterPanel, gbc, 0, 0, "Mã đơn:", txtMaDon);
        addFilterRow(filterPanel, gbc, 0, 2, "Số CCCD:", txtCCCD);

        // Row 2: Thời gian bắt đầu và Tên phòng
        addFilterRow(filterPanel, gbc, 1, 0, "Thời gian bắt đầu:", spnStartDate);
        addFilterRow(filterPanel, gbc, 1, 2, "Tên phòng:", txtTenPhong);

        // Row 3: Thời gian kết thúc, Checkbox và Reset button
        addFilterRow(filterPanel, gbc, 2, 0, "Thời gian kết thúc:", spnEndDate);

        // Checkbox "Các đơn hiện tại"
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        chkCurrentReservations.setFont(CustomUI.smallFont);
        filterPanel.add(chkCurrentReservations, gbc);

        // Reset button
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.2;
        btnReset.setPreferredSize(new Dimension(120, 35));
        filterPanel.add(btnReset, gbc);

//        filterPanel.setPreferredSize(new Dimension(0, 250));
//        filterPanel.setMinimumSize(new Dimension(0, 250));
//        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        // Create wrapper panel with spacing
        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.add(filterPanel, BorderLayout.CENTER);
        filterWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add margins

        add(filterWrapper, BorderLayout.CENTER);
    }

    private void initializeFilterComponents() {
        // Mã đơn text field with auto-filtering
        txtMaDon = new JTextField(15);
        txtMaDon.setFont(CustomUI.smallFont);
        txtMaDon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        // CCCD text field with auto-filtering
        txtCCCD = new JTextField(15);
        txtCCCD.setFont(CustomUI.smallFont);
        txtCCCD.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        // Tên phòng text field with auto-filtering
        txtTenPhong = new JTextField(15);
        txtTenPhong.setFont(CustomUI.smallFont);
        txtTenPhong.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        // Start date spinner
        spnStartDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(spnStartDate, "dd/MM/yyyy HH:mm");
        spnStartDate.setEditor(startDateEditor);
        spnStartDate.setValue(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)); // 30 days ago
        spnStartDate.setFont(CustomUI.smallFont);
        spnStartDate.addChangeListener(e -> applyFilters());

        // End date spinner
        spnEndDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(spnEndDate, "dd/MM/yyyy HH:mm");
        spnEndDate.setEditor(endDateEditor);
        spnEndDate.setValue(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)); // 30 days from now
        spnEndDate.setFont(CustomUI.smallFont);
        spnEndDate.addChangeListener(e -> applyFilters());

        // Checkbox for current reservations
        chkCurrentReservations = new JCheckBox("Các đơn hiện tại");
        chkCurrentReservations.setFont(CustomUI.smallFont);
        chkCurrentReservations.setSelected(true); // Default to showing current reservations
        chkCurrentReservations.addActionListener(e -> applyFilters());

        // Reset button
        btnReset = new JButton("Hoàn tác");
        btnReset.setFont(CustomUI.smallFont);
        btnReset.setBackground(CustomUI.lightGray);
        btnReset.setForeground(Color.BLACK);
        btnReset.setFocusPainted(false);
        btnReset.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        btnReset.addActionListener(e -> resetFilters());
    }

    private void createTablePanel() {
        // Create table model
        String[] columnNames = {"Số CCCD", "Khách hàng", "Mã đơn", "Phòng", "Checkin", "Checkout", "Trạng thái", "Thao tác"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only action column is editable
            }
        };

        // Create table
        reservationTable = new JTable(tableModel) { // Tạo JTable mới dựa trên model
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                // prepareRenderer được gọi mỗi khi JTable vẽ 1 cell.
                Component c = super.prepareRenderer(renderer, row, column);

                // reuse font constant (không new font mỗi cell)
                c.setFont(CustomUI.TABLE_FONT);

                if (!isRowSelected(row)) {
                    // reuse color constant
                    c.setBackground(row % 2 == 0 ? CustomUI.ROW_ODD : CustomUI.ROW_EVEN);
                } else {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                }
                return c;
            }
        };
        reservationTable.setFont(CustomUI.TABLE_FONT);
        reservationTable.setRowHeight(40);
        reservationTable.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        reservationTable.setGridColor(CustomUI.tableBorder);
        reservationTable.setShowGrid(true);
        reservationTable.setIntercellSpacing(new Dimension(1, 1));

        // Enhanced header styling
        reservationTable.getTableHeader().setPreferredSize(new Dimension(reservationTable.getWidth(), 40));
        reservationTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        reservationTable.getTableHeader().setBackground(CustomUI.blue);
        reservationTable.getTableHeader().setForeground(CustomUI.white);
        reservationTable.getTableHeader().setOpaque(true);
        reservationTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        // Set alternating row colors
        reservationTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        // Set column widths
        TableColumnModel columnModel = reservationTable.getColumnModel();
        reservationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        columnModel.getColumn(0).setPreferredWidth(120); // Số CCCD
        columnModel.getColumn(1).setPreferredWidth(150); // Khách hàng
        columnModel.getColumn(2).setPreferredWidth(120); // Mã đơn
        columnModel.getColumn(3).setPreferredWidth(80);  // Phòng
        columnModel.getColumn(4).setPreferredWidth(130); // Checkin
        columnModel.getColumn(5).setPreferredWidth(130); // Checkout
        columnModel.getColumn(6).setPreferredWidth(120); // Trạng thái
        columnModel.getColumn(7).setPreferredWidth(150); // Thao tác

        // Set cell renderer and editor for action column
        reservationTable.getColumn("Thao tác").setCellRenderer(new ActionButtonRenderer());
        reservationTable.getColumn("Thao tác").setCellEditor(new ActionButtonEditor());

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.SOUTH);

        // Populate table with initial data
        applyFilters(); // Apply default filter on load
    }

    // Custom renderer for alternating row colors
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            component.setFont(CustomUI.TABLE_FONT);

            if (isSelected) {
                component.setBackground(CustomUI.ROW_SELECTED_COLOR);
                component.setForeground(Color.BLACK);
            } else {
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.ROW_EVEN);
                } else {
                    component.setBackground(CustomUI.ROW_ODD);
                }
                component.setForeground(Color.BLACK);
            }

            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

            return component;
        }
    }

    private void addFilterRow(JPanel panel, GridBagConstraints gbc, int row, int startCol,
                              String labelText, JComponent component) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;

        // Label
        gbc.gridx = startCol;
        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        // Component
        gbc.gridx = startCol + 1;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        component.setFont(CustomUI.smallFont);
        component.setPreferredSize(new Dimension(180, 35));
        component.setMinimumSize(new Dimension(150, 35));
        panel.add(component, gbc);
    }

    private void applyFilters() {
        filteredReservations.clear();

        String maDonFilter = txtMaDon.getText().trim().toLowerCase();
        String cccdFilter = txtCCCD.getText().trim().toLowerCase();
        String tenPhongFilter = txtTenPhong.getText().trim().toLowerCase();
        Date startDate = (Date) spnStartDate.getValue();
        Date endDate = (Date) spnEndDate.getValue();
        boolean showCurrentOnly = chkCurrentReservations.isSelected();

        for (ReservationResponse reservation : allReservations) {
            // Apply status filter first
            if (showCurrentOnly) {
                // Show only current reservations (Chờ checkin, Đang sử dụng, etc.)
                if (!isCurrentReservation(reservation.getStatus())) {
                    continue;
                }
            } else {
                // Show only ended reservations (Đã trả phòng, Checkout trễ)
                if (!isEndedReservation(reservation.getStatus())) {
                    continue;
                }
            }

            // Mã đơn filter
            if (!maDonFilter.isEmpty() &&
                !reservation.getMaDonDatPhong().toLowerCase().contains(maDonFilter)) {
                continue;
            }

            // CCCD filter
            if (!cccdFilter.isEmpty() &&
                !reservation.getCCCD().toLowerCase().contains(cccdFilter)) {
                continue;
            }

            // Tên phòng filter
            if (!tenPhongFilter.isEmpty() &&
                !reservation.getRoomName().toLowerCase().contains(tenPhongFilter)) {
                continue;
            }

            // Date range filter - check if reservation's checkin or checkout falls within range
            if (reservation.getTimeIn() != null && reservation.getTimeOut() != null) {
                Timestamp checkinTime = reservation.getTimeIn();
                Timestamp checkoutTime = reservation.getTimeOut();

                // Check if the reservation overlaps with the date range
                if (checkinTime.after(new Timestamp(endDate.getTime())) ||
                    checkoutTime.before(new Timestamp(startDate.getTime()))) {
                    continue;
                }
            }

            filteredReservations.add(reservation);
        }

        populateTable();
    }

    private boolean isCurrentReservation(String status) {
        // Current reservations include: "Chờ checkin", "Đang sử dụng", "Đang dọn dẹp"
        return status != null && (
            status.equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus()) ||
            status.equalsIgnoreCase(RoomStatus.ROOM_USING_STATUS.getStatus()) ||
            status.equalsIgnoreCase(RoomStatus.ROOM_CLEANING_STATUS.getStatus()) ||
            status.equalsIgnoreCase(RoomStatus.ROOM_CHECKING_STATUS.getStatus())
        );
    }

    private boolean isEndedReservation(String status) {
        // Ended reservations include: "Đã trả phòng", "Checkout trễ", etc.
        return status != null && (
            status.equalsIgnoreCase(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus()) ||
            status.equalsIgnoreCase("Đã trả phòng") ||
            status.equalsIgnoreCase("Không xác định")
        );
    }

    private void resetFilters() {
        txtMaDon.setText("");
        txtCCCD.setText("");
        txtTenPhong.setText("");
        spnStartDate.setValue(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
        spnEndDate.setValue(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        chkCurrentReservations.setSelected(true);

        filteredReservations = new ArrayList<>(allReservations);
        applyFilters();
    }

    private void populateTable() {
        tableModel.setRowCount(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Sort by checkin date (ascending) and then by mã đơn (ascending)
        filteredReservations.sort((r1, r2) -> {
            // First sort by checkin date
            int dateCompare = 0;
            if (r1.getTimeIn() != null && r2.getTimeIn() != null) {
                dateCompare = r1.getTimeIn().compareTo(r2.getTimeIn());
            } else if (r1.getTimeIn() == null && r2.getTimeIn() != null) {
                return 1;
            } else if (r1.getTimeIn() != null && r2.getTimeIn() == null) {
                return -1;
            }

            // If checkin dates are equal, sort by mã đơn
            if (dateCompare == 0) {
                return r1.getMaDonDatPhong().compareTo(r2.getMaDonDatPhong());
            }

            return dateCompare;
        });

        // Add filtered reservations to table
        for (ReservationResponse reservation : filteredReservations) {
            Object[] rowData = new Object[8];
            rowData[0] = reservation.getCCCD();
            rowData[1] = reservation.getCustomerName();
            rowData[2] = reservation.getMaDonDatPhong();
            rowData[3] = reservation.getRoomName();
            rowData[4] = reservation.getTimeIn() != null ? dateFormat.format(reservation.getTimeIn()) : "N/A";
            rowData[5] = reservation.getTimeOut() != null ? dateFormat.format(reservation.getTimeOut()) : "N/A";
            rowData[6] = reservation.getStatus();
            rowData[7] = reservation; // Store the reservation object for action buttons

            tableModel.addRow(rowData);
        }
    }

    private void handleViewDetail(ReservationResponse reservation) {
        if (reservation == null) return;

        // TODO: Implement view detail functionality
        JOptionPane.showMessageDialog(this,
            "Xem chi tiết đơn: " + reservation.getMaDonDatPhong() +
            "\nKhách hàng: " + reservation.getCustomerName() +
            "\nPhòng: " + reservation.getRoomName() +
            "\nTrạng thái: " + reservation.getStatus(),
            "Chi tiết đơn lưu trú",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshPanel() {
        loadReservationData();
        applyFilters();
    }

    // Custom cell renderer for action button
    private class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton btnViewDetail;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            setOpaque(true);

            btnViewDetail = new JButton("Xem chi tiết");
            btnViewDetail.setFont(CustomUI.verySmallFont);
            btnViewDetail.setBackground(CustomUI.lightBlue);
            btnViewDetail.setForeground(Color.WHITE);
            btnViewDetail.setPreferredSize(new Dimension(120, 30));
            btnViewDetail.setFocusPainted(false);
            btnViewDetail.putClientProperty(FlatClientProperties.STYLE, " arc: 8");

            add(btnViewDetail);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    // Custom cell editor for action button
    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnViewDetail;
        private ReservationResponse currentReservation;

        public ActionButtonEditor() {
            super(new JCheckBox());

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));

            btnViewDetail = new JButton("Xem chi tiết");
            btnViewDetail.setFont(CustomUI.verySmallFont);
            btnViewDetail.setBackground(CustomUI.lightBlue);
            btnViewDetail.setForeground(Color.WHITE);
            btnViewDetail.setPreferredSize(new Dimension(120, 30));
            btnViewDetail.setFocusPainted(false);
            btnViewDetail.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnViewDetail.addActionListener(e -> {
                ReservationResponse reservationToProcess = currentReservation;

                SwingUtilities.invokeLater(() -> {
                    fireEditingStopped();

                    if (reservationToProcess != null) {
                        handleViewDetail(reservationToProcess);
                    }
                });
            });

            panel.add(btnViewDetail);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            try {
                currentReservation = (ReservationResponse) value;
                return panel;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Object getCellEditorValue() {
            return currentReservation;
        }
    }
}


package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.servcie.BookingService;
import vn.iuh.servcie.impl.BookingServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReservationFormManagementPanel extends JPanel {
    private final BookingService bookingService;
    
    // Filter components
    private JTextField txtRoomName;
    private JTextField txtCustomerName;
    private JSpinner spnCheckinDate;
    private JButton btnReset;
    
    // Table components
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    
    // Data
    private List<BookingResponse> allReservations;
    private List<BookingResponse> filteredReservations;
    
    // Filter state
    private ReservationFilter reservationFilter;
    
    public ReservationFormManagementPanel() {
        bookingService = new BookingServiceImpl();
        reservationFilter = new ReservationFilter(null, null, null);
        
        // Load data
        loadReservationData();
        
        setLayout(new BorderLayout());
        init();
    }
    
    private void loadReservationData() {
        allReservations = new ArrayList<>();
        
        // Get all booking responses from service
        List<BookingResponse> bookingResponses = bookingService.getAllBookingInfo();
        
        // Filter only reserved/booked rooms
        for (BookingResponse booking : bookingResponses) {
            if (booking.getRoomStatus().equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus()) ||
                booking.getRoomStatus().equalsIgnoreCase(RoomStatus.ROOM_USING_STATUS.getStatus())) {
                allReservations.add(booking);
            }
        }
        
        filteredReservations = new ArrayList<>(allReservations);
    }
    
    private void init() {
        createTopPanel();
        createFilterPanel();
        createTablePanel();
    }
    
    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("Quản lí đơn đặt phòng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);
        
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);
        
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMinimumSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        
        add(pnlTop, BorderLayout.NORTH);
    }
    
    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Initialize filter components
        initializeFilterComponents();
        
        // Row 1: Tên phòng và Tên khách hàng
        addFilterRow(filterPanel, gbc, 0, 0, "Tên phòng:", txtRoomName);
        addFilterRow(filterPanel, gbc, 0, 2, "Tên khách hàng:", txtCustomerName);
        
        // Row 2: Thời gian checkin và Reset button
        addFilterRow(filterPanel, gbc, 1, 0, "Thời gian checkin:", spnCheckinDate);
        
        // Reset button
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        btnReset.setPreferredSize(new Dimension(120, 35));
        filterPanel.add(btnReset, gbc);
        
        // Set fixed height for filter panel
        filterPanel.setPreferredSize(new Dimension(0, 120));
        filterPanel.setMinimumSize(new Dimension(0, 120));
        filterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        add(filterPanel, BorderLayout.CENTER);
    }
    
    private void initializeFilterComponents() {
        // Room name text field
        txtRoomName = new JTextField(15);
        txtRoomName.setFont(CustomUI.smallFont);
        txtRoomName.addActionListener(e -> applyFilters());
        
        // Customer name text field
        txtCustomerName = new JTextField(15);
        txtCustomerName.setFont(CustomUI.smallFont);
        txtCustomerName.addActionListener(e -> applyFilters());
        
        // Check-in date spinner
        spnCheckinDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnCheckinDate, "dd/MM/yyyy");
        spnCheckinDate.setEditor(dateEditor);
        spnCheckinDate.setValue(new Date());
        spnCheckinDate.setFont(CustomUI.smallFont);
        spnCheckinDate.addChangeListener(e -> applyFilters());
        
        // Reset button
        btnReset = new JButton("HOÀN TÁC");
        btnReset.setFont(CustomUI.smallFont);
        btnReset.setBackground(CustomUI.lightGray);
        btnReset.setForeground(Color.BLACK);
        btnReset.setFocusPainted(false);
        btnReset.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        btnReset.addActionListener(e -> resetFilters());
    }
    
    private void createTablePanel() {
        // Create table model
        String[] columnNames = {"Khách hàng", "Phòng", "Checkin", "Checkout", "Thao tác"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only action column is editable
            }
        };
        
        // Create table
        reservationTable = new JTable(tableModel);
        reservationTable.setFont(CustomUI.tableDataFont); // Non-bold font for data
        reservationTable.setRowHeight(40);
        reservationTable.setSelectionBackground(CustomUI.tableSelection);
        reservationTable.setGridColor(CustomUI.tableBorder);
        reservationTable.setShowGrid(true); // Show grid lines
        reservationTable.setIntercellSpacing(new Dimension(1, 1)); // Thin borders

        // Enhanced header styling
        reservationTable.getTableHeader().setFont(CustomUI.tableHeaderFont);
        reservationTable.getTableHeader().setBackground(CustomUI.tableHeaderBackground);
        reservationTable.getTableHeader().setForeground(CustomUI.tableHeaderForeground);
        reservationTable.getTableHeader().setOpaque(true);
        reservationTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        // Set alternating row colors
        reservationTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        // Set column widths
        TableColumnModel columnModel = reservationTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150); // Khách hàng
        columnModel.getColumn(1).setPreferredWidth(100); // Phòng
        columnModel.getColumn(2).setPreferredWidth(120); // Checkin
        columnModel.getColumn(3).setPreferredWidth(120); // Checkout
        columnModel.getColumn(4).setPreferredWidth(200); // Thao tác
        
        // Set cell renderer for action column
        reservationTable.getColumn("Thao tác").setCellRenderer(new ActionButtonRenderer());
        reservationTable.getColumn("Thao tác").setCellEditor(new ActionButtonEditor());
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.SOUTH);

        // Populate table with initial data
        populateTable();
    }
    
    // Custom renderer for alternating row colors and proper styling
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set font to non-bold for data
            component.setFont(CustomUI.tableDataFont);

            if (isSelected) {
                component.setBackground(CustomUI.tableSelection);
                component.setForeground(Color.BLACK);
            } else {
                // Alternating row colors
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.tableRowEven);
                } else {
                    component.setBackground(CustomUI.tableRowOdd);
                }
                component.setForeground(Color.BLACK);
            }

            // Center align text for all columns except action column
            if (column < 4) {
                setHorizontalAlignment(JLabel.CENTER);
            }

            // Add subtle border
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

            return component;
        }
    }

    private void addFilterRow(JPanel panel, GridBagConstraints gbc, int row, int startCol, String labelText, JComponent component) {
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
        reservationFilter.roomName = txtRoomName.getText().trim();
        reservationFilter.customerName = txtCustomerName.getText().trim();
        reservationFilter.checkinDate = (Date) spnCheckinDate.getValue();
        
        filteredReservations.clear();
        
        for (BookingResponse reservation : allReservations) {
            if (passesAllFilters(reservation)) {
                filteredReservations.add(reservation);
            }
        }
        
        populateTable();
    }
    
    private boolean passesAllFilters(BookingResponse reservation) {
        // Room name filter
        if (reservationFilter.roomName != null && !reservationFilter.roomName.isEmpty()) {
            if (!reservation.getRoomName().toLowerCase().contains(reservationFilter.roomName.toLowerCase())) {
                return false;
            }
        }
        
        // Customer name filter
        if (reservationFilter.customerName != null && !reservationFilter.customerName.isEmpty()) {
            if (!reservation.getCustomerName().toLowerCase().contains(reservationFilter.customerName.toLowerCase())) {
                return false;
            }
        }
        
        // Checkin date filter
        if (reservationFilter.checkinDate != null && reservation.getTimeIn() != null) {
            Date filterDate = reservationFilter.checkinDate;
            Date reservationDate = new Date(reservation.getTimeIn().getTime());
            
            // Compare only dates, not times
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String filterDateStr = dateFormat.format(filterDate);
            String reservationDateStr = dateFormat.format(reservationDate);
            
            if (!filterDateStr.equals(reservationDateStr)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void resetFilters() {
        txtRoomName.setText("");
        txtCustomerName.setText("");
        spnCheckinDate.setValue(new Date());
        
        reservationFilter = new ReservationFilter(null, null, null);
        filteredReservations = new ArrayList<>(allReservations);
        populateTable();
    }
    
    private void populateTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        // Add filtered reservations to table
        for (BookingResponse reservation : filteredReservations) {
            Object[] rowData = new Object[5];
            rowData[0] = reservation.getCustomerName();
            rowData[1] = reservation.getRoomName();
            rowData[2] = reservation.getTimeIn() != null ? dateFormat.format(reservation.getTimeIn()) : "N/A";
            rowData[3] = reservation.getTimeOut() != null ? dateFormat.format(reservation.getTimeOut()) : "N/A";
            rowData[4] = reservation; // Store the reservation object for action buttons
            
            tableModel.addRow(rowData);
        }
    }
    
    private void handleChangeRoom(BookingResponse reservation) {
        String newRoom = JOptionPane.showInputDialog(this,
            "Nhập số phòng muốn chuyển đến:",
            "Đổi phòng", JOptionPane.QUESTION_MESSAGE);
        
        if (newRoom != null && !newRoom.trim().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Xác nhận đổi phòng từ " + reservation.getRoomName() + " sang " + newRoom + "?",
                "Xác nhận đổi phòng", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this,
                    "Đã đổi phòng thành công từ " + reservation.getRoomName() + " sang " + newRoom,
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                // TODO: Implement actual room change logic
                // bookingService.changeRoom(reservation.getId(), newRoom);
                
                // Refresh data
                loadReservationData();
                applyFilters();
            }
        }
    }
    
    private void handleCancelReservation(BookingResponse reservation) {
        int result = JOptionPane.showConfirmDialog(this,
            "Xác nhận hủy đơn đặt phòng " + reservation.getRoomName() + " của khách " + reservation.getCustomerName() + "?",
            "Hủy đơn đặt phòng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "Đã hủy đơn đặt phòng " + reservation.getRoomName() + " thành công",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // TODO: Implement actual cancellation logic
            // bookingService.cancelReservation(reservation.getId());
            
            // Refresh data
            loadReservationData();
            applyFilters();
        }
    }
    
    // Custom cell renderer for action buttons
    private class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton btnChangeRoom;
        private JButton btnCancel;
        
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            
            btnChangeRoom = new JButton("Đổi phòng");
            btnChangeRoom.setFont(CustomUI.verySmallFont);
            btnChangeRoom.setBackground(CustomUI.lightBlue);
            btnChangeRoom.setForeground(Color.WHITE);
            btnChangeRoom.setPreferredSize(new Dimension(120, 30));
            btnChangeRoom.setFocusPainted(false);
            btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            
            btnCancel = new JButton("Hủy đơn");
            btnCancel.setFont(CustomUI.verySmallFont);
            btnCancel.setBackground(CustomUI.red);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setPreferredSize(new Dimension(120, 30));
            btnCancel.setFocusPainted(false);
            btnCancel.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            
            add(btnChangeRoom);
            add(btnCancel);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }
    
    // Custom cell editor for action buttons
    private class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnChangeRoom;
        private JButton btnCancel;
        private BookingResponse currentReservation;
        
        public ActionButtonEditor() {
            super(new JCheckBox());
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            
            btnChangeRoom = new JButton("Đổi phòng");
            btnChangeRoom.setFont(CustomUI.verySmallFont);
            btnChangeRoom.setBackground(CustomUI.lightBlue);
            btnChangeRoom.setForeground(Color.WHITE);
            btnChangeRoom.setPreferredSize(new Dimension(120, 30));
            btnChangeRoom.setFocusPainted(false);
            btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnChangeRoom.addActionListener(e -> {
                handleChangeRoom(currentReservation);
                fireEditingStopped();
            });
            
            btnCancel = new JButton("Hủy đơn");
            btnCancel.setFont(CustomUI.verySmallFont);
            btnCancel.setBackground(CustomUI.red);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setPreferredSize(new Dimension(120, 30));
            btnCancel.setFocusPainted(false);
            btnCancel.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnCancel.addActionListener(e -> {
                handleCancelReservation(currentReservation);
                fireEditingStopped();
            });
            
            panel.add(btnChangeRoom);
            panel.add(btnCancel);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentReservation = (BookingResponse) value;
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return currentReservation;
        }
    }
    
    // Filter state holder
    private static class ReservationFilter {
        String roomName;
        String customerName;
        Date checkinDate;
        
        public ReservationFilter(String roomName, String customerName, Date checkinDate) {
            this.roomName = roomName;
            this.customerName = customerName;
            this.checkinDate = checkinDate;
        }
    }
}

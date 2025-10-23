package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.PreReservationResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.panel.DoiPhongDiaLog;
import vn.iuh.service.BookingService;
import vn.iuh.service.CheckinService;
import vn.iuh.service.DoiPhongService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.service.impl.CheckinServiceImpl;
import vn.iuh.service.impl.DoiPhongServiceImpl;
import vn.iuh.util.RefreshManager;

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
import java.util.Objects;

public class PreReservationManagementPanel extends JPanel {
    private final BookingService bookingService;
    private final CheckinService checkinService;
    private final DoiPhongService doiPhongService;

    // Filter components
    private JTextField txtRoomName;
    private JTextField txtCustomerName;
    private JSpinner spnCheckinDate;
    private JButton btnReset;
    private JButton btnUndo;

    // Table components
    private JTable reservationTable;
    private DefaultTableModel tableModel;

    // Data
    private List<PreReservationResponse> allReservations;
    private List<PreReservationResponse> filteredReservations;

    // Filter state
    private ReservationFilter reservationFilter;

    public PreReservationManagementPanel() {
        // Initialize services and data
        bookingService = new BookingServiceImpl();
        checkinService = new CheckinServiceImpl();
        doiPhongService = new DoiPhongServiceImpl();

        reservationFilter = new ReservationFilter(null, null, null);
        RefreshManager.setPreReservationManagementPanel(this);

        // Load data
        loadReservationData();

        setLayout(new BorderLayout());
        init();
    }

    private void loadReservationData() {
        allReservations = bookingService.getAllReservationForms();
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
        JLabel lblTop = new JLabel("QUẢN LÝ ĐƠN ĐẶT PHÒNG TRƯỚC", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMinimumSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        pnlTop.add(lblTop, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);
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

        // Create wrapper panel with spacing
        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.add(filterPanel, BorderLayout.CENTER);
        filterWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add margins

        add(filterWrapper, BorderLayout.CENTER);
    }

    private void initializeFilterComponents() {
        // Room name text field with auto-filtering
        txtRoomName = new JTextField(15);
        txtRoomName.setFont(CustomUI.smallFont);
        txtRoomName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Auto-filter on every key release
            }
        });

        // Customer name text field with auto-filtering
        txtCustomerName = new JTextField(15);
        txtCustomerName.setFont(CustomUI.smallFont);
        txtCustomerName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Auto-filter on every key release
            }
        });

        // Check-in date spinner with time
        spnCheckinDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnCheckinDate, "dd/MM/yyyy HH:mm");
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
        String[] columnNames = {"Khách hàng", "Đơn đặt phòng", "Phòng", "Checkin", "Checkout", "Thao tác"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only action column is editable
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
        reservationTable.setFont(CustomUI.TABLE_FONT); // Non-bold font for data
        reservationTable.setRowHeight(40);
        reservationTable.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        reservationTable.setGridColor(CustomUI.tableBorder);
        reservationTable.setShowGrid(true); // Show grid lines
        reservationTable.setIntercellSpacing(new Dimension(1, 1)); // Thin borders

        // Enhanced header styling
        reservationTable.getTableHeader().setPreferredSize(new Dimension(reservationTable.getWidth(), 40));
        reservationTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        reservationTable.getTableHeader().setBackground(CustomUI.blue);
        reservationTable.getTableHeader().setForeground(CustomUI.white);
        reservationTable.getTableHeader().setOpaque(true);
        reservationTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        // Set alternating row colors
        reservationTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        // Set column widths using relative proportions
        reservationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        reservationTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = reservationTable.getWidth();
                TableColumnModel columnModel = reservationTable.getColumnModel();

                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.15)); // 15%
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.15)); // 15%
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.10)); // 10%
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.15)); // 15%
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15%
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.30)); // 30%
            }
        });


        // Set cell renderer for action column
        reservationTable.getColumn("Thao tác").setCellRenderer(new ActionButtonRenderer());
        reservationTable.getColumn("Thao tác").setCellEditor(new ActionButtonEditor());

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(reservationTable);
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
            component.setFont(CustomUI.TABLE_FONT);

            if (isSelected) {
                component.setBackground(CustomUI.ROW_SELECTED_COLOR);
                component.setForeground(Color.BLACK);
            } else {
                // Alternating row colors
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.ROW_EVEN);
                } else {
                    component.setBackground(CustomUI.ROW_ODD);
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

        for (PreReservationResponse reservation : allReservations) {
            if (passesAllFilters(reservation)) {
                filteredReservations.add(reservation);
            }
        }

        populateTable();
    }

    private boolean passesAllFilters(PreReservationResponse reservation) {
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

        // Checkin date filter - show all reservations with check-in time after the selected date/time
        if (reservationFilter.checkinDate != null && reservation.getTimeIn() != null) {
            Date filterDateTime = reservationFilter.checkinDate;
            Date reservationDateTime = new Date(reservation.getTimeIn().getTime());

            // Show reservations where check-in is after the filter date/time
            if (reservationDateTime.before(filterDateTime)) {
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Sort filtered reservations by check-in date (nearest first)
        filteredReservations.sort((r1, r2) -> {
            if (r1.getTimeIn() == null && r2.getTimeIn() == null) return 0;
            if (r1.getTimeIn() == null) return 1;
            if (r2.getTimeIn() == null) return -1;
            return r1.getTimeIn().compareTo(r2.getTimeIn());
        });

        // Add filtered reservations to table
        for (PreReservationResponse reservation : filteredReservations) {
            Object[] rowData = new Object[6];
            rowData[0] = reservation.getCustomerName();
            rowData[1] = reservation.getMaDonDatPhong();
            rowData[2] = reservation.getRoomName();
            rowData[3] = reservation.getTimeIn() != null ? dateFormat.format(reservation.getTimeIn()) : "N/A";
            rowData[4] = reservation.getTimeOut() != null ? dateFormat.format(reservation.getTimeOut()) : "N/A";
            rowData[5] = reservation; // Store the reservation object for action buttons

            tableModel.addRow(rowData);
        }
    }

    private void handleCheckIn(PreReservationResponse reservation) {
        if (reservation == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận check-in cho khách " + reservation.getCustomerName()
                        + " vào phòng " + reservation.getRoomName() + "?",
                "Xác nhận check-in", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Gọi hàm checkin
            boolean success = checkinService.checkin(
                    reservation.getMaDonDatPhong(),
                    reservation.getRoomName()
            );

            // Nếu thành công thì thông báo
            if (success) {
                JOptionPane.showMessageDialog(PreReservationManagementPanel.this,
                        "Đã check-in thành công cho khách " + reservation.getCustomerName()
                                + " vào phòng " + reservation.getRoomName(),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // Làm mới UI
                RefreshManager.refreshAfterCheckIn();
            } else {
                // Lấy lỗi từ service nếu có
                String err = null;
                try { err = checkinService.getLastError(); } catch (Exception ignored) {}
                if (err == null || err.trim().isEmpty()) err = "Check-in thất bại. Vui lòng kiểm tra log hoặc thử lại.";
                JOptionPane.showMessageDialog(PreReservationManagementPanel.this,
                        err,
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(PreReservationManagementPanel.this,
                    "Có lỗi khi thực hiện check-in: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleChangeRoom(PreReservationResponse reservation) {
        if (reservation == null) return;

        // 1) Lấy số người cần (mặc định là 1)
        int neededPersons = 1;

        // 2) Tính khoảng thời gian cần phòng trống
        Timestamp now = new Timestamp(new Date().getTime());
        Timestamp timeIn = reservation.getTimeIn() != null ? new Timestamp(reservation.getTimeIn().getTime()) : null;

        Timestamp fromTime;
        if (timeIn == null) {
            // không có thời gian nhận phòng => bắt đầu từ hiện tại
            fromTime = now;
        } else {
            // nếu tg nhận phòng còn ở tương lai => bắt đầu từ tg nhận phòng, ngược lại bắt đầu từ now (khách đã checkin)
            fromTime = timeIn.after(now) ? timeIn : now;
        }
        Timestamp toTime = reservation.getTimeOut() != null ? new Timestamp(reservation.getTimeOut().getTime()) : null;

        // 3) Tìm danh sách phòng ứng viên bằng service
        List<BookingResponse> candidates;
        try {
            candidates = doiPhongService.timPhongPhuHopChoDoiPhong(reservation.getRoomId(), neededPersons, fromTime, toTime);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tìm phòng ứng viên: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (candidates == null || candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy phòng phù hợp (cùng loại, đủ sức chứa và trống trong khoảng thời gian yêu cầu).",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 4) Tạo BookingResponse đại diện cho đơn hiện tại để dialog hiển thị thông tin
        String roomType = (candidates.get(0).getRoomType() != null) ? candidates.get(0).getRoomType() : "";
        BookingResponse currentBooking = new BookingResponse(
                reservation.getRoomId(),
                reservation.getRoomName(),
                true,
                "OCCUPIED",
                roomType,
                String.valueOf(neededPersons),
                0.0,
                0.0,
                reservation.getCustomerName(),
                reservation.getMaDonDatPhong(),
                reservation.getTimeIn(),
                reservation.getTimeOut()
        );

        // 5) Mở dialog đổi phòng, truyền currentBooking và danh sách các phòng phù hợp
        DoiPhongDiaLog doiPhongDiaLog = new DoiPhongDiaLog(currentBooking, candidates);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Đổi phòng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(doiPhongDiaLog);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // 6) Callback: dialog sẽ thực hiện đổi phòng (gọi service). Panel chỉ cần đóng dialog và refresh khi dialog báo thành công.
        doiPhongDiaLog.setChangeRoomCallback(new DoiPhongDiaLog.ChangeRoomCallback() {
            @Override
            public void onChangeRoom(String oldRoomId, BookingResponse newRoom, boolean applyFee) {
                // Thông báo đã hoàn tất đổi phòng => đóng dialog và refresh UI
                dialog.dispose();
                // làm mới danh sách (refresh manager đã có sẵn)
                RefreshManager.refreshAfterCancelReservation();
            }

            @Override
            public void onCancel() {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    private void handleCancelReservation(PreReservationResponse reservation) {
        int result = JOptionPane.showConfirmDialog(this,
                "Xác nhận hủy đơn đặt phòng " + reservation.getMaDonDatPhong() + " Tại phòng:" + reservation.getRoomName() +" cho khách hàng: " + reservation.getCustomerName() + "?",
                "Hủy đơn đặt phòng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            System.out.println("Cancelling reservation ID: " + reservation.getMaDonDatPhong());
            boolean isSuccess = bookingService.cancelRoomReservation(reservation.getMaDonDatPhong(), reservation.getRoomId());
            if (!isSuccess) {
                JOptionPane.showMessageDialog(this,
                        "Hủy đơn đặt phòng thất bại. Vui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Đã hủy đơn đặt phòng " + reservation.getRoomName() + " thành công",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            RefreshManager.refreshAfterCancelReservation();
        }
    }

    public void refreshPanel() {
        loadReservationData();
        resetFilters();
    }

    // Custom cell renderer for action buttons
    private class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton btnCheckIn;
        private JButton btnChangeRoom;
        private JButton btnCancel;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            setOpaque(true);

            // Check-in button
            btnCheckIn = new JButton("Check-in");
            btnCheckIn.setFont(CustomUI.verySmallFont);
            btnCheckIn.setBackground(CustomUI.darkGreen);
            btnCheckIn.setForeground(Color.WHITE);
            btnCheckIn.setPreferredSize(new Dimension(120, 30));
            btnCheckIn.setFocusPainted(false);
            btnCheckIn.putClientProperty(FlatClientProperties.STYLE, " arc: 8");

            // Change room button
            btnChangeRoom = new JButton("Đổi phòng");
            btnChangeRoom.setFont(CustomUI.verySmallFont);
            btnChangeRoom.setBackground(CustomUI.lightBlue);
            btnChangeRoom.setForeground(Color.WHITE);
            btnChangeRoom.setPreferredSize(new Dimension(120, 30));
            btnChangeRoom.setFocusPainted(false);
            btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, " arc: 8");

            // Cancel button (small square with trash icon)
            ImageIcon trashIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/bin.png")));
            trashIcon = new ImageIcon(trashIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            btnCancel = new JButton(trashIcon);
            btnCancel.setBackground(CustomUI.red);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setPreferredSize(new Dimension(30, 30));
            btnCancel.setFocusPainted(false);
            btnCancel.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnCancel.setToolTipText("Hủy đơn");

            add(btnCheckIn);
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
        private JButton btnCheckIn;
        private JButton btnChangeRoom;
        private JButton btnCancel;
        private PreReservationResponse currentReservation;
        private int currentRow;

        public ActionButtonEditor() {
            super(new JCheckBox());

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));

            // Check-in button
            btnCheckIn = new JButton("Check-in");
            btnCheckIn.setFont(CustomUI.verySmallFont);
            btnCheckIn.setBackground(CustomUI.darkGreen);
            btnCheckIn.setForeground(Color.WHITE);
            btnCheckIn.setPreferredSize(new Dimension(120, 30));
            btnCheckIn.setFocusPainted(false);
            btnCheckIn.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnCheckIn.addActionListener(e -> {
                // Store the reservation reference before stopping editing
                PreReservationResponse reservationToProcess = currentReservation;

                // Stop editing immediately to prevent table access issues
                SwingUtilities.invokeLater(() -> {
                    fireEditingStopped();

                    // Process the action after editor is stopped
                    if (reservationToProcess != null) {
                        // Gọi handle checkin để xử lí
                        PreReservationManagementPanel.this.handleCheckIn(reservationToProcess);
                    }
                });
            });

            // Change room button
            btnChangeRoom = new JButton("Đổi phòng");
            btnChangeRoom.setFont(CustomUI.verySmallFont);
            btnChangeRoom.setBackground(CustomUI.lightBlue);
            btnChangeRoom.setForeground(Color.WHITE);
            btnChangeRoom.setPreferredSize(new Dimension(120, 30));
            btnChangeRoom.setFocusPainted(false);
            btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnChangeRoom.addActionListener(e -> {
                // Store the reservation reference before stopping editing
                PreReservationResponse reservationToProcess = currentReservation;

                // Stop editing immediately to prevent table access issues
                SwingUtilities.invokeLater(() -> {
                    fireEditingStopped();

                    // Process the action after editor is stopped
                    if (reservationToProcess != null) {
                        handleChangeRoom(reservationToProcess);
                    }
                });
            });

            // Cancel button (small square with trash icon)
            ImageIcon trashIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/error.png")));
            trashIcon = new ImageIcon(trashIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            btnCancel = new JButton(trashIcon);
            btnCancel.setBackground(CustomUI.red);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setPreferredSize(new Dimension(30, 30));
            btnCancel.setFocusPainted(false);
            btnCancel.putClientProperty(FlatClientProperties.STYLE, " arc: 8");
            btnCancel.setToolTipText("Hủy đơn");
            btnCancel.addActionListener(e -> {
                // Store the reservation reference before stopping editing
                PreReservationResponse reservationToProcess = currentReservation;

                // Stop editing immediately to prevent table access issues
                SwingUtilities.invokeLater(() -> {
                    fireEditingStopped();

                    // Process the action after editor is stopped
                    if (reservationToProcess != null) {
                        handleCancelReservation(reservationToProcess);
                    }
                });
            });

            panel.add(btnCheckIn);
            panel.add(btnChangeRoom);
            panel.add(btnCancel);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            try {
                currentReservation = (PreReservationResponse) value;
                currentRow = row;

                return panel;
            } catch (Exception e) {
                resetFilters();
            }
            return null;
        }
    }

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


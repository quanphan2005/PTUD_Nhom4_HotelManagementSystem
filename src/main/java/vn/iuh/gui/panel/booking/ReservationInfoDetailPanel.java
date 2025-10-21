package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.MovingHistoryResponse;
import vn.iuh.dto.response.ReservationDetailResponse;
import vn.iuh.dto.response.ReservationInfoDetailResponse;
import vn.iuh.dto.response.RoomUsageServiceResponse;
import vn.iuh.gui.base.CustomUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ReservationInfoDetailPanel extends JPanel {
    private ReservationInfoDetailResponse reservationInfo;
    private ReservationManagementPanel parentPanel;

    // Customer info components
    private JLabel lblCCCD;
    private JLabel lblCustomerName;
    private JLabel lblReservationCode;
    private JLabel lblStatus;
    private JLabel lblType;
    private JLabel lblAdvance;

    // Tables
    private JTable tblRoomDetails;
    private DefaultTableModel roomDetailsModel;
    private JTable tblServices;
    private DefaultTableModel servicesModel;
    private JTable tblMovingHistory;
    private DefaultTableModel movingHistoryModel;

    // Buttons
    private JButton btnBack;
    private JButton btnPrintInvoice;
    private JButton btnPrintReceipt;
    private JButton btnExtendRoom;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ReservationInfoDetailPanel(ReservationInfoDetailResponse reservationInfo, ReservationManagementPanel parentPanel) {
        this.reservationInfo = reservationInfo;
        this.parentPanel = parentPanel;

        setLayout(new BorderLayout());
        init();
        loadData();
    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTopPanel();
        createCustomerInfoPanel();
        createRoomDetailsTable();
        createServicesTable();
        createMovingHistoryTable();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CustomUI.blue);
        topPanel.setPreferredSize(new Dimension(0, 50));
        topPanel.setMinimumSize(new Dimension(0, 50));
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        topPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Title
        JLabel lblTitle = new JLabel("CHI TIẾT ĐƠN ĐẶT PHÒNG", SwingConstants.CENTER);
        lblTitle.setForeground(CustomUI.white);
        lblTitle.setFont(CustomUI.bigFont);

        // Add components
        topPanel.add(lblTitle, BorderLayout.CENTER);

        add(topPanel);
    }

    private void createCustomerInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize labels
        lblCCCD = new JLabel();
        lblCCCD.setFont(CustomUI.normalFont);

        lblCustomerName = new JLabel();
        lblCustomerName.setFont(CustomUI.normalFont);

        lblReservationCode = new JLabel();
        lblReservationCode.setFont(CustomUI.normalFont);

        lblStatus = new JLabel();
        lblStatus.setFont(CustomUI.normalFont);

        lblType = new JLabel();
        lblType.setFont(CustomUI.normalFont);

        lblAdvance = new JLabel();
        lblAdvance.setFont(CustomUI.normalFont);

        // Row 1: CCCD and Customer Name
        addInfoRow(infoPanel, gbc, 0, 0, "CCCD:", lblCCCD);
        addInfoRow(infoPanel, gbc, 0, 2, "Tên KH:", lblCustomerName);

        // Row 2: Reservation Code and Status
        addInfoRow(infoPanel, gbc, 1, 0, "Mã đơn:", lblReservationCode);
        addInfoRow(infoPanel, gbc, 1, 2, "Trạng thái:", lblStatus);

        // Row 3: Type and Advance
        addInfoRow(infoPanel, gbc, 2, 0, "Loại:", lblType);
        addInfoRow(infoPanel, gbc, 2, 2, "Đặt Trước:", lblAdvance);

        // Add action buttons vertically on the right side (starting from row 0)
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHEAST;

        btnPrintInvoice = new JButton("Xem hóa đơn đặt cọc");
        btnPrintInvoice.setFont(CustomUI.verySmallFont);
        btnPrintInvoice.setPreferredSize(new Dimension(220, 35));
        btnPrintInvoice.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        if (reservationInfo.isAdvance()) {
            btnPrintInvoice.setBackground(CustomUI.blue);
            btnPrintInvoice.setForeground(CustomUI.white);
            btnPrintInvoice.setFocusPainted(false);
            btnPrintInvoice.addActionListener(e -> handlePrintInvoice());
        } else {
            btnPrintInvoice.setBackground(CustomUI.gray);
            btnPrintInvoice.setForeground(CustomUI.white);
            btnPrintInvoice.setEnabled(false);
        }
        infoPanel.add(btnPrintInvoice, gbc);

        // Button 2 - Row 1
        gbc.gridy = 1;
        btnPrintReceipt = new JButton("Xem hóa đơn thanh toán");
        btnPrintReceipt.setFont(CustomUI.verySmallFont);
        btnPrintReceipt.setBackground(CustomUI.darkGreen);
        btnPrintReceipt.setForeground(CustomUI.white);
        btnPrintReceipt.setPreferredSize(new Dimension(220, 35));
        btnPrintReceipt.setFocusPainted(false);
        btnPrintReceipt.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnPrintReceipt.addActionListener(e -> handlePrintReceipt());
        infoPanel.add(btnPrintReceipt, gbc);

        // Button 3 - Row 2
        gbc.gridy = 2;
        btnExtendRoom = new JButton("Xem lịch sử đổi phòng");
        btnExtendRoom.setFont(CustomUI.verySmallFont);
        btnExtendRoom.setBackground(CustomUI.orange);
        btnExtendRoom.setForeground(CustomUI.white);
        btnExtendRoom.setPreferredSize(new Dimension(220, 35));
        btnExtendRoom.setFocusPainted(false);
        btnExtendRoom.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnExtendRoom.addActionListener(e -> handleExtendRoom());
        infoPanel.add(btnExtendRoom, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(wrapper);
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, int startCol, String labelText, JLabel valueLabel) {
        gbc.gridy = row;
        gbc.gridx = startCol;
        gbc.weightx = 0.0;
        gbc.gridheight = 1;

        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = startCol + 1;
        gbc.weightx = 0.5;
        panel.add(valueLabel, gbc);
    }

    private void createRoomDetailsTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Chi tiết phòng");

        // Create table
        String[] columnNames = {"Mã chi tiết", "Phòng", "Checkin", "Checkout", "Trạng thái", "Thao tác"};
        roomDetailsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only action column is editable
            }
        };

        tblRoomDetails = createStyledTable(roomDetailsModel);

        // Set dynamic column widths
        tblRoomDetails.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = tblRoomDetails.getWidth();
                TableColumnModel columnModel = tblRoomDetails.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.12)); // 12% - Mã chi tiết
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Phòng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.18)); // 18% - Checkin
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.18)); // 18% - Checkout
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Trạng thái
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.27)); // 27% - Thao tác
            }
        });

        // Set cell renderer and editor for action column
        tblRoomDetails.getColumn("Thao tác").setCellRenderer(new RoomActionButtonRenderer());
        tblRoomDetails.getColumn("Thao tác").setCellEditor(new RoomActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(tblRoomDetails);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Set initial height

        tablePanel.add(titlePanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel);
    }

    private void createServicesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Đơn gọi dịch vụ");

        // Create table
        String[] columnNames = {"Đơn gọi DV", "Phòng", "Dịch vụ", "Số lượng", "Được tặng"};
        servicesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblServices = createStyledTable(servicesModel);

        // Set dynamic column widths
        tblServices.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = tblServices.getWidth();
                TableColumnModel columnModel = tblServices.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Đơn gọi DV
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Phòng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.40)); // 40% - Dịch vụ
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Số lượng
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Được tặng
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblServices);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Set initial height

        tablePanel.add(titlePanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel);
    }

    private void createMovingHistoryTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Lịch sử ra - vào");

        // Create table
        String[] columnNames = {"Mã chi tiết", "Phòng", "Thời gian vào", "Thời gian ra", "Ghi chú"};
        movingHistoryModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblMovingHistory = createStyledTable(movingHistoryModel);

        // Set dynamic column widths
        tblMovingHistory.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = tblMovingHistory.getWidth();
                TableColumnModel columnModel = tblMovingHistory.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.12)); // 12% - Mã chi tiết
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Phòng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.26)); // 26% - Thời gian vào
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.26)); // 26% - Thời gian ra
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.26)); // 26% - Ghi chú
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblMovingHistory);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Set initial height

        tablePanel.add(titlePanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel);
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CustomUI.ROW_ODD : CustomUI.ROW_EVEN);
                } else {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                }
                return c;
            }
        };

        table.setFont(CustomUI.TABLE_FONT);
        table.setRowHeight(40);
        table.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        table.setGridColor(CustomUI.tableBorder);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Enhanced header styling
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 40));
        table.getTableHeader().setFont(CustomUI.HEADER_FONT);
        table.getTableHeader().setBackground(CustomUI.blue);
        table.getTableHeader().setForeground(CustomUI.white);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        return table;
    }

    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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

    private void loadData() {
        if (reservationInfo == null) return;

        // Load customer info
        lblCCCD.setText(reservationInfo.getCCCD());
        lblCustomerName.setText(reservationInfo.getCustomerName());
        lblReservationCode.setText(reservationInfo.getMaDonDatPhong());
        lblStatus.setText(reservationInfo.getStatus());

        // Determine type based on number of details
        String type = (reservationInfo.getDetails() != null && reservationInfo.getDetails().size() > 1)
            ? "Đặt nhiều" : "Đặt đơn";
        lblType.setText(type);

        lblAdvance.setText(reservationInfo.isAdvance() ? "Có" : "Không");

        // Load room details
        loadRoomDetails();

        // Load services
        loadServices();

        // Load moving history
        loadMovingHistory();
    }

    private void loadRoomDetails() {
        roomDetailsModel.setRowCount(0);

        if (reservationInfo.getDetails() == null) return;

        for (ReservationDetailResponse detail : reservationInfo.getDetails()) {
            Object[] rowData = new Object[6];
            rowData[0] = detail.getRoomId();
            rowData[1] = detail.getRoomName();
            rowData[2] = detail.getTimeIn() != null ? dateFormat.format(detail.getTimeIn()) : "N/A";
            rowData[3] = detail.getTimeOut() != null ? dateFormat.format(detail.getTimeOut()) : "N/A";
            rowData[4] = reservationInfo.getStatus();
            rowData[5] = detail; // Store detail object for actions

            roomDetailsModel.addRow(rowData);
        }
    }

    private void loadServices() {
        servicesModel.setRowCount(0);

        if (reservationInfo.getServices() == null) return;

        for (RoomUsageServiceResponse service : reservationInfo.getServices()) {
            Object[] rowData = new Object[5];
            rowData[0] = service.getRoomId();
            rowData[1] = service.getRoomName();
            rowData[2] = service.getServiceName();
            rowData[3] = service.getQuantity();
            rowData[4] = service.isGifted() ? "✓" : "-";

            servicesModel.addRow(rowData);
        }
    }

    private void loadMovingHistory() {
        movingHistoryModel.setRowCount(0);

        if (reservationInfo.getMovingHistories() == null) return;

        for (MovingHistoryResponse history : reservationInfo.getMovingHistories()) {
            Object[] rowData = new Object[5];
            rowData[0] = history.getRoomId();
            rowData[1] = history.getRoomName();
            rowData[2] = history.getTimeIn() != null ? dateFormat.format(history.getTimeIn()) : "N/A";
            rowData[3] = history.getTimeOut() != null ? dateFormat.format(history.getTimeOut()) : "N/A";

            // Determine note based on timeIn and timeOut
            String note;
            if (history.getTimeIn() == null) {
                note = "checkout";
            } else if (history.getTimeOut() == null) {
                note = "checkin";
            } else {
                note = "-";
            }
            rowData[4] = note;

            movingHistoryModel.addRow(rowData);
        }
    }

    // Action handlers
    private void handleBack() {
        // Navigate back to ReservationManagementPanel
        Container parent = getParent();
        if (parent != null) {
            CardLayout layout = (CardLayout) parent.getLayout();
            layout.show(parent, "reservationManagement");
        }
    }

    private void handlePrintInvoice() {
        JOptionPane.showMessageDialog(this,
            "Chức năng in hóa đơn đang được phát triển",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handlePrintReceipt() {
        JOptionPane.showMessageDialog(this,
            "Chức năng in phiếu thu đang được phát triển",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleExtendRoom() {
        JOptionPane.showMessageDialog(this,
            "Chức năng gia hạn phòng đang được phát triển",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCheckin(ReservationDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
            "Chức năng check-in đang được phát triển cho phòng: " + detail.getRoomName(),
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleChangeRoom(ReservationDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
            "Chức năng đổi phòng đang được phát triển cho phòng: " + detail.getRoomName(),
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCancelRoom(ReservationDetailResponse detail) {
        int result = JOptionPane.showConfirmDialog(this,
            "Xác nhận hủy phòng " + detail.getRoomName() + "?",
            "Hủy phòng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "Chức năng hủy phòng đang được phát triển",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Custom renderer for room action buttons
    private class RoomActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton btnCheckIn;
        private JButton btnChangeRoom;
        private JButton btnCancel;

        public RoomActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            setOpaque(true);

            // Check-in button
            btnCheckIn = new JButton("Check-in");
            btnCheckIn.setFont(CustomUI.verySmallFont);
            btnCheckIn.setBackground(CustomUI.darkGreen);
            btnCheckIn.setForeground(Color.WHITE);
            btnCheckIn.setPreferredSize(new Dimension(100, 30));
            btnCheckIn.setFocusPainted(false);
            btnCheckIn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

            // Change room button
            btnChangeRoom = new JButton("Đổi phòng");
            btnChangeRoom.setFont(CustomUI.verySmallFont);
            btnChangeRoom.setBackground(CustomUI.lightBlue);
            btnChangeRoom.setForeground(Color.WHITE);
            btnChangeRoom.setPreferredSize(new Dimension(100, 30));
            btnChangeRoom.setFocusPainted(false);
            btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, "arc: 8");

            // Cancel button (small square with trash icon)
            ImageIcon trashIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/bin.png")));
            trashIcon = new ImageIcon(trashIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            btnCancel = new JButton(trashIcon);
            btnCancel.setBackground(CustomUI.red);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setPreferredSize(new Dimension(30, 30));
            btnCancel.setFocusPainted(false);
            btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
            btnCancel.setToolTipText("Hủy phòng");

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

            // Get status from the row
            String status = (String) table.getValueAt(row, 4);

            // Show/hide action column based on status
            boolean isFinished = status.equals("Trả phòng") || status.equals("Hủy Phòng");
            setVisible(!isFinished);

            if (!isFinished) {
                // Enable/disable buttons based on status
                boolean isWaitingCheckin = status.equals(RoomStatus.ROOM_BOOKED_STATUS.getStatus());

                if (isWaitingCheckin) {
                    btnCheckIn.setBackground(CustomUI.darkGreen);
                    btnCheckIn.setEnabled(true);
                    btnCancel.setBackground(CustomUI.red);
                    btnCancel.setEnabled(true);
                } else {
                    btnCheckIn.setBackground(CustomUI.gray);
                    btnCheckIn.setEnabled(false);
                    btnCancel.setBackground(CustomUI.gray);
                    btnCancel.setEnabled(false);
                }

                // Change room button is always enabled
                btnChangeRoom.setBackground(CustomUI.lightBlue);
                btnChangeRoom.setEnabled(true);
            }

            return this;
        }
    }

    // Custom editor for room action buttons
    private class RoomActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnCheckIn;
        private JButton btnChangeRoom;
        private JButton btnCancel;
        private ReservationDetailResponse currentDetail;

        public RoomActionButtonEditor() {
            super(new JCheckBox());

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));

            // Check-in button
            btnCheckIn = new JButton("Check-in");
            btnCheckIn.setFont(CustomUI.verySmallFont);
            btnCheckIn.setBackground(CustomUI.darkGreen);
            btnCheckIn.setForeground(Color.WHITE);
            btnCheckIn.setPreferredSize(new Dimension(100, 30));
            btnCheckIn.setFocusPainted(false);
            btnCheckIn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
            btnCheckIn.addActionListener(e -> {
                handleCheckin(currentDetail);
                fireEditingStopped();
            });

            // Change room button
            btnChangeRoom = new JButton("Đổi phòng");
            btnChangeRoom.setFont(CustomUI.verySmallFont);
            btnChangeRoom.setBackground(CustomUI.lightBlue);
            btnChangeRoom.setForeground(Color.WHITE);
            btnChangeRoom.setPreferredSize(new Dimension(100, 30));
            btnChangeRoom.setFocusPainted(false);
            btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
            btnChangeRoom.addActionListener(e -> {
                handleChangeRoom(currentDetail);
                fireEditingStopped();
            });

            // Cancel button (small square with trash icon)
            ImageIcon trashIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/bin.png")));
            trashIcon = new ImageIcon(trashIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            btnCancel = new JButton(trashIcon);
            btnCancel.setBackground(CustomUI.red);
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setPreferredSize(new Dimension(30, 30));
            btnCancel.setFocusPainted(false);
            btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
            btnCancel.setToolTipText("Hủy phòng");
            btnCancel.addActionListener(e -> {
                handleCancelRoom(currentDetail);
                fireEditingStopped();
            });

            panel.add(btnCheckIn);
            panel.add(btnChangeRoom);
            panel.add(btnCancel);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentDetail = (ReservationDetailResponse) value;

            // Get status from the row
            String status = (String) table.getValueAt(row, 4);

            // Show/hide action column based on status
            boolean isFinished = status.equals("Trả phòng") || status.equals("Hủy Phòng");
            panel.setVisible(!isFinished);

            if (!isFinished) {
                // Enable/disable buttons based on status
                boolean isWaitingCheckin = status.equals(RoomStatus.ROOM_BOOKED_STATUS.getStatus());

                if (isWaitingCheckin) {
                    btnCheckIn.setBackground(CustomUI.darkGreen);
                    btnCheckIn.setEnabled(true);
                    btnCancel.setBackground(CustomUI.red);
                    btnCancel.setEnabled(true);
                } else {
                    btnCheckIn.setBackground(CustomUI.gray);
                    btnCheckIn.setEnabled(false);
                    btnCancel.setBackground(CustomUI.gray);
                    btnCancel.setEnabled(false);
                }

                // Change room button is always enabled
                btnChangeRoom.setBackground(CustomUI.lightBlue);
                btnChangeRoom.setEnabled(true);
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentDetail;
        }
    }

    private JPanel createCollapsibleTitlePanel(String title) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CustomUI.COLLAPSIBLE_BG);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(CustomUI.normalFont);

        // Add arrow icon to indicate collapsible state
        JLabel lblArrow = new JLabel("▼");
        lblArrow.setFont(new Font("Arial", Font.BOLD, 12));
        lblArrow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(lblArrow);
        leftPanel.add(lblTitle);

        titlePanel.add(leftPanel, BorderLayout.WEST);
        titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Make the title panel clickable to toggle collapse/expand
        titlePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            boolean isCollapsed = false;

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                isCollapsed = !isCollapsed;
                lblArrow.setText(isCollapsed ? "►" : "▼");

                // Find the parent panel and toggle its scroll pane visibility
                Container parent = titlePanel.getParent();
                if (parent != null) {
                    Component[] components = parent.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JScrollPane) {
                            comp.setVisible(!isCollapsed);
                            parent.revalidate();
                            parent.repaint();
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                titlePanel.setBackground(CustomUI.COLLAPSIBLE_HOVER);
                leftPanel.setBackground(CustomUI.COLLAPSIBLE_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                titlePanel.setBackground(CustomUI.COLLAPSIBLE_BG);
                leftPanel.setBackground(CustomUI.COLLAPSIBLE_BG);
            }
        });

        return titlePanel;
    }
}

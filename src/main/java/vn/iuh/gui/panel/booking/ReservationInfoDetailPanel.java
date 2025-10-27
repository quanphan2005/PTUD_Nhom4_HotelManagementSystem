package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.ReservationStatus;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.MovingHistoryResponse;
import vn.iuh.dto.response.ReservationDetailResponse;
import vn.iuh.dto.response.ReservationInfoDetailResponse;
import vn.iuh.dto.response.RoomUsageServiceResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.util.PriceFormat;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
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
    private JButton btnPrintInvoice;
    private JButton btnPrintReceipt;
    private JButton btnCheckoutAndPrintReceipt;
    private JButton btnTranferRoomHistory;

    private DecimalFormat priceFormatter = PriceFormat.getPriceFormatter();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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

        infoPanel.setMinimumSize(new Dimension(0, 150));
        infoPanel.setPreferredSize(new Dimension(0, 150));
        infoPanel.setMaximumSize(new Dimension(0, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize labels
        lblCCCD = new JLabel();
        lblCCCD.setFont(CustomUI.smallFont);

        lblCustomerName = new JLabel();
        lblCustomerName.setFont(CustomUI.smallFont);

        lblReservationCode = new JLabel();
        lblReservationCode.setFont(CustomUI.smallFont);

        lblStatus = new JLabel();
        lblStatus.setFont(CustomUI.smallFont);

        lblType = new JLabel();
        lblType.setFont(CustomUI.smallFont);

        lblAdvance = new JLabel();
        lblAdvance.setFont(CustomUI.smallFont);

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

        btnPrintInvoice = createPrintInvoiceBtn();
        infoPanel.add(btnPrintInvoice, gbc);

        // Button 2 - Row 1
        gbc.gridy = 1;
        if (isEndedStatus(reservationInfo.getStatus())) {
            btnPrintReceipt = createPrintReceiptBtn();
            infoPanel.add(btnPrintReceipt, gbc);
        } else {
            btnCheckoutAndPrintReceipt = createCheckoutAndPrintReceiptBtn();
            infoPanel.add(btnCheckoutAndPrintReceipt, gbc);
        }

        // Button 3 - Row 2
        gbc.gridy = 2;
        btnTranferRoomHistory = new JButton("Xem lịch sử đổi phòng");
        btnTranferRoomHistory.setFont(CustomUI.verySmallFont);
        btnTranferRoomHistory.setBackground(CustomUI.orange);
        btnTranferRoomHistory.setForeground(CustomUI.white);
        btnTranferRoomHistory.setPreferredSize(new Dimension(220, 35));
        btnTranferRoomHistory.setFocusPainted(false);
        btnTranferRoomHistory.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnTranferRoomHistory.addActionListener(e -> handleCheckTranferRoomHistory(reservationInfo));
        infoPanel.add(btnTranferRoomHistory, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());

        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        wrapper.add(infoPanel, BorderLayout.CENTER);

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

        JLabel valueLabelCopy = valueLabel;
        label.setFont(CustomUI.smallFont);
        panel.add(valueLabelCopy, gbc);
    }

    private boolean isEndedStatus(String status) {
        return Objects.equals(status, ReservationStatus.COMPLETED.getStatus())
            || Objects.equals(status, ReservationStatus.CANCELLED.getStatus());
    }

    private boolean canCheckout(String status) {
        return Objects.equals(status, ReservationStatus.USING.getStatus())
        || Objects.equals(status, ReservationStatus.CHECKOUT_LATE.getStatus());
    }

    private void createRoomDetailsTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Chi tiết phòng");


        // Create table base on reservation status
        String[] columnNames;
        if (Objects.equals(reservationInfo.getStatus(), ReservationStatus.COMPLETED.getStatus())
            || Objects.equals(reservationInfo.getStatus(), ReservationStatus.CANCELLED.getStatus())) {
            columnNames = new String[]{"Mã chi tiết", "Phòng", "Checkin", "Checkout", "Trạng thái"};
        } else {
            columnNames = new String[]{"Mã chi tiết", "Phòng", "Checkin", "Checkout", "Trạng thái", "Thao tác"};
        }

        roomDetailsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only action column is editable
            }
        };

        tblRoomDetails = createStyledTable(roomDetailsModel);

        // Set dynamic column widths
        if (Objects.equals(reservationInfo.getStatus(), ReservationStatus.COMPLETED.getStatus())
            || Objects.equals(reservationInfo.getStatus(), ReservationStatus.CANCELLED.getStatus())) {
            tblRoomDetails.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int tableWidth = tblRoomDetails.getWidth();
                    TableColumnModel columnModel = tblRoomDetails.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.20)); // 12% - Mã chi tiết
                    columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.20)); // 10% - Phòng
                    columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.20)); // 18% - Checkin
                    columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.20)); // 18% - Checkout
                    columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.20)); // 15% - Trạng thái
                }
            });
        } else {
            tblRoomDetails.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int tableWidth = tblRoomDetails.getWidth();
                    TableColumnModel columnModel = tblRoomDetails.getColumnModel();
                    columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.12)); // 12% - Mã chi tiết
                    columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Phòng
                    columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.15)); // 18% - Checkin
                    columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.15)); // 18% - Checkout
                    columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Trạng thái
                    columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.33)); // 33% - Thao tác
                }
            });

            // Set cell renderer and editor for action column
            tblRoomDetails.getColumn("Thao tác").setCellRenderer(new RoomActionButtonRenderer());
            tblRoomDetails.getColumn("Thao tác").setCellEditor(new RoomActionButtonEditor());
        }

        JScrollPane scrollPane = new JScrollPane(tblRoomDetails);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Set initial height

        tablePanel.add(titlePanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel);
    }

    // Action Buttons for reservation
    private JButton createPrintInvoiceBtn() {
        // Print Invoice button
        JButton btnPrintInvoice = new JButton("Xem hóa đơn đặt cọc");
        btnPrintInvoice.setFont(CustomUI.verySmallFont);
        btnPrintInvoice.setPreferredSize(new Dimension(220, 35));
        btnPrintInvoice.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        if (reservationInfo.isAdvance()) {
            btnPrintInvoice.setBackground(CustomUI.blue);
            btnPrintInvoice.setForeground(CustomUI.white);
            btnPrintInvoice.setFocusPainted(false);
            btnPrintInvoice.addActionListener(e -> handlePrintInvoice(reservationInfo));
        } else {
            btnPrintInvoice.setBackground(CustomUI.gray);
            btnPrintInvoice.setForeground(CustomUI.white);
            btnPrintInvoice.setEnabled(false);
        }

        return btnPrintInvoice;
    }

    private JButton createPrintReceiptBtn() {
        // Print Receipt button
        JButton btnPrintReceipt = new JButton("Xem hóa đơn thanh toán");
        btnPrintReceipt.setFont(CustomUI.verySmallFont);
        btnPrintReceipt.setBackground(CustomUI.darkGreen);
        btnPrintReceipt.setForeground(CustomUI.white);
        btnPrintReceipt.setPreferredSize(new Dimension(220, 35));
        btnPrintReceipt.setFocusPainted(false);
        btnPrintReceipt.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnPrintReceipt.addActionListener(e -> handlePrintReceipt(reservationInfo));

        return btnPrintReceipt;
    }

    private JButton createCheckoutAndPrintReceiptBtn() {
        // Checkout button
        btnCheckoutAndPrintReceipt = new JButton("Thanh toán & In hóa đơn");
        btnCheckoutAndPrintReceipt.setFont(CustomUI.verySmallFont);
        btnCheckoutAndPrintReceipt.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        if (canCheckout(reservationInfo.getStatus())) {
            btnCheckoutAndPrintReceipt.setBackground(CustomUI.darkGreen);
            btnCheckoutAndPrintReceipt.setForeground(CustomUI.white);
            btnCheckoutAndPrintReceipt.setFocusPainted(false);
            btnCheckoutAndPrintReceipt.setPreferredSize(new Dimension(220, 35));
            btnCheckoutAndPrintReceipt.addActionListener(e -> handleCheckoutAndPrintReceipt(reservationInfo));
        } else {
            btnCheckoutAndPrintReceipt.setBackground(CustomUI.gray);
            btnCheckoutAndPrintReceipt.setForeground(CustomUI.white);
            btnCheckoutAndPrintReceipt.setEnabled(false);
            btnCheckoutAndPrintReceipt.setPreferredSize(new Dimension(220, 35));
            btnCheckoutAndPrintReceipt.setToolTipText("Không thể thanh toán khi có phòng chưa check-in hoặc đang sử dụng dịch vụ");
        }

        return btnCheckoutAndPrintReceipt;
    }

    // Action Buttons for room details
    private JButton createOrderServiceBtn() {
        // Order service button
        JButton btnOrderService = new JButton("Gọi DV");
        btnOrderService.setFont(CustomUI.verySmallFont);
        btnOrderService.setBackground(CustomUI.darkGreen);
        btnOrderService.setForeground(Color.WHITE);
        btnOrderService.setPreferredSize(new Dimension(100, 30));
        btnOrderService.setFocusPainted(false);
        btnOrderService.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnOrderService.addActionListener(e -> handleOrderService(reservationInfo));

        return btnOrderService;
    }

    private JButton createCheckinBtn() {
        // Check-in button
        JButton btnCheckIn = new JButton("Checkin");
        btnCheckIn.setFont(CustomUI.verySmallFont);
        btnCheckIn.setBackground(CustomUI.darkGreen);
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setPreferredSize(new Dimension(100, 30));
        btnCheckIn.setFocusPainted(false);
        btnCheckIn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnCheckIn.addActionListener(e -> handleCheckin(reservationInfo));

        return btnCheckIn;
    }

    private JButton createChangeRoomBtn() {
        // Change room button
        JButton btnChangeRoom = new JButton("Đổi phòng");
        btnChangeRoom.setFont(CustomUI.verySmallFont);
        btnChangeRoom.setBackground(CustomUI.lightBlue);
        btnChangeRoom.setForeground(Color.WHITE);
        btnChangeRoom.setPreferredSize(new Dimension(100, 30));
        btnChangeRoom.setFocusPainted(false);
        btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnChangeRoom.addActionListener(e -> handleChangeRoom(reservationInfo));

        return btnChangeRoom;
    }

    private JButton createExtendTimeBtn() {
        // Change Extend time button
        JButton btnExtendTime = new JButton("Gia hạn");
        btnExtendTime.setFont(CustomUI.verySmallFont);
        btnExtendTime.setBackground(CustomUI.orange);
        btnExtendTime.setForeground(Color.WHITE);
        btnExtendTime.setPreferredSize(new Dimension(100, 30));
        btnExtendTime.setFocusPainted(false);
        btnExtendTime.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnExtendTime.addActionListener(e -> handleExtendTime(reservationInfo));

        return btnExtendTime;
    }

    private JButton createCancelBtn() {
        // Cancel button (small square with trash icon)
        JButton btnCancel = new JButton("Hủy đơn");
        btnCancel.setFont(CustomUI.verySmallFont);
        btnCancel.setBackground(CustomUI.red);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(100, 30));
        btnCancel.setFocusPainted(false);
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnCancel.setToolTipText("Hủy phòng");
        btnCancel.addActionListener(e -> handleCancelRoom(reservationInfo));

        return btnCancel;
    }

    private void createServicesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Đơn gọi dịch vụ");

        // Create table
        String[] columnNames = {"Đơn DV", "Phòng", "Dịch vụ", "Số lượng", "Đơn giá", "Được tặng", "Thành tiền"};
        servicesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblServices = createStyledTable(servicesModel);

        // Set dynamic column widths
        tblServices.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int tableWidth = tblServices.getWidth();
                TableColumnModel columnModel = tblServices.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.10)); // 15% - Đơn gọi DV
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Phòng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Dịch vụ
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Số lượng
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Đơn giá
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.10)); // 15% - Được tặng
                columnModel.getColumn(6).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Thành tiền
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
        tblMovingHistory.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
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
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
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

    // Load data into components
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
            rowData[0] = detail.getReservationDetailId();
            rowData[1] = detail.getRoomName();
            rowData[2] = detail.getTimeIn() != null ? dateFormat.format(detail.getTimeIn()) : "N/A";
            rowData[3] = detail.getTimeOut() != null ? dateFormat.format(detail.getTimeOut()) : "N/A";
            rowData[4] = detail.getStatus();
            rowData[5] = detail; // Store detail object for actions

            roomDetailsModel.addRow(rowData);
        }
    }

    private void loadServices() {
        servicesModel.setRowCount(0);

        if (reservationInfo.getServices() == null) return;

        for (RoomUsageServiceResponse service : reservationInfo.getServices()) {

            Object[] rowData = new Object[7];
            rowData[0] = service.getRoomUsageServiceId();
            rowData[1] = service.getRoomName();
            rowData[2] = service.getServiceName();
            rowData[3] = service.getQuantity();
            rowData[4] = priceFormatter.format(service.getPrice()) + " VND";
            rowData[5] = service.isGifted() ? "Có" : "Không";
            rowData[6] = priceFormatter.format(service.getPrice()) + " VND";

            servicesModel.addRow(rowData);
        }
    }

    private void loadMovingHistory() {
        movingHistoryModel.setRowCount(0);

        if (reservationInfo.getMovingHistories() == null) return;

        for (MovingHistoryResponse history : reservationInfo.getMovingHistories()) {
            Object[] rowData = new Object[5];
            rowData[0] = history.getReservationDetailId();
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

    public void attachButtons(JPanel panel, String status) {
        System.out.println("Visualizing buttons for status: " + status);
        panel.removeAll();

        if (status == null) {
            panel.revalidate();
            panel.repaint();
            return;
        }

        JButton btnOrderService = createOrderServiceBtn();
        JButton btnCheckIn = createCheckinBtn();
        JButton btnChangeRoom = createChangeRoomBtn();
        JButton btnExtendTime = createExtendTimeBtn();
        JButton btnCancel = createCancelBtn();

        switch (ReservationStatus.fromStatus(status)) {
            case CHECKED_IN:
                panel.add(btnCheckIn);
                panel.add(btnChangeRoom);
                panel.add(btnCancel);
                break;
            case CHECKING:
            case USING:
                panel.add(btnOrderService);
                panel.add(btnExtendTime);
                panel.add(btnChangeRoom);
                break;
            case CHECKOUT_LATE:
            case null:
            default:
                break;
        }

        panel.revalidate();
        panel.repaint();
    }

    // Custom renderer for room action buttons
    private class RoomActionButtonRenderer extends JPanel implements TableCellRenderer {

        public RoomActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            removeAll(); // Clear previous buttons
            ReservationDetailResponse detail = (ReservationDetailResponse) table.getValueAt(row, 5);
            attachButtons(this, detail.getStatus()); // Re-attach buttons for current state

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            revalidate();
            repaint();
            return this;
        }
    }

    // Custom editor for room action buttons
    private class RoomActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private ReservationDetailResponse currentDetail;

        public RoomActionButtonEditor() {
            super(new JCheckBox());
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            setClickCountToStart(1); // Make it respond to single click
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentDetail = (ReservationDetailResponse) value;

            panel.removeAll();
            ReservationDetailResponse detail = (ReservationDetailResponse) table.getValueAt(row, 5);
            attachButtons(panel, detail.getStatus()); // Populate panel with buttons

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
        titlePanel.addMouseListener(new MouseAdapter() {
            boolean isCollapsed = false;

            @Override
            public void mouseClicked(MouseEvent e) {
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
            public void mouseEntered(MouseEvent e) {
                titlePanel.setBackground(CustomUI.COLLAPSIBLE_HOVER);
                leftPanel.setBackground(CustomUI.COLLAPSIBLE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                titlePanel.setBackground(CustomUI.COLLAPSIBLE_BG);
                leftPanel.setBackground(CustomUI.COLLAPSIBLE_BG);
            }
        });

        return titlePanel;
    }

    // Top Action handlers
    private void handlePrintInvoice(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng in hóa đơn đặt cọc đang được phát triển",
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handlePrintReceipt(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng in hóa đơn thanh toán đang được phát triển",
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCheckoutAndPrintReceipt(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng checkout và in hóa đơn đang được phát triển cho phòng: " + detail.getCustomerName(),
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCheckTranferRoomHistory(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng xem lịch sử đổi phòng đang được phát triển",
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // Action handlers for reservation details
    private void handleOrderService(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng gọi dịch vụ đang được phát triển cho phòng: " + detail.getCustomerName(),
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCheckin(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng check-in đang được phát triển cho phòng: " + detail.getCustomerName(),
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleExtendTime(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng gia hạn phòng đang được phát triển",
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleChangeRoom(ReservationInfoDetailResponse detail) {
        JOptionPane.showMessageDialog(this,
                                      "Chức năng đổi phòng đang được phát triển cho phòng: " + detail.getCustomerName(),
                                      "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleCancelRoom(ReservationInfoDetailResponse detail) {
        int result = JOptionPane.showConfirmDialog(this,
                                                   "Xác nhận hủy phòng " + detail.getCustomerName() + "?",
                                                   "Hủy phòng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                                          "Chức năng hủy phòng đang được phát triển",
                                          "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

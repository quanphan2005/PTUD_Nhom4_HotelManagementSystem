package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.PanelName;
import vn.iuh.constraint.ReservationStatus;
import vn.iuh.dto.repository.BookThemGioInfo;
import vn.iuh.dto.response.*;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.dialog.BookThemGioDialog;
import vn.iuh.gui.dialog.DepositInvoiceDialog;
import vn.iuh.gui.dialog.LichSuDoiPhongDialog;
import vn.iuh.gui.panel.DoiPhongDiaLog;
import vn.iuh.gui.dialog.InvoiceDialog2;
import vn.iuh.gui.panel.DoiPhongDiaLog;
import vn.iuh.service.BookingService;
import vn.iuh.service.CheckinService;
import vn.iuh.service.impl.*;
import vn.iuh.util.PriceFormat;
import vn.iuh.util.RefreshManager;
import vn.iuh.util.RefreshManager;

import java.sql.Timestamp;
import java.util.List;

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
import java.text.SimpleDateFormat;
import java.util.Objects;

import static vn.iuh.constraint.PanelName.SERVICE_ORDER;

public class ReservationInfoDetailPanel extends JPanel {
    private ReservationInfoDetailResponse reservationInfo;
    private ReservationManagementPanel parentPanel;

    private BookingService bookingService;
    private CheckinService checkinService;
    private CheckOutServiceImpl checkOutService;

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
    private JButton btnExtendTime;

    private DecimalFormat priceFormatter = PriceFormat.getPriceFormatter();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private boolean isDialog;

    public ReservationInfoDetailPanel(ReservationInfoDetailResponse reservationInfo, ReservationManagementPanel parentPanel) {
        this.reservationInfo = reservationInfo;
        this.parentPanel = parentPanel;

        this.bookingService = new BookingServiceImpl();
        this.checkinService = new CheckinServiceImpl();
        this.checkOutService = new CheckOutServiceImpl();

        setLayout(new BorderLayout());
        init();
        loadData();
    }

    public ReservationInfoDetailPanel(ReservationInfoDetailResponse reservationInfo, boolean isDialog) {
        this.reservationInfo = reservationInfo;

        this.bookingService = new BookingServiceImpl();
        this.checkinService = new CheckinServiceImpl();
        this.checkOutService = new CheckOutServiceImpl();
        this.isDialog = isDialog;

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
        infoPanel.setBackground(CustomUI.white);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        infoPanel.setMinimumSize(new Dimension(0, 150));
        infoPanel.setPreferredSize(new Dimension(0, 150));
        infoPanel.setMaximumSize(new Dimension(0, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
//        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;

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
        gbc.ipadx = 8;
        gbc.ipady = 5;

        btnTranferRoomHistory = createTransferRoomHistoryBtn();
        infoPanel.add(btnTranferRoomHistory, gbc);

        gbc.gridy = 1;
        btnExtendTime = createExtendTimeBtn();
        infoPanel.add(btnExtendTime, gbc);

        // Button 2 - Row 1
        gbc.gridx = 5;

        gbc.gridy = 0;
        btnPrintInvoice = createPrintInvoiceBtn();
        infoPanel.add(btnPrintInvoice, gbc);

        gbc.gridy = 1;
        if (isEndedStatus(reservationInfo.getStatus())) {
            btnPrintReceipt = createPrintReceiptBtn();
            infoPanel.add(btnPrintReceipt, gbc);
        } else {
            btnCheckoutAndPrintReceipt = createCheckoutAndPrintReceiptBtn();
            infoPanel.add(btnCheckoutAndPrintReceipt, gbc);
        }

        // Button 3 - Row 2
//        gbc.gridy = 2

        JPanel wrapper = new JPanel(new BorderLayout());

        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        wrapper.add(infoPanel, BorderLayout.CENTER);

        add(wrapper);
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, int startCol, String labelText, JLabel valueLabel) {
        gbc.gridy = row;
        gbc.gridx = startCol;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;

        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = startCol + 1;

        JLabel valueLabelCopy = valueLabel;
        valueLabelCopy.setFont(CustomUI.smallFont);
        panel.add(valueLabelCopy, gbc);
    }

    private boolean isEndedStatus(String status) {
        return Objects.equals(status, ReservationStatus.COMPLETED.getStatus())
            || Objects.equals(status, ReservationStatus.CANCELLED.getStatus());
    }

    public boolean canCheckChangeRoomHistory(String status) {
        return Objects.equals(status, ReservationStatus.USING.getStatus())
               || Objects.equals(status, ReservationStatus.CHECKING.getStatus())
               || Objects.equals(status, ReservationStatus.CHECKOUT_LATE.getStatus())
                || Objects.equals(status, ReservationStatus.COMPLETED.getStatus());
    }

    private boolean canExtendTime(String status) {
        return Objects.equals(status, ReservationStatus.CHECKED_IN.getStatus())
               || Objects.equals(status, ReservationStatus.CHECKING.getStatus())
               || Objects.equals(status, ReservationStatus.USING.getStatus());
    }

    private boolean canCheckout(String status) {
        return Objects.equals(status, ReservationStatus.USING.getStatus())
        || Objects.equals(status, ReservationStatus.CHECKOUT_LATE.getStatus())
                || Objects.equals(status, ReservationStatus.CHECKING.getStatus());
    }

    private void createRoomDetailsTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CustomUI.white);
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
                    columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.14)); // 18% - Checkin
                    columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.14)); // 18% - Checkout
                    columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.14)); // 15% - Trạng thái
                    columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.38)); // 33% - Thao tác
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
        btnPrintInvoice.setFont(CustomUI.smallFont);
        btnPrintInvoice.setPreferredSize(new Dimension(220, 35));
        btnPrintInvoice.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnPrintInvoice.setFocusPainted(false);

        if (reservationInfo.isAdvance()) {
            btnPrintInvoice.setBackground(CustomUI.blue);
            btnPrintInvoice.setForeground(CustomUI.white);
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
        btnPrintReceipt.setFont(CustomUI.smallFont);
        btnPrintReceipt.setBackground(CustomUI.darkGreen);
        btnPrintReceipt.setForeground(CustomUI.white);
        btnPrintReceipt.setPreferredSize(new Dimension(220, 35));
        btnPrintReceipt.setFocusPainted(false);
        btnPrintReceipt.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        btnPrintReceipt.addActionListener(e -> handlePrintReceipt(reservationInfo));

        return btnPrintReceipt;
    }

    private JButton createTransferRoomHistoryBtn() {
        JButton btnTransferRoomHistoryBtn = new JButton("Xem lịch sử đổi phòng");
        btnTransferRoomHistoryBtn.setFont(CustomUI.smallFont);
        btnTransferRoomHistoryBtn.setPreferredSize(new Dimension(220, 35));
        btnTransferRoomHistoryBtn.setFocusPainted(false);
        btnTransferRoomHistoryBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        if (canCheckChangeRoomHistory(reservationInfo.getStatus())) {
            btnTransferRoomHistoryBtn.setBackground(CustomUI.orange);
            btnTransferRoomHistoryBtn.setForeground(CustomUI.white);
            btnTransferRoomHistoryBtn.addActionListener(e -> handleCheckTranferRoomHistory(reservationInfo));
        } else {
            btnTransferRoomHistoryBtn.setBackground(CustomUI.gray);
            btnTransferRoomHistoryBtn.setForeground(CustomUI.white);
            btnTransferRoomHistoryBtn.setEnabled(false);
        }


        return btnTransferRoomHistoryBtn;
    }

    private JButton createExtendTimeBtn() {
        JButton btnExtendTime = new JButton("Gia hạn thời gian");
        btnExtendTime.setFont(CustomUI.smallFont);
        btnExtendTime.setPreferredSize(new Dimension(220, 35));
        btnExtendTime.setFocusPainted(false);
        btnExtendTime.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        if (canExtendTime(reservationInfo.getStatus())) {
            btnExtendTime.setBackground(CustomUI.orange);
            btnExtendTime.setForeground(CustomUI.white);
            btnExtendTime.addActionListener(e -> handleExtendTime(reservationInfo.getDetails().get(0)));
        } else {
            btnExtendTime.setBackground(CustomUI.gray);
            btnExtendTime.setForeground(CustomUI.white);
            btnExtendTime.setEnabled(false);
        }

        // TODO: Change this line to pass Reservation instead of Detail
        btnExtendTime.addActionListener(e -> handleExtendTime(reservationInfo.getDetails().get(0)));

        return btnExtendTime;
    }

    private JButton createCheckoutAndPrintReceiptBtn() {
        // Checkout button
        btnCheckoutAndPrintReceipt = new JButton("Thanh toán & In hóa đơn");
        btnCheckoutAndPrintReceipt.setFont(CustomUI.smallFont);
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
    private JButton createOrderServiceBtn(ReservationDetailResponse detail) {
        // Order service button
        JButton btnOrderService = new JButton("Gọi DV");
        btnOrderService.setFont(CustomUI.verySmallFont);
        btnOrderService.setBackground(CustomUI.darkGreen);
        btnOrderService.setForeground(CustomUI.white);
        btnOrderService.setPreferredSize(new Dimension(100, 30));
        btnOrderService.setFocusPainted(false);
        btnOrderService.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnOrderService.addActionListener(e -> handleOrderService(detail));

        return btnOrderService;
    }

    private JButton createCheckinBtn(ReservationDetailResponse detail) {
        // Check-in button
        JButton btnCheckIn = new JButton("Nhận phòng");
        btnCheckIn.setFont(CustomUI.verySmallFont);
        btnCheckIn.setBackground(CustomUI.darkGreen);
        btnCheckIn.setForeground(CustomUI.white);
        btnCheckIn.setPreferredSize(new Dimension(120, 30));
        btnCheckIn.setFocusPainted(false);
        btnCheckIn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnCheckIn.addActionListener(e -> handleCheckin(detail));

        return btnCheckIn;
    }

    private JButton createChangeRoomBtn(ReservationDetailResponse detail) {
        // Change room button
        JButton btnChangeRoom = new JButton("Đổi phòng");
        btnChangeRoom.setFont(CustomUI.verySmallFont);
        btnChangeRoom.setBackground(CustomUI.blue);
        btnChangeRoom.setForeground(CustomUI.white);
        btnChangeRoom.setPreferredSize(new Dimension(110, 30));
        btnChangeRoom.setFocusPainted(false);
        btnChangeRoom.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnChangeRoom.addActionListener(e -> handleChangeRoom(detail));

        return btnChangeRoom;
    }

    private JButton createCancelBtn(ReservationDetailResponse detail) {
        // Cancel button (small square with trash icon)
        JButton btnCancel = new JButton("Hủy đơn");
        btnCancel.setFont(CustomUI.verySmallFont);
        btnCancel.setBackground(CustomUI.red);
        btnCancel.setForeground(CustomUI.white);
        btnCancel.setPreferredSize(new Dimension(100, 30));
        btnCancel.setFocusPainted(false);
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        btnCancel.setToolTipText("Hủy phòng");
        btnCancel.addActionListener(e -> handleCancelRoom(detail));

        return btnCancel;
    }

    private void createServicesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(CustomUI.white);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Đơn gọi dịch vụ");

        // Create table - removed "Được tặng" column
        String[] columnNames = {"Đơn DV", "Phòng", "Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"};
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
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Đơn gọi DV
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Phòng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.25)); // 25% - Dịch vụ
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Số lượng
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Đơn giá
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Thành tiền
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
        tablePanel.setBackground(CustomUI.white);
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
                component.setForeground(CustomUI.black);
            } else {
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.ROW_EVEN);
                } else {
                    component.setBackground(CustomUI.ROW_ODD);
                }
                component.setForeground(CustomUI.black);
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

            Object[] rowData = new Object[6];
            rowData[0] = service.getRoomUsageServiceId();
            rowData[1] = service.getRoomName();
            rowData[2] = service.getServiceName();
            rowData[3] = service.getQuantity();
            rowData[4] = priceFormatter.format(service.getPrice()) + " VND";
            rowData[5] = priceFormatter.format(service.getPrice() * service.getQuantity()) + " VND";

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
            String note = history.getNote();
            if (note == null || note.isEmpty()) {
                if (history.getTimeIn() == null) {
                    note = "Đã rời phòng";
                } else if (history.getTimeOut() == null) {
                    note = "Đã vào phòng";
                } else {
                    note = "N/A";
                }
            }
            rowData[4] = note;

            movingHistoryModel.addRow(rowData);
        }
    }

    public void attachButtons(JPanel panel, String status, ReservationDetailResponse detail) {
        System.out.println("Visualizing buttons for status: " + status);
        panel.removeAll();

        if (status == null) {
            panel.revalidate();
            panel.repaint();
            return;
        }

        JButton btnOrderService = createOrderServiceBtn(detail);
        JButton btnCheckIn = createCheckinBtn(detail);
        JButton btnChangeRoom = createChangeRoomBtn(detail);
        JButton btnCancel = createCancelBtn(detail);

        switch (ReservationStatus.fromStatus(status)) {
            case CHECKED_IN:
                panel.add(btnCheckIn);
                panel.add(btnChangeRoom);
                panel.add(btnCancel);
                break;
            case CHECKING:
            case USING:
                panel.add(btnOrderService);
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
            attachButtons(this, detail.getStatus(), detail); // Pass detail to attachButtons

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
            attachButtons(panel, detail.getStatus(), detail); // Pass detail to attachButtons

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
        int result = 0;
        if(!this.isDialog){
            result = JOptionPane.showConfirmDialog(null,
                    "Xác nhận trả đơn đặt phòng " + detail.getMaDonDatPhong() + "?",
                    "Trả phòng", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        }
        if (result == JOptionPane.YES_OPTION) {
            InvoiceResponse invoiceResponse = checkOutService.checkOutReservation(detail.getMaDonDatPhong());
            if (invoiceResponse != null) {
                SwingUtilities.invokeLater(() -> {
                    InvoiceDialog2 dialog = new InvoiceDialog2(invoiceResponse);
                    dialog.setVisible(true);
                    RefreshManager.refreshAfterCheckout();
                    refreshPanel();
                });
            } else {
                JOptionPane.showMessageDialog(this,
                        "Trả phòng thất bại cho đơn đặt phòng" + detail.getMaDonDatPhong(),
                        "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCheckTranferRoomHistory(ReservationInfoDetailResponse detail) {
        if (detail == null) return;

        try {
            String maDon = detail.getMaDonDatPhong();
            if (maDon == null || maDon.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không xác định được mã đơn để xem lịch sử đổi phòng.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lấy owner window (an toàn với cả frame/dialog)
            Window owner = SwingUtilities.getWindowAncestor(this);
            Frame frameOwner = owner instanceof Frame ? (Frame) owner : null;

            // Mở dialog (modal). LichSuDoiPhongDialog.showDialog sẽ block cho đến khi đóng.
            LichSuDoiPhongDialog.showDialog(frameOwner, maDon);

            // Sau khi đóng dialog, refresh dữ liệu hiển thị (nếu cần)
            try {
                if (reservationInfo != null && reservationInfo.getMaDonDatPhong() != null) {
                    ReservationInfoDetailResponse updated = bookingService.getReservationDetailInfo(reservationInfo.getMaDonDatPhong());
                    if (updated != null) {
                        reservationInfo = updated;
                    }
                }
            } catch (Exception ex) {
                // nếu load lại thất bại thì bỏ qua (không phá flow)
                ex.printStackTrace();
            }
            // reload UI
            loadData();
            refreshPanel();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở lịch sử đổi phòng: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Action handlers for reservation details
    private void handleOrderService(ReservationDetailResponse detail) {
        handleViewOrderService(detail);
    }

    private void handleCheckin(ReservationDetailResponse detail) {
        if (detail == null) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhận phòng " + detail.getRoomName() + "?",
                "Nhận phòng", JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success;
        try {
            // Gọi hàm xử lí checkin
            String maDonDatPhong = reservationInfo != null ? reservationInfo.getMaDonDatPhong() : null;
            String tenPhong = detail.getRoomName();

            success = checkinService.checkin(maDonDatPhong, tenPhong);
        } catch (Exception ex) {
            ex.printStackTrace();
            success = false;
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Check-in thành công cho phòng: " + detail.getRoomName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Refresh UI sau khi checkin thành công
            RefreshManager.refreshAfterCheckIn();
            refreshPanel();

            // Cập nhật dữ liệu hiển thị
            try {
                ReservationInfoDetailResponse updated = bookingService.getReservationDetailInfo(reservationInfo.getMaDonDatPhong());
                if (updated != null) {
                    this.reservationInfo = updated;
                    loadData();
                } else {
                    loadData();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                loadData();
            }
        } else {
            // Thông báo lỗi nếu có
            String err = null;
            try {
                err = checkinService.getLastError();
            } catch (Exception ignore) { }
            if (err == null || err.isEmpty()) {
                err = "Check-in thất bại. Vui lòng kiểm tra lại hoặc xem nhật ký.";
            }

            JOptionPane.showMessageDialog(this,
                    err,
                    "Thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void handleExtendTime(ReservationDetailResponse detail) {
        if (detail == null) return;

        try {
            // Lấy maChiTietDatPhong và maPhong từ detail
            String maChiTiet = null;
            String maPhong = null;
            try {
                maChiTiet = detail.getReservationDetailId();
            } catch (Throwable ignore) { }

            try {
                // Lấy roomID
                maPhong = detail.getRoomId();
            } catch (Throwable ignore) {
                maPhong = null;
            }

            // Nếu không có maChiTiet
            if ((maChiTiet == null || maChiTiet.isEmpty()) && detail != null) {
                try { maChiTiet = detail.getReservationDetailId(); } catch (Throwable ignore) { }
            }

            // Lấy thông tin hỗ trợ cho dialog (BookThemGioInfo)
            BookThemGioServiceImpl bookThemGioService = new vn.iuh.service.impl.BookThemGioServiceImpl();
            BookThemGioInfo info = null;
            try {
                info = bookThemGioService.layThongTinChoBookThemGio(maChiTiet, maPhong);
            } catch (Exception ex) {
                // nếu service lỗi, vẫn tiếp tục với info = null (dialog sẽ dùng fallback)
                ex.printStackTrace();
            }

            // Chuẩn bị BookingResponse để truyền vào dialog
            String roomIdForBR = maPhong != null ? maPhong : (detail.getRoomName() != null ? detail.getRoomName() : detail.getReservationDetailId());
            String roomNameForBR = detail.getRoomName() != null ? detail.getRoomName() : roomIdForBR;
            String statusForBR = detail.getStatus();
            String roomType = "";
            try {
                // cố gắng lấy tên loại phòng bằng DoiPhongServiceImpl nếu có roomId
                DoiPhongServiceImpl helper = new DoiPhongServiceImpl();
                if (maPhong != null) roomType = helper.timTenLoaiPhong(maPhong);
            } catch (Exception ignore) { }

            BookingResponse br = new BookingResponse(
                    roomIdForBR,
                    roomNameForBR,
                    true,
                    statusForBR,
                    (roomType != null ? roomType : ""),
                    String.valueOf(1),
                    0.0,
                    0.0,
                    reservationInfo != null ? reservationInfo.getCustomerName() : null,
                    maChiTiet,
                    detail.getTimeIn(),
                    detail.getTimeOut()
            );

            // Tạo dialog BookThemGioDialog
            Window owner = SwingUtilities.getWindowAncestor(this);
            BookThemGioDialog dlg = new BookThemGioDialog(owner, br, info);

            // Đăng ký callback: khi user xác nhận thành công sẽ cập nhật UI hiện tại
            dlg.setCallback(new vn.iuh.gui.dialog.BookThemGioDialog.BookThemGioCallback() {
                @Override
                public void onXacNhan(long thoiGianThemMillis) {
                    // Cập nhật lại reservation
                    try {
                        if (reservationInfo != null && reservationInfo.getMaDonDatPhong() != null) {
                            ReservationInfoDetailResponse updated = bookingService.getReservationDetailInfo(reservationInfo.getMaDonDatPhong());
                            if (updated != null) {
                                reservationInfo = updated;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    // reload UI
                    loadData();

                    // gọi refresh manager để các panel khác cập nhật
                    RefreshManager.refreshAfterCheckIn();
                    refreshPanel();

                    // Thông báo ngắn cho user
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ReservationInfoDetailPanel.this),
                            "Gia hạn thời gian thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void onHuy() {

                }
            });

            // Hiển thị dialog
            dlg.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở dialog gia hạn: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleChangeRoom(ReservationDetailResponse detail) {
        if (detail == null) return;

        try {
            // Khởi tạo service đổi phòng
            DoiPhongServiceImpl doiPhongService = new DoiPhongServiceImpl();

            // Lấy roomId
            String currentRoomId = null;
            try {
                currentRoomId = detail.getRoomId();
            } catch (Throwable ignore) {
                // Nếu DTO không có roomId thì lấy mã chi tiết đặt phòng
                currentRoomId = detail.getReservationDetailId();
            }

            // Lấy số người cần
            int neededPersons = 1;
            try {
                neededPersons = doiPhongService.timSoNguoiCan(currentRoomId);
            } catch (Exception ignore) { }

            // Lấy khoảng thời gian cần kiểm tra
            Timestamp fromTime = null;
            Timestamp toTime = null;
            try { fromTime = detail.getTimeIn(); } catch (Throwable ignore) {}
            try { toTime = detail.getTimeOut(); } catch (Throwable ignore) {}

            // Tìm phòng ứng viên
            List<BookingResponse> candidates = doiPhongService.timPhongPhuHopChoDoiPhong(currentRoomId, neededPersons, fromTime, toTime);

            if (candidates == null || candidates.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy phòng phù hợp để đổi.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Tạo BookingResponse cho phòng hiện tại (để dialog hiển thị thông tin)
            BookingResponse currentBooking = new BookingResponse(
                    currentRoomId,
                    detail.getRoomName(),
                    true,
                    detail.getStatus(),
                    doiPhongService.timTenLoaiPhong(currentRoomId),
                    String.valueOf(neededPersons),
                    0.0,
                    0.0,
                    reservationInfo != null ? reservationInfo.getCustomerName() : null,
                    detail.getReservationDetailId(),
                    detail.getTimeIn(),
                    detail.getTimeOut()
            );

            // Tạo dialog đổi phòng
            DoiPhongDiaLog dialogPanel = new DoiPhongDiaLog(currentBooking, candidates);

            // Đăng ký callback để cập nhật UI khi đổi phòng thành công
            dialogPanel.setChangeRoomCallback(new DoiPhongDiaLog.ChangeRoomCallback() {
                @Override
                public void onChangeRoom(String oldRoomId, BookingResponse newRoom, boolean applyFee) {
                    // refresh dữ liệu hiện tại của panel
                    try {
                        if (reservationInfo != null && reservationInfo.getMaDonDatPhong() != null) {
                            ReservationInfoDetailResponse updated = bookingService.getReservationDetailInfo(reservationInfo.getMaDonDatPhong());
                            if (updated != null) {
                                reservationInfo = updated;
                            }
                        }
                    } catch (Exception ex) {

                        ex.printStackTrace();
                    }

                    // refesh dữ liệu
                    loadData();

                    // Gọi refresh manager chung
                    RefreshManager.refreshAfterCancelReservation();
                    refreshPanel();

                    // Bạn có thể hiện thông báo thêm (tuỳ ý)
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ReservationInfoDetailPanel.this),
                            "Đã đổi phòng: " + oldRoomId + " → " + (newRoom != null ? newRoom.getRoomName() : newRoom),
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            // Hiển thị dialog modal
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Đổi phòng", true);
            dialog.setContentPane(dialogPanel);
            dialog.pack();
            dialog.setSize(1100, 650);
            dialog.setLocationRelativeTo(owner);
            dialog.setResizable(false);
            dialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở dialog đổi phòng: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelRoom(ReservationDetailResponse detail) {
        int result = JOptionPane.showConfirmDialog(this,
                                                   "Xác nhận hủy phòng " + "?",
                                                   "Hủy phòng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean isSuccess = bookingService.cancelReservationDetail(detail.getReservationDetailId());

            if (isSuccess) {
                JOptionPane.showMessageDialog(this,
                                              "Hủy phòng thành công!",
                                              "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                RefreshManager.refreshAfterCancelReservation();
                refreshPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                                              "Hủy phòng thất bại! Vui lòng thử lại.",
                                              "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewOrderService(ReservationDetailResponse detail) {
        ServiceSelectionPanel serviceSelectionPanel = new ServiceSelectionPanel(detail.getReservationDetailId(), detail.getRoomName());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), SERVICE_ORDER + reservationInfo.getMaDonDatPhong(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(serviceSelectionPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Refresh after dialog is closed
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                RefreshManager.refreshAll();
                refreshPanel();
            }
        });
    }

    public void refreshPanel() {
        removeAll();
        reservationInfo = bookingService.getReservationDetailInfo(reservationInfo.getMaDonDatPhong());
        setLayout(new BorderLayout());
        init();
        loadData();
    }
    public ReservationInfoDetailResponse getReservationInfo() {
        return reservationInfo;
    }
}

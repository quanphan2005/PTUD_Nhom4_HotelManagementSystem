package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.ReservationStatus;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.response.ReservationResponse;
import vn.iuh.dto.response.ReservationInfoDetailResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.BookingService;
import vn.iuh.service.impl.BookingServiceImpl;
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

public class ReservationManagementPanel extends JPanel {
    private final BookingService bookingService;

    // Filter components
    private JTextField txtMaDon;
    private JTextField txtCCCD;
    private JTextField txtTenKhachHang;
    private JSpinner spnStartDate;
    private JSpinner spnEndDate;
    private JCheckBox chkCurrentReservations;
    private JButton btnReset;

    // Table components
    private JTable reservationTable;
    private DefaultTableModel tableModel;

    // Data
    private List<ReservationResponse> currentReservations;
    private List<ReservationResponse> filteredReservations;

    private List<ReservationResponse> passReservations;

    // Parent container for panel navigation
    private JPanel parentContainer;
    private String panelName = "reservationManagement";

    public ReservationManagementPanel() {
        RefreshManager.setReservationManagementPanel(this);

        bookingService = new BookingServiceImpl();

        setLayout(new BorderLayout());
        init();

        // Load data
        loadReservationData();
        applyFilters(); // Apply default filter on load
    }

    public void setParentContainer(JPanel parentContainer) {
        this.parentContainer = parentContainer;
    }

    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }

    private void loadReservationData() {
        // Get all reservations with relevant statuses in range of spninner dates
        currentReservations = bookingService.getAllCurrentReservationsWithStatus();
        filteredReservations = new ArrayList<>(currentReservations);

        passReservations = bookingService.getAllPastReservationsWithStatusInRange(
                new Timestamp(((Date) spnStartDate.getValue()).getTime()),
                new Timestamp(((Date) spnEndDate.getValue()).getTime())
        );
    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTopPanel();
        createFilterPanel();
        createTablePanel();
        setUpEvents();
    }

    private void setUpEvents() {
        // Events are set up in the component initialization methods
        txtMaDon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        txtCCCD.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        txtTenKhachHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });
        spnStartDate.addChangeListener(e -> applyFilters());
        spnEndDate.addChangeListener(e -> applyFilters());
        chkCurrentReservations.addActionListener(e -> applyFilters());
        btnReset.addActionListener(e -> resetFilters());
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("QUẢN LÝ ĐƠN ĐẶT PHÒNG", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.setPreferredSize(new Dimension(0, 40));
        pnlTop.setMinimumSize(new Dimension(0, 40));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
        addFilterRow(filterPanel, gbc, 1, 2, "Tên khách hàng:", txtTenKhachHang);

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

        // CCCD text field with auto-filtering
        txtCCCD = new JTextField(15);
        txtCCCD.setFont(CustomUI.smallFont);

        // Tên phòng text field with auto-filtering
        txtTenKhachHang = new JTextField(15);
        txtTenKhachHang.setFont(CustomUI.smallFont);

        // Start date spinner
        spnStartDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(spnStartDate, "dd/MM/yyyy HH:mm");
        spnStartDate.setEditor(startDateEditor);
        spnStartDate.setValue(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)); // 30 days ago
        spnStartDate.setFont(CustomUI.smallFont);

        // End date spinner
        spnEndDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(spnEndDate, "dd/MM/yyyy HH:mm");
        spnEndDate.setEditor(endDateEditor);
        spnEndDate.setValue(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)); // 30 days from now
        spnEndDate.setFont(CustomUI.smallFont);

        // Checkbox for current reservations
        chkCurrentReservations = new JCheckBox("Các đơn hiện tại");
        chkCurrentReservations.setFont(CustomUI.smallFont);
        chkCurrentReservations.setSelected(true); // Default to showing current reservations

        // Reset button
        btnReset = new JButton("Hoàn tác");
        btnReset.setFont(CustomUI.smallFont);
        btnReset.setBackground(CustomUI.lightGray);
        btnReset.setForeground(Color.BLACK);
        btnReset.setFocusPainted(false);
        btnReset.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
    }

    private void createTablePanel() {
        // Create table model
        String[] columnNames = {"Số CCCD", "Khách hàng", "Mã đơn", "Loại đơn", "Checkin", "Checkout", "Trạng thái", "Thao tác"};
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
        reservationTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        reservationTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = reservationTable.getWidth();
                TableColumnModel columnModel = reservationTable.getColumnModel();

                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Số CCCD
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Khách hàng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Mã đơn
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Phòng
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.12)); // 15% - Checkin
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.12)); // 15% - Checkout
                columnModel.getColumn(6).setPreferredWidth((int) (tableWidth * 0.12)); // 10% - Trạng thái
                columnModel.getColumn(7).setPreferredWidth((int) (tableWidth * 0.15)); // 10% - Thao tác
            }
        });

        // Set cell renderer and editor for action column
        reservationTable.getColumn("Thao tác").setCellRenderer(new ActionButtonRenderer());
        reservationTable.getColumn("Thao tác").setCellEditor(new ActionButtonEditor());

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.SOUTH);
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
        String maDonFilter = txtMaDon.getText().trim().toLowerCase();
        String cccdFilter = txtCCCD.getText().trim().toLowerCase();
        String tenKhachHangFilter = txtTenKhachHang.getText().trim().toLowerCase();
        Date startDate = (Date) spnStartDate.getValue();
        Date endDate = (Date) spnEndDate.getValue();
        boolean showCurrentOnly = chkCurrentReservations.isSelected();

        List<ReservationResponse> allReservations;
        if (showCurrentOnly) {
            allReservations = bookingService.getAllCurrentReservationsWithStatus(
            );
        } else {
            allReservations = bookingService.getAllPastReservationsWithStatusInRange(
                    new Timestamp(startDate.getTime()),
                    new Timestamp(endDate.getTime())
            );
        }
        filteredReservations.clear();

        for (ReservationResponse reservation : allReservations) {
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
            if (!tenKhachHangFilter.isEmpty() &&
                !reservation.getCustomerName().toLowerCase().contains(tenKhachHangFilter)) {
                continue;
            }

            // Check date range filter only for current reservations
            if (!showCurrentOnly) {
                if (reservation.getTimeIn() != null && reservation.getTimeOut() != null) {
                    Timestamp checkinTime = reservation.getTimeIn();
                    Timestamp checkoutTime = reservation.getTimeOut();

                    // Check if the reservation overlaps with the date range
                    if (checkinTime.after(new Timestamp(endDate.getTime())) ||
                        checkoutTime.before(new Timestamp(startDate.getTime()))) {
                        continue;
                    }
                }

                spnStartDate.setEnabled(true);
                spnEndDate.setEnabled(true);
            } else {
                spnStartDate.setEnabled(false);
                spnEndDate.setEnabled(false);
            }

            filteredReservations.add(reservation);
        }

        populateTable();
    }

    private void resetFilters() {
        txtMaDon.setText("");
        txtCCCD.setText("");
        txtTenKhachHang.setText("");
        spnStartDate.setValue(new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
        spnEndDate.setValue(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
        chkCurrentReservations.setSelected(true);

//        filteredReservations = new ArrayList<>(currentReservations);
//        applyFilters();
        refreshPanel();
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
            rowData[3] = reservation.getType();
            rowData[4] = reservation.getTimeIn() != null ? dateFormat.format(reservation.getTimeIn()) : "N/A";
            rowData[5] = reservation.getTimeOut() != null ? dateFormat.format(reservation.getTimeOut()) : "N/A";
            rowData[6] = reservation.getStatus();
            rowData[7] = reservation; // Store the reservation object for action buttons

            tableModel.addRow(rowData);
        }
    }

    private void handleViewDetail(ReservationResponse reservation) {
        if (reservation == null) return;

        try {
            // Fetch detailed reservation information from service
            ReservationInfoDetailResponse detailInfo = bookingService.getReservationDetailInfo(reservation.getMaDonDatPhong());
            
            if (detailInfo == null) {
                JOptionPane.showMessageDialog(this,
                    "Không thể tải thông tin chi tiết đơn đặt phòng.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create detail panel
            ReservationInfoDetailPanel detailPanel = new ReservationInfoDetailPanel(detailInfo, this);
            
            // Navigate to detail panel using CardLayout
            if (parentContainer != null) {
                // Check if detail panel already exists, if so remove it
                Component[] components = parentContainer.getComponents();
                for (Component comp : components) {
                    if (comp instanceof ReservationInfoDetailPanel) {
                        parentContainer.remove(comp);
                        break;
                    }
                }
                
                // Add new detail panel
                parentContainer.add(detailPanel, "reservationDetail");
                
                // Show detail panel
                CardLayout layout = (CardLayout) parentContainer.getLayout();
                layout.show(parentContainer, "reservationDetail");
            } else {
                // Fallback: Open in dialog if parent container is not set
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Đơn " + reservation.getMaDonDatPhong(), true);

                // Wrap detail panel in a scroll pane
                JScrollPane scrollPane = new JScrollPane(detailPanel);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);

                dialog.setContentPane(scrollPane);
                dialog.setSize(1000, 700); // Reduced width from 1200 to 1000
                dialog.setLocationRelativeTo(null); // Center on screen
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setResizable(false);
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Có lỗi xảy ra khi tải thông tin chi tiết: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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


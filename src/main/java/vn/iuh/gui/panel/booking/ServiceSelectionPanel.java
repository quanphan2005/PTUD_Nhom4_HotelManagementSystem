package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.service.GoiDichVuService;
import vn.iuh.service.ServiceCategoryService;
import vn.iuh.service.impl.GoiDichVuServiceImpl;
import vn.iuh.service.impl.ServiceCategoryServiceImpl;
import vn.iuh.util.PriceFormat;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class ServiceSelectionPanel extends JPanel {
    private String parentName;

    private ServiceCategoryService serviceCategoryService;
    private GoiDichVuService goiDichVuService;

    // Left Components
    private JTextField txtSearchService;
    private JComboBox<String> cmbServiceType;
    private JComboBox<String> cmbSelectedRoom;
    private JButton btnOrderHistory;
    private JLabel lblTotalServices;
    private JLabel lblAvailableServices;

    // Left table
    private JTable serviceTable;
    private DefaultTableModel serviceTableModel;

    // Right Components
    private JLabel lblTotalCost;
    private JButton btnReset;
    private JButton btnConfirm;
    private JButton btnClose;

    // Right table
    private JTable selectedServicesTable;
    private DefaultTableModel selectedServicesTableModel;

    // Selected rooms
    private int totalRoom;
    private List<String> selectedRoomNames;

    // For existing booking
    private String maChiTietDatPhong;

    // Data
    private List<ThongTinDichVu> allServices;
    private List<ThongTinDichVu> filteredServices;
    private List<DonGoiDichVu> serviceOrders;
    private Map<String, Double> servicePricesMap; // Track service prices for total cost calculation
    private ServiceSelectionCallback callback;

    // Formatters
    private DecimalFormat priceFormatter = PriceFormat.getPriceFormatter();

    private final String ALL_SERVICE = "Tất cả loại dịch vụ";
    private final String ALL_ROOMS = "Tất cả phòng";

    public interface ServiceSelectionCallback {
        void onServiceConfirmed(List<DonGoiDichVu> ServiceOrders);
    }

    // For new booking
    public ServiceSelectionPanel(String parentName, int totalRoom, List<String> selectedRoomNames, String maChiTietDatPhong, ServiceSelectionCallback callback) {
        this.parentName = parentName;

        this.serviceCategoryService = new ServiceCategoryServiceImpl();
        this.goiDichVuService = new GoiDichVuServiceImpl();
        this.callback = callback;
        this.serviceOrders = new ArrayList<>();

        this.totalRoom = totalRoom;
        this.selectedRoomNames = selectedRoomNames;
        this.maChiTietDatPhong = maChiTietDatPhong;

        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();
        loadData();
        setupEventHandlers();
    }

    // For existing booking
    public ServiceSelectionPanel(String maChiTietDatPhong, String roomName) {
        this.serviceCategoryService = new ServiceCategoryServiceImpl();
        this.goiDichVuService = new GoiDichVuServiceImpl();
        this.serviceOrders = new ArrayList<>();

        this.maChiTietDatPhong = maChiTietDatPhong;
        this.totalRoom = 1; // Default to 1 for viewing existing bookings
        this.selectedRoomNames = new ArrayList<>();
        this.selectedRoomNames.add(roomName);

        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();
        loadData();
        setupEventHandlers();

        hideCloseButton();
    }

    private void initializeComponents() {
        // Search components
        cmbServiceType = new JComboBox<>();
        cmbServiceType.setFont(CustomUI.smallFont);
        cmbServiceType.setPreferredSize(new Dimension(200, 30));
        cmbServiceType.setMinimumSize(new Dimension(200, 30));

        txtSearchService = new JTextField();
        txtSearchService.setFont(CustomUI.normalFont);
        txtSearchService.setPreferredSize(new Dimension(400, 35));
        txtSearchService.setMinimumSize(new Dimension(400, 35)); // Add minimum size
        txtSearchService.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên dịch vụ");

        // Room selected & Info labels with fixed sizes
        cmbSelectedRoom = new JComboBox<>();
        cmbSelectedRoom.setFont(CustomUI.smallFont);
        cmbSelectedRoom.setPreferredSize(new Dimension(100, 30));
        cmbSelectedRoom.setMinimumSize(new Dimension(100, 30));

        lblTotalServices = new JLabel("Tổng dịch vụ: 0");
        lblTotalServices.setFont(CustomUI.normalFont);
        lblTotalServices.setOpaque(true);
        lblTotalServices.setForeground(CustomUI.black);
        lblTotalServices.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        lblTotalServices.setPreferredSize(new Dimension(120, 40)); // Fixed size
        lblTotalServices.setMinimumSize(new Dimension(120, 40)); // Fixed minimum

        lblAvailableServices = new JLabel("Dịch vụ khả dụng: 0");
        lblAvailableServices.setFont(CustomUI.normalFont);
        lblAvailableServices.setOpaque(true);
        lblAvailableServices.setForeground(CustomUI.black);
        lblAvailableServices.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        lblAvailableServices.setPreferredSize(new Dimension(120, 40)); // Fixed size
        lblAvailableServices.setMinimumSize(new Dimension(120, 40)); // Fixed minimum

        // Service table - Remove gift column
        String[] serviceColumns = {"Dịch vụ", "Loại", "Giá", "Tồn kho", "Đã chọn"};
        serviceTableModel = new DefaultTableModel(serviceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only quantity column is editable
            }
        };
        serviceTable = new JTable(serviceTableModel);
        serviceTable.setFont(CustomUI.TABLE_FONT); // Non-bold font for data
        serviceTable.setRowHeight(40);
        serviceTable.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        serviceTable.setGridColor(CustomUI.tableBorder);
        serviceTable.setShowGrid(true); // Show grid lines
        serviceTable.setIntercellSpacing(new Dimension(1, 1)); // Thin borders

        // Enhanced header styling
        serviceTable.getTableHeader().setPreferredSize(new Dimension(serviceTable.getWidth(), 40));
        serviceTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        serviceTable.getTableHeader().setBackground(CustomUI.TABLE_HEADER_BACKGROUND);
        serviceTable.getTableHeader().setForeground(CustomUI.TABLE_HEADER_FOREGROUND);
        serviceTable.getTableHeader().setOpaque(true);
        serviceTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        // Set alternating row colors for regular columns
        serviceTable.setDefaultRenderer(Object.class, new ServiceTableRenderer());

        TableColumnModel serviceColumnModel = serviceTable.getColumnModel();
        serviceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Set column widths using relative proportions
        serviceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        serviceTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = serviceTable.getWidth();
                TableColumnModel columnModel = serviceTable.getColumnModel();

                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.25)); // 25% - Tên
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.25)); // 25% - Loại
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Giá
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Tồn kho
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Đã chọn
            }
        });

        // Custom renderer and editor for quantity column (column 4)
        serviceTable.getColumnModel().getColumn(4).setCellRenderer(new QuantityRenderer());
        serviceTable.getColumnModel().getColumn(4).setCellEditor(new QuantityEditor());

        // Selected services table with fixed column widths
        String[] selectedColumns = {"Phòng", "Dịch vụ", "SL", "Thành tiền"};
        selectedServicesTableModel = new DefaultTableModel(selectedColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        selectedServicesTable = new JTable(selectedServicesTableModel) { // Tạo JTable mới dựa trên model
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
        selectedServicesTable.setFont(CustomUI.TABLE_FONT); // Non-bold font for data
        selectedServicesTable.setRowHeight(40);
        selectedServicesTable.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        selectedServicesTable.setGridColor(CustomUI.tableBorder);
        selectedServicesTable.setShowGrid(true); // Show grid lines
        selectedServicesTable.setIntercellSpacing(new Dimension(1, 1)); // Thin borders

        // Enhanced header styling for selected services table
        selectedServicesTable.getTableHeader().setPreferredSize(new Dimension(selectedServicesTable.getWidth(), 30));
        selectedServicesTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        selectedServicesTable.getTableHeader().setBackground(CustomUI.TABLE_HEADER_BACKGROUND);
        selectedServicesTable.getTableHeader().setForeground(CustomUI.TABLE_HEADER_FOREGROUND);
        selectedServicesTable.getTableHeader().setOpaque(true);
        selectedServicesTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        // Set alternating row colors for selected services table
        selectedServicesTable.setDefaultRenderer(Object.class, new SelectedServicesTableRenderer());

        // Set column widths using relative proportions
        selectedServicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        selectedServicesTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = selectedServicesTable.getWidth();
                TableColumnModel columnModel = selectedServicesTable.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Tên dịch vụ
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.30)); // 20% - Tên dịch vụ
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.10)); // 25% - Số lượng
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.40)); // 15% - Thành tiền
            }
        });

        // Action buttons with fixed sizes
        btnReset = new JButton("Hoàn Tác");
        btnReset.setBackground(CustomUI.gray);
        btnReset.setForeground(CustomUI.white);
        btnReset.setFont(CustomUI.normalFont);
        btnReset.setPreferredSize(new Dimension(280, 40));
        btnReset.setMinimumSize(new Dimension(280, 40)); // Fixed minimum
        btnReset.setMaximumSize(new Dimension(280, 40)); // Fixed maximum
        btnReset.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnConfirm = new JButton("Xác Nhận");
        btnConfirm.setBackground(CustomUI.darkGreen);
        btnConfirm.setForeground(CustomUI.white);
        btnConfirm.setFont(CustomUI.normalFont);
        btnConfirm.setPreferredSize(new Dimension(280, 40));
        btnConfirm.setMinimumSize(new Dimension(280, 40)); // Fixed minimum
        btnConfirm.setMaximumSize(new Dimension(280, 40)); // Fixed maximum
        btnConfirm.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnOrderHistory = new JButton("Xem Lịch Sử");
        btnOrderHistory.setBackground(CustomUI.purple);
        btnOrderHistory.setForeground(CustomUI.white);
        btnOrderHistory.setFont(CustomUI.normalFont);
        btnOrderHistory.setPreferredSize(new Dimension(280, 40));
        btnOrderHistory.setMinimumSize(new Dimension(280, 40));
        btnOrderHistory.setMaximumSize(new Dimension(280, 40));
        btnOrderHistory.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Only show order history button for existing bookings
        if (maChiTietDatPhong == null) {
            btnOrderHistory.setBackground(CustomUI.gray);
            btnOrderHistory.setBackground(CustomUI.white);
            btnOrderHistory.setEnabled(false);
        }

        btnClose = new JButton("x");
        btnClose.setFont(CustomUI.bigFont);
        btnClose.setBackground(CustomUI.red);
        btnClose.setForeground(CustomUI.white);
        btnClose.setPreferredSize(new Dimension(50, 20));
        btnClose.setFocusPainted(false);
        btnClose.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Total cost label with fixed size and improved formatting
        lblTotalCost = new JLabel("Tổng tiền: 0 VNĐ");
        lblTotalCost.setFont(CustomUI.normalFont);
        lblTotalCost.setBorder(BorderFactory.createLineBorder(CustomUI.black, 1));
        lblTotalCost.setOpaque(true);
        lblTotalCost.setBackground(CustomUI.yellow);
        lblTotalCost.setForeground(CustomUI.black);
        lblTotalCost.setPreferredSize(new Dimension(280, 40)); // Fixed wider size
        lblTotalCost.setMinimumSize(new Dimension(280, 40)); // Fixed minimum
        lblTotalCost.setMaximumSize(new Dimension(280, 40)); // Fixed maximum
        lblTotalCost.setHorizontalAlignment(SwingConstants.CENTER);
        // radius
    }

    private void setupLayout() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 40));
        headerPanel.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        headerPanel.setBackground(CustomUI.blue);

        JLabel titleLabel = new JLabel("Gọi dịch vụ (" + totalRoom + " Phòng)", SwingConstants.CENTER);
        titleLabel.setFont(CustomUI.bigFont);
        titleLabel.setForeground(CustomUI.white);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(btnClose, BorderLayout.EAST);

        // Main content panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(CustomUI.white);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // LEFT COLUMN
        // Search area
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, gbc);

        // Info labels
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.gridy = 1;
        mainPanel.add(cmbSelectedRoom, gbc);

        gbc.gridx = 1;
        mainPanel.add(lblTotalServices, gbc);

        gbc.gridx = 2;
//        gbc.weightx = 0.2;
        mainPanel.add(lblAvailableServices, gbc);

        // Service table
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        JScrollPane serviceScrollPane = new JScrollPane(serviceTable);
        serviceScrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH DỊCH VỤ"));
        serviceScrollPane.setPreferredSize(new Dimension(500, 400));
        serviceScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(serviceScrollPane, gbc);

        // RIGHT COLUMN
        // Action buttons
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.1;
        gbc.weighty = 0;
        mainPanel.add(btnReset, gbc);

        gbc.gridy = 1;
        mainPanel.add(btnConfirm, gbc);

        gbc.gridy = 2;
        mainPanel.add(btnOrderHistory, gbc);

        // Total cost
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        mainPanel.add(lblTotalCost, gbc);

        // Selected services table
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        JScrollPane selectedScrollPane = new JScrollPane(selectedServicesTable);
        selectedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        selectedScrollPane.setBorder(BorderFactory.createTitledBorder("DANH SÁCH ĐÃ CHỌN"));
        selectedScrollPane.setPreferredSize(new Dimension(350, 400));
        mainPanel.add(selectedScrollPane, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(CustomUI.white);
        panel.add(cmbServiceType);
        panel.add(txtSearchService);
        return panel;
    }

    private void loadData() {
        loadServiceType();
        loadServices();
        loadSelectedRooms();
    }

    private void loadServiceType() {
        List<LoaiDichVu> allServiceCategories = serviceCategoryService.getAllServiceCategories();
        cmbServiceType.addItem(ALL_SERVICE);
        for (LoaiDichVu category : allServiceCategories) {
            cmbServiceType.addItem(category.getTenDichVu());
        }
        cmbServiceType.setSelectedIndex(0);
    }

    private void loadServices() {
        allServices = goiDichVuService.timTatCaThongTinDichVu();
        servicePricesMap = new HashMap<>();
        for (ThongTinDichVu service : allServices) {
            servicePricesMap.put(service.getMaDichVu(), service.getDonGia());
        }
        filteredServices = new ArrayList<>(allServices);
        updateServiceTable();
        updateInfoLabels();
    }

    private void loadSelectedRooms() {
        // Single room for existing booking
        if (selectedRoomNames.size() == 1) {
            cmbSelectedRoom.addItem(selectedRoomNames.get(0));
            cmbSelectedRoom.setEnabled(false);
        }

        // Multiple rooms for new booking
        else {
            cmbSelectedRoom.addItem(ALL_ROOMS);
            for (String roomName : selectedRoomNames) {
                cmbSelectedRoom.addItem(roomName);
            }
            cmbSelectedRoom.setSelectedIndex(0);
        }
    }

    private void setupEventHandlers() {
        cmbServiceType.addActionListener(e -> handleCmbServiceChangeEvent());

        cmbSelectedRoom.addActionListener(e -> handleCmbSelectedRoomChangeEvent());

        btnOrderHistory.addActionListener(e -> handleFindServiceOrderedHistory());

        txtSearchService.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                handleSearchTextChangeEvent();
            }
        });

        btnReset.addActionListener(e -> handleResetBtn());

        btnConfirm.addActionListener(e -> confirmSelection());

        btnClose.addActionListener(e -> {Main.showCard(parentName);});
    }
    private void resetPanel() {
        resetAllSelections();
        loadServices();
    }

    private void resetAllSelections() {
        // Stop any active cell editing to ensure updates take effect
        if (serviceTable.isEditing()) {
            serviceTable.getCellEditor().stopCellEditing();
        }

//        selectedServicesMap.clear();
        serviceOrders.clear();

        resetAllRowEditingCell();

        updateServiceTable();
        updateSelectedServicesTable();

        // Update total services cost on booking panel
        if (callback != null) {
            callback.onServiceConfirmed(new ArrayList<>());
        }
    }

    private void resetAllRowEditingCell() {
        serviceOrders.clear();

        // Then update table display
        for (int i = 0; i < serviceTableModel.getRowCount(); i++) {
            serviceTableModel.setValueAt(0, i, 4);     // Reset quantity column (now column 4 instead of 5)
        }

        // Force table repaint to ensure UI updates
        serviceTable.repaint();
    }

    private void confirmSelection() {
        if (serviceOrders.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                                          "Vui lòng chọn ít nhất một dịch vụ",
                                          "Thông báo",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Service ordered for existing booking
        if (maChiTietDatPhong != null) {
            boolean success = goiDichVuService.goiDichVu(maChiTietDatPhong, serviceOrders,
                                                         Main.getCurrentLoginSession());
            if (success) {
                JOptionPane.showMessageDialog(this,
                                              "Gọi thêm " + serviceOrders.size() + " dịch vụ thành công.",
                                              "Thành công",
                                              JOptionPane.INFORMATION_MESSAGE);
                resetPanel();
                Main.showCard(parentName);
            } else {
                JOptionPane.showMessageDialog(this,
                                              "Gọi dịch vụ thất bại. Vui lòng thử lại.",
                                              "Lỗi",
                                              JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        if (callback != null) {
            callback.onServiceConfirmed(serviceOrders);
        }

        // Show dialog base on selected rooms
        if (totalRoom > 1) {
            JOptionPane.showMessageDialog(this,
                                          "Tạo " + serviceOrders.size() + " đơn dùng dịch vụ cho " + totalRoom + " phòng\n",
                                          "Xác nhận",
                                          JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                                          "Tạo " + serviceOrders.size() + " đơn dùng dịch vụ cho phòng: " + selectedRoomNames.get(0),
                                          "Xác nhận",
                                          JOptionPane.INFORMATION_MESSAGE);
        }

        Main.showCard(parentName);
    }

    private void updateServiceTable() {
        serviceTableModel.setRowCount(0);

        for (ThongTinDichVu service : filteredServices) {
            int selectedQuantity = 0;
            for (DonGoiDichVu order : serviceOrders) {
                // For "All Rooms" selection
                if (cmbSelectedRoom.getSelectedItem().toString().equalsIgnoreCase(ALL_ROOMS)) {
                    if (order.getMaDichVu().equalsIgnoreCase(service.getMaDichVu())) {
                        selectedQuantity += order.getSoLuong();
                    }
                }
                // For single room selection
                else if (order.getMaDichVu().equalsIgnoreCase(service.getMaDichVu()) &&
                         order.getTenPhong().equalsIgnoreCase(cmbSelectedRoom.getSelectedItem().toString())) {
                    selectedQuantity = order.getSoLuong();
                    break;
                }
            }
            Object[] row = {
                    service.getTenDichVu(),
                    service.getTenLoaiDichVu(),
                    priceFormatter.format(service.getDonGia()) + " VNĐ",
                    service.getTonKho(),
                    selectedQuantity
            };
            serviceTableModel.addRow(row);
        }
    }

    private void updateSelectedServicesTable() {
        selectedServicesTableModel.setRowCount(0);

        for (DonGoiDichVu order : serviceOrders) {
            ThongTinDichVu service = findServiceById(order.getMaDichVu());
            if (service != null) {
                double totalPrice = service.getDonGia() * order.getSoLuong(); // 0 if gift
                Object[] row = {
                        order.getTenPhong(),
                        service.getTenDichVu(),
                        order.getSoLuong(),
                        priceFormatter.format(totalPrice) + " VNĐ"
                };
                selectedServicesTableModel.addRow(row);
            }
        }

        updateTotalCost();
    }

    private void updateInfoLabels() {
        lblTotalServices.setText("Tổng dịch vụ: " + filteredServices.size());

        int availableCount = (int) filteredServices.stream()
                                                   .mapToInt(ThongTinDichVu::getTonKho)
                                                   .filter(stock -> stock > 0)
                                                   .count();
        lblAvailableServices.setText("Dịch vụ khả dụng: " + availableCount);
    }

    private void updateTotalCost() {
        double total = 0;

        for (DonGoiDichVu order : serviceOrders) {
            ThongTinDichVu service = findServiceById(order.getMaDichVu());
            if (service != null) {
                total += service.getDonGia() * order.getSoLuong();
            }
        }

        lblTotalCost.setText("Tổng tiền: " + priceFormatter.format(total) + " VNĐ");
    }

    private void handleResetBtn() {
        resetAllSelections();
        JOptionPane.showMessageDialog(this,
                                      "Đã xóa tất cả dịch vụ đã chọn",
                                      "Hoàn tác",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    private ThongTinDichVu findServiceById(String serviceId) {
        return allServices.stream()
                          .filter(service -> service.getMaDichVu().equals(serviceId))
                          .findFirst()
                          .orElse(null);
    }

    private void handleCmbServiceChangeEvent() {
        // Make table stop editing to capture any in-progress changes
        if (serviceTable.isEditing()) {
            serviceTable.getCellEditor().stopCellEditing();
        }
        filterServices();
    }

    private void handleCmbSelectedRoomChangeEvent() {
        // Make table stop editing to capture any in-progress changes
        if (serviceTable.isEditing()) {
            serviceTable.getCellEditor().stopCellEditing();
        }
        updateServiceTable();
    }


    private void handleFindServiceOrderedHistory() {
        ServiceOrderedHistoryPanel serviceOrderedHistoryPanel =
                new ServiceOrderedHistoryPanel(maChiTietDatPhong);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                                     "Lịch sử gọi dịch vụ", Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(serviceOrderedHistoryPanel);
        dialog.pack();
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(null); // Center on screen
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void handleSearchTextChangeEvent() {
        filterServices();
    }

    private void filterServices() {
        String selectedCategory = (String) cmbServiceType.getSelectedItem();
        String searchText = txtSearchService.getText().toLowerCase().trim();

        filteredServices = new ArrayList<>();
        for (ThongTinDichVu service : allServices) {
            boolean matchesSearch = service.getTenDichVu().toLowerCase().contains(searchText) ||
                                    service.getTenLoaiDichVu().toLowerCase().contains(searchText);
            boolean matchesCategory = selectedCategory.equals(ALL_SERVICE) ||
                                      service.getTenLoaiDichVu().equals(selectedCategory);
            if (matchesSearch && matchesCategory) {
                filteredServices.add(service);
            }
        }

        updateServiceTable();
        updateInfoLabels();
    }

    // Custom renderer for quantity column (updated for new column index)
    private class QuantityRenderer extends JPanel implements TableCellRenderer {
        // Change label to text field to support user can edit directly
        private JTextField quantityField;
        private JButton btnDecrease;
        private JButton btnIncrease;

        public QuantityRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

            btnDecrease = new JButton("▼");
            btnDecrease.setPreferredSize(new Dimension(25, 25));
            btnDecrease.setFont(new Font("Arial", Font.BOLD, 10));
            // Prevent button from taking focus to avoid editor issues
            btnDecrease.setFocusable(false);

            quantityField = new JTextField("0", 3);
            quantityField.setPreferredSize(new Dimension(40, 25));
            quantityField.setHorizontalAlignment(SwingConstants.CENTER);
            quantityField.setBorder(BorderFactory.createLineBorder(CustomUI.gray));
            quantityField.setFont(new Font("Arial", Font.PLAIN, 12));

            btnIncrease = new JButton("▲");
            btnIncrease.setPreferredSize(new Dimension(25, 25));
            btnIncrease.setFont(new Font("Arial", Font.BOLD, 10));
            // Prevent button from taking focus to avoid editor issues
            btnIncrease.setFocusable(false);

            add(btnDecrease);
            add(quantityField);
            add(btnIncrease);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            int quantity = (Integer) value;
            quantityField.setText(String.valueOf(quantity));

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                quantityField.setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
                quantityField.setBackground(CustomUI.white);
            }

            return this;
        }
    }

    // Custom editor for quantity column (updated for new column index)
    private class QuantityEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JTextField quantityField;
        private JButton btnDecrease;
        private JButton btnIncrease;
        private int currentQuantity;
        private int currentRow;

        public QuantityEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

            btnDecrease = new JButton("▼");
            btnDecrease.setPreferredSize(new Dimension(25, 25));
            btnDecrease.setFont(new Font("Arial", Font.BOLD, 10));
            // Prevent button from taking focus to avoid editor issues
            btnDecrease.setFocusable(false);

            quantityField = new JTextField("0", 3);
            quantityField.setPreferredSize(new Dimension(40, 25));
            quantityField.setHorizontalAlignment(SwingConstants.CENTER);
            quantityField.setBorder(BorderFactory.createLineBorder(CustomUI.gray));
            quantityField.setFont(new Font("Arial", Font.PLAIN, 12));

            btnIncrease = new JButton("▲");
            btnIncrease.setPreferredSize(new Dimension(25, 25));
            btnIncrease.setFont(new Font("Arial", Font.BOLD, 10));
            // Prevent button from taking focus to avoid editor issues
            btnIncrease.setFocusable(false);

            panel.add(btnDecrease);
            panel.add(quantityField);
            panel.add(btnIncrease);

            // Button event handlers - Updated with smart increment/decrement
            btnDecrease.addActionListener(e -> {
                ThongTinDichVu service = filteredServices.get(currentRow);
                int decrementAmount = getOptimalDecrementAmount(currentQuantity, service);

                if (currentQuantity >= decrementAmount) {
                    currentQuantity -= decrementAmount;
                    quantityField.setText(String.valueOf(currentQuantity));
                    updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), -decrementAmount, false);

                    // Show feedback for multi-room decrements
                    if (cmbSelectedRoom.getSelectedItem().toString().equalsIgnoreCase(ALL_ROOMS) && decrementAmount > 1) {
                        showQuantityFeedback("Giảm " + decrementAmount + " (" + totalRoom + " phòng)", false);
                    }
                } else {
                    // If can't decrease by full amount, set to 0
                    if (currentQuantity > 0) {
                        currentQuantity = 0;
                        quantityField.setText("0");
                        updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), -decrementAmount, false);
                        showQuantityFeedback("Đặt về 0", false);
                    }
                }

                this.fireEditingStopped();
            });

            btnIncrease.addActionListener(e -> {
                ThongTinDichVu service = filteredServices.get(currentRow);
                int incrementAmount = getOptimalIncrementAmount(currentQuantity, service);

                if (currentQuantity + incrementAmount <= service.getTonKho()) {
                    currentQuantity += incrementAmount;
                    quantityField.setText(String.valueOf(currentQuantity));
                    updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), incrementAmount, false);

                    // Show feedback for multi-room increments
                    if (cmbSelectedRoom.getSelectedItem().toString().equalsIgnoreCase(ALL_ROOMS)  && incrementAmount > 1) {
                        showQuantityFeedback("Tăng " + incrementAmount + " (" + totalRoom + " phòng)", true);
                    }
                } else {
                    // Try to add as much as possible up to stock limit
                    int maxPossible = getMaxPossibleQuantity(service);
                    if (maxPossible > currentQuantity) {
                        int actualIncrement = maxPossible - currentQuantity;
                        currentQuantity = maxPossible;
                        quantityField.setText(String.valueOf(currentQuantity));
                        updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), actualIncrement, false);
                        showQuantityFeedback("Tăng tối đa " + actualIncrement + " (giới hạn tồn kho)", true);
                    } else {
                        showStockLimitWarning(service);
                    }
                }

                this.fireEditingStopped();
            });

            // TextField event handlers for direct input
            quantityField.addKeyListener(new KeyAdapter() {
                // Allow only numeric input
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    // Allow digits, backspace, and delete
                    if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                        e.consume(); // Ignore non-numeric characters
                    }
                }

                // Handle Enter key to validate input
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        validateAndUpdateFromTextField();
                    }
                }
            });

            quantityField.addFocusListener(new java.awt.event.FocusListener() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    // Select all text when field gains focus
                    SwingUtilities.invokeLater(() -> quantityField.selectAll());
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    validateAndUpdateFromTextField();
                }
            });
        }

        /**
         * Calculate optimal increment amount based on current quantity and selected rooms
         */
        private int getOptimalIncrementAmount(int currentQuantity, ThongTinDichVu service) {
            // For multi-room booking, increment by cmbSelectedRoom
            String selectedRoom = Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString();
            if (selectedRoom.equalsIgnoreCase(ALL_ROOMS)) {
                int maxPossible = service.getTonKho() - currentQuantity;
                return Math.min(totalRoom, maxPossible);
            }

            // Single room selected from multiple rooms
            return 1;
        }

        /**
         * Calculate optimal decrement amount based on current quantity and selected rooms
         */
        private int getOptimalDecrementAmount(int currentQuantity, ThongTinDichVu service) {
            // For multi-room booking, decrement by cmbSelectedRoom
            String selectedRoom = Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString();
            if (selectedRoom.equalsIgnoreCase(ALL_ROOMS)) {
                return Math.min(totalRoom, currentQuantity);
            }

            // Single room selected from multiple rooms
            return 1;
        }

        /**
         * Get maximum possible quantity that satisfies room division requirement
         */
        private int getMaxPossibleQuantity(ThongTinDichVu service) {
            // Calculate max quantity that divides evenly by totalRoom
            int maxStock = service.getTonKho();
            return (maxStock / totalRoom) * totalRoom;
        }

        /**
         * Show user-friendly feedback for quantity changes
         */
        private void showQuantityFeedback(String message, boolean isIncrease) {
            // Create a small, non-intrusive tooltip-style message
            JLabel feedbackLabel = new JLabel(message);
            feedbackLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            feedbackLabel.setForeground(isIncrease ? new Color(0, 120, 0) : new Color(120, 0, 0));
            feedbackLabel.setOpaque(true);
            feedbackLabel.setBackground(new Color(255, 255, 200));
            feedbackLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            // Position it near the quantity field
            Point location = quantityField.getLocationOnScreen();

            // Create a temporary popup window
            JWindow popup = new JWindow();
            popup.add(feedbackLabel);
            popup.pack();
            popup.setLocation(location.x, location.y - 25);
            popup.setVisible(true);

            // Auto-hide after 1.5 seconds
            Timer timer = new Timer(1500, e -> popup.dispose());
            timer.setRepeats(false);
            timer.start();
        }

        /**
         * Show stock limit warning with helpful information
         */
        private void showStockLimitWarning(ThongTinDichVu service) {
            int maxPossible = getMaxPossibleQuantity(service);
            String message = String.format(
                    "Không đủ tồn kho!\n" +
                    "Tồn kho hiện tại: %d\n" +
                    "Số lượng tối đa có thể đặt: %d\n" +
                    "(%d phòng × %d = %d)",
                    service.getTonKho(),
                    maxPossible,
                    totalRoom,
                    maxPossible / totalRoom,
                    maxPossible
            );

            JOptionPane.showMessageDialog(panel, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
        }

        private void validateAndUpdateFromTextField() {
            try {
                String text = quantityField.getText().trim();
                if (text.isEmpty()) {
                    text = "0";
                    quantityField.setText("0");
                }

                int newQuantity = Integer.parseInt(text);
                ThongTinDichVu service = filteredServices.get(currentRow);

                if (newQuantity == currentQuantity) {
                    return; // No change
                }

                if (newQuantity < 0) {
                    quantityField.setText(String.valueOf(currentQuantity));
                    JOptionPane.showMessageDialog(panel,
                                                  "Số lượng không thể âm!",
                                                  "Thông báo",
                                                  JOptionPane.WARNING_MESSAGE);
                }
                // Remove service ordered if quantity is zero
                else if (newQuantity == 0) {
                    quantityField.setText("0");
                    currentQuantity = 0;
                    updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), 0, false);
                }
                else if (newQuantity > service.getTonKho()) {
                        quantityField.setText(String.valueOf(currentQuantity));
                        JOptionPane.showMessageDialog(panel,
                                                      "Số lượng vượt quá tồn kho!",
                                                      "Thông báo",
                                                      JOptionPane.WARNING_MESSAGE);
                }
                else {
                    // Check it can divide evenly for multiple rooms - if not, split equally first, the remain will put for last room
                    if (cmbSelectedRoom.getSelectedItem().toString().equalsIgnoreCase(ALL_ROOMS)) {
//                            if (newQuantity % totalRoom != 0) {
//                                quantityField.setText(String.valueOf(currentQuantity));
//                                JOptionPane.showMessageDialog(panel,
//                                                              "Số lượng dịch vụ đã chọn phải chia hết cho " + totalRoom + " phòng!",
//                                                              "Thông báo",
//                                                              JOptionPane.WARNING_MESSAGE);
//                                return;
//                            }
                        currentQuantity = newQuantity;
                        int quantity = newQuantity / totalRoom;
                        System.out.println("Quantity per room: " + quantity);
                        updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), quantity, true);

                        if (newQuantity % totalRoom != 0) {
                            // Handle remainder for last room
                            int remainder = newQuantity % totalRoom;
                            String lastRoom = selectedRoomNames.get(selectedRoomNames.size() - 1);

                            updateServiceOrdersAndUpdateTable(lastRoom, remainder, false);
                            currentQuantity += remainder;
                        }
                        return;
                    }

                    int quantityChange = newQuantity - currentQuantity;
                    currentQuantity = newQuantity;
                    updateServiceOrdersAndUpdateTable(Objects.requireNonNull(cmbSelectedRoom.getSelectedItem()).toString(), quantityChange, true);
                }

            } catch (NumberFormatException e) {
                // Reset to current valid value if invalid input
                quantityField.setText(String.valueOf(currentQuantity));
                JOptionPane.showMessageDialog(panel,
                                              "Vui lòng nhập số hợp lệ!",
                                              "Lỗi",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateServiceOrdersAndUpdateTable(String tenPhong, int quantity, boolean isDirectSet) {
            // If direct set, calculate difference
            if (isDirectSet) {
                // Update all selected rooms
                ThongTinDichVu service = filteredServices.get(currentRow);
                if (Objects.equals(tenPhong, ALL_ROOMS)) {
                    for (String roomName : selectedRoomNames) {
                        // Update existing order if found
                        boolean found = false;
                        for (DonGoiDichVu order : serviceOrders) {
                            if (order.getMaDichVu().equals(service.getMaDichVu()) &&
                                order.getTenPhong().equals(roomName)) {
                                order.setSoLuong(quantity);
                                found = true;
                                break;
                            }
                        }

                        // If not found, add new order
                        if (!found) {
                            serviceOrders.add(new DonGoiDichVu(
                                    service.getMaDichVu(),
                                    roomName,
                                    service.getDonGia(),
                                    quantity
                            ));
                        }
                    }
                }
                // Single room selected
                else {
                    // Update existing order if found
                    boolean found = false;
                    for (DonGoiDichVu order : serviceOrders) {
                        if (order.getMaDichVu().equals(service.getMaDichVu()) &&
                            order.getTenPhong().equals(tenPhong)) {
                            order.setSoLuong(order.getSoLuong() + quantity);
                            found = true;
                            break;
                        }
                    }

                    // If not found, add new order
                    if (!found) {
                        serviceOrders.add(new DonGoiDichVu(
                                service.getMaDichVu(),
                                tenPhong,
                                service.getDonGia(),
                                quantity
                        ));
                    }
                }

                // Check and remove any orders with zero quantity
                serviceOrders.removeIf(order -> order.getSoLuong() <= 0);

                updateSelectedServicesTable();
                serviceTableModel.setValueAt(currentQuantity, currentRow, 4);
                return;
            }

            // Handle zero quantity case
            if (quantity == 0) {
                // Remove service order if quantity is zero
                ThongTinDichVu service = filteredServices.get(currentRow);
                if (tenPhong.equalsIgnoreCase(ALL_ROOMS)) {
                    serviceOrders.removeIf(order -> order.getMaDichVu().equals(service.getMaDichVu()));
                } else {
                    serviceOrders.removeIf(order -> order.getMaDichVu().equals(service.getMaDichVu()) &&
                                                    order.getTenPhong().equals(tenPhong));
                }

                updateSelectedServicesTable();
                serviceTableModel.setValueAt(currentQuantity, currentRow, 4);
                return;
            }

            // Update all selected rooms
            ThongTinDichVu service = filteredServices.get(currentRow);
            if (Objects.equals(tenPhong, ALL_ROOMS)) {
                for (String roomName : selectedRoomNames) {
                    // Update existing order if found
                    boolean found = false;
                    for (DonGoiDichVu order : serviceOrders) {
                        if (order.getMaDichVu().equals(service.getMaDichVu()) &&
                            order.getTenPhong().equals(roomName)) {
                            order.setSoLuong(order.getSoLuong() + (quantity / totalRoom));
                            found = true;
                            break;
                        }
                    }

                    // If not found, add new order
                    if (!found) {
                        serviceOrders.add(new DonGoiDichVu(
                                service.getMaDichVu(),
                                roomName,
                                service.getDonGia(),
                                quantity / totalRoom
                        ));
                    }
                }
            }
            // Single room selected
            else {
                // Update existing order if found
                boolean found = false;
                for (DonGoiDichVu order : serviceOrders) {
                    if (order.getMaDichVu().equals(service.getMaDichVu()) &&
                        order.getTenPhong().equals(tenPhong)) {
                        order.setSoLuong(order.getSoLuong() + quantity);
                        found = true;
                        break;
                    }
                }

                // If not found, add new order
                if (!found) {
                    serviceOrders.add(new DonGoiDichVu(
                            service.getMaDichVu(),
                            tenPhong,
                            service.getDonGia(),
                            quantity
                    ));
                }
            }

            // Check and remove any orders with zero quantity
            serviceOrders.removeIf(order -> order.getSoLuong() <= 0);

            updateSelectedServicesTable();

            // Update the main table (quantity column is now at index 4)
            serviceTableModel.setValueAt(currentQuantity, currentRow, 4);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {

            currentQuantity = (Integer) value;
            currentRow = row;
            quantityField.setText(String.valueOf(currentQuantity));

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentQuantity;
        }
    }

    private void hideCloseButton() {
        btnClose.setVisible(false);
    }

    // Custom renderer for service table with alternating row colors and proper styling
    private class ServiceTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set font to non-bold for data
            component.setFont(CustomUI.TABLE_FONT);

            if (isSelected) {
                component.setBackground(CustomUI.ROW_SELECTED_COLOR);
                component.setForeground(CustomUI.black);
            } else {
                // Alternating row colors
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.ROW_EVEN);
                } else {
                    component.setBackground(CustomUI.ROW_ODD);
                }
                component.setForeground(CustomUI.black);
            }

            // Center align text for all columns except custom rendered columns (4 and 5)
            if (column < 4) {
                setHorizontalAlignment(JLabel.CENTER);
            }

            // Add subtle border
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

            return component;
        }
    }

    // Custom renderer for selected services table with alternating row colors and proper styling
    private class SelectedServicesTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Set font to non-bold for data
            component.setFont(CustomUI.TABLE_FONT);

            if (isSelected) {
                component.setBackground(CustomUI.ROW_SELECTED_COLOR);
                component.setForeground(CustomUI.black);
            } else {
                // Alternating row colors
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.ROW_EVEN);
                } else {
                    component.setBackground(CustomUI.ROW_ODD);
                }
                component.setForeground(CustomUI.black);
            }

            // Center align text for all columns
            setHorizontalAlignment(JLabel.CENTER);

            // Add subtle border
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

            return component;
        }
    }
}

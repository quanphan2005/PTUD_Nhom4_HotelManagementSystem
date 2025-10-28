package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.PanelName;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.dto.response.ReservationInfoDetailResponse;
import vn.iuh.dto.response.ReservationResponse;
import vn.iuh.dto.response.RoomUsageServiceResponse;
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
import java.awt.image.BufferedImage;
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
    private int selectedRooms;

    // For existing booking
    private String maChiTietDatPhong;

    // Data
    private List<ThongTinDichVu> allServices;
    private List<ThongTinDichVu> filteredServices;
    private Map<String, Integer> selectedServicesMap;
    private Map<String, Double> servicePricesMap; // Track service prices for total cost calculation
    private Map<String, Boolean> giftServicesMap; // Track which services are marked as gifts
    private ServiceSelectionCallback callback;

    // Formatters
    private DecimalFormat priceFormatter = PriceFormat.getPriceFormatter();

    private final String ALL_SERVICE = "Tất cả loại dịch vụ";

    public interface ServiceSelectionCallback {
        void onServiceConfirmed(List<DonGoiDichVu> ServiceOrders);
    }

    public ServiceSelectionPanel(String parentName, int selectedRooms, String maChiTietDatPhong, ServiceSelectionCallback callback) {
        this.parentName = parentName;

        this.serviceCategoryService = new ServiceCategoryServiceImpl();
        this.goiDichVuService = new GoiDichVuServiceImpl();
        this.callback = callback;
        this.selectedServicesMap = new HashMap<>();
        this.giftServicesMap = new HashMap<>();

        this.selectedRooms = selectedRooms;
        this.maChiTietDatPhong = maChiTietDatPhong;

        initializeComponents();
        loadData();
        setupLayout();
        setupEventHandlers();
    }

    public ServiceSelectionPanel(String maChiTietDatPhong) {

        this.serviceCategoryService = new ServiceCategoryServiceImpl();
        this.goiDichVuService = new GoiDichVuServiceImpl();
        this.selectedServicesMap = new HashMap<>();
        this.giftServicesMap = new HashMap<>();

        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    private void initializeComponents() {
        // Search components
        cmbServiceType = new JComboBox<>();
        cmbServiceType.setFont(CustomUI.smallFont);
        cmbServiceType.setPreferredSize(new Dimension(200, 30));
        cmbServiceType.setMinimumSize(new Dimension(180, 30));

        txtSearchService = new JTextField();
        txtSearchService.setFont(CustomUI.normalFont);
        txtSearchService.setPreferredSize(new Dimension(500, 35));
        txtSearchService.setMinimumSize(new Dimension(500, 35)); // Add minimum size
        txtSearchService.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tên dịch vụ");

        // Info labels with fixed sizes
        lblTotalServices = new JLabel("Tổng dịch vụ: 0");
        lblTotalServices.setFont(CustomUI.normalFont);
        lblTotalServices.setOpaque(true);
        lblTotalServices.setForeground(CustomUI.black);
        lblTotalServices.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        lblTotalServices.setPreferredSize(new Dimension(150, 40)); // Fixed size
        lblTotalServices.setMinimumSize(new Dimension(150, 40)); // Fixed minimum

        lblAvailableServices = new JLabel("Dịch vụ khả dụng: 0");
        lblAvailableServices.setFont(CustomUI.normalFont);
        lblAvailableServices.setOpaque(true);
        lblAvailableServices.setForeground(CustomUI.black);
        lblAvailableServices.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        lblAvailableServices.setPreferredSize(new Dimension(150, 40)); // Fixed size
        lblAvailableServices.setMinimumSize(new Dimension(150, 40)); // Fixed minimum

        // Service table - Updated with Gift column
        String[] serviceColumns = {"Dịch vụ", "Loại", "Giá", "Tồn kho", "Quà", "Đã chọn"};
        serviceTableModel = new DefaultTableModel(serviceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Gift and quantity columns are editable
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

        // Set alternating row colors for regular columns (not gift and quantity)
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

                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Tên
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.25)); // 25% - Loại
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Giá
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Tồn kho
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.10)); // 10% - Quà tặng
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Đã chọn
            }
        });

        // Custom renderer and editor for gift column (column 4)
        serviceTable.getColumnModel().getColumn(4).setCellRenderer(new GiftRenderer());
        serviceTable.getColumnModel().getColumn(4).setCellEditor(new GiftEditor());

        // Custom renderer and editor for quantity column (column 5)
        serviceTable.getColumnModel().getColumn(5).setCellRenderer(new QuantityRenderer());
        serviceTable.getColumnModel().getColumn(5).setCellEditor(new QuantityEditor());

        // Selected services table with fixed column widths
        String[] selectedColumns = {"Dịch vụ", "SL", "Thành tiền"};
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

                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.40)); // 20% - Tên dịch vụ
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.10)); // 25% - Số lượng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.50)); // 15% - Thành tiền
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
        btnClose.setBackground(Color.RED);
        btnClose.setForeground(Color.WHITE);
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
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 40));
        headerPanel.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        headerPanel.setBackground(CustomUI.blue);


        JLabel titleLabel = new JLabel("Gọi dịch vụ (" + selectedRooms + " Phòng)", SwingConstants.CENTER);
        titleLabel.setFont(CustomUI.bigFont);
        titleLabel.setForeground(CustomUI.white);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(btnClose, BorderLayout.EAST);

        // Main content panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // LEFT COLUMN
        // Search area
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, gbc);

        // Info labels
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        mainPanel.add(lblTotalServices, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        mainPanel.add(lblAvailableServices, gbc);

        // Service table
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
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
        gbc.gridx = 2;
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
        panel.setBackground(Color.WHITE);
        panel.add(cmbServiceType);
        panel.add(txtSearchService);
        return panel;
    }

    private void loadData() {
        loadServiceType();
        loadServices();
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

    private void setupEventHandlers() {
        cmbServiceType.addActionListener(e -> handleCmbServiceChangeEvent());

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

        selectedServicesMap.clear();
        giftServicesMap.clear(); // Also clear gift selections

        resetAllRowEditingCell();

        updateServiceTable();
        updateSelectedServicesTable();

        // Update total services cost on booking panel
        if (callback != null) {
            callback.onServiceConfirmed(new ArrayList<>());
        }
    }

    private void resetAllRowEditingCell() {
        // Reset data in maps first
        for (ThongTinDichVu service : filteredServices) {
            selectedServicesMap.put(service.getMaDichVu(), 0);
            giftServicesMap.put(service.getMaDichVu(), false);
        }

        // Then update table display
        for (int i = 0; i < serviceTableModel.getRowCount(); i++) {
            serviceTableModel.setValueAt(false, i, 4); // Reset gift column
            serviceTableModel.setValueAt(0, i, 5);     // Reset quantity column
        }

        // Force table repaint to ensure UI updates
        serviceTable.repaint();
    }


    private void confirmSelection() {
        if (selectedServicesMap.isEmpty() || selectedServicesMap.values().stream().allMatch(qty -> qty == 0)) {
            JOptionPane.showMessageDialog(this,
                                          "Vui lòng chọn ít nhất một dịch vụ",
                                          "Thông báo",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Filter out services with 0 quantity
        List<DonGoiDichVu> serviceOrdered = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedServicesMap.entrySet()) {
            if (entry.getValue() > 0) {
                String serviceId = entry.getKey();
                double price = servicePricesMap.getOrDefault(serviceId, 0.0);
                int quantity = entry.getValue();
                boolean isGift = giftServicesMap.getOrDefault(serviceId, false);
                serviceOrdered.add(new DonGoiDichVu(serviceId, price, quantity, isGift));
            }
        }

        validateServiceQuantity(serviceOrdered);

        // Service ordered for existing booking
        if (maChiTietDatPhong != null) {
            boolean success = goiDichVuService.goiDichVu(maChiTietDatPhong, serviceOrdered,
                                                         Main.getCurrentLoginSession());
            if (success) {
                JOptionPane.showMessageDialog(this,
                                              "Gọi thêm " + serviceOrdered.size() + " dịch vụ thành công.",
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
            callback.onServiceConfirmed(serviceOrdered);
        }

        // Show dialog base on selected rooms
        if (selectedRooms > 1) {
            JOptionPane.showMessageDialog(this,
                                          "Đã thêm " + serviceOrdered.size() + " dịch vụ cho " + selectedRooms + " phòng\n" +
                                          "Số lượng dịch vụ đã gọi sẽ được chia đều cho (" + selectedRooms + " phòng)",
                                          "Xác nhận",
                                          JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                                          "Đã thêm " + serviceOrdered.size() + " dịch vụ cho phòng",
                                          "Xác nhận",
                                          JOptionPane.INFORMATION_MESSAGE);
        }

        Main.showCard(parentName);
    }

    private void updateServiceTable() {
        serviceTableModel.setRowCount(0);

        for (ThongTinDichVu service : filteredServices) {
            int selectedQuantity = selectedServicesMap.getOrDefault(service.getMaDichVu(), 0);
            boolean isGift = giftServicesMap.getOrDefault(service.getMaDichVu(), false);
            Object[] row = {
                    service.getTenDichVu(),
                    service.getTenLoaiDichVu(), // Updated to use correct method name
                    priceFormatter.format(service.getDonGia()) + " VNĐ",
                    service.getTonKho(),
                    isGift, // Gift checkbox value
                    selectedQuantity
            };
            serviceTableModel.addRow(row);
        }
    }

    private void updateSelectedServicesTable() {
        selectedServicesTableModel.setRowCount(0);

        for (Map.Entry<String, Integer> entry : selectedServicesMap.entrySet()) {
            if (entry.getValue() > 0) {
                ThongTinDichVu service = findServiceById(entry.getKey());
                if (service != null) {
                    boolean isGift = giftServicesMap.getOrDefault(entry.getKey(), false);
                    double totalPrice = isGift ? 0 : service.getDonGia() * entry.getValue(); // 0 if gift
                    Object[] row = {
                            service.getTenDichVu(),
                            entry.getValue(),
                            priceFormatter.format(totalPrice) + " VNĐ"
                    };
                    selectedServicesTableModel.addRow(row);
                }
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
        for (Map.Entry<String, Integer> entry : selectedServicesMap.entrySet()) {
            if (entry.getValue() > 0) {
                ThongTinDichVu service = findServiceById(entry.getKey());
                if (service != null) {
                    boolean isGift = giftServicesMap.getOrDefault(entry.getKey(), false);
                    if (!isGift) { // Only add to total if not a gift
                        total += service.getDonGia() * entry.getValue();
                    }
                }
            }
        }
        lblTotalCost.setText("Tổng tiền: " + priceFormatter.format(total) + " VNĐ");
    }

    // Validate that ordered quantities that can divide equally for selected rooms
    private void validateServiceQuantity(List<DonGoiDichVu> serviceOrdered) {
        for (DonGoiDichVu order : serviceOrdered) {
            ThongTinDichVu service = findServiceById(order.getMaDichVu());
            if (order.getSoLuong() % selectedRooms != 0) {
                JOptionPane.showMessageDialog(this,
                                              "Số lượng dịch vụ '" + service.getTenDichVu() +
                                              "' phải chia hết cho số phòng đã chọn (" + selectedRooms + " phòng).",
                                              "Lỗi",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
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
        filterServices();
    }

    private void handleFindServiceOrderedHistory() {
        ServiceOrderedHistoryPanel serviceOrderedHistoryPanel =
                new ServiceOrderedHistoryPanel(maChiTietDatPhong);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                     "Lịch sử gọi dịch vụ", true);

        dialog.setContentPane(serviceOrderedHistoryPanel);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(null); // Center on screen
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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

    // Custom renderer for gift column
    private class GiftRenderer extends JPanel implements TableCellRenderer {
        private JCheckBox giftCheckBox;

        public GiftRenderer() {
            setLayout(new GridBagLayout()); // Use GridBagLayout for perfect centering
            giftCheckBox = new JCheckBox();
            giftCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            giftCheckBox.setVerticalAlignment(SwingConstants.CENTER);
            giftCheckBox.setPreferredSize(new Dimension(50, 50)); // Make checkbox bigger
            giftCheckBox.setOpaque(false); // Make checkbox background transparent

            // Add checkbox to center of panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;
            add(giftCheckBox, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            boolean isGift = (Boolean) value;
            giftCheckBox.setSelected(isGift);

            // Check if service is giftable
            ThongTinDichVu service = filteredServices.get(row);
            giftCheckBox.setEnabled(service.isCoTheTang());

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    // Custom editor for gift column
    private class GiftEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JCheckBox giftCheckBox;
        private boolean currentGiftStatus;
        private int currentRow;

        public GiftEditor() {
            panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for perfect centering
            giftCheckBox = new JCheckBox();
            giftCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            giftCheckBox.setVerticalAlignment(SwingConstants.CENTER);
            giftCheckBox.setOpaque(false); // Make checkbox background transparent

            // Add checkbox to center of panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(giftCheckBox, gbc);

            giftCheckBox.addActionListener(e -> {
                currentGiftStatus = giftCheckBox.isSelected();
                updateGiftStatus();
            });
        }

        private void updateGiftStatus() {
            ThongTinDichVu service = filteredServices.get(currentRow);

            // Only update if service is giftable
            if (service.isCoTheTang()) {
                giftServicesMap.put(service.getMaDichVu(), currentGiftStatus);
                updateSelectedServicesTable();

                // Update the main table
                serviceTableModel.setValueAt(currentGiftStatus, currentRow, 4);

                // Show feedback message
                if (currentGiftStatus) {
                    JOptionPane.showMessageDialog(panel,
                                                  "Dịch vụ '" + service.getTenDichVu() +
                                                  "' đã được đánh dấu là quà tặng (miễn phí)",
                                                  "Quà tặng",
                                                  JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {

            currentGiftStatus = (Boolean) value;
            currentRow = row;
            giftCheckBox.setSelected(currentGiftStatus);

            // Check if service is giftable
            ThongTinDichVu service = filteredServices.get(row);
            giftCheckBox.setEnabled(service.isCoTheTang());

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentGiftStatus;
        }
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

            quantityField = new JTextField("0", 3);
            quantityField.setPreferredSize(new Dimension(40, 25));
            quantityField.setHorizontalAlignment(SwingConstants.CENTER);
            quantityField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            quantityField.setFont(new Font("Arial", Font.PLAIN, 12));
            quantityField.setEditable(false); // Make read-only in renderer

            btnIncrease = new JButton("▲");
            btnIncrease.setPreferredSize(new Dimension(25, 25));
            btnIncrease.setFont(new Font("Arial", Font.BOLD, 10));

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
                quantityField.setBackground(Color.WHITE);
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

            quantityField = new JTextField("0", 3);
            quantityField.setPreferredSize(new Dimension(40, 25));
            quantityField.setHorizontalAlignment(SwingConstants.CENTER);
            quantityField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            quantityField.setFont(new Font("Arial", Font.PLAIN, 12));

            btnIncrease = new JButton("▲");
            btnIncrease.setPreferredSize(new Dimension(25, 25));
            btnIncrease.setFont(new Font("Arial", Font.BOLD, 10));

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
                    updateQuantity();

                    // Show feedback for multi-room decrements
                    if (selectedRooms > 1 && decrementAmount > 1) {
                        showQuantityFeedback("Giảm " + decrementAmount + " (" + selectedRooms + " phòng)", false);
                    }
                } else {
                    // If can't decrease by full amount, set to 0
                    if (currentQuantity > 0) {
                        currentQuantity = 0;
                        quantityField.setText("0");
                        updateQuantity();
                        showQuantityFeedback("Đặt về 0", false);
                    }
                }
            });

            btnIncrease.addActionListener(e -> {
                ThongTinDichVu service = filteredServices.get(currentRow);
                int incrementAmount = getOptimalIncrementAmount(currentQuantity, service);

                if (currentQuantity + incrementAmount <= service.getTonKho()) {
                    currentQuantity += incrementAmount;
                    quantityField.setText(String.valueOf(currentQuantity));
                    updateQuantity();

                    // Show feedback for multi-room increments
                    if (selectedRooms > 1 && incrementAmount > 1) {
                        showQuantityFeedback("Tăng " + incrementAmount + " (" + selectedRooms + " phòng)", true);
                    }
                } else {
                    // Try to add as much as possible up to stock limit
                    int maxPossible = getMaxPossibleQuantity(service);
                    if (maxPossible > currentQuantity) {
                        int actualIncrement = maxPossible - currentQuantity;
                        currentQuantity = maxPossible;
                        quantityField.setText(String.valueOf(currentQuantity));
                        updateQuantity();
                        showQuantityFeedback("Tăng tối đa " + actualIncrement + " (giới hạn tồn kho)", true);
                    } else {
                        showStockLimitWarning(service);
                    }
                }
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
            // For single room booking, always increment by 1
            if (selectedRooms == 1) {
                return 1;
            }

            // For multi-room booking, increment by selectedRooms
            // But ensure we don't exceed stock limit
            int maxPossible = service.getTonKho() - currentQuantity;
            return Math.min(selectedRooms, maxPossible);
        }

        /**
         * Calculate optimal decrement amount based on current quantity and selected rooms
         */
        private int getOptimalDecrementAmount(int currentQuantity, ThongTinDichVu service) {
            // For single room booking, always decrement by 1
            if (selectedRooms == 1) {
                return 1;
            }

            // For multi-room booking, decrement by selectedRooms
            // But ensure we don't go below 0
            return Math.min(selectedRooms, currentQuantity);
        }

        /**
         * Get maximum possible quantity that satisfies room division requirement
         */
        private int getMaxPossibleQuantity(ThongTinDichVu service) {
            // Calculate max quantity that divides evenly by selectedRooms
            int maxStock = service.getTonKho();
            return (maxStock / selectedRooms) * selectedRooms;
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
                    selectedRooms,
                    maxPossible / selectedRooms,
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

                // Validate bounds with improved logic
                newQuantity = validateServiceQuantityWithSuggestion(newQuantity, service);

                currentQuantity = newQuantity;
                updateQuantity();

            } catch (NumberFormatException e) {
                // Reset to current valid value if invalid input
                quantityField.setText(String.valueOf(currentQuantity));
                JOptionPane.showMessageDialog(panel,
                                              "Vui lòng nhập số hợp lệ!",
                                              "Lỗi",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Enhanced validation with helpful suggestions
         */
        private int validateServiceQuantityWithSuggestion(int newQuantity, ThongTinDichVu service) {
            if (newQuantity < 0) {
                newQuantity = 0;
                quantityField.setText("0");
                JOptionPane.showMessageDialog(panel,
                                              "Số lượng không thể âm!",
                                              "Thông báo",
                                              JOptionPane.WARNING_MESSAGE);
            } else
                if (newQuantity > service.getTonKho()) {
                    int maxPossible = getMaxPossibleQuantity(service);
                    newQuantity = maxPossible;
                    quantityField.setText(String.valueOf(newQuantity));
                    showStockLimitWarning(service);
                } else
                    if (newQuantity % selectedRooms != 0) {
                        // Provide smart suggestions for quantity adjustment
                        int roundedDown = (newQuantity / selectedRooms) * selectedRooms;
                        int roundedUp = roundedDown + selectedRooms;

                        // Choose the closer valid value, but don't exceed stock
                        int maxPossible = getMaxPossibleQuantity(service);
                        int suggestion;

                        if (roundedUp <= maxPossible && (newQuantity - roundedDown) > (roundedUp - newQuantity)) {
                            suggestion = roundedUp;
                        } else {
                            suggestion = roundedDown;
                        }

                        newQuantity = suggestion;
                        quantityField.setText(String.valueOf(newQuantity));

                        String message = String.format(
                                "Số lượng dịch vụ phải chia hết cho số phòng đã chọn (%d).\n" +
                                "Đã điều chỉnh thành: %d\n" +
                                "(Mỗi phòng sẽ nhận: %d)",
                                selectedRooms,
                                suggestion,
                                suggestion / selectedRooms
                        );

                        JOptionPane.showMessageDialog(panel, message, "Điều chỉnh số lượng",
                                                      JOptionPane.INFORMATION_MESSAGE);
                    }
            return newQuantity;
        }

        // Keep existing validateServiceQuantity method for backward compatibility
        private int validateServiceQuantity(int newQuantity, ThongTinDichVu service) {
            return validateServiceQuantityWithSuggestion(newQuantity, service);
        }

        private void updateQuantity() {
            ThongTinDichVu service = filteredServices.get(currentRow);
            selectedServicesMap.put(service.getMaDichVu(), currentQuantity);

            updateSelectedServicesTable();

            // Update the main table
            serviceTableModel.setValueAt(currentQuantity, currentRow, 5); // Updated column index
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

            // Center align text for all columns
            setHorizontalAlignment(JLabel.CENTER);

            // Add subtle border
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

            return component;
        }
    }

    // Custom glass pane for blur effect
    private static class BlurGlassPane extends JComponent {
        private BufferedImage blurBuffer;
        private JFrame parentFrame;

        public BlurGlassPane(JFrame parent) {
            this.parentFrame = parent;
            setOpaque(false);
            createBlurEffect();
        }

        private void createBlurEffect() {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Capture the parent frame content
                    Rectangle bounds = parentFrame.getBounds();
                    Robot robot = new Robot();
                    BufferedImage screenshot = robot.createScreenCapture(bounds);

                    // Create blurred version
                    blurBuffer = createBlurredImage(screenshot);

                    repaint();
                } catch (Exception e) {
                    System.err.println("Failed to create blur effect: " + e.getMessage());
                    // Fallback to semi-transparent overlay
                    createFallbackOverlay();
                }
            });
        }

        private BufferedImage createBlurredImage(BufferedImage source) {
            if (source == null) return null;

            int width = source.getWidth();
            int height = source.getHeight();

            BufferedImage blurred = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = blurred.createGraphics();

            // Apply blur using convolution
            float[] blurKernel = {
                    1f / 16f, 2f / 16f, 1f / 16f,
                    2f / 16f, 4f / 16f, 2f / 16f,
                    1f / 16f, 2f / 16f, 1f / 16f
            };

            try {
                java.awt.image.ConvolveOp blurOp = new java.awt.image.ConvolveOp(
                        new java.awt.image.Kernel(3, 3, blurKernel),
                        java.awt.image.ConvolveOp.EDGE_NO_OP,
                        null
                );

                // Apply blur effect multiple times for stronger blur
                BufferedImage temp = source;
                for (int i = 0; i < 3; i++) {
                    temp = blurOp.filter(temp, null);
                }

                g2d.drawImage(temp, 0, 0, null);

                // Add semi-transparent overlay for better effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, width, height);

            } finally {
                g2d.dispose();
            }

            return blurred;
        }

        private void createFallbackOverlay() {
            // Simple fallback - semi-transparent dark overlay
            blurBuffer = new BufferedImage(
                    parentFrame.getWidth(),
                    parentFrame.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2d = blurBuffer.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, blurBuffer.getWidth(), blurBuffer.getHeight());
            g2d.dispose();

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (blurBuffer != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Draw the blurred background
                g2d.drawImage(blurBuffer, 0, 0, getWidth(), getHeight(), null);

                g2d.dispose();
            } else {
                // Fallback overlay while blur is being created
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}

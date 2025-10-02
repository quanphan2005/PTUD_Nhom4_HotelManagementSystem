package vn.iuh.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.servcie.GoiDichVuService;
import vn.iuh.servcie.impl.GoiDichVuServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class ServiceSelectionDialog extends JDialog {
    private GoiDichVuService goiDichVuService;

    // Components
    private JTextField txtSearchService;
    private JButton btnSearch;
    private JLabel lblTotalServices;
    private JLabel lblAvailableServices;
    private JTable serviceTable;
    private DefaultTableModel serviceTableModel;
    private JTable selectedServicesTable;
    private DefaultTableModel selectedServicesTableModel;
    private JLabel lblTotalCost;
    private JButton btnReset;
    private JButton btnConfirm;

    // Data
    private List<ThongTinDichVu> allServices;
    private List<ThongTinDichVu> filteredServices;
    private Map<String, Integer> selectedServicesMap;
    private Map<String, Boolean> giftServicesMap; // Track which services are marked as gifts
    private ServiceSelectionCallback callback;

    // Formatters
    private DecimalFormat priceFormatter = new DecimalFormat("#,###");

    public interface ServiceSelectionCallback {
        void onServiceConfirmed(Map<String, Integer> selectedServices);
    }

    public ServiceSelectionDialog(Frame parent, ServiceSelectionCallback callback) {
        super(parent, "Gọi dịch vụ", true);

        this.goiDichVuService = new GoiDichVuServiceImpl();
        this.callback = callback;
        this.selectedServicesMap = new HashMap<>();
        this.giftServicesMap = new HashMap<>(); // Initialize gift tracking

        initializeComponents();
        loadServices();
        setupLayout();
        setupEventHandlers();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1200, 650);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Search components
        txtSearchService = new JTextField();
        txtSearchService.setFont(CustomUI.normalFont);
        txtSearchService.setPreferredSize(new Dimension(600, 35));
        txtSearchService.setMinimumSize(new Dimension(500, 35)); // Add minimum size
        txtSearchService.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm");

        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(200, 150, 255));
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFont(CustomUI.normalFont);
        btnSearch.setPreferredSize(new Dimension(200, 35));
        btnSearch.setMinimumSize(new Dimension(200, 35)); // Add minimum size
        btnSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Info labels with fixed sizes
        lblTotalServices = new JLabel("Tổng dịch vụ: 0");
        lblTotalServices.setFont(CustomUI.normalFont);
        lblTotalServices.setOpaque(true);
        lblTotalServices.setBackground(new Color(100, 150, 255));
        lblTotalServices.setForeground(Color.WHITE);
        lblTotalServices.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        lblTotalServices.setPreferredSize(new Dimension(150, 40)); // Fixed size
        lblTotalServices.setMinimumSize(new Dimension(150, 40)); // Fixed minimum

        lblAvailableServices = new JLabel("Dịch vụ khả dụng: 0");
        lblAvailableServices.setFont(CustomUI.normalFont);
        lblAvailableServices.setOpaque(true);
        lblAvailableServices.setBackground(Color.GREEN);
        lblAvailableServices.setForeground(Color.BLACK);
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
        serviceTable.setFont(CustomUI.smallFont);
        serviceTable.setRowHeight(40);
        serviceTable.getTableHeader().setFont(CustomUI.normalFont);
        serviceTable.getTableHeader().setBackground(Color.LIGHT_GRAY);

        // Set fixed column widths to prevent resizing
        serviceTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Tên
        serviceTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Loại
        serviceTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Giá
        serviceTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Tồn kho
        serviceTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Quà tặng
        serviceTable.getColumnModel().getColumn(5).setPreferredWidth(140); // Đã chọn

        // Lock column widths - set min and max to same as preferred
        serviceTable.getColumnModel().getColumn(0).setMinWidth(200);
        serviceTable.getColumnModel().getColumn(0).setMaxWidth(200);
        serviceTable.getColumnModel().getColumn(1).setMinWidth(150);
        serviceTable.getColumnModel().getColumn(1).setMaxWidth(150);
        serviceTable.getColumnModel().getColumn(2).setMinWidth(120);
        serviceTable.getColumnModel().getColumn(2).setMaxWidth(120);
        serviceTable.getColumnModel().getColumn(3).setMinWidth(100);
        serviceTable.getColumnModel().getColumn(3).setMaxWidth(100);
        serviceTable.getColumnModel().getColumn(4).setMinWidth(80);
        serviceTable.getColumnModel().getColumn(4).setMaxWidth(80);
        serviceTable.getColumnModel().getColumn(5).setMinWidth(140);
        serviceTable.getColumnModel().getColumn(5).setMaxWidth(140);

        // Disable auto-resize to maintain fixed column widths
        serviceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Disable column selection and reordering to prevent movement
        serviceTable.setColumnSelectionAllowed(false);
        serviceTable.getTableHeader().setReorderingAllowed(false);
        serviceTable.getTableHeader().setResizingAllowed(false);

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
        selectedServicesTable = new JTable(selectedServicesTableModel);
        selectedServicesTable.setFont(CustomUI.smallFont);
        selectedServicesTable.setRowHeight(30);
        selectedServicesTable.getTableHeader().setFont(CustomUI.smallFont);
        selectedServicesTable.getTableHeader().setBackground(Color.LIGHT_GRAY);

        // Set fixed column widths for selected services table
        selectedServicesTable.getColumnModel().getColumn(0).setPreferredWidth(130); // Tên
        selectedServicesTable.getColumnModel().getColumn(1).setPreferredWidth(50);  // SL
        selectedServicesTable.getColumnModel().getColumn(2).setPreferredWidth(140); // Thành tiền
        selectedServicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Action buttons with fixed sizes
        btnReset = new JButton("Hoàn Tác");
        btnReset.setBackground(Color.YELLOW);
        btnReset.setForeground(Color.BLACK);
        btnReset.setFont(CustomUI.normalFont);
        btnReset.setPreferredSize(new Dimension(120, 40));
        btnReset.setMinimumSize(new Dimension(120, 40)); // Fixed minimum
        btnReset.setMaximumSize(new Dimension(120, 40)); // Fixed maximum
        btnReset.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnConfirm = new JButton("Xác Nhận");
        btnConfirm.setBackground(Color.GREEN);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(CustomUI.normalFont);
        btnConfirm.setPreferredSize(new Dimension(120, 40));
        btnConfirm.setMinimumSize(new Dimension(120, 40)); // Fixed minimum
        btnConfirm.setMaximumSize(new Dimension(120, 40)); // Fixed maximum
        btnConfirm.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Total cost label with fixed size and improved formatting
        lblTotalCost = new JLabel("Tổng tiền: 0 VNĐ");
        lblTotalCost.setFont(CustomUI.normalFont);
        lblTotalCost.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        lblTotalCost.setOpaque(true);
        lblTotalCost.setBackground(new Color(255, 255, 200));
        lblTotalCost.setPreferredSize(new Dimension(280, 40)); // Fixed wider size
        lblTotalCost.setMinimumSize(new Dimension(280, 40)); // Fixed minimum
        lblTotalCost.setMaximumSize(new Dimension(280, 40)); // Fixed maximum
        lblTotalCost.setHorizontalAlignment(SwingConstants.CENTER);

    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(65, 130, 255));
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("Gọi dịch vụ", SwingConstants.CENTER);
        titleLabel.setFont(CustomUI.veryBigFont);
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Main content panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // LEFT COLUMN
        // Search area
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, gbc);

        // Info labels
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.35;
        mainPanel.add(lblTotalServices, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        mainPanel.add(lblAvailableServices, gbc);

        // Service table
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 1.0;
        JScrollPane serviceScrollPane = new JScrollPane(serviceTable);
        serviceScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách dịch vụ"));
        serviceScrollPane.setPreferredSize(new Dimension(500, 400));
        serviceScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(serviceScrollPane, gbc);

        // RIGHT COLUMN
        // Action buttons
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.1; gbc.weighty = 0;
        mainPanel.add(btnReset, gbc);

        gbc.gridy = 1;
        mainPanel.add(btnConfirm, gbc);

        // Total cost
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        mainPanel.add(lblTotalCost, gbc);

        // Selected services table
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.4; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        JScrollPane selectedScrollPane = new JScrollPane(selectedServicesTable);
        selectedScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        selectedScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách đã chọn"));
        selectedScrollPane.setPreferredSize(new Dimension(350, 400));
        mainPanel.add(selectedScrollPane, gbc);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.add(txtSearchService);
        panel.add(btnSearch);
        return panel;
    }

    private void loadServices() {
        allServices = goiDichVuService.timTatCaThongTinDichVu();
        filteredServices = new ArrayList<>(allServices);
        updateServiceTable();
        updateInfoLabels();
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

    private ThongTinDichVu findServiceById(String serviceId) {
        return allServices.stream()
            .filter(service -> service.getMaDichVu().equals(serviceId))
            .findFirst()
            .orElse(null);
    }

    private void filterServices() {
        String searchText = txtSearchService.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredServices = new ArrayList<>(allServices);
        } else {
            filteredServices = allServices.stream()
                .filter(service ->
                    service.getTenDichVu().toLowerCase().contains(searchText) ||
                    service.getTenLoaiDichVu().toLowerCase().contains(searchText)
                )
                .collect(java.util.stream.Collectors.toList());
        }

        updateServiceTable();
        updateInfoLabels();
    }

    private void resetAllSelections() {
        selectedServicesMap.clear();
        giftServicesMap.clear(); // Also clear gift selections
        updateServiceTable();
        updateSelectedServicesTable();

        JOptionPane.showMessageDialog(this,
            "Đã xóa tất cả dịch vụ đã chọn",
            "Hoàn tác",
            JOptionPane.INFORMATION_MESSAGE);
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
        Map<String, Integer> finalSelection = new HashMap<>();
        for (Map.Entry<String, Integer> entry : selectedServicesMap.entrySet()) {
            if (entry.getValue() > 0) {
                finalSelection.put(entry.getKey(), entry.getValue());
            }
        }

        if (callback != null) {
            callback.onServiceConfirmed(finalSelection);
        }

        dispose();
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
            giftCheckBox.setPreferredSize(new Dimension(20, 20)); // Make checkbox bigger
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
                        "Dịch vụ '" + service.getTenDichVu() + "' đã được đánh dấu là quà tặng (miễn phí)",
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
        private JLabel quantityLabel;
        private JButton btnDecrease;
        private JButton btnIncrease;

        public QuantityRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

            btnDecrease = new JButton("▼");
            btnDecrease.setPreferredSize(new Dimension(25, 25));
            btnDecrease.setFont(new Font("Arial", Font.BOLD, 10));

            quantityLabel = new JLabel("0", SwingConstants.CENTER);
            quantityLabel.setPreferredSize(new Dimension(30, 25));
            quantityLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            quantityLabel.setOpaque(true);
            quantityLabel.setBackground(Color.WHITE);

            btnIncrease = new JButton("▲");
            btnIncrease.setPreferredSize(new Dimension(25, 25));
            btnIncrease.setFont(new Font("Arial", Font.BOLD, 10));

            add(btnDecrease);
            add(quantityLabel);
            add(btnIncrease);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            int quantity = (Integer) value;
            quantityLabel.setText(String.valueOf(quantity));

            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    // Custom editor for quantity column (updated for new column index)
    private class QuantityEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JLabel quantityLabel;
        private JButton btnDecrease;
        private JButton btnIncrease;
        private int currentQuantity;
        private int currentRow;

        public QuantityEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

            btnDecrease = new JButton("▼");
            btnDecrease.setPreferredSize(new Dimension(25, 25));
            btnDecrease.setFont(new Font("Arial", Font.BOLD, 10));

            quantityLabel = new JLabel("0", SwingConstants.CENTER);
            quantityLabel.setPreferredSize(new Dimension(30, 25));
            quantityLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            quantityLabel.setOpaque(true);
            quantityLabel.setBackground(Color.WHITE);

            btnIncrease = new JButton("▲");
            btnIncrease.setPreferredSize(new Dimension(25, 25));
            btnIncrease.setFont(new Font("Arial", Font.BOLD, 10));

            panel.add(btnDecrease);
            panel.add(quantityLabel);
            panel.add(btnIncrease);

            btnDecrease.addActionListener(e -> {
                if (currentQuantity > 0) {
                    currentQuantity--;
                    updateQuantity();
                }
            });

            btnIncrease.addActionListener(e -> {
                ThongTinDichVu service = filteredServices.get(currentRow);
                if (currentQuantity < service.getTonKho()) {
                    currentQuantity++;
                    updateQuantity();
                } else {
                    JOptionPane.showMessageDialog(panel,
                        "Không đủ tồn kho! Tồn kho hiện tại: " + service.getTonKho(),
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        private void updateQuantity() {
            quantityLabel.setText(String.valueOf(currentQuantity));

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
            quantityLabel.setText(String.valueOf(currentQuantity));

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentQuantity;
        }
    }

    private void setupEventHandlers() {
        btnSearch.addActionListener(e -> filterServices());

        txtSearchService.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterServices();
            }
        });

        btnReset.addActionListener(e -> resetAllSelections());

        btnConfirm.addActionListener(e -> confirmSelection());
    }
}

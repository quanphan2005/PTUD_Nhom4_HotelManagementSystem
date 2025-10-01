package vn.iuh.gui.dialog;

import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.DichVu;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.servcie.GoiDichVuService;
import vn.iuh.servcie.ServiceItemService;
import vn.iuh.servcie.impl.GoiDichVuServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceSelectionDialog extends JDialog {
    GoiDichVuService goiDichVuService;

    private JTextField txtSearchService;
    private JPanel serviceListPanel;
    private JLabel lblTotalServiceCost;
    private List<String> selectedServices = new ArrayList<>();
    private ServiceSelectionCallback callback;

    public interface ServiceSelectionCallback {
        void onServiceConfirmed(Map<String, Integer> selectedServices);
    }

    public ServiceSelectionDialog(Frame parent, ServiceSelectionCallback callback) {
        super(parent, "GỌI DỊCH VỤ", true);

        goiDichVuService = new GoiDichVuServiceImpl();

        this.callback = callback;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Search field
        txtSearchService = new JTextField("Tìm kiếm");
        txtSearchService.setPreferredSize(new Dimension(350, 40));
        txtSearchService.setFont(CustomUI.normalFont);
        txtSearchService.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        // Service list panel
        serviceListPanel = new JPanel();
        serviceListPanel.setLayout(new BoxLayout(serviceListPanel, BoxLayout.Y_AXIS));
        serviceListPanel.setBackground(new Color(255, 255, 204));

        // Add sample services
        List<ThongTinDichVu> danhSachThongTinDichVu = goiDichVuService.timTatCaThongTinDichVu();
        for (ThongTinDichVu dv : danhSachThongTinDichVu) {
            addServiceItem(dv);
        }

        // Total cost label
        lblTotalServiceCost = new JLabel("Tổng tiền:                                        500.000");
        lblTotalServiceCost.setFont(CustomUI.normalFont);
        lblTotalServiceCost.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        lblTotalServiceCost.setPreferredSize(new Dimension(350, 40));
        lblTotalServiceCost.setOpaque(true);
        lblTotalServiceCost.setBackground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(255, 255, 204));

        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(255, 255, 204));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search field at the top
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0;
        contentPanel.add(txtSearchService, gbc);

        // Service list panel
        JScrollPane serviceScrollPane = new JScrollPane(serviceListPanel);
        serviceScrollPane.setPreferredSize(new Dimension(350, 250));
        serviceScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        serviceScrollPane.setBackground(new Color(255, 255, 204));
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(serviceScrollPane, gbc);

        // Total cost label
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(lblTotalServiceCost, gbc);

        // Confirm button
        JButton btnConfirmService = new JButton("XÁC NHẬN");
        btnConfirmService.setBackground(CustomUI.lightGreen);
        btnConfirmService.setForeground(Color.WHITE);
        btnConfirmService.setFont(CustomUI.normalFont);
        btnConfirmService.setPreferredSize(new Dimension(350, 50));
        btnConfirmService.addActionListener(e -> confirmServiceSelection());
        gbc.gridy = 3;
        contentPanel.add(btnConfirmService, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // Add focus listener to search field for placeholder text behavior
        txtSearchService.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearchService.getText().equals("Tìm kiếm")) {
                    txtSearchService.setText("");
                    txtSearchService.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearchService.getText().isEmpty()) {
                    txtSearchService.setText("Tìm kiếm");
                    txtSearchService.setForeground(Color.GRAY);
                }
            }
        });

        // Initial placeholder text style
        txtSearchService.setForeground(Color.GRAY);
    }

    private void addServiceItem(ThongTinDichVu thongTinDichVu) {
        JPanel serviceItem = new JPanel(new BorderLayout());
        serviceItem.setBackground(Color.WHITE);
        serviceItem.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        serviceItem.setPreferredSize(new Dimension(350, 35));

        JLabel nameLabel = new JLabel(thongTinDichVu.getTenDichVu());
        nameLabel.setFont(CustomUI.normalFont);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel priceLabel = new JLabel(String.format("%.0f", thongTinDichVu.getDonGia()));
        priceLabel.setFont(CustomUI.normalFont);
        priceLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        serviceItem.add(nameLabel, BorderLayout.WEST);
        serviceItem.add(priceLabel, BorderLayout.EAST);

        // Add click functionality
        serviceItem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectService(thongTinDichVu.getTenDichVu(), String.valueOf(thongTinDichVu.getDonGia()));
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                serviceItem.setBackground(CustomUI.lightBlue);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                serviceItem.setBackground(Color.WHITE);
            }
        });

        serviceListPanel.add(serviceItem);
        serviceListPanel.add(Box.createVerticalStrut(5));
    }

    // TODO - Implement service selection logic
    private void selectService(String serviceName, String priceStr) {
        String quantityStr = JOptionPane.showInputDialog(this,
            "Nhập số lượng cho dịch vụ: " + serviceName,
            "Chọn số lượng",
            JOptionPane.QUESTION_MESSAGE);

        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr.trim());
                if (quantity > 0) {
                    selectedServices.add(serviceName + " (x" + quantity + ")");
                    updateTotalServiceCost();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // TODO - Calculate total cost based on selected services and their quantities
    private void updateTotalServiceCost() {
        // Simple calculation - in real implementation, you'd calculate based on actual services and quantities
        double total = selectedServices.size() * 75000; // Sample calculation
        lblTotalServiceCost.setText("Tổng tiền:                                        " + String.format("%.0f", total));
    }

    private void confirmServiceSelection() {
        if (!selectedServices.isEmpty()) {
            if (callback != null) {
                callback.onServiceConfirmed(new HashMap<>());
            }
            JOptionPane.showMessageDialog(this,
                "Đã chọn " + selectedServices.size() + " dịch vụ",
                "Xác nhận", JOptionPane.INFORMATION_MESSAGE);
        }
        dispose();
    }

    // Public methods for external access
    public List<String> getSelectedServices() {
        return new ArrayList<>(selectedServices);
    }

    public void clearSelectedServices() {
        selectedServices.clear();
        updateTotalServiceCost();
    }
}

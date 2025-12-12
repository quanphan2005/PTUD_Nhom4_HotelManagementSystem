package vn.iuh.gui.dialog;

import vn.iuh.dto.response.ServicePriceHistoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.service.ServiceService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChiTietDichVuDialog extends JDialog {

    private final ServiceResponse service;
    private final ServiceService serviceService;

    private final DefaultTableModel historyTableModel;
    private final JTable historyTable;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ChiTietDichVuDialog(Window owner, ServiceResponse service, ServiceService serviceService) {
        super(owner, "Chi tiết dịch vụ: " + (service != null ? service.getTenDichVu() : ""), ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.serviceService = serviceService;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 520);
        setLocationRelativeTo(owner);

        // Top panel: dịch vụ info
        add(createTopInfoPanel(), BorderLayout.NORTH);

        // Center: history table
        String[] cols = {"Tên dịch vụ", "Tên nhân viên", "Thời gian thay đổi", "Giá sau khi đổi"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        styleTable(historyTable);
        JScrollPane sp = new JScrollPane(historyTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sp, BorderLayout.CENTER);

        // Bottom: close button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);

        loadHistory(); // nạp dữ liệu lịch sử
    }

    private JPanel createTopInfoPanel() {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Mã dịch vụ:"), gc);
        gc.gridx = 1;
        p.add(new JLabel(service != null ? service.getMaDichVu() : "-"), gc);

        gc.gridx = 0; gc.gridy = 1;
        p.add(new JLabel("Tên dịch vụ:"), gc);
        gc.gridx = 1;
        p.add(new JLabel(service != null ? service.getTenDichVu() : "-"), gc);

        gc.gridx = 0; gc.gridy = 2;
        p.add(new JLabel("Tồn kho:"), gc);
        gc.gridx = 1;
        p.add(new JLabel(service != null ? String.valueOf(service.getTonKho()) : "-"), gc);

        gc.gridx = 0; gc.gridy = 3;
        p.add(new JLabel("Loại (mã):"), gc);
        gc.gridx = 1;
        p.add(new JLabel(service != null ? service.getMaLoaiDichVu() : "-"), gc);

        gc.gridx = 0; gc.gridy = 4;
        p.add(new JLabel("Giá hiện tại:"), gc);
        gc.gridx = 1;
        double g = (service != null && service.getGiaHienTai() != null) ? service.getGiaHienTai() : 0.0;
        p.add(new JLabel(formatPrice(g)), gc);

        return p;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(44);
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 36));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(59,130,246));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JLabel)c).setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });
    }

    private void loadHistory() {
        SwingUtilities.invokeLater(() -> {
            historyTableModel.setRowCount(0);
            if (service == null) return;
            List<ServicePriceHistoryResponse> histories;
            try {
                histories = serviceService.layLichSuGiaDichVu(service.getMaDichVu());
            } catch (Exception ex) {
                ex.printStackTrace();
                histories = java.util.Collections.emptyList();
            }

            if (histories == null || histories.isEmpty()) {
                historyTableModel.addRow(new Object[] {"-", "-", "-", "-"});
                return;
            }

            for (ServicePriceHistoryResponse h : histories) {
                String tenDV = h.getTenDichVu() != null ? h.getTenDichVu() : (service.getTenDichVu() != null ? service.getTenDichVu() : "-");
                String tenNV = h.getTenNhanVien() != null ? h.getTenNhanVien() : (h.getMaNhanVien() != null ? h.getMaNhanVien() : "-");
                String ngay = h.getNgayThayDoi() != null ? SDF.format(h.getNgayThayDoi()) : "-";
                String gia = h.getGiaSauThayDoi() != null ? formatPrice(h.getGiaSauThayDoi()) : "0 VNĐ";
                historyTableModel.addRow(new Object[] {tenDV, tenNV, ngay, gia});
            }
        });
    }

    private static String formatPrice(double price) {
        if (price <= 0.0) return "0 VNĐ";
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " VNĐ";
    }
}

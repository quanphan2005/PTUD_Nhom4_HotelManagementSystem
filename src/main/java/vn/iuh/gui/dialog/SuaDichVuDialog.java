package vn.iuh.gui.dialog;

import vn.iuh.dto.response.ServicePriceHistoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.service.ServiceService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SuaDichVuDialog extends JDialog {

    private final ServiceResponse service;
    private final ServiceService serviceService;
    private final Runnable onSuccess;

    private JTextField tfMa;
    private JTextField tfTen;
    private JTextField tfTonKho;
    private JComboBox<String> cbLoai;
    private JTextField tfGia;

    private final DefaultTableModel historyModel;
    private final JTable historyTable;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SuaDichVuDialog(Window owner, ServiceResponse service, ServiceService serviceService, Runnable onSuccess) {
        super(owner, "Sửa dịch vụ: " + (service != null ? service.getMaDichVu() : ""), ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.serviceService = serviceService;
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout());
        setSize(820, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(createInfoPanel(), BorderLayout.NORTH);

        String[] cols = {"Tên dịch vụ", "Tên nhân viên", "Thời gian thay đổi", "Giá sau khi đổi"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyModel);
        styleTable(historyTable);
        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu");
        bottom.add(btnCancel);
        bottom.add(btnSave);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        loadHistory();
    }

    private JPanel createInfoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Mã dịch vụ:"), gc);
        gc.gridx = 1;
        tfMa = new JTextField(16);
        tfMa.setEditable(false);
        tfMa.setText(service != null ? service.getMaDichVu() : "");
        p.add(tfMa, gc);

        gc.gridx = 0; gc.gridy = 1;
        p.add(new JLabel("Tên dịch vụ:"), gc);
        gc.gridx = 1;
        tfTen = new JTextField(28);
        tfTen.setText(service != null ? service.getTenDichVu() : "");
        p.add(tfTen, gc);

        gc.gridx = 0; gc.gridy = 2;
        p.add(new JLabel("Tồn kho:"), gc);
        gc.gridx = 1;
        tfTonKho = new JTextField(8);
        tfTonKho.setText(String.valueOf(service != null ? service.getTonKho() : 0));
        p.add(tfTonKho, gc);

        gc.gridx = 0; gc.gridy = 3;
        p.add(new JLabel("Loại dịch vụ:"), gc);
        gc.gridx = 1;
        cbLoai = new JComboBox<>();
        try {
            List<LoaiDichVu> list = serviceService.layTatCaLoaiDichVu();
            cbLoai.addItem("Không chọn");
            if (list != null) {
                for (LoaiDichVu l : list) {
                    cbLoai.addItem(l.getMaLoaiDichVu() + " - " + l.getTenDichVu());
                }
            }
            // chọn mặc định
            if (service != null && service.getMaLoaiDichVu() != null) {
                for (int i = 0; i < cbLoai.getItemCount(); i++) {
                    String it = cbLoai.getItemAt(i);
                    if (it != null && it.startsWith(service.getMaLoaiDichVu() + " -")) {
                        cbLoai.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            cbLoai.addItem("Không lấy được loại");
        }
        p.add(cbLoai, gc);

        gc.gridx = 0; gc.gridy = 4;
        p.add(new JLabel("Giá hiện tại (VNĐ):"), gc);
        gc.gridx = 1;
        tfGia = new JTextField(12);
        tfGia.setText(service != null && service.getGiaHienTai() != null ? String.valueOf(service.getGiaHienTai()) : "0");
        p.add(tfGia, gc);

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
            historyModel.setRowCount(0);
            if (service == null) return;
            List<ServicePriceHistoryResponse> histories;
            try {
                histories = serviceService.layLichSuGiaDichVu(service.getMaDichVu());
            } catch (Exception ex) {
                ex.printStackTrace();
                histories = java.util.Collections.emptyList();
            }
            if (histories == null || histories.isEmpty()) {
                historyModel.addRow(new Object[] {"-", "-", "-", "-"});
                return;
            }
            for (ServicePriceHistoryResponse h : histories) {
                String tenDV = h.getTenDichVu() != null ? h.getTenDichVu() : (service.getTenDichVu() != null ? service.getTenDichVu() : "-");
                String tenNV = h.getTenNhanVien() != null ? h.getTenNhanVien() : (h.getMaNhanVien() != null ? h.getMaNhanVien() : "-");
                String ngay = h.getNgayThayDoi() != null ? SDF.format(h.getNgayThayDoi()) : "-";
                String gia = h.getGiaSauThayDoi() != null ? formatPrice(h.getGiaSauThayDoi()) : "0 VNĐ";
                historyModel.addRow(new Object[] {tenDV, tenNV, ngay, gia});
            }
        });
    }

    private static String formatPrice(double price) {
        if (price <= 0.0) return "0 VNĐ";
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " VNĐ";
    }

    private void onSave() {
        // validate inputs
        String ma = tfMa.getText().trim();
        String ten = tfTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên dịch vụ không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ton = 0;
        try { ton = Integer.parseInt(tfTonKho.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tồn kho phải là số nguyên", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String maLoai = null;
        String sel = (String) cbLoai.getSelectedItem();
        if (sel != null && !sel.equals("Không chọn") && sel.contains(" - ")) maLoai = sel.split(" - ")[0];

        double gia = 0.0;
        try { gia = Double.parseDouble(tfGia.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá phải là số", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gọi service để cập nhật — service sẽ kiểm tra trùng tên và thực hiện transaction
        try {
            ServiceResponse updated = serviceService.capNhatDichVu(ma, ten, ton, maLoai, gia);
            if (updated == null) {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Có thể tên dịch vụ đã trùng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Cập nhật thành công: " + updated.getMaDichVu(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // reload lịch sử hiển thị trong dialog
            loadHistory();

            // gọi callback UI reload (nếu có) để load dữ liệu mới nhất và đóng dialog
            if (onSuccess != null) onSuccess.run();

            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package vn.iuh.gui.dialog;

import vn.iuh.dto.response.ServicePriceHistoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.service.ServiceService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

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

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}0-9 \\-]+$"); // chữ cái (unicode), số, khoảng trắng, dấu '-'

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
        // Row / header basic
        table.setRowHeight(48);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(table.getWidth(), 40));
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(59, 130, 246));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        // Custom renderer: center, font, zebra rows, selection color, border
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Arial", Font.PLAIN, 14));

                if (isSelected) {
                    c.setBackground(new Color(0xE6F0FF)); // selected-like
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7F9FB));
                    c.setForeground(Color.BLACK);
                }

                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }

                // border giống panel
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0xE5E7EB)));
                return c;
            }
        };

        // Apply renderer for all Object cells (covers most table content)
        table.setDefaultRenderer(Object.class, customRenderer);

        // hide default grid (we use renderer borders)
        table.setShowGrid(false);

        // Column width behavior: set preferred widths on resize
        table.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = table.getWidth();
                javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
                if (columnModel.getColumnCount() < 4) return;
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.35)); // Tên dịch vụ
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.25)); // Tên nhân viên
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.25)); // Thời gian
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.15)); // Giá
            }
        });

        // đảm bảo header style vẫn được áp dụng nếu table đã được khởi tạo trước khi show
        table.getTableHeader().repaint();
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

        // tên dịch vụ: không rỗng & chỉ cho phép chữ (cả tiếng Việt có dấu), số, khoảng trắng và '-'
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên dịch vụ không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!NAME_PATTERN.matcher(ten).matches()) {
            JOptionPane.showMessageDialog(this, "Tên dịch vụ chỉ được chứa chữ cái, chữ số, khoảng trắng và dấu '-'", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // tồn kho: số nguyên, >= 0
        int ton = 0;
        try { ton = Integer.parseInt(tfTonKho.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tồn kho phải là số nguyên", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ton < 0) {
            JOptionPane.showMessageDialog(this, "Tồn kho không được âm", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // loại dịch vụ: phải chọn
        String maLoai = null;
        String sel = (String) cbLoai.getSelectedItem();
        if (sel == null || sel.equals("Không chọn")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một loại dịch vụ", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sel.contains(" - ")) maLoai = sel.split(" - ")[0];

        // giá: số > 0
        double gia = 0.0;
        try { gia = Double.parseDouble(tfGia.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá phải là số", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (gia <= 0.0) {
            JOptionPane.showMessageDialog(this, "Giá dịch vụ phải lớn hơn 0", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // so sánh thay đổi so với service ban đầu
        boolean nameChanged = !safeEqualsIgnoreCase(service.getTenDichVu(), ten);
        boolean tonChanged = service.getTonKho() != ton;
        boolean loaiChanged = !safeEquals(service.getMaLoaiDichVu(), maLoai);

        Double existingPrice = service.getGiaHienTai();
        boolean priceChanged;
        if (existingPrice == null) {
            priceChanged = gia != 0.0;
        } else {
            priceChanged = Double.compare(existingPrice, gia) != 0;
        }

        if (!nameChanged && !tonChanged && !loaiChanged && !priceChanged) {
            JOptionPane.showMessageDialog(this, "Không có thay đổi để lưu", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        // Gọi service để cập nhật — service sẽ kiểm tra trùng tên và thực hiện transaction
        try {
            ServiceResponse updated = serviceService.capNhatDichVu(ma, ten, ton, maLoai, gia);
            if (updated == null) {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Có thể tên dịch vụ đã trùng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Thông báo cụ thể nếu chỉ giá thay đổi / chỉ thông tin khác thay đổi cũng được (tùy bạn muốn chi tiết thêm)
            JOptionPane.showMessageDialog(this, "Cập nhật thành công: " + updated.getMaDichVu(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // reload lịch sử hiển thị trong dialog (nếu giá đổi thì sẽ show thêm; nếu không đổi thì history không thêm)
            loadHistory();

            // gọi callback UI reload (nếu có) để load dữ liệu mới nhất và đóng dialog
            if (onSuccess != null) onSuccess.run();
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Helpers **/
    private static boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    private static boolean safeEqualsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}

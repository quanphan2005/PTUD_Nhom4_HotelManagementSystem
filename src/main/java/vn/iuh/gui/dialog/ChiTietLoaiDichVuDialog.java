package vn.iuh.gui.dialog;

import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.ServiceService;
import vn.iuh.service.impl.ServiceImpl;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dialog hiển thị chi tiết 1 loại dịch vụ:
 * - Header: mã loại, tên loại, số lượng dịch vụ
 * - Table: danh sách dịch vụ trong loại (Mã, Tên, Tồn kho, Giá)
 */
public class ChiTietLoaiDichVuDialog extends JDialog {

    private final String maLoai;
    private final String tenLoai;
    private final int soLuong;
    private final ServiceService serviceService;

    private final DefaultTableModel tableModel;
    private final JTable table;

    public ChiTietLoaiDichVuDialog(Window owner, String maLoai, String tenLoai, int soLuong, ServiceService serviceService) {
        super(owner, "Chi tiết loại dịch vụ: " + (tenLoai != null ? tenLoai : maLoai), ModalityType.APPLICATION_MODAL);
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
        this.soLuong = soLuong;
        // nếu caller không truyền service, tạo default
        this.serviceService = serviceService != null ? serviceService : new ServiceImpl();

        setLayout(new BorderLayout());
        setSize(900, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(createHeaderPanel(), BorderLayout.NORTH);

        String[] cols = {"Mã dịch vụ", "Tên dịch vụ", "Tồn kho", "Giá", "OBJ"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(new Font("Arial", Font.PLAIN, 13));
                if (isRowSelected(row)) {
                    c.setBackground(new Color(0xE6F0FF));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7F9FB));
                    c.setForeground(Color.BLACK);
                }
                if (c instanceof JLabel) ((JLabel)c).setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };
        styleTable(table);

        // hide OBJ column
        table.getColumnModel().getColumn(4).setMinWidth(0);
        table.getColumnModel().getColumn(4).setMaxWidth(0);
        table.getColumnModel().getColumn(4).setPreferredWidth(0);
        table.getColumnModel().getColumn(4).setResizable(false);

        JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(0, 420));
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);

        // load data
        loadServicesOfCategory();
    }

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Thông tin loại dịch vụ", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel info = new JPanel(new GridLayout(3, 1, 4, 4));
        info.add(new JLabel("Mã loại: " + (maLoai != null ? maLoai : "-")));
        info.add(new JLabel("Tên loại: " + (tenLoai != null ? tenLoai : "-")));
        info.add(new JLabel("Số lượng dịch vụ: " + soLuong));

        p.add(title, BorderLayout.NORTH);
        p.add(info, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(42);
        t.getTableHeader().setPreferredSize(new Dimension(t.getWidth(), 36));
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(59,130,246));
        t.getTableHeader().setForeground(Color.WHITE);
        t.setFont(new Font("Arial", Font.PLAIN, 13));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) ((JLabel)c).setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        // column widths
        t.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int w = t.getWidth();
                TableColumnModel cm = t.getColumnModel();
                if (cm.getColumnCount() < 4) return;
                cm.getColumn(0).setPreferredWidth((int)(w*0.12)); // mã
                cm.getColumn(1).setPreferredWidth((int)(w*0.50)); // tên
                cm.getColumn(2).setPreferredWidth((int)(w*0.12)); // tồn
                cm.getColumn(3).setPreferredWidth((int)(w*0.26)); // giá
            }
        });
    }

    private void loadServicesOfCategory() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (maLoai == null) return;
            List<ServiceResponse> all;
            try {
                all = serviceService.layTatCaDichVuCungGia();
            } catch (Exception ex) {
                ex.printStackTrace();
                all = new ArrayList<>();
            }
            if (all == null || all.isEmpty()) {
                tableModel.addRow(new Object[] {"-", "Không có dịch vụ", "-", "-", null});
                return;
            }
            boolean any = false;
            for (ServiceResponse s : all) {
                if (maLoai.equals(s.getMaLoaiDichVu())) {
                    any = true;
                    Object[] row = new Object[5];
                    row[0] = s.getMaDichVu();
                    row[1] = s.getTenDichVu();
                    row[2] = s.getTonKho();
                    row[3] = formatPrice(s.getGiaHienTai() == null ? 0.0 : s.getGiaHienTai());
                    row[4] = s; // hidden object for future use
                    tableModel.addRow(row);
                }
            }
            if (!any) {
                tableModel.addRow(new Object[] {"-", "Không có dịch vụ thuộc loại này.", "-", "-", null});
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

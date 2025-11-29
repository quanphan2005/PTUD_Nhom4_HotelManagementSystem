// Thay thế toàn bộ file ThemLoaiPhongDialog bằng mã sau
package vn.iuh.gui.dialog;

import vn.iuh.constraint.ActionType;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.NoiThat;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.gui.base.Main;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.NoiThatService;
import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.util.EntityUtil;
import vn.iuh.constraint.EntityIDSymbol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner.DefaultEditor;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dialog thêm loại phòng (đã chỉnh sửa theo yêu cầu)
 */
public class ThemLoaiPhongDialog extends JDialog {

    private final LoaiPhongService loaiPhongService;
    private final NoiThatService noiThatService;

    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JSpinner spnSoNguoi = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JComboBox<String> cboPhanLoai = new JComboBox<>(new String[] {"Thường", "VIP"});

    // new price fields
    private final JFormattedTextField txtGiaNgay;
    private final JFormattedTextField txtGiaGio;

    // Available furniture table
    private final DefaultTableModel availableTableModel = new DefaultTableModel(new Object[] {"Mã", "Tên", "Mô tả"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tableAvailable = new JTable(availableTableModel);

    // Selected furniture table (includes quantity column)
    private final DefaultTableModel selectedTableModel = new DefaultTableModel(new Object[] {"Mã", "Tên", "Mô tả", "Số lượng"}, 0) {
        @Override public boolean isCellEditable(int row, int column) {
            return column == 3; // only quantity editable
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) return Integer.class;
            return String.class;
        }
    };
    private final JTable tableSelected = new JTable(selectedTableModel);

    public ThemLoaiPhongDialog(Frame owner, LoaiPhongService loaiPhongService, NoiThatService noiThatService) {
        super(owner, "Thêm loại phòng", true);
        this.loaiPhongService = loaiPhongService;
        this.noiThatService = noiThatService;

        // setup number formats for price fields
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        txtGiaNgay = new JFormattedTextField(currencyFormat);
        txtGiaGio = new JFormattedTextField(currencyFormat);

        initComponents();
        pack();
        setLocationRelativeTo(owner);
        setPreferredSize(new Dimension(900, 620));
        setMinimumSize(new Dimension(800, 540));
        loadInitialData();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(12,12));
        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Mã (auto)
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Mã loại phòng:"), gc);
        txtMa.setEditable(false);
        txtMa.setBackground(Color.LIGHT_GRAY);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        form.add(txtMa, gc);

        // Row 2: Tên
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Tên loại phòng:"), gc);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1;
        form.add(txtTen, gc);

        // Row 3: Số người
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Số người tối đa:"), gc);
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1;
        form.add(spnSoNguoi, gc);

        // Row 4: Phân loại
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        form.add(new JLabel("Phân loại:"), gc);
        gc.gridx = 1; gc.gridy = 3; gc.weightx = 1;
        form.add(cboPhanLoai, gc);

        // Row 5: Giá ngày
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0;
        form.add(new JLabel("Giá (theo ngày):"), gc);
        txtGiaNgay.setColumns(12);
        txtGiaNgay.setValue(0);
        gc.gridx = 1; gc.gridy = 4; gc.weightx = 1;
        form.add(txtGiaNgay, gc);

        // Row 6: Giá giờ
        gc.gridx = 0; gc.gridy = 5; gc.weightx = 0;
        form.add(new JLabel("Giá (theo giờ):"), gc);
        txtGiaGio.setColumns(12);
        txtGiaGio.setValue(0);
        gc.gridx = 1; gc.gridy = 5; gc.weightx = 1;
        form.add(txtGiaGio, gc);

        main.add(form, BorderLayout.NORTH);

        // Center: furniture selection area (left available, right selected)
        JPanel center = new JPanel(new GridLayout(1,2,12,12));

        // Left - available (JTable)
        JPanel left = new JPanel(new BorderLayout(6,6));
        left.setBorder(BorderFactory.createTitledBorder("Nội thất có sẵn"));
        tableAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableAvailable.setFillsViewportHeight(true);
        left.add(new JScrollPane(tableAvailable), BorderLayout.CENTER);

        JButton btnAdd = new JButton("Thêm >>");
        btnAdd.addActionListener(e -> addSelectedFurniture());
        JPanel leftSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftSouth.add(btnAdd);
        left.add(leftSouth, BorderLayout.SOUTH);

        // Right - selected (JTable with quantity Spinner editor)
        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setBorder(BorderFactory.createTitledBorder("Nội thất cho loại phòng (đã chọn)"));
        tableSelected.setFillsViewportHeight(true);
        // set spinner editor for quantity column
        tableSelected.getColumnModel().getColumn(3).setCellEditor(new SpinnerEditor(1, 1, 999, 1));
        right.add(new JScrollPane(tableSelected), BorderLayout.CENTER);

        JButton btnRemove = new JButton("<< Bỏ");
        btnRemove.addActionListener(e -> removeSelectedFromTable());
        JPanel rightSouth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightSouth.add(btnRemove);
        right.add(rightSouth, BorderLayout.SOUTH);

        center.add(left);
        center.add(right);

        main.add(center, BorderLayout.CENTER);

        // Buttons bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        bottom.add(btnCancel);
        bottom.add(btnSave);

        main.add(bottom, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void loadInitialData() {
        // 1) Sinh mã mới: gọi service để lấy latest (bao gồm deleted). Nếu service không hỗ trợ, fallback.
        try {
            LoaiPhong latest = null;
            try {
                latest = loaiPhongService.findLatestIncludingDeleted();
            } catch (Throwable ignored) {
                latest = null;
            }

            String newId;
            if (latest != null && latest.getMaLoaiPhong() != null) {
                newId = EntityUtil.increaseEntityID(latest.getMaLoaiPhong(), "LP", 8);
            } else {
                newId = EntityUtil.increaseEntityID(null, "LP", 8);
            }
            txtMa.setText(newId);
        } catch (Exception ex) {
            txtMa.setText(EntityUtil.increaseEntityID(null, "LP", 8));
        }

        // 2) Load all furniture from NoiThatService into availableTable
        try {
            List<NoiThat> all = noiThatService.getAllNoiThat();
            availableTableModel.setRowCount(0);
            if (all != null) {
                for (NoiThat n : all) {
                    availableTableModel.addRow(new Object[] { n.getMaNoiThat(), n.getTenNoiThat(), n.getMoTa() });
                }
            }
        } catch (Exception ex) {
            availableTableModel.setRowCount(0);
        }
    }

    private void addSelectedFurniture() {
        int[] rows = tableAvailable.getSelectedRows();
        if (rows == null || rows.length == 0) return;

        // add selected rows to selectedTable with default quantity 1, remove from available model
        // iterate from bottom to top to remove correctly
        for (int i = rows.length - 1; i >= 0; i--) {
            int r = rows[i];
            String ma = (String) availableTableModel.getValueAt(r, 0);
            String ten = (String) availableTableModel.getValueAt(r, 1);
            String moTa = (String) availableTableModel.getValueAt(r, 2);
            selectedTableModel.addRow(new Object[] { ma, ten, moTa, Integer.valueOf(1) });
            availableTableModel.removeRow(r);
        }
    }

    private void removeSelectedFromTable() {
        int[] rows = tableSelected.getSelectedRows();
        if (rows == null || rows.length == 0) {
            // nếu không chọn hàng, bỏ hàng cuối
            int last = selectedTableModel.getRowCount() - 1;
            if (last >= 0) {
                String ma = (String) selectedTableModel.getValueAt(last, 0);
                String ten = (String) selectedTableModel.getValueAt(last, 1);
                String moTa = (String) selectedTableModel.getValueAt(last, 2);
                availableTableModel.addRow(new Object[] { ma, ten, moTa });
                selectedTableModel.removeRow(last);
            }
            return;
        }

        for (int i = rows.length - 1; i >= 0; i--) {
            int r = rows[i];
            String ma = (String) selectedTableModel.getValueAt(r, 0);
            String ten = (String) selectedTableModel.getValueAt(r, 1);
            String moTa = (String) selectedTableModel.getValueAt(r, 2);
            availableTableModel.addRow(new Object[] { ma, ten, moTa });
            selectedTableModel.removeRow(r);
        }
    }

    private void onSave() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        int soNguoi = (Integer) spnSoNguoi.getValue();
        String phanLoai = (String) cboPhanLoai.getSelectedItem();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại phòng không được để trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // parse prices
        double giaNgay = 0.0;
        double giaGio = 0.0;
        try {
            Object v1 = txtGiaNgay.getValue();
            if (v1 instanceof Number) giaNgay = ((Number) v1).doubleValue();
            else giaNgay = Double.parseDouble(String.valueOf(v1));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giá theo ngày không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Object v2 = txtGiaGio.getValue();
            if (v2 instanceof Number) giaGio = ((Number) v2).doubleValue();
            else giaGio = Double.parseDouble(String.valueOf(v2));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giá theo giờ không hợp lệ", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build LoaiPhong
        LoaiPhong lp = new LoaiPhong();
        lp.setMaLoaiPhong(ma);
        lp.setTenLoaiPhong(ten);
        lp.setSoLuongKhach(soNguoi);
        lp.setPhanLoai(phanLoai);

        // prepare selected furniture list with quantities
        List<NoiThatAssignment> assignments = new ArrayList<>();
        for (int r = 0; r < selectedTableModel.getRowCount(); r++) {
            String maNT = (String) selectedTableModel.getValueAt(r, 0);
            Object qtyObj = selectedTableModel.getValueAt(r, 3);
            int qty = 1;
            if (qtyObj instanceof Integer) qty = (Integer) qtyObj;
            else {
                try { qty = Integer.parseInt(qtyObj.toString()); } catch (Exception ignored) {}
            }
            NoiThatAssignment a = new NoiThatAssignment(maNT, qty);
            assignments.add(a);
        }

        try {
            // call service (new signature)
            LoaiPhong created = loaiPhongService.createRoomCategoryV2(lp, giaNgay, giaGio, assignments);
            if (created == null) {
                JOptionPane.showMessageDialog(this, "Tạo loại phòng thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ghi Lịch sử thao tác (nếu service chưa làm hoặc bạn muốn hiển thị thêm) - service đã ghi rồi, nên bạn có thể bỏ phần này.
            JOptionPane.showMessageDialog(this, "Tạo loại phòng thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo loại phòng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Simple spinner cell editor to edit integer quantities in table cell.
     */
    private static class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner;

        public SpinnerEditor(int value, int min, int max, int step) {
            spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
            // make spinner display without editor issues
            JComponent comp = ((DefaultEditor) spinner.getEditor()).getTextField();
            comp.setBorder(null);
            spinner.setBorder(null);
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof Integer) spinner.setValue(value);
            else {
                try { spinner.setValue(Integer.parseInt(String.valueOf(value))); } catch (Exception ignored) {}
            }
            return spinner;
        }
    }
}

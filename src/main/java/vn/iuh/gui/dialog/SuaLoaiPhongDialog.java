package vn.iuh.gui.dialog;

import vn.iuh.constraint.ActionType;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.NoiThat;
import vn.iuh.gui.base.Main;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.NoiThatService;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.util.EntityUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog sửa loại phòng.
 */
public class SuaLoaiPhongDialog extends JDialog {

    private final LoaiPhongService loaiPhongService;
    private final NoiThatService noiThatService;
    private final LoaiPhong current;

    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JSpinner spnSoNguoi = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JComboBox<String> cboPhanLoai = new JComboBox<>(new String[] {"Thường", "VIP"});

    private final DefaultListModel<NoiThat> availableModel = new DefaultListModel<>();
    private final JList<NoiThat> listAvailable = new JList<>(availableModel);

    private final DefaultTableModel selectedTableModel = new DefaultTableModel(
            new Object[] {"Mã", "Tên", "Mô tả", "Số lượng"}, 0) {
        @Override public boolean isCellEditable(int row, int column) {
            // chỉ cho chỉnh sửa cột Số lượng (index 3)
            return column == 3;
        }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) return Integer.class; // số lượng
            return String.class;
        }
    };

    private final JTable tableSelected = new JTable(selectedTableModel);

    public SuaLoaiPhongDialog(Frame owner,
                              LoaiPhongService loaiPhongService,
                              NoiThatService noiThatService,
                              LoaiPhong current,
                              List<NoiThat> currentFurniture) {
        super(owner, "Sửa loại phòng", true);
        this.loaiPhongService = loaiPhongService;
        this.noiThatService = noiThatService;
        this.current = current;

        initComponents();
        pack();
        setLocationRelativeTo(owner);
        setPreferredSize(new Dimension(900, 600));
        setMinimumSize(new Dimension(800, 520));
        loadInitialData(currentFurniture);
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(12,12));
        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Mã (không sửa)
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Mã loại phòng:"), gc);
        txtMa.setEditable(false);
        txtMa.setBackground(Color.LIGHT_GRAY);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        form.add(txtMa, gc);

        // Tên
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Tên loại phòng:"), gc);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1;
        form.add(txtTen, gc);

        // Số người
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Số người tối đa:"), gc);
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1;
        form.add(spnSoNguoi, gc);

        // Phân loại
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        form.add(new JLabel("Phân loại:"), gc);
        gc.gridx = 1; gc.gridy = 3; gc.weightx = 1;
        form.add(cboPhanLoai, gc);

        main.add(form, BorderLayout.NORTH);

        // center: furniture selection
        JPanel center = new JPanel(new GridLayout(1,2,12,12));

        // left available
        JPanel left = new JPanel(new BorderLayout(6,6));
        left.setBorder(BorderFactory.createTitledBorder("Nội thất có sẵn"));
        listAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // renderer để hiển thị tên (mã)
        listAvailable.setCellRenderer((JList<? extends NoiThat> list, NoiThat value, int index, boolean isSelected, boolean cellHasFocus) -> {
            JLabel lbl = new JLabel();
            if (value == null) {
                lbl.setText("");
            } else {
                String ten = value.getTenNoiThat() == null ? "" : value.getTenNoiThat();
                String ma  = value.getMaNoiThat() == null ? "" : value.getMaNoiThat();
                lbl.setText(String.format("%s (%s)", ten, ma));
            }
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            return lbl;
        });
        left.add(new JScrollPane(listAvailable), BorderLayout.CENTER);
        JButton btnAdd = new JButton("Thêm >>");
        btnAdd.addActionListener(e -> addSelectedFurniture());
        JPanel leftSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftSouth.add(btnAdd);
        left.add(leftSouth, BorderLayout.SOUTH);

        // right selected
        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setBorder(BorderFactory.createTitledBorder("Nội thất (đã chọn)"));
        tableSelected.setFillsViewportHeight(true);
        tableSelected.setRowHeight(28);

        // đặt editor spinner cho cột Số lượng (cần custom editor)
        TableColumnModel tcm = tableSelected.getColumnModel();
        TableColumn qtyCol = tcm.getColumn(3);
        qtyCol.setCellEditor(new SpinnerEditor(1, 1, 999, 1));

        right.add(new JScrollPane(tableSelected), BorderLayout.CENTER);
        JButton btnRemove = new JButton("<< Bỏ");
        btnRemove.addActionListener(e -> removeSelectedFromTable());
        JPanel rightSouth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightSouth.add(btnRemove);
        right.add(rightSouth, BorderLayout.SOUTH);

        center.add(left);
        center.add(right);

        main.add(center, BorderLayout.CENTER);

        // bottom buttons
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

    private void loadInitialData(List<NoiThat> currentFurniture) {
        // fill fields from current LoaiPhong
        if (current != null) {
            txtMa.setText(current.getMaLoaiPhong());
            txtTen.setText(current.getTenLoaiPhong());
            spnSoNguoi.setValue(current.getSoLuongKhach());
            if ("VIP".equalsIgnoreCase(current.getPhanLoai())) cboPhanLoai.setSelectedItem("VIP");
            else cboPhanLoai.setSelectedItem("Thường");
        }

        // load all furniture and mark selected
        List<NoiThat> all = new ArrayList<>();
        try {
            all = noiThatService.getAllNoiThat();
        } catch (Exception ignored) { all = new ArrayList<>(); }

        // selected (from parameter or via service)
        List<NoiThat> selected = (currentFurniture != null) ? currentFurniture : new ArrayList<>();
        if (selected.isEmpty()) {
            try {
                selected = noiThatService.getNoiThatByLoaiPhong(current != null ? current.getMaLoaiPhong() : "");
            } catch (Exception ignored) { selected = new ArrayList<>(); }
        }

        // populate selected table and available list
        selectedTableModel.setRowCount(0);
        availableModel.clear();

        // Add selected rows; quantity mặc định = 1 (nếu bạn có cách lấy số lượng mapping thực, thay ở đây)
        for (NoiThat s : selected) {
            if (s == null) continue;
            selectedTableModel.addRow(new Object[] {
                    s.getMaNoiThat(),
                    s.getTenNoiThat(),
                    s.getMoTa(),
                    Integer.valueOf(1)
            });
        }

        for (NoiThat n : all) {
            boolean isSelected = false;
            for (NoiThat s : selected) {
                if (s != null && n != null && s.getMaNoiThat().equals(n.getMaNoiThat())) { isSelected = true; break; }
            }
            if (!isSelected) availableModel.addElement(n);
        }
    }

    private void addSelectedFurniture() {
        List<NoiThat> selected = listAvailable.getSelectedValuesList();
        for (NoiThat n : selected) {
            if (n == null) continue;
            selectedTableModel.addRow(new Object[] { n.getMaNoiThat(), n.getTenNoiThat(), n.getMoTa(), Integer.valueOf(1) });
            availableModel.removeElement(n);
        }
    }

    private void removeSelectedFromTable() {
        int[] rows = tableSelected.getSelectedRows();
        if (rows == null || rows.length == 0) return;
        for (int i = rows.length - 1; i >= 0; i--) {
            int r = rows[i];
            String ma = (String) selectedTableModel.getValueAt(r, 0);
            String ten = (String) selectedTableModel.getValueAt(r, 1);
            String moTa = (String) selectedTableModel.getValueAt(r, 2);
            NoiThat nt = new NoiThat();
            nt.setMaNoiThat(ma);
            nt.setTenNoiThat(ten);
            nt.setMoTa(moTa);
            availableModel.addElement(nt);
            selectedTableModel.removeRow(r);
        }
    }

    private void onSave() {
        if (current == null) return;
        String ten = txtTen.getText().trim();
        int soNguoi = (Integer) spnSoNguoi.getValue();
        String phanLoai = (String) cboPhanLoai.getSelectedItem();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại phòng không được để trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        current.setTenLoaiPhong(ten);
        current.setSoLuongKhach(soNguoi);
        current.setPhanLoai(phanLoai);

        // kiểm tra booking hiện tại/tương lai bằng ChiTietDatPhongDAO (bạn đã thêm method)
        try {
            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO();
            boolean hasBooking = ctDao.hasCurrentOrFutureBookingsForLoaiPhong(current.getMaLoaiPhong());
            if (hasBooking) {
                JOptionPane.showMessageDialog(this,
                        "Loại phòng đang được đặt/đang sử dụng (hiện tại hoặc tương lai). Không thể cập nhật nội thất.",
                        "Không thể cập nhật", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra booking: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        // build assignments with quantities (List<NoiThatAssignment>)
        List<NoiThatAssignment> newAssignments = new ArrayList<>();
        for (int r = 0; r < selectedTableModel.getRowCount(); r++) {
            String ma = (String) selectedTableModel.getValueAt(r, 0);
            Object qtyObj = selectedTableModel.getValueAt(r, 3);
            int qty = 1;
            if (qtyObj instanceof Integer) qty = (Integer) qtyObj;
            else {
                try { qty = Integer.parseInt(String.valueOf(qtyObj)); } catch (Exception ignored) {}
            }
            newAssignments.add(new NoiThatAssignment(ma, qty));
        }

        // maPhienDangNhap: bạn nên thay cách lấy này bằng session thực tế
        String maPhien = System.getProperty("user.name");
        if (maPhien == null) maPhien = "UNKNOWN";

        try {
            if (loaiPhongService instanceof LoaiPhongServiceImpl) {
                // nếu implement của bạn có method transaction + audit, gọi nó
                LoaiPhongServiceImpl impl = (LoaiPhongServiceImpl) loaiPhongService;
                boolean ok = impl.updateRoomCategoryWithAudit(current, newAssignments);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // fallback: cập nhật loại phòng rồi gán nội thất bằng List<NoiThatAssignment>
                LoaiPhong updated = loaiPhongService.updateRoomCategory(current);
                if (updated == null) {
                    JOptionPane.showMessageDialog(this, "Cập nhật loại phòng thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean assignOk = false;
                try {
                    // **SỬA**: truyền newAssignments thay vì List<NoiThat>
                    assignOk = noiThatService.assignNoiThatToLoaiPhong(updated.getMaLoaiPhong(), newAssignments);
                } catch (Exception ignored) { assignOk = false; }

                // Ghi lịch sử thao tác (fallback) bằng LichSuThaoTacDAO
                try {
                    String latestId = null;
                    LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();
                    LichSuThaoTac wh = new LichSuThaoTac();

                    var latest = lichSuDao.timLichSuThaoTacMoiNhat();

                    if (latest != null) latestId = latest.getMaLichSuThaoTac();
                    String newId = EntityUtil.increaseEntityID(latestId, "LT", 8);

                    wh.setMaLichSuThaoTac(newId);
                    wh.setTenThaoTac(ActionType.EDIT_ROOM_CATEGORY.getActionName());
                    wh.setMoTa(String.format("Cập nhật loại phòng %s; nội thất count=%d", updated.getMaLoaiPhong(), newAssignments.size()));
                    wh.setMaPhienDangNhap(Main.getCurrentLoginSession());

                    lichSuDao.themLichSuThaoTac(wh);
                } catch (Exception ignored) {}

                if (!assignOk) {
                    JOptionPane.showMessageDialog(this, "Cập nhật loại phòng xong nhưng gán nội thất không thành công", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                dispose();
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    /**
     * Editor spinner dùng làm TableCellEditor cho cột số lượng.
     */
    private static class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner;

        SpinnerEditor(int value, int min, int max, int step) {
            spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
            spinner.setBorder(null);
        }

        @Override
        public Object getCellEditorValue() {
            Object v = spinner.getValue();
            if (v instanceof Integer) return v;
            try {
                return Integer.parseInt(String.valueOf(v));
            } catch (Exception e) {
                return Integer.valueOf(1);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            try {
                if (value instanceof Integer) spinner.setValue(value);
                else spinner.setValue(Integer.parseInt(String.valueOf(value)));
            } catch (Exception e) {
                spinner.setValue(1);
            }
            return spinner;
        }
    }
}

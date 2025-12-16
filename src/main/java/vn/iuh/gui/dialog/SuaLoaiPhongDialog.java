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
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class SuaLoaiPhongDialog extends JDialog {

    private final LoaiPhongService loaiPhongService;
    private final NoiThatService noiThatService;
    private final LoaiPhong current;

    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JSpinner spnSoNguoi = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JComboBox<String> cboPhanLoai = new JComboBox<>(new String[] {"Thường", "VIP"});

    private final JTextField txtGiaGio = new JTextField();
    private final JTextField txtGiaNgay = new JTextField();

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

    // --- initial snapshot for change detection ---
    private String initialName = null;
    private int initialSoNguoi = -1;
    private String initialPhanLoai = null;
    private Double initialGiaGio = null;   // null means unknown/not-set
    private Double initialGiaNgay = null;
    private Map<String,Integer> initialAssignments = new HashMap<>();

    // name validation pattern: letters A-Z/a-z, digits, hyphen and space
    // Cho phép mọi chữ (Unicode letters, bao gồm tiếng Việt có dấu), chữ số, dấu '-' và khoảng trắng
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}0-9\\-\\s]+$");


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
        setPreferredSize(new Dimension(900, 640)); // tăng chút chiều cao để chứa 2 field giá
        setMinimumSize(new Dimension(800, 560));
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

        // --- NEW: Giá giờ ---
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0;
        form.add(new JLabel("Giá giờ (VND):"), gc);
        gc.gridx = 1; gc.gridy = 4; gc.weightx = 1;
        txtGiaGio.setToolTipText("Nhập số (ví dụ: 100000 hoặc 100,000)");
        form.add(txtGiaGio, gc);

        // --- NEW: Giá ngày ---
        gc.gridx = 0; gc.gridy = 5; gc.weightx = 0;
        form.add(new JLabel("Giá ngày (VND):"), gc);
        gc.gridx = 1; gc.gridy = 5; gc.weightx = 1;
        txtGiaNgay.setToolTipText("Nhập số (ví dụ: 300000 hoặc 300,000)");
        form.add(txtGiaNgay, gc);

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

        // --- attempt to load latest price (if service implementation exposes it) ---
        try {
            Map<String, Double> latest = null;
            if (loaiPhongService instanceof LoaiPhongServiceImpl && current != null) {
                latest = ((LoaiPhongServiceImpl) loaiPhongService).getLatestPriceMap(current.getMaLoaiPhong());
            }
            if (latest != null) {
                double gGio = latest.getOrDefault("gia_gio", 0.0);
                double gNgay = latest.getOrDefault("gia_ngay", 0.0);
                NumberFormat nf = NumberFormat.getIntegerInstance();
                if (gGio > 0) {
                    txtGiaGio.setText(nf.format(Math.round(gGio)));
                    initialGiaGio = gGio;
                } else {
                    txtGiaGio.setText("");
                    initialGiaGio = null;
                }
                if (gNgay > 0) {
                    txtGiaNgay.setText(nf.format(Math.round(gNgay)));
                    initialGiaNgay = gNgay;
                } else {
                    txtGiaNgay.setText("");
                    initialGiaNgay = null;
                }
            }
        } catch (Exception ignored) {
            initialGiaGio = null;
            initialGiaNgay = null;
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
        initialAssignments.clear();
        for (NoiThat s : selected) {
            if (s == null) continue;
            selectedTableModel.addRow(new Object[] {
                    s.getMaNoiThat(),
                    s.getTenNoiThat(),
                    s.getMoTa(),
                    Integer.valueOf(1)
            });
            initialAssignments.put(s.getMaNoiThat(), 1);
        }

        for (NoiThat n : all) {
            boolean isSelected = false;
            for (NoiThat s : selected) {
                if (s != null && n != null && s.getMaNoiThat().equals(n.getMaNoiThat())) { isSelected = true; break; }
            }
            if (!isSelected) availableModel.addElement(n);
        }

        // snapshot for change detection on basic fields
        initialName = current != null ? current.getTenLoaiPhong() : null;
        initialSoNguoi = current != null ? current.getSoLuongKhach() : -1;
        initialPhanLoai = current != null ? current.getPhanLoai() : null;
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

    private Double parsePriceField(String text) {
        if (text == null) return null;
        String cleaned = text.trim().replaceAll("[,\\s]", "");
        if (cleaned.isEmpty()) return null;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean assignmentsEqual(Map<String,Integer> a, Map<String,Integer> b) {
        if (a == null) a = Collections.emptyMap();
        if (b == null) b = Collections.emptyMap();
        if (a.size() != b.size()) return false;
        for (Map.Entry<String,Integer> e : a.entrySet()) {
            Integer v = b.get(e.getKey());
            if (v == null || !v.equals(e.getValue())) return false;
        }
        return true;
    }

    private void onSave() {
        if (current == null) return;
        String ten = txtTen.getText().trim();
        int soNguoi = (Integer) spnSoNguoi.getValue();
        String phanLoai = (String) cboPhanLoai.getSelectedItem();

        // -- basic validation --
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại phòng không được để trống", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // name pattern
        if (!NAME_PATTERN.matcher(ten).matches()) {
            JOptionPane.showMessageDialog(this, "Tên loại phòng chỉ chứa chữ cái, chữ số, dấu '-' và khoảng trắng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // số người tối đa = 6
        if (soNguoi > 6) {
            JOptionPane.showMessageDialog(this, "Số người tối đa khi thêm loại phòng là 6", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // danh sách nội thất không được rỗng
        if (selectedTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Danh sách nội thất không được rỗng", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // build new assignments map and list
        Map<String,Integer> newAssignMap = new HashMap<>();
        List<NoiThatAssignment> newAssignments = new ArrayList<>();
        for (int r = 0; r < selectedTableModel.getRowCount(); r++) {
            String ma = String.valueOf(selectedTableModel.getValueAt(r, 0));
            Object qtyObj = selectedTableModel.getValueAt(r, 3);
            int qty = 1;
            if (qtyObj instanceof Integer) qty = (Integer) qtyObj;
            else {
                try { qty = Integer.parseInt(String.valueOf(qtyObj)); } catch (Exception ignored) { qty = 1; }
            }
            if (qty < 1) qty = 1;
            newAssignMap.put(ma, qty);
            newAssignments.add(new NoiThatAssignment(ma, qty));
        }

        // parse price inputs
        String sGiaGio = txtGiaGio.getText().trim().replaceAll("[,\\s]", "");
        String sGiaNgay = txtGiaNgay.getText().trim().replaceAll("[,\\s]", "");
        boolean giaGioEmpty = sGiaGio.isEmpty();
        boolean giaNgayEmpty = sGiaNgay.isEmpty();

        Double giaGioVal = parsePriceField(txtGiaGio.getText());
        Double giaNgayVal = parsePriceField(txtGiaNgay.getText());

        // Price validation rules:
        // - if user changed price then both must be provided and > 0
        // - if user left both empty -> no price change
        boolean userProvidedAnyPrice = !giaGioEmpty || !giaNgayEmpty;
        if (userProvidedAnyPrice) {
            // must provide both
            if (giaGioEmpty || giaNgayEmpty) {
                JOptionPane.showMessageDialog(this, "Nếu thay đổi giá, vui lòng nhập cả Giá giờ và Giá ngày.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // parse success?
            if (giaGioVal == null || giaNgayVal == null) {
                JOptionPane.showMessageDialog(this, "Giá nhập không hợp lệ. Vui lòng nhập số hợp lệ (ví dụ: 100000).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // not negative or zero
            if (giaGioVal <= 0.0 || giaNgayVal <= 0.0) {
                JOptionPane.showMessageDialog(this, "Giá phải là số dương lớn hơn 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // check booking current/future before allowing content changes (same as before)
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

        // Determine if anything changed compared to initial state
        boolean basicChanged = false;
        if (initialName == null) initialName = "";
        if (!initialName.equals(ten)) basicChanged = true;
        if (initialSoNguoi != soNguoi) basicChanged = true;
        if (initialPhanLoai == null) initialPhanLoai = "";
        if (!initialPhanLoai.equalsIgnoreCase(phanLoai == null ? "" : phanLoai)) basicChanged = true;

        boolean assignmentsChanged = !assignmentsEqual(initialAssignments, newAssignMap);

        // price changed detection
        boolean priceChanged = false;
        if (userProvidedAnyPrice) {
            // user wants to change price (we validated both present and >0)
            double oldGio = initialGiaGio == null ? -1.0 : initialGiaGio;
            double oldNgay = initialGiaNgay == null ? -1.0 : initialGiaNgay;
            if (initialGiaGio == null || initialGiaNgay == null) {
                // previously not set -> any provided >0 means change
                priceChanged = true;
            } else {
                if (Double.compare(oldGio, giaGioVal) != 0 || Double.compare(oldNgay, giaNgayVal) != 0) {
                    priceChanged = true;
                }
            }
        }

        // if nothing changed -> do nothing
        if (!basicChanged && !assignmentsChanged && !priceChanged) {
            JOptionPane.showMessageDialog(this, "Không có thay đổi nào để cập nhật.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        // Prepare LoaiPhong to update
        current.setTenLoaiPhong(ten);
        current.setSoLuongKhach(soNguoi);
        current.setPhanLoai(phanLoai);

        try {
            if (loaiPhongService instanceof LoaiPhongServiceImpl) {
                LoaiPhongServiceImpl impl = (LoaiPhongServiceImpl) loaiPhongService;
                boolean ok;
                if (priceChanged) {
                    ok = impl.updateRoomCategoryWithAudit(current, newAssignments, giaGioVal, giaNgayVal);
                } else {
                    ok = impl.updateRoomCategoryWithAudit(current, newAssignments);
                }

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // fallback: update via service generic methods
                LoaiPhong updated = loaiPhongService.updateRoomCategory(current);
                if (updated == null) {
                    JOptionPane.showMessageDialog(this, "Cập nhật loại phòng thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean assignOk = false;
                try {
                    assignOk = noiThatService.assignNoiThatToLoaiPhong(updated.getMaLoaiPhong(), newAssignments);
                } catch (Exception ignored) { assignOk = false; }

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

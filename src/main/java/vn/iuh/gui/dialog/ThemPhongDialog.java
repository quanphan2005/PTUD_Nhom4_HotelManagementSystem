package vn.iuh.gui.dialog;

import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.Phong;
import vn.iuh.service.impl.RoomServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ThemPhongDialog extends JDialog {
    private final RoomServiceImpl roomService;

    private JTextField txtMaPhong;
    private JTextField txtTenPhong;
    private JComboBox<String> cbLoaiPhong;
    private JLabel lblSoNguoi;
    private JLabel lblGiaNgay;
    private JLabel lblGiaGio;
    private JTextArea txtMoTa;
    private JTextArea txtGhiChu;
    private JList<String> lstNoiThat;
    private DefaultListModel<String> noiThatModel;

    // Danh sách loại phòng
    private List<LoaiPhong> loaiPhongList;

    public ThemPhongDialog(Window owner, boolean modal, RoomServiceImpl roomService) {
        super(owner, "Thêm phòng", ModalityType.APPLICATION_MODAL);
        this.roomService = roomService;

        initComponents();
        loadInitialData();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Mã phòng
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Mã phòng:"), gc);
        txtMaPhong = new JTextField(14);
        txtMaPhong.setEditable(false);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(txtMaPhong, gc);

        // Tên phòng
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Tên phòng:"), gc);
        txtTenPhong = new JTextField(14);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(txtTenPhong, gc);

        // Loại phòng
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Loại phòng:"), gc);
        cbLoaiPhong = new JComboBox<>();
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(cbLoaiPhong, gc);

        // Số người, Giá ngày, Giá giờ
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Số người:"), gc);
        lblSoNguoi = new JLabel("-");
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(lblSoNguoi, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Giá theo ngày:"), gc);
        lblGiaNgay = new JLabel("-");
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(lblGiaNgay, gc);

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Giá theo giờ:"), gc);
        lblGiaGio = new JLabel("-");
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(lblGiaGio, gc);

        // Danh sách nội thất
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridheight = 3;
        form.add(new JLabel("Nội thất:"), gc);
        noiThatModel = new DefaultListModel<>();
        lstNoiThat = new JList<>(noiThatModel);
        lstNoiThat.setVisibleRowCount(6);
        JScrollPane spNoiThat = new JScrollPane(lstNoiThat);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.gridheight = 3;
        form.add(spNoiThat, gc);
        gc.gridheight = 1;
        row += 3;

        // Mô tả
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Mô tả:"), gc);
        txtMoTa = new JTextArea(3, 20);
        JScrollPane spMoTa = new JScrollPane(txtMoTa);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(spMoTa, gc);

        // Ghi chú
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        form.add(new JLabel("Ghi chú:"), gc);
        txtGhiChu = new JTextArea(2, 20);
        JScrollPane spGhiChu = new JScrollPane(txtGhiChu);
        gc.gridx = 1; gc.gridy = row++; gc.weightx = 1;
        form.add(spGhiChu, gc);

        content.add(form, BorderLayout.CENTER);

        // Các nút ở phần bottom
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Thêm");
        JButton btnCancel = new JButton("Hủy");
        btns.add(btnCancel);
        btns.add(btnAdd);
        content.add(btns, BorderLayout.SOUTH);

        // Sự kiện khi loại phòng được thay đổi
        cbLoaiPhong.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                onLoaiPhongChanged(cbLoaiPhong.getSelectedIndex());
            }
        });

        // Sự kiện cho nút hủy (tắt dialog)
        btnCancel.addActionListener(e -> dispose());

        // Sự kiện cho nút thêm
        btnAdd.addActionListener(e -> onAdd());

        // Enter = add, Esc = cancel
        getRootPane().setDefaultButton(btnAdd);
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(ev -> dispose(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadInitialData() {
        // 1) Lấy ID mới
        try {
            txtMaPhong.setText(roomService.getNextRoomID());
        } catch (Exception ignored) {}

        // 2) Load danh sách LoaiPhong
        try {
            loaiPhongList = roomService.getAllRoomCategories();
            cbLoaiPhong.removeAllItems();
            if (loaiPhongList != null && !loaiPhongList.isEmpty()) {
                for (LoaiPhong lp : loaiPhongList) {
                    cbLoaiPhong.addItem(lp.getTenLoaiPhong());
                }
                // Chọn phần tử đầu và kích hoạt update UI
                cbLoaiPhong.setSelectedIndex(0);
                onLoaiPhongChanged(0);
            } else {
                lblSoNguoi.setText("-");
                lblGiaNgay.setText("-");
                lblGiaGio.setText("-");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Sự kiện khi loại phòng thay đổi
    private void onLoaiPhongChanged(int index) {
        if (loaiPhongList == null || loaiPhongList.isEmpty() || index < 0 || index >= loaiPhongList.size()) {
            return;
        }
        LoaiPhong selected = loaiPhongList.get(index);
        // Số người
        lblSoNguoi.setText(String.valueOf(selected.getSoLuongKhach()));

        // Giá ngày / giờ
        double[] price = roomService.getLatestPriceForLoaiPhong(selected.getMaLoaiPhong());
        lblGiaNgay.setText(price != null ? String.format("%.0f", price[0]) : "0");
        lblGiaGio.setText(price != null ? String.format("%.0f", price[1]) : "0");

        // Danh sách nội thất
        noiThatModel.clear();
        List<RoomFurnitureItem> furniture = roomService.getFurnitureForLoaiPhong(selected.getMaLoaiPhong());
        if (furniture != null) {
            for (RoomFurnitureItem item : furniture) {
                String label = String.format("%s (x%d)", item.getName(), item.getQuantity());
                noiThatModel.addElement(label);
            }
        }
    }

    // Sự kiện khi nhấn nút thêm
    private void onAdd() {
        String ma = txtMaPhong.getText().trim();
        String ten = txtTenPhong.getText().trim();
        int idx = cbLoaiPhong.getSelectedIndex();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên phòng.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra trùng tên phòng
        if (roomService.isRoomNameExists(ten)) {
            JOptionPane.showMessageDialog(this, "Tên phòng đã tồn tại. Vui lòng chọn tên khác.", "Trùng tên", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (idx < 0 || loaiPhongList == null || idx >= loaiPhongList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại phòng.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoaiPhong selectedLoai = loaiPhongList.get(idx);

        // Tạo RoomCreationEvent
        RoomCreationEvent ev = new RoomCreationEvent(ten, false,  txtGhiChu.getText().trim(), txtMoTa.getText().trim(), selectedLoai.getMaLoaiPhong());

        // Gọi service để tạo phòng
        try {
            Phong phong= roomService.createRoom(ev);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm phòng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Thêm phòng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}

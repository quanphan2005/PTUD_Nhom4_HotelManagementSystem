package vn.iuh.gui.dialog;

import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.Phong;
import vn.iuh.gui.panel.QuanLyPhongPanel;
import vn.iuh.service.RoomService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Collections;
import java.util.List;

// Dialog sửa phòng
public class SuaPhongDialog extends JDialog {
    private final RoomService roomService;
    private final Phong phong;

    // Các textfield chứa thông tin của phòng
    private final JTextField tfMaPhong = new JTextField();
    private final JTextField tfTenPhong = new JTextField();
    private final JComboBox<LoaiPhong> cbLoaiPhong = new JComboBox<>();
    private final JSpinner spSoNguoi = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JTextArea taMoTa = new JTextArea();
    private final JTextArea taGhiChu = new JTextArea();

    // Danh sách nội thất
    private final DefaultListModel<String> furnitureModel = new DefaultListModel<>();
    private final JList<String> listFurniture = new JList<>(furnitureModel);

    // Trạng thái của action (true nếu user lưu những thay đổi sau khi sửa phòng)
    // reload ở QuanLyPhongPanel sẽ dựa vào giá trị này để quyết định reload
    private boolean saved = false;

    // Các nút
    private final JButton btnSave = new JButton("Lưu");
    private final JButton btnCancel = new JButton("Huỷ");
    private final JLabel lblInfo = new JLabel();

    private final JButton btnStartMaintenance = new JButton("Đặt bảo trì");
    private final JButton btnEndMaintenance = new JButton("Kết thúc bảo trì");

    public SuaPhongDialog(Window owner, Phong phong, RoomService roomService) {
        super(owner, "Sửa phòng - " + (phong != null ? phong.getMaPhong() : ""), ModalityType.APPLICATION_MODAL);
        this.phong = phong;
        this.roomService = roomService;
        initUI();
        loadData(); // async
        pack();
        setMinimumSize(new Dimension(620, 480));
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(content, BorderLayout.CENTER);

        // Top info
        lblInfo.setForeground(Color.RED);
        lblInfo.setVisible(false);
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD));
        add(lblInfo, BorderLayout.NORTH);

        // Phần thông tin bên trái
        JPanel left = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        left.add(new JLabel("Mã phòng:"), gc);
        tfMaPhong.setEditable(false);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        left.add(tfMaPhong, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        left.add(new JLabel("Tên phòng:"), gc);
        gc.gridx = 1; gc.gridy = 1;
        left.add(tfTenPhong, gc);

        gc.gridx = 0; gc.gridy = 2;
        left.add(new JLabel("Loại phòng:"), gc);
        gc.gridx = 1; gc.gridy = 2;
        cbLoaiPhong.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiPhong) {
                    LoaiPhong lp = (LoaiPhong) value;
                    setText(lp.getTenLoaiPhong()
                            + (lp.getPhanLoai() != null && !lp.getPhanLoai().isBlank() ? " - " + lp.getPhanLoai() : "")
                            + " (" + lp.getSoLuongKhach() + " ng.)");
                }
                return this;
            }
        });

        // Khi chọn loại phòng -> cập nhật số người và nội thất
        cbLoaiPhong.addActionListener(e -> {
            LoaiPhong sel = (LoaiPhong) cbLoaiPhong.getSelectedItem();
            if (sel != null) {
                spSoNguoi.setValue(Math.max(1, sel.getSoLuongKhach()));
                populateFurnitureForLoaiPhongAsync(sel.getMaLoaiPhong());
            } else {
                spSoNguoi.setValue(1);
                furnitureModel.clear();
            }
        });

        left.add(cbLoaiPhong, gc);

        gc.gridx = 0; gc.gridy = 3;
        left.add(new JLabel("Số người:"), gc);
        spSoNguoi.setEnabled(false);
        gc.gridx = 1; gc.gridy = 3;
        left.add(spSoNguoi, gc);

        gc.gridx = 0; gc.gridy = 4;
        left.add(new JLabel("Mô tả:"), gc);
        taMoTa.setLineWrap(true);
        taMoTa.setWrapStyleWord(true);
        gc.gridx = 1; gc.gridy = 4;
        left.add(new JScrollPane(taMoTa), gc);

        gc.gridx = 0; gc.gridy = 5;
        left.add(new JLabel("Ghi chú:"), gc);
        taGhiChu.setLineWrap(true);
        taGhiChu.setWrapStyleWord(true);
        gc.gridx = 1; gc.gridy = 5;
        left.add(new JScrollPane(taGhiChu), gc);

        content.add(left, BorderLayout.CENTER);

        // Phần thông tin bên phải (chứa danh sách nội thất)
        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setPreferredSize(new Dimension(260, 0));
        right.setBorder(BorderFactory.createTitledBorder("Nội thất (theo loại phòng)"));
        listFurniture.setModel(furnitureModel);
        right.add(new JScrollPane(listFurniture), BorderLayout.CENTER);
        content.add(right, BorderLayout.EAST);

        // Các nút ở phần bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bottom.add(btnStartMaintenance);
        bottom.add(btnEndMaintenance);
        bottom.add(btnSave);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        // Thiết lập trạng thái mặc định
        btnStartMaintenance.setEnabled(true);
        btnEndMaintenance.setEnabled(true);

        // Action: Hủy
        btnCancel.addActionListener(e -> {
            saved = false;
            dispose();
        });

        // Lưu
        btnSave.addActionListener(e -> onSave());

        // Đặt bảo trì (3 ngày)
        btnStartMaintenance.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(SuaPhongDialog.this,
                    "Bạn có chắc chắn muốn đặt trạng thái phòng thành BẢO TRÌ trong 3 ngày?",
                    "Xác nhận đặt bảo trì", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;

            // disable controls trong khi thực hiện
            setControlsEnabled(false);

            SwingWorker<Boolean, Void> wk = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        // gọi service để tạo công việc bảo trì 3 ngày
                        return roomService.scheduleMaintenance(phong.getMaPhong(), 3);
                    } catch (Throwable ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(SuaPhongDialog.this, "Đặt phòng vào trạng thái BẢO TRÌ thành công.");
                            // reload UI quản lý phòng và đóng dialog
                            reloadRoomManagementPanel();
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(SuaPhongDialog.this, "Đặt bảo trì thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(SuaPhongDialog.this, "Đặt bảo trì thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setControlsEnabled(true);
                    }
                }
            };
            wk.execute();
        });

        // Kết thúc bảo trì
        btnEndMaintenance.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(SuaPhongDialog.this,
                    "Bạn có chắc chắn muốn kết thúc trạng thái BẢO TRÌ cho phòng này?",
                    "Xác nhận kết thúc bảo trì", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;

            setControlsEnabled(false);

            SwingWorker<Boolean, Void> wk = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        return roomService.endMaintenance(phong.getMaPhong());
                    } catch (Throwable ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(SuaPhongDialog.this, "Kết thúc trạng thái BẢO TRÌ thành công.");
                            // reload UI quản lý phòng và đóng dialog
                            reloadRoomManagementPanel();
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(SuaPhongDialog.this, "Kết thúc bảo trì không thành công (không tìm thấy công việc bảo trì).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(SuaPhongDialog.this, "Kết thúc bảo trì thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setControlsEnabled(true);
                    }
                }
            };
            wk.execute();
        });


    }

    // Hàm gọi khi lưu thông tin sau khi sửa
    private void onSave() {
        // Kiểm tra trùng tên phòng
        String name = tfTenPhong.getText() != null ? tfTenPhong.getText().trim() : "";
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên phòng không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy các thông tin
        LoaiPhong sel = (LoaiPhong) cbLoaiPhong.getSelectedItem();
        String selectedLoaiId = sel != null ? sel.getMaLoaiPhong() : null;
        String note = taGhiChu.getText() != null ? taGhiChu.getText().trim() : "";
        String description = taMoTa.getText() != null ? taMoTa.getText().trim() : "";

        // Vô hiệu hóa UI khi nhấn nút save tránh việc người dùng thao tác trên UI khi đang save
        setControlsEnabled(false);
        lblInfo.setVisible(false);

        // Kiểm tra trùng tên, nếu không trùng tên thì lưuu thông tin
        SwingWorker<Phong, Void> wk = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Phong doInBackground() {
                try {
                    // Lấy tất cả phòng để kiểm tra trùng tên
                    List<Phong> all = roomService.getAll();
                    if (all != null) {
                        for (Phong r : all) {
                            if (r == null) continue;
                            String otherId = r.getMaPhong();
                            String otherName = r.getTenPhong();
                            if (otherName == null) continue;
                            // Nếu tìm thấy phòng khác có cùng tên (không phân biệt hoa thường)
                            if (!phong.getMaPhong().equals(otherId) && otherName.trim().equalsIgnoreCase(name)) {
                                errorMessage = "Tên phòng đã tồn tại. Vui lòng chọn tên khác.";
                                return null;
                            }
                        }
                    }

                    // Nếu không trùng thì cập nhật
                    RoomModificationEvent evt = new RoomModificationEvent(
                            phong.getMaPhong(),
                            name,
                            note,
                            description,
                            selectedLoaiId
                    );
                    return roomService.updateRoom(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMessage = "Lỗi khi lưu: " + ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    if (errorMessage != null) {
                        // Hiển thị thông báo lỗi nếu có
                        JOptionPane.showMessageDialog(SuaPhongDialog.this, errorMessage, "Lỗi", JOptionPane.WARNING_MESSAGE);
                        setControlsEnabled(true);
                        return;
                    }

                    Phong updated = get();
                    if (updated != null) {
                        saved = true;
                        JOptionPane.showMessageDialog(SuaPhongDialog.this, "Cập nhật thành công.");
                        dispose();
                    } else {
                        // Updated == null và không có thông báo lỗi ==> lưu thất bại
                        JOptionPane.showMessageDialog(SuaPhongDialog.this, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        setControlsEnabled(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SuaPhongDialog.this, "Lỗi khi lưu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    setControlsEnabled(true);
                }
            }
        };

        wk.execute();
    }

    // Load dữ liệu ban đầu
    private void loadData() {
        if (phong == null) return;

        // Điền các thông tin có sẵn vào các textfield thông tin
        tfMaPhong.setText(phong.getMaPhong());
        tfTenPhong.setText(phong.getTenPhong());
        taMoTa.setText(phong.getMoTaPhong() != null ? phong.getMoTaPhong() : "");
        taGhiChu.setText(phong.getGhiChu() != null ? phong.getGhiChu() : "");

        // Load danh sách các loại phòng, kiểm tra trạng thái
        SwingWorker<List<LoaiPhong>, Void> wk = new SwingWorker<>() {
            @Override
            protected List<LoaiPhong> doInBackground() {
                try {
                    return roomService.getAllRoomCategories();
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    // Lấy kết quả trả về từ hàm doInBackground
                    List<LoaiPhong> cats = get();
                    // Xóa hết các mục cũ trong combobox trước khi update lại
                    cbLoaiPhong.removeAllItems();

                    // Biến dùng để lưu loại phòng đã chọn
                    LoaiPhong selected = null;
                    String currentLoaiId = phong.getMaLoaiPhong();
                    if (cats != null) {
                        for (LoaiPhong lp : cats) {
                            cbLoaiPhong.addItem(lp);
                            if (lp.getMaLoaiPhong() != null && lp.getMaLoaiPhong().equals(currentLoaiId)) {
                                selected = lp;
                            }
                        }
                    }

                    // Nếu tìm được loại phù hợp với phòng đang sửa ==> Chọn
                    if (selected != null) {
                        cbLoaiPhong.setSelectedItem(selected);
                        spSoNguoi.setValue(Math.max(1, selected.getSoLuongKhach()));
                        populateFurnitureForLoaiPhongAsync(selected.getMaLoaiPhong());
                    } else if (cbLoaiPhong.getItemCount() > 0) {
                        // Nếu không tìm được loại khớp nhưng combobox có phần tử ==> chọn phần tử đầu tiên
                        cbLoaiPhong.setSelectedIndex(0);
                        LoaiPhong first = (LoaiPhong) cbLoaiPhong.getSelectedItem();
                        if (first != null) {
                            spSoNguoi.setValue(Math.max(1, first.getSoLuongKhach()));
                            populateFurnitureForLoaiPhongAsync(first.getMaLoaiPhong());
                        }
                    } else {
                        // Nếu không có loại phòng nào thì đặt mặc định số người = 1 và clear danh sách nội thất
                        spSoNguoi.setValue(1);
                        furnitureModel.clear();
                    }

                    // KHÔNG kiểm tra trạng thái ở dialog nữa: luôn cho phép thao tác (QuanLyPhongPanel đã kiểm tra trước khi mở dialog)
                    setControlsEnabled(true);
                    lblInfo.setVisible(false);

                } catch (Exception e) {
                    e.printStackTrace();
                    spSoNguoi.setValue(1);
                    furnitureModel.clear();
                    setControlsEnabled(true);
                }
            }
        };
        setControlsEnabled(false);
        // Khởi chạy SwingWorker (bắt đầu thread nền)
        wk.execute();
    }

    // Lấyn nột thất theo mã loại phòng
    private void populateFurnitureForLoaiPhongAsync(String maLoaiPhong) {
        if (maLoaiPhong == null) {
            furnitureModel.clear();
            return;
        }

        SwingWorker<List<RoomFurnitureItem>, Void> wk = new SwingWorker<>() {
            @Override
            protected List<RoomFurnitureItem> doInBackground() {
                try {
                    return roomService.getFurnitureForLoaiPhong(maLoaiPhong);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<RoomFurnitureItem> items = get();
                    furnitureModel.clear();
                    // Thêm từng nội thất + số lượng vào danh sách
                    if (items != null && !items.isEmpty()) {
                        for (RoomFurnitureItem it : items) {
                            String line = (it.getName() != null ? it.getName() : "N/A")
                                    + " (x" + (it.getQuantity()) + ")";
                            furnitureModel.addElement(line);
                        }
                    } else {
                        furnitureModel.addElement("Không có nội thất mẫu cho loại này.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    furnitureModel.clear();
                }
            }
        };
        wk.execute();
    }

    // Set trạng thái cho các dòng thông tin
    private void setControlsEnabled(boolean enabled) {
        tfTenPhong.setEnabled(enabled);
        cbLoaiPhong.setEnabled(enabled);
        // spSoNguoi chỉ hiển thị, không cho sửa
        taMoTa.setEnabled(enabled);
        taGhiChu.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        // list nội thất chỉ đọc
        // giữ cho các nút bảo trì tương tự
        btnStartMaintenance.setEnabled(enabled);
        btnEndMaintenance.setEnabled(enabled);
    }

    // Chuyển dialog sang chế độ read-only và hiển thị thông báo lý do
    private void setReadOnlyMode(String message) {
        setControlsEnabled(false);
        lblInfo.setText(message);
        lblInfo.setVisible(true);
    }

    // Tìm instance của QuanLyPhongPanel trong cây component của cửa sổ
    private QuanLyPhongPanel findRoomPanel(Component c) {
        if (c == null) return null;
        if (c instanceof QuanLyPhongPanel) return (QuanLyPhongPanel) c;
        if (c instanceof Container) {
            Component[] children = ((Container) c).getComponents();
            for (Component ch : children) {
                QuanLyPhongPanel found = findRoomPanel(ch);
                if (found != null) return found;
            }
        }
        return null;
    }

    // Gọi phương thức reload
    private void reloadRoomManagementPanel() {
        try {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w == null) return;
            QuanLyPhongPanel panel = findRoomPanel(w);
            if (panel == null) return;

            java.lang.reflect.Method m = panel.getClass().getDeclaredMethod("reloadRoomsAsync", java.util.function.Supplier.class);
            m.setAccessible(true);
            java.util.function.Supplier supplier = (java.util.function.Supplier) (() -> roomService.getAllQuanLyPhongPanel());
            m.invoke(panel, supplier);

            panel.revalidate();
            panel.repaint();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

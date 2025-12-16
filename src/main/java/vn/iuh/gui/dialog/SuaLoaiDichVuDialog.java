package vn.iuh.gui.dialog;

import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.ServiceCategoryService;
import vn.iuh.util.AppEventBus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

public class SuaLoaiDichVuDialog extends JDialog {
    private final String maLoai;
    private final ServiceCategoryService categoryService;
    private final Runnable onSaved;
    private final String maPhienDangNhap;

    private final JTextField tfTen = new JTextField();
    private final JButton btnSave = new JButton("Lưu");
    private final JButton btnCancel = new JButton("Hủy");

    // lưu tên ban đầu để so sánh
    private final String originalName;

    // chỉ cho phép chữ (unicode, gồm tiếng Việt có dấu), số, khoảng trắng và dấu '-'
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}0-9 \\-]+$");

    public SuaLoaiDichVuDialog(Window owner,
                               String maLoai,
                               String tenHienTai,
                               ServiceCategoryService categoryService,
                               Runnable onSaved,
                               String maPhienDangNhap) {
        super(owner, "Sửa loại dịch vụ", ModalityType.APPLICATION_MODAL);
        this.maLoai = maLoai;
        this.categoryService = categoryService;
        this.onSaved = onSaved;
        this.maPhienDangNhap = maPhienDangNhap;
        this.originalName = tenHienTai != null ? tenHienTai : "";

        initUI(tenHienTai);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI(String tenHienTai) {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(CustomUI.white);

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CustomUI.white);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // label mã (không sửa)
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Mã loại (không sửa): "), gc);

        JLabel lblMa = new JLabel(maLoai != null ? maLoai : "-");
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1.0;
        form.add(lblMa, gc);

        // tên (editable)
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Tên loại dịch vụ:"), gc);

        tfTen.setText(tenHienTai != null ? tenHienTai : "");
        tfTen.setColumns(30);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1.0;
        form.add(tfTen, gc);

        root.add(form, BorderLayout.CENTER);

        // buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(CustomUI.white);
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnSave.setPreferredSize(new Dimension(120, 36));
        btns.add(btnCancel);
        btns.add(btnSave);
        root.add(btns, BorderLayout.SOUTH);

        // actions
        btnCancel.addActionListener(e -> dispose());

        btnSave.addActionListener(e -> onSaveClicked());

        // Enter key triggers save, Esc triggers cancel
        tfTen.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSaveClicked();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });

        setContentPane(root);
        getRootPane().setDefaultButton(btnSave);
    }

    private void setUiBusy(boolean busy) {
        btnSave.setEnabled(!busy);
        btnCancel.setEnabled(!busy);
        tfTen.setEnabled(!busy);
        Cursor c = busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor();
        setCursor(c);
    }

    private void onSaveClicked() {
        String newName = tfTen.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại dịch vụ không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            tfTen.requestFocusInWindow();
            return;
        }

        if (!NAME_PATTERN.matcher(newName).matches()) {
            JOptionPane.showMessageDialog(this,
                    "Tên loại dịch vụ chỉ được chứa chữ (gồm tiếng Việt có dấu), chữ số, khoảng trắng và dấu '-'",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            tfTen.requestFocusInWindow();
            return;
        }

        // nếu không có thay đổi so với tên gốc -> không cập nhật
        if (originalName.equals(newName)) {
            JOptionPane.showMessageDialog(this, "Không có thay đổi để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        setUiBusy(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Exception error;

            @Override
            protected Boolean doInBackground() {
                try {
                    // gọi service đã được implement (transaction + ghi lịch sử)
                    // categoryService.capNhatTenLoaiDichVu(...) trả về boolean
                    return categoryService.capNhatTenLoaiDichVu(maLoai, newName);
                } catch (Exception ex) {
                    error = ex;
                    return false;
                }
            }

            @Override
            protected void done() {
                setUiBusy(false);
                try {
                    if (error != null) {
                        // lỗi không mong muốn
                        JOptionPane.showMessageDialog(SuaLoaiDichVuDialog.this,
                                "Lỗi khi cập nhật loại dịch vụ: " + error.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean ok = false;
                    try { ok = get(); } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SuaLoaiDichVuDialog.this,
                                "Lỗi khi cập nhật loại dịch vụ: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!ok) {
                        // service trả về false khi update thất bại (ví dụ trùng tên hoặc không tồn tại)
                        JOptionPane.showMessageDialog(SuaLoaiDichVuDialog.this,
                                "Cập nhật thất bại. Có thể do tên trùng hoặc lỗi khác.",
                                "Không thành công",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // thành công -> gọi callback để reload UI (panel quản lý loại + panel quản lý dịch vụ)
                    if (onSaved != null) SwingUtilities.invokeLater(onSaved);

                    AppEventBus.publish("CATEGORY_UPDATED");

                    JOptionPane.showMessageDialog(SuaLoaiDichVuDialog.this,
                            "Cập nhật loại dịch vụ thành công.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } finally {
                    setUiBusy(false);
                }
            }
        };

        worker.execute();
    }
}

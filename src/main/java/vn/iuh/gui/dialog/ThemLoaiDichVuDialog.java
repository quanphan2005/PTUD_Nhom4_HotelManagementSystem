package vn.iuh.gui.dialog;

import vn.iuh.entity.LoaiDichVu;
import vn.iuh.service.ServiceCategoryService;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog thêm loại dịch vụ.
 * - Người dùng chỉ nhập "Tên loại dịch vụ"
 * - Mã loại sẽ được sinh tự động tại tầng Service/DAO
 * - Khi lưu thành công gọi onSuccess.run()
 */
public class ThemLoaiDichVuDialog extends JDialog {

    private final ServiceCategoryService categoryService;
    private final Runnable onSuccess;

    private final JTextField tfTen = new JTextField(30);

    public ThemLoaiDichVuDialog(Window owner, ServiceCategoryService categoryService, Runnable onSuccess) {
        super(owner, "Thêm loại dịch vụ", ModalityType.APPLICATION_MODAL);
        this.categoryService = categoryService;
        this.onSuccess = onSuccess;

        setLayout(new BorderLayout());
        setSize(520, 220);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8,8,8,8);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Tên loại dịch vụ:"), gc);
        gc.gridx = 1;
        tfTen.setFont(new Font("Arial", Font.PLAIN, 14));
        p.add(tfTen, gc);

        return p;
    }

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private void onSave() {
        String ten = tfTen.getText();
        if (ten == null || ten.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại dịch vụ không được để trống", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // build entity
        LoaiDichVu toCreate = new LoaiDichVu();
        toCreate.setTenDichVu(ten.trim());

        try {
            LoaiDichVu created = categoryService.createServiceCategoryV2(toCreate);
            if (created == null) {
                // null nghĩa trùng tên (theo service impl) hoặc lỗi khác mà service trả null
                JOptionPane.showMessageDialog(this, "Không thể thêm: tên loại dịch vụ có thể đã tồn tại", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm thành công: " + created.getMaLoaiDichVu(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // onSuccess dùng để reload toàn bộ UI quản lý loại dịch vụ + (nếu muốn) management dịch vụ
            if (onSuccess != null) onSuccess.run();

            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm loại dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package vn.iuh.gui.dialog;

import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.service.ServiceService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ThemDichVuDialog extends JDialog {

    private final ServiceService serviceService;
    private final Runnable onSuccess; // callback để reload panel
    private JTextField tfTen;
    private JTextField tfTonKho;
    private JComboBox<String> cbLoai;
    private JTextField tfGia;

    public ThemDichVuDialog(Window owner, ServiceService serviceService, Runnable onSuccess) {
        super(owner, "Thêm dịch vụ mới", ModalityType.APPLICATION_MODAL);
        this.serviceService = serviceService;
        this.onSuccess = onSuccess;

        initUI();
        pack();
        setSize(520, 320);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Row 0 - Tên dịch vụ
        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Tên dịch vụ:"), gc);
        gc.gridx = 1;
        tfTen = new JTextField(28);
        form.add(tfTen, gc);

        // Row 1 - Tồn kho
        gc.gridx = 0; gc.gridy = 1;
        form.add(new JLabel("Tồn kho:"), gc);
        gc.gridx = 1;
        tfTonKho = new JTextField("0", 10);
        form.add(tfTonKho, gc);

        // Row 2 - Loại dịch vụ
        gc.gridx = 0; gc.gridy = 2;
        form.add(new JLabel("Loại dịch vụ:"), gc);
        gc.gridx = 1;
        cbLoai = new JComboBox<>();
        // load loại từ service
        try {
            List<LoaiDichVu> list = serviceService.layTatCaLoaiDichVu();
            cbLoai.addItem("Không chọn");
            if (list != null) {
                for (LoaiDichVu l : list) {
                    cbLoai.addItem(l.getMaLoaiDichVu() + " - " + l.getTenDichVu());
                }
            }
        } catch (Exception ex) {
            cbLoai.addItem("Không lấy được loại");
        }
        form.add(cbLoai, gc);

        // Row 3 - Giá
        gc.gridx = 0; gc.gridy = 3;
        form.add(new JLabel("Giá (VNĐ):"), gc);
        gc.gridx = 1;
        tfGia = new JTextField("0", 12);
        form.add(tfGia, gc);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Thêm");
        JButton btnCancel = new JButton("Hủy");
        bottom.add(btnCancel);
        bottom.add(btnAdd);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());

        btnAdd.addActionListener(e -> {
            // validate
            String ten = tfTen.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên dịch vụ", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int ton = 0;
            try {
                ton = Integer.parseInt(tfTonKho.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tồn kho phải là số nguyên", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // luôn set coTheTang = false (0)
            boolean coTheTang = false;

            String maLoai = null;
            String sel = (String) cbLoai.getSelectedItem();
            if (sel != null && !sel.equals("Không chọn") && sel.contains(" - ")) {
                maLoai = sel.split(" - ")[0];
            }

            double gia = 0.0;
            try {
                gia = Double.parseDouble(tfGia.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Giá phải là số", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // gọi service để thêm
            try {
                ServiceResponse created = serviceService.themDichVuMoi(ten, ton, coTheTang, maLoai, gia);
                if (created == null) {
                    // Service trả null khi trùng tên hoặc thất bại
                    JOptionPane.showMessageDialog(this, "Thêm dịch vụ thất bại. Có thể tên dịch vụ đã tồn tại.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công: " + created.getMaDichVu(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

                // gọi callback để reload panel
                if (onSuccess != null) {
                    onSuccess.run();
                }
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

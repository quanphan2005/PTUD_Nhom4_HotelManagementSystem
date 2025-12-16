package vn.iuh.gui.dialog;

import vn.iuh.service.ServiceService;

import javax.swing.*;
import java.awt.*;

public class ServiceStockEditDialog extends JDialog {
    private final String maDichVu;
    private final String tenDichVu;
    private final int currentQty;
    private final ServiceService serviceService;
    private final Runnable onSavedCallback;

    private JSpinner spnQty;

    public ServiceStockEditDialog(Window owner,
                                  String maDichVu,
                                  String tenDichVu,
                                  int currentQty,
                                  ServiceService serviceService,
                                  Runnable onSavedCallback) {
        super(owner instanceof Frame ? (Frame) owner : null, "Chỉnh sửa tồn kho", ModalityType.APPLICATION_MODAL);
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.currentQty = Math.max(0, currentQty);
        this.serviceService = serviceService;
        this.onSavedCallback = onSavedCallback;

        initComponents();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Mã dịch vụ:"), gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        JTextField tfMa = new JTextField(maDichVu);
        tfMa.setEditable(false);
        form.add(tfMa, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(new JLabel("Tên dịch vụ:"), gc);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1;
        JTextField tfTen = new JTextField(tenDichVu != null ? tenDichVu : "");
        tfTen.setEditable(false);
        form.add(tfTen, gc);

        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Tồn kho hiện tại:"), gc);
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1;
        spnQty = new JSpinner(new SpinnerNumberModel(this.currentQty, 0, Integer.MAX_VALUE, 1));
        // make the spinner's editor accept only integers visually
        JComponent editor = spnQty.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(10);
        }
        form.add(spnQty, gc);

        p.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu");
        bottom.add(btnCancel);
        bottom.add(btnSave);
        p.add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        setContentPane(p);
    }

    private void onSave() {
        Object val = spnQty.getValue();
        int newQty;
        try {
            if (val instanceof Number) newQty = ((Number) val).intValue();
            else newQty = Integer.parseInt(String.valueOf(val));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Giá trị tồn kho không hợp lệ. Vui lòng nhập số nguyên >= 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // explicit validation: không âm
        if (newQty < 0) {
            JOptionPane.showMessageDialog(this, "Tồn kho phải là số nguyên >= 0.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // nếu không đổi thì chỉ đóng dialog (không gọi service)
        if (newQty == currentQty) {
            JOptionPane.showMessageDialog(this, "Không có thay đổi nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            return;
        }

        // gọi Service (theo kiến trúc 3 tầng). Service có nhiệm vụ gọi DAO và xử lý transaction/log nếu cần.
        try {
            if (serviceService == null) {
                JOptionPane.showMessageDialog(this, "Lỗi: service chưa được khởi tạo.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean ok = serviceService.capNhatTonKhoDichVu(maDichVu, newQty);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Cập nhật tồn kho thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (onSavedCallback != null) onSavedCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật tồn kho thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật tồn kho: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

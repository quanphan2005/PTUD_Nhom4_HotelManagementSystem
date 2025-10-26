package vn.iuh.gui.dialog;

import vn.iuh.dao.TaiKhoanDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class PasswordChangeDialog extends JDialog {
    private JPasswordField txtCurrentPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;
    private JButton btnSave;
    private JButton btnCancel;
    private String maNhanVien;
    private TaiKhoanDAO taiKhoanDAO;

    public PasswordChangeDialog(Frame parent, String maNhanVien) {
        super(parent, "Đổi Mật Khẩu", true);
        this.maNhanVien = maNhanVien;
        this.taiKhoanDAO = new TaiKhoanDAO();

        init();
        addActions();

        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    private void init() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Mật khẩu hiện tại:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        txtCurrentPass = new JPasswordField(20);
        panel.add(txtCurrentPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Mật khẩu mới:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtNewPass = new JPasswordField(20);
        panel.add(txtNewPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Xác nhận mật khẩu:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        txtConfirmPass = new JPasswordField(20);
        panel.add(txtConfirmPass, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(buttonPanel, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void addActions() {
        btnCancel.addActionListener(e -> dispose());

        btnSave.addActionListener(this::onSave);
    }

    private void onSave(ActionEvent e) {
        char[] currentPassChars = txtCurrentPass.getPassword();
        char[] newPassChars = txtNewPass.getPassword();
        char[] confirmPassChars = txtConfirmPass.getPassword();

        if (currentPassChars.length == 0 || newPassChars.length == 0 || confirmPassChars.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Arrays.equals(newPassChars, confirmPassChars)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtNewPass.setText("");
            txtConfirmPass.setText("");
            txtNewPass.requestFocus();
            return;
        }

        String currentPass = new String(currentPassChars);
        String newPass = new String(newPassChars);

        if (!taiKhoanDAO.kiemTraMatKhau(this.maNhanVien, currentPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtCurrentPass.setText("");
            txtCurrentPass.requestFocus();
            return;
        }

        boolean updateSuccess = taiKhoanDAO.doiMatKhau(this.maNhanVien, newPass);
        if (updateSuccess) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi cập nhật mật khẩu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        Arrays.fill(currentPassChars, '0');
        Arrays.fill(newPassChars, '0');
        Arrays.fill(confirmPassChars, '0');
    }
}
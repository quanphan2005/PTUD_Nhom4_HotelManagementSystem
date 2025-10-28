package vn.iuh.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.entity.NhanVien;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.DateChooser; // Import DateChooser của bạn

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EmployeeDialog extends JDialog {
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_FIELD = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 15);
    private static final Dimension FIELD_SIZE = new Dimension(350, 40);
    private static final Dimension LABEL_SIZE = new Dimension(120, FIELD_SIZE.height);
    private JTextField txtMaNV, txtTenNV, txtCCCD, txtSDT;
    private DateChooser datePickerNgaySinh;
    private JButton btnSave, btnCancel;
    private NhanVien nhanVien;
    private boolean isSaved = false;
    private final boolean isEditMode;

    /**
     * Constructor cho chế độ "Thêm mới"
     * @param owner Frame cha
     * @param title Tiêu đề của Dialog
     */
    public EmployeeDialog(Frame owner, String title, String newMaNhanVien) {
        super(owner, title, true); // Gọi constructor chính với NhanVien = null
        this.isEditMode = false;
        this.nhanVien = null;

        init();

        txtMaNV.setText(newMaNhanVien);
        txtMaNV.setEnabled(false);
        txtMaNV.setBackground(Color.decode("#E5E7EB"));
        txtTenNV.requestFocusInWindow();
    }

    public EmployeeDialog(Frame owner, String title, NhanVien existingNhanVien) {
        super(owner, title, true);
        this.isEditMode = (existingNhanVien != null);
        this.nhanVien = existingNhanVien;

        init();

        if (isEditMode) {
            loadData(existingNhanVien);
        }
    }

    private void init() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        setBackground(CustomUI.white);

        JLabel lblTitle = new JLabel(getTitle(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(CustomUI.white);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(CustomUI.blue);
        lblTitle.setPreferredSize(new Dimension(0, 50));

        JPanel mainPanel = createMainPanel();
        JPanel buttonPanel = createButtonPanel();
        initEvents();
        add(lblTitle, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CustomUI.white);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtMaNV = new JTextField(20);
        txtTenNV = new JTextField(20);
        txtCCCD = new JTextField(20);
        txtSDT = new JTextField(20);
        datePickerNgaySinh = new DateChooser();
        styleField(txtMaNV);
        styleField(txtTenNV);
        styleField(txtCCCD);
        styleField(txtSDT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 15, 10);
        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.anchor = GridBagConstraints.WEST;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.weightx = 1.0;
        gbcField.insets = new Insets(0, 0, 15, 0);
        gbc.gridy = 0;
        JLabel lblMaNV = new JLabel("Mã nhân viên:");
        lblMaNV.setFont(FONT_LABEL);
        panel.add(lblMaNV, gbc);

        gbcField.gridy = 0;
        panel.add(txtMaNV, gbcField);

        gbc.gridy = 1;
        JLabel lblTenNV = new JLabel("Tên nhân viên:");
        lblTenNV.setFont(FONT_LABEL);
        panel.add(lblTenNV, gbc);

        gbcField.gridy = 1;
        panel.add(txtTenNV, gbcField);

        gbc.gridy = 2;
        JLabel lblCCCD = new JLabel("Số CCCD:");
        lblCCCD.setFont(FONT_LABEL);
        panel.add(lblCCCD, gbc);

        gbcField.gridy = 2;
        panel.add(txtCCCD, gbcField);

        gbc.gridy = 3;
        JLabel lblNgaySinh = new JLabel("Ngày sinh:");
        lblNgaySinh.setFont(FONT_LABEL);
        panel.add(lblNgaySinh, gbc);

        gbcField.gridy = 3;
        panel.add(datePickerNgaySinh, gbcField);

        gbc.gridy = 4;
        JLabel lblSDT = new JLabel("Số điện thoại:");
        lblSDT.setFont(FONT_LABEL);
        panel.add(lblSDT, gbc);

        gbcField.gridy = 4;
        panel.add(txtSDT, gbcField);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(CustomUI.white);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E7EB")));
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(FONT_BUTTON);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.decode("#DC2626"));
        btnCancel.setPreferredSize(new Dimension(120, 40));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnSave = new JButton("Lưu lại");
        btnSave.setFont(FONT_BUTTON);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(Color.decode("#1D4ED8"));
        btnSave.setPreferredSize(new Dimension(120, 40));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        panel.add(btnCancel);
        panel.add(btnSave);
        return panel;
    }

    private JPanel createFieldRow(String labelText, Component field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(CustomUI.white);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setPreferredSize(LABEL_SIZE);
        label.setMinimumSize(LABEL_SIZE);

        row.add(label);
        row.add(Box.createHorizontalStrut(10));

        if (!(field instanceof DateChooser)) {
            field.setMaximumSize(FIELD_SIZE);
        } else {
            field.setMaximumSize(FIELD_SIZE);
        }

        row.add(field);
        return row;
    }

    private void styleField(JComponent field) {
        field.setFont(FONT_FIELD);
        field.setPreferredSize(FIELD_SIZE);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
    }
    private void loadData(NhanVien nv) {
        txtMaNV.setText(nv.getMaNhanVien());

        txtMaNV.setEnabled(false);
        txtMaNV.setBackground(Color.decode("#E5E7EB"));

        txtTenNV.setText(nv.getTenNhanVien());
        txtCCCD.setText(nv.getCCCD());
        txtSDT.setText(nv.getSoDienThoai());

        // --- Chuyển đổi java.util.Date (Timestamp) sang LocalDate ---
        if (nv.getNgaySinh() != null) {
            // 1. Lấy java.util.Date (hoặc Timestamp) từ Entity
            Date ngaySinhDate = nv.getNgaySinh();

            // 2. Chuyển đổi sang LocalDate
            LocalDate localDate = ngaySinhDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // 3. Đặt ngày cho DateChooser của bạn
            datePickerNgaySinh.setDate(localDate);
        }
    }

    private void initEvents() {
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> onCancel());
    }

    private void onCancel() {
        isSaved = false;
        dispose();
    }

    private void onSave() {
        try {
            if (!validateInput()) {
                return;
            }
            if (!isEditMode) {
                this.nhanVien = new NhanVien();
                this.nhanVien.setMaNhanVien(txtMaNV.getText().trim());
            }

            this.nhanVien.setTenNhanVien(txtTenNV.getText().trim());
            this.nhanVien.setCCCD(txtCCCD.getText().trim());
            this.nhanVien.setSoDienThoai(txtSDT.getText().trim());

            LocalDate localDate = datePickerNgaySinh.getDate();

            if (localDate != null) {
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Timestamp timestamp = new Timestamp(date.getTime());
                this.nhanVien.setNgaySinh(timestamp);
            } else {
                this.nhanVien.setNgaySinh(null);
            }
            isSaved = true;
            dispose();
        }catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Đã xảy ra lỗi bên trong Dialog khi lưu:\n" + e.getMessage(),
                    "Lỗi nghiêm trọng (Dialog)",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            isSaved = false; // Đảm bảo isSaved là false
            dispose();
        }
    }

    private boolean validateInput() {
        String tenNV = txtTenNV.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String sdt = txtSDT.getText().trim();

        if (txtMaNV.getText().trim().isEmpty() && !isEditMode) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMaNV.requestFocus();
            return false;
        }
        if (tenNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTenNV.requestFocus();
            return false;
        }
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số CCCD không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }

        // Ví dụ: if (!cccd.matches("^0[0-9]{11}$")) { ... }

        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }
        // Ví dụ: if (!sdt.matches("^0[0-9]{9}$")) { ... }

        if (datePickerNgaySinh.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Kiểm tra ngày sinh không được là ngày tương lai
        if (datePickerNgaySinh.getDate().isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không thể là ngày trong tương lai.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        LocalDate da18Tuoi = datePickerNgaySinh.getDate().plusYears(18);
        LocalDate homNay = LocalDate.now();
        if (da18Tuoi.isAfter(homNay)) {
            JOptionPane.showMessageDialog(this, "Nhân viên phải đủ 18 tuổi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }


    public NhanVien getNhanVien() {
        return this.nhanVien;
    }

    public boolean isSaved() {
        return this.isSaved;
    }
}
package vn.iuh.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.config.SecurityConfig;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.util.AccountUtil;

import javax.swing.*;
import java.awt.*;

/**
 * JDialog để thêm hoặc chỉnh sửa thông tin TaiKhoan.
 * Kế thừa phong cách từ EmployeeDialog.
 */
public class AccountDialog extends JDialog {

    // --- Định nghĩa Style (Sao chép từ EmployeeDialog) ---
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_FIELD = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 15);

    private static final Dimension FIELD_SIZE = new Dimension(350, 40);
    private static final Dimension LABEL_SIZE = new Dimension(120, FIELD_SIZE.height);

    private JTextField txtMaTK, txtMaNV, txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JComboBox<String> cmbChucVu;
    private JButton btnSave, btnCancel;
    private TaiKhoan taiKhoan;
    private boolean isSaved = false;
    private final boolean isEditMode;

    public AccountDialog(Frame owner, String title, String newMaTaiKhoan, String maNhanVien, String tenNV) {
        super(owner, title, true);
        this.isEditMode = false;
        this.taiKhoan = null;
        AccountUtil accountUtil = new AccountUtil();
        String tenDN = accountUtil.taoTenDangNhap(tenNV);
        init();

        txtMaTK.setText(newMaTaiKhoan);
        txtMaNV.setText(maNhanVien);
        txtTenDangNhap.setText(tenDN);
        txtMatKhau.setText("1");
        txtMaTK.setEnabled(false);
        txtMaNV.setEnabled(false);
        txtTenDangNhap.setEnabled(false);
        txtMatKhau.setEnabled(false);
        txtMaTK.setBackground(Color.decode("#E5E7EB"));
        txtMaNV.setBackground(Color.decode("#E5E7EB"));
        txtTenDangNhap.setBackground(Color.decode("#E5E7EB"));
        txtMatKhau.setBackground(Color.decode("#E5E7EB"));
    }

    public AccountDialog(Frame owner, String title, TaiKhoan existingTaiKhoan) {
        super(owner, title, true); // true = modal
        this.isEditMode = true;
        this.taiKhoan = existingTaiKhoan;

        init();
        loadData(existingTaiKhoan);
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

        txtMaTK = new JTextField();
        txtMaNV = new JTextField();
        txtTenDangNhap = new JTextField();
        txtMatKhau = new JPasswordField();
        cmbChucVu = new JComboBox<>(new String[]{"Lễ tân", "Quản lý", "Admin"});

        styleField(txtMaTK);
        styleField(txtMaNV);
        styleField(txtTenDangNhap);
        styleField(txtMatKhau);
        styleField(cmbChucVu);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 15, 10);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.anchor = GridBagConstraints.WEST;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.weightx = 1.0;
        gbcField.insets = new Insets(0, 0, 15, 0);

        gbc.gridy = 0;
        JLabel lblMaTK = new JLabel("Mã tài khoản:");
        lblMaTK.setFont(FONT_LABEL);
        panel.add(lblMaTK, gbc);

        gbcField.gridy = 0;
        panel.add(txtMaTK, gbcField);

        gbc.gridy = 1;
        JLabel lblMaNV = new JLabel("Mã nhân viên:");
        lblMaNV.setFont(FONT_LABEL);
        panel.add(lblMaNV, gbc);

        gbcField.gridy = 1;
        panel.add(txtMaNV, gbcField);

        gbc.gridy = 2;
        JLabel lblTenDN = new JLabel("Tên đăng nhập:");
        lblTenDN.setFont(FONT_LABEL);
        panel.add(lblTenDN, gbc);

        gbcField.gridy = 2;
        panel.add(txtTenDangNhap, gbcField);

        gbc.gridy = 3;
        JLabel lblMatKhau = new JLabel("Mật khẩu:");
        lblMatKhau.setFont(FONT_LABEL);
        panel.add(lblMatKhau, gbc);

        gbcField.gridy = 3;
        panel.add(txtMatKhau, gbcField);

        gbc.gridy = 4;
        JLabel lblChucVu = new JLabel("Chức vụ:");
        lblChucVu.setFont(FONT_LABEL);
        panel.add(lblChucVu, gbc);

        gbcField.gridy = 4;
        panel.add(cmbChucVu, gbcField);

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

    private void styleField(JComponent field) {
        field.setFont(FONT_FIELD);
        field.setPreferredSize(new Dimension(350, 40));
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
    }

    private void loadData(TaiKhoan tk) {
        txtMaTK.setText(tk.getMaTaiKhoan());
        txtMaNV.setText(tk.getMaNhanVien());
        txtTenDangNhap.setText(tk.getTenDangNhap());
        txtMatKhau.setText("");
        txtMatKhau.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "******");

        // Chuyển đổi mã CV (CV001) sang tên (Lễ tân)
        cmbChucVu.setSelectedItem(convertMaChucVuToTen(tk.getMaChucVu()));

        // --- Mã không được chỉnh sửa ===
        txtMaTK.setEnabled(false);
        txtMaNV.setEnabled(false);
        txtMaTK.setBackground(Color.decode("#E5E7EB"));
        txtMaNV.setBackground(Color.decode("#E5E7EB"));
    }

    /**
     * Gán sự kiện cho các nút
     */
    private void initEvents() {
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> onCancel());
    }

    /**
     * Xử lý khi nhấn Hủy
     */
    private void onCancel() {
        isSaved = false;
        dispose();
    }

    /**
     * Xử lý khi nhấn Lưu
     */
    private void onSave() {
        if (!validateInput()) {
            return; // Dừng lại nếu dữ liệu không hợp lệ
        }

        // Nếu là chế độ "Thêm", tạo đối tượng mới
        if (!isEditMode) {
            this.taiKhoan = new TaiKhoan();
            this.taiKhoan.setMaTaiKhoan(txtMaTK.getText());
            this.taiKhoan.setMaNhanVien(txtMaNV.getText());
        }

        // Cập nhật thông tin từ form vào đối tượng taiKhoan
        this.taiKhoan.setTenDangNhap(txtTenDangNhap.getText().trim());
        // Lấy mật khẩu từ JPasswordField
        this.taiKhoan.setMatKhau(new String(txtMatKhau.getPassword()));

        // Chuyển đổi tên Ví d như (Lễ tân) về mã (CV001)
        String selectedRole = (String) cmbChucVu.getSelectedItem();
        this.taiKhoan.setMaChucVu(convertTenToMaChucVu(selectedRole));

        String matKhauMoi = new String(txtMatKhau.getPassword()).trim();

        if (!matKhauMoi.isEmpty()) {

            String matKhauDaMaHoa = SecurityConfig.hashPassword(matKhauMoi);

            this.taiKhoan.setMatKhau(matKhauDaMaHoa);
        }

        isSaved = true;
        dispose(); // Đóng dialog
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu
     */
    private boolean validateInput() {
        String tenDN = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword()).trim();

        if (tenDN.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtTenDangNhap.requestFocus();
            return false;
        }

        // Regex đơn giản: ít nhất 6 ký tự, không khoảng trắng
//        if (!tenDN.matches("^[a-zA-Z0-9_]{6,}$")) {
//            JOptionPane.showMessageDialog(this, "Tên đăng nhập phải có ít nhất 6 ký tự và không chứa khoảng trắng hoặc ký tự đặc biệt.", "Lỗi", JOptionPane.ERROR_MESSAGE);
//            txtTenDangNhap.requestFocus();
//            return false;
//        }

        if (isEditMode && matKhau.isEmpty()) {
            // Đang ở chế độ "Sửa" và mật khẩu để trống -> Hợp lệ (nghĩa là không đổi)
        } else {
            // Đây là chế độ "Thêm" (mật khẩu không được trống hoặc chế độ "Sửa" (người dùng đã nhập mật khẩu mới)
            // Phải validate mật khẩu mới
            if (matKhau.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtMatKhau.requestFocus();
                return false;
            }
//            if (!matKhau.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
//                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
//                txtMatKhau.requestFocus();
//                return false;
//            }
        }

        if (cmbChucVu.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn chức vụ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
    private String convertMaChucVuToTen(String maChucVu) {
        if (maChucVu == null) return "Lễ tân"; // Mặc định
        return switch (maChucVu.trim().toUpperCase()) {
            case "CV001" -> "Lễ tân";
            case "CV002" -> "Quản lý";
            case "CV003" -> "Admin";
            default -> "Lễ tân";
        };
    }

    private String convertTenToMaChucVu(String tenChucVu) {
        if (tenChucVu == null) return "CV001"; // Mặc định
        return switch (tenChucVu) {
            case "Lễ tân" -> "CV001";
            case "Quản lý" -> "CV002";
            case "Admin" -> "CV003";
            default -> "CV001";
        };
    }

    // --- Các phương thức Public để Panel chính gọi ---

    /**
     * Lấy đối tượng TaiKhoan sau khi nhấn "Lưu"
     */
    public TaiKhoan getTaiKhoan() {
        return this.taiKhoan;
    }

    /**
     * Kiểm tra xem người dùng đã nhấn "Lưu" hay "Hủy"
     */
    public boolean isSaved() {
        return this.isSaved;
    }
}
package vn.iuh.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.config.SecurityConfig;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.gui.base.CustomUI;

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

    // Kích thước chuẩn cho các trường nhập liệu
    private static final Dimension FIELD_SIZE = new Dimension(350, 40);
    // Kích thước cố định cho nhãn (để căn chỉnh)
    private static final Dimension LABEL_SIZE = new Dimension(120, FIELD_SIZE.height);

    // --- Components ---
    private JTextField txtMaTK, txtMaNV, txtTenDangNhap;
    private JPasswordField txtMatKhau; // Dùng JPasswordField cho mật khẩu
    private JComboBox<String> cmbChucVu;
    private JButton btnSave, btnCancel;

    // --- Trạng thái ---
    private TaiKhoan taiKhoan; // Dữ liệu trả về
    private boolean isSaved = false;
    private final boolean isEditMode;

    /**
     * Constructor cho chế độ "Thêm mới"
     * @param owner Frame cha
     * @param title Tiêu đề
     * @param newMaTaiKhoan Mã tài khoản mới đã được tạo
     * @param maNhanVien Mã nhân viên được chọn từ bảng
     */
    public AccountDialog(Frame owner, String title, String newMaTaiKhoan, String maNhanVien) {
        super(owner, title, true);
        this.isEditMode = false;
        this.taiKhoan = null;

        init();

        // Thiết lập các trường không cho phép chỉnh sửa
        txtMaTK.setText(newMaTaiKhoan);
        txtMaNV.setText(maNhanVien);

        txtMaTK.setEnabled(false);
        txtMaNV.setEnabled(false);
        txtMaTK.setBackground(Color.decode("#E5E7EB"));
        txtMaNV.setBackground(Color.decode("#E5E7EB"));

        txtTenDangNhap.requestFocusInWindow(); // Focus vào ô nhập liệu đầu tiên
    }

    /**
     * Constructor cho chế độ "Chỉnh sửa"
     * @param owner Frame cha
     * @param title Tiêu đề
     * @param existingTaiKhoan Tài khoản có sẵn để chỉnh sửa
     */
    public AccountDialog(Frame owner, String title, TaiKhoan existingTaiKhoan) {
        super(owner, title, true); // true = modal
        this.isEditMode = true;
        this.taiKhoan = existingTaiKhoan; // Lưu lại bản gốc

        init();
        loadData(existingTaiKhoan);
    }

    /**
     * Khởi tạo giao diện Dialog
     */
    private void init() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        setBackground(CustomUI.white);

        // --- 1. Tiêu đề ---
        JLabel lblTitle = new JLabel(getTitle(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(CustomUI.white);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(CustomUI.blue); // Màu xanh dương
        lblTitle.setPreferredSize(new Dimension(0, 50));

        // --- 2. Panel nội dung (Form nhập liệu) ---
        JPanel mainPanel = createMainPanel();

        // --- 3. Panel Nút (Lưu, Hủy) ---
        JPanel buttonPanel = createButtonPanel();

        // --- 4. Gán sự kiện ---
        initEvents();

        // --- 5. Thêm vào Dialog ---
        add(lblTitle, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack(); // Tự động điều chỉnh kích thước
        setLocationRelativeTo(getOwner());
    }

    /**
     * Tạo panel chính chứa các trường nhập liệu
     */
    /**
     * Tạo panel chính chứa các trường nhập liệu (Sử dụng GridBagLayout)
     */
    private JPanel createMainPanel() {
        // 1. Thay đổi Layout thành GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CustomUI.white);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // 2. Khởi tạo các components (không cần kích thước (20))
        txtMaTK = new JTextField();
        txtMaNV = new JTextField();
        txtTenDangNhap = new JTextField();
        txtMatKhau = new JPasswordField();
        cmbChucVu = new JComboBox<>(new String[]{"Lễ tân", "Quản lý", "Admin"});

        // 3. Áp dụng style (CHỈ font và bo góc, KHÔNG setSize)
        styleField(txtMaTK);
        styleField(txtMaNV);
        styleField(txtTenDangNhap);
        styleField(txtMatKhau);
        styleField(cmbChucVu); // Áp dụng cho cả JComboBox

        // 4. Khởi tạo GridBagConstraints (gbc)
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Định nghĩa Cột 0 (Nhãn) ---
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST; // Căn lề phải cho nhãn
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0; // Nhãn không co giãn
        // Đặt khoảng cách: 15px ở dưới (giống
        gbc.insets = new Insets(0, 0, 15, 10);

        // --- Định nghĩa Cột 1 (Ô nhập liệu) ---
        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.anchor = GridBagConstraints.WEST; // Căn lề trái
        gbcField.fill = GridBagConstraints.HORIZONTAL; // Lấp đầy theo chiều ngang
        gbcField.weightx = 1.0; // Ô nhập liệu CHIẾM HẾT độ rộng
        gbcField.insets = new Insets(0, 0, 15, 0); // 15px ở dưới

        // --- Hàng 0: Mã tài khoản ---
        gbc.gridy = 0;
        JLabel lblMaTK = new JLabel("Mã tài khoản:");
        lblMaTK.setFont(FONT_LABEL);
        panel.add(lblMaTK, gbc);

        gbcField.gridy = 0;
        panel.add(txtMaTK, gbcField);

        // --- Hàng 1: Mã nhân viên ---
        gbc.gridy = 1;
        JLabel lblMaNV = new JLabel("Mã nhân viên:");
        lblMaNV.setFont(FONT_LABEL);
        panel.add(lblMaNV, gbc);

        gbcField.gridy = 1;
        panel.add(txtMaNV, gbcField);

        // --- Hàng 2: Tên đăng nhập ---
        gbc.gridy = 2;
        JLabel lblTenDN = new JLabel("Tên đăng nhập:");
        lblTenDN.setFont(FONT_LABEL);
        panel.add(lblTenDN, gbc);

        gbcField.gridy = 2;
        panel.add(txtTenDangNhap, gbcField);

        // --- Hàng 3: Mật khẩu ---
        gbc.gridy = 3;
        JLabel lblMatKhau = new JLabel("Mật khẩu:");
        lblMatKhau.setFont(FONT_LABEL);
        panel.add(lblMatKhau, gbc);

        gbcField.gridy = 3;
        panel.add(txtMatKhau, gbcField);

        // --- Hàng 4: Chức vụ ---
        gbc.gridy = 4;
        JLabel lblChucVu = new JLabel("Chức vụ:");
        lblChucVu.setFont(FONT_LABEL);
        panel.add(lblChucVu, gbc);

        gbcField.gridy = 4;
        // GBC này cũng áp dụng cho JComboBox
        panel.add(cmbChucVu, gbcField);

        return panel;
    }

    /**
     * Tạo panel chứa các nút "Lưu" và "Hủy"
     * (Sao chép từ EmployeeDialog)
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(CustomUI.white);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E7EB")));

        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(FONT_BUTTON);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.decode("#DC2626")); // Màu đỏ
        btnCancel.setPreferredSize(new Dimension(120, 40));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        btnSave = new JButton("Lưu lại");
        btnSave.setFont(FONT_BUTTON);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(Color.decode("#1D4ED8")); // Màu xanh
        btnSave.setPreferredSize(new Dimension(120, 40));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        panel.add(btnCancel);
        panel.add(btnSave);

        return panel;
    }

    /**
     * Helper: Tạo một hàng (gồm Label và Component)
     * (Sao chép từ EmployeeDialog và điều chỉnh)
     */
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

        // Tất cả các trường (JTextField, JPasswordField, JComboBox)
        // đều nên có kích thước tối đa cố định để căn chỉnh
        field.setMaximumSize(FIELD_SIZE);

        row.add(field);
        return row;
    }

    /**
     * Helper: Áp dụng style chuẩn cho JTextField
     * (Sao chép từ EmployeeDialog)
     */
    private void styleField(JComponent field) {
        field.setFont(FONT_FIELD);

        field.setPreferredSize(new Dimension(350, 40));
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10"); // Bo góc
    }

    /**
     * Tải dữ liệu của TaiKhoan (từ DB) vào form (GUI)
     */
    private void loadData(TaiKhoan tk) {
        txtMaTK.setText(tk.getMaTaiKhoan());
        txtMaNV.setText(tk.getMaNhanVien());
        txtTenDangNhap.setText(tk.getTenDangNhap());
        txtMatKhau.setText(tk.getMatKhau());

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

    // --- Các hàm Helper chuyển đổi Chức vụ ---

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
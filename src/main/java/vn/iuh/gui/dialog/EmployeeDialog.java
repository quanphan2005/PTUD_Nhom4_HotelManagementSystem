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

/**
 * JDialog để thêm hoặc chỉnh sửa thông tin NhanVien.
 * Tương thích với DateChooser (LGoodDatePicker) và java.util.Date/Timestamp.
 */
public class EmployeeDialog extends JDialog {

    // --- Định nghĩa Style ---
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_FIELD = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 15);

    // Kích thước chuẩn cho các trường nhập liệu
    private static final Dimension FIELD_SIZE = new Dimension(350, 40);
    // Kích thước cố định cho nhãn (để căn chỉnh)
    private static final Dimension LABEL_SIZE = new Dimension(120, FIELD_SIZE.height);

    // --- Components ---
    private JTextField txtMaNV, txtTenNV, txtCCCD, txtSDT;
    private DateChooser datePickerNgaySinh;
    private JButton btnSave, btnCancel;

    // --- Trạng thái ---
    private NhanVien nhanVien; // Dữ liệu trả về
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
        txtMaNV.setBackground(Color.decode("#E5E7EB")); // Màu xám nhạt
        txtTenNV.requestFocusInWindow();
    }

    /**
     * Constructor cho chế độ "Chỉnh sửa"
     * @param owner Frame cha
     * @param title Tiêu đề của Dialog
     * @param existingNhanVien Nhân viên có sẵn để chỉnh sửa
     */
    public EmployeeDialog(Frame owner, String title, NhanVien existingNhanVien) {
        super(owner, title, true); // true = modal
        this.isEditMode = (existingNhanVien != null);
        this.nhanVien = existingNhanVien; // Lưu lại bản gốc (nếu là edit)

        init();

        if (isEditMode) {
            loadData(existingNhanVien);
        }
    }

    /**
     * Khởi tạo giao diện Dialog
     */
    private void init() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        setBackground(CustomUI.white);

        // --- 1. Tiêu đề (Giống TopPanel) ---
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

        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Tạo panel chính chứa các trường nhập liệu
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CustomUI.white);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // Khởi tạo các components
        txtMaNV = new JTextField(20);
        txtTenNV = new JTextField(20);
        txtCCCD = new JTextField(20);
        txtSDT = new JTextField(20);
        datePickerNgaySinh = new DateChooser();

        // Áp dụng style cho các trường
        styleTextField(txtMaNV);
        styleTextField(txtTenNV);
        styleTextField(txtCCCD);
        styleTextField(txtSDT);

        // DateChooser đã có style riêng, không cần gọi styleTextField

        // Thêm từng hàng (Label + Field) vào panel
        panel.add(createFieldRow("Mã nhân viên:", txtMaNV));
        panel.add(Box.createVerticalStrut(15)); // Khoảng cách dọc
        panel.add(createFieldRow("Tên nhân viên:", txtTenNV));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("Số CCCD:", txtCCCD));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("Ngày sinh:", datePickerNgaySinh)); // Thêm DateChooser
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFieldRow("Số điện thoại:", txtSDT));

        return panel;
    }

    /**
     * Tạo panel chứa các nút "Lưu" và "Hủy"
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); // Căn phải
        panel.setBackground(CustomUI.white);
        // Tạo đường viền mỏng phía trên
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E7EB")));

        // Nút Hủy
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(FONT_BUTTON);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.decode("#DC2626")); // Màu đỏ (giống nút Xóa)
        btnCancel.setPreferredSize(new Dimension(120, 40));
        btnCancel.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Nút Lưu
        btnSave = new JButton("Lưu lại");
        btnSave.setFont(FONT_BUTTON);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(Color.decode("#1D4ED8")); // Màu xanh (giống nút Tìm)
        btnSave.setPreferredSize(new Dimension(120, 40));
        btnSave.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        panel.add(btnCancel);
        panel.add(btnSave);

        return panel;
    }

    /**
     * Helper: Tạo một hàng (gồm Label và Component)
     */
    private JPanel createFieldRow(String labelText, Component field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS)); // Sắp xếp ngang
        row.setBackground(CustomUI.white);
        row.setAlignmentX(Component.LEFT_ALIGNMENT); // Căn lề trái

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setPreferredSize(LABEL_SIZE);
        label.setMinimumSize(LABEL_SIZE);

        row.add(label);
        row.add(Box.createHorizontalStrut(10));

        if (!(field instanceof DateChooser)) {
            // JTextField thì cần set MaxSize
            field.setMaximumSize(FIELD_SIZE);
        } else {
            // DateChooser thì set theo kích thước của nó
            field.setMaximumSize(FIELD_SIZE);
        }

        row.add(field);
        return row;
    }

    /**
     * Helper: Áp dụng style chuẩn cho JTextField
     */
    private void styleTextField(JTextField field) {
        field.setFont(FONT_FIELD);
        field.setPreferredSize(FIELD_SIZE);
        // field.setMaximumSize(FIELD_SIZE); // Đã chuyển vào createFieldRow
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10"); // Bo góc
    }

    /**
     * Tải dữ liệu của NhanVien (từ DB) vào form (GUI)
     * Đây là nơi chuyển đổi Timestamp/Date (DB) sang LocalDate (GUI)
     */
    private void loadData(NhanVien nv) {
        txtMaNV.setText(nv.getMaNhanVien());

        // === YÊU CẦU: Mã không được chỉnh sửa ===
        txtMaNV.setEnabled(false);
        txtMaNV.setBackground(Color.decode("#E5E7EB")); // Màu xám nhạt

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

            // 3. Đặt ngày cho DateChooser của bạn [cite: 231]
            datePickerNgaySinh.setDate(localDate);
        }
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
     * Đây là nơi chuyển đổi LocalDate (GUI) sang java.util.Date (DB)
     */
    private void onSave() {
        try {
            if (!validateInput()) {
                return; // Dừng lại nếu dữ liệu không hợp lệ
            }

            // Nếu là chế độ "Thêm", tạo đối tượng mới
            if (!isEditMode) {
                this.nhanVien = new NhanVien();
                // (Bạn nên có hàm phát sinh mã NV tự động và đặt vào đây)
                this.nhanVien.setMaNhanVien(txtMaNV.getText().trim());
            }

            // Cập nhật thông tin từ form vào đối tượng nhanVien
            this.nhanVien.setTenNhanVien(txtTenNV.getText().trim());
            this.nhanVien.setCCCD(txtCCCD.getText().trim());
            this.nhanVien.setSoDienThoai(txtSDT.getText().trim());

            // --- Chuyển đổi LocalDate sang java.util.Date (để lưu vào DB) ---
            // 1. Lấy LocalDate từ DateChooser của bạn
            LocalDate localDate = datePickerNgaySinh.getDate();

            if (localDate != null) {
                // 2. Chuyển đổi sang java.util.Date
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                Timestamp timestamp = new Timestamp(date.getTime());

                // 3. Đặt ngày cho Entity
                this.nhanVien.setNgaySinh(timestamp);
            } else {
                this.nhanVien.setNgaySinh(null); // Hoặc xử lý lỗi nếu không cho phép null
            }

            isSaved = true;
            dispose(); // Đóng dialog
        }catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Đã xảy ra lỗi bên trong Dialog khi lưu:\n" + e.getMessage(),
                    "Lỗi nghiêm trọng (Dialog)",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace(); // In lỗi ra console
            isSaved = false; // Đảm bảo isSaved là false
            dispose();
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu
     */
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

        // (Bạn có thể thêm regex cho CCCD và SĐT ở đây)
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

    // --- Các phương thức Public để Panel chính gọi ---

    /**
     * Lấy đối tượng NhanVien sau khi nhấn "Lưu"
     */
    public NhanVien getNhanVien() {
        return this.nhanVien;
    }

    /**
     * Kiểm tra xem người dùng đã nhấn "Lưu" hay "Hủy"
     */
    public boolean isSaved() {
        return this.isSaved;
    }
}
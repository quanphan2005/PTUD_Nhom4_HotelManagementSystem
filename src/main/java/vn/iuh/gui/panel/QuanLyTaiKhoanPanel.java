package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.entity.NhanVien;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.AccountDialog;
import vn.iuh.util.EntityUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel quản lý Tài khoản và Nhân viên .
 * Chứa 2 bảng có thể chuyển đổi.
 */
public class QuanLyTaiKhoanPanel extends JPanel {

    // --- Kích thước ---
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(150, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(190, 50);
    private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(130, 52);

    // --- Fonts ---
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_CATEGORY = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_TABLE_SWITCHER = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_TABLE_HEADER = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_TABLE_CELL = new Font("Arial", Font.PLAIN, 14);

    // --- Icon Cache ---
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    // --- Thành phần UI ---
    private JPanel pnlTimKiemCards; // Panel chứa các ô tìm kiếm
    private CardLayout cardLayoutTimKiem;
    private JTextField txtTimTenDN; // Ô tìm theo Tên Đăng Nhập
    private JTextField txtTimMaTK; // Ô tìm theo Mã Tài Khoản

    private JComboBox<String> cmbTimKiem;
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton, editButton, deleteButton;

    // Nút lọc
    private JButton leTanButton, quanLyButton, adminButton;
    private JButton allCategoryButton;

    // Thành phần cho 2 bảng
    private JPanel tableCardPanel; // Panel chứa 2 bảng (CardLayout)
    private CardLayout tableCardLayout;
    private JLabel lblTabTaiKhoan, lblTabNhanVien; // Nhãn để chuyển tab

    // Models cho bảng
    private DefaultTableModel modelTaiKhoan;
    private JTable tblTaiKhoan;
    private DefaultTableModel modelNhanVien;
    private JTable tblNhanVien;

    // DAO
    private TaiKhoanDAO taiKhoanDAO;
    private NhanVienDAO nhanVienDAO;

    private TableRowSorter<DefaultTableModel> sorterTaiKhoan;
    private JComboBox<String> roleComboBox;

    public QuanLyTaiKhoanPanel() {
        this.taiKhoanDAO = new TaiKhoanDAO();
        this.nhanVienDAO = new NhanVienDAO();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
        loadTaiKhoanData();
        loadNhanVienData();
    }

    private void init() {
        initButtons();
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndCategoryPanel();
        add(Box.createVerticalStrut(10));
        createListPanel();
    }

    private void initButtons() {
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // Nút Thêm/Sửa/Xóa
        addButton = createActionButtonAsync("Thêm", "/icons/add.png", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton = createActionButtonAsync("Sửa", "/icons/edit.png", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButtonAsync("Xóa", "/icons/delete.png", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // Nút lọc Chức vụ
        leTanButton = createCategoryButton("Lễ tân", "#34D399", CATEGORY_BUTTON_SIZE);
        quanLyButton = createCategoryButton("Quản lý", "#FB923C", CATEGORY_BUTTON_SIZE);
        adminButton = createCategoryButton("Admin", "#A78BFA", CATEGORY_BUTTON_SIZE);
        allCategoryButton = createCategoryButton("Tất cả", "#3B82F6", CATEGORY_BUTTON_SIZE);

        // Trong QuanLyTaiKhoanPanel.java

        addButton.addActionListener(e -> {

            // 1. Kiểm tra xem tab "Nhân viên" có đang được kích hoạt không
            // Chúng ta kiểm tra màu của nhãn, nếu là MÀU XANH (active)
            boolean isNhanVienTabActive = lblTabNhanVien.getForeground().equals(CustomUI.blue);

            if (!isNhanVienTabActive) {
                // CHẾ ĐỘ 1: Đang ở tab TÀI KHOẢN
                // Tải dữ liệu, chuyển tab và thông báo

                // Tải lại danh sách nhân viên CHƯA có tài khoản
                loadNhanVienData();

                // Chuyển sang tab Nhân viên
                tableCardLayout.show(tableCardPanel, "NHAN_VIEN");
                styleTabLabel(lblTabTaiKhoan, false);
                styleTabLabel(lblTabNhanVien, true);

                // Thông báo cho người dùng
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên từ danh sách để tạo tài khoản.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // CHẾ ĐỘ 2: Đang ở tab NHÂN VIÊN
            // -> Nhiệm vụ: Xử lý người được chọn

            // 2. Lấy nhân viên đang chọn (KHÔNG load lại data)
            int selectedRow = tblNhanVien.getSelectedRow();

            if(selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên từ danh sách để tạo tài khoản.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Xử lý logic như cũ
            int modelRow = tblNhanVien.convertRowIndexToModel(selectedRow);
            String maNhanVien = (String) modelNhanVien.getValueAt(modelRow, 0);

            try {
                // 3. (QUAN TRỌNG) Kiểm tra xem nhân viên này đã có tài khoản chưa
                if (taiKhoanDAO.findByMaNhanVien(maNhanVien) != null) {
                    JOptionPane.showMessageDialog(this, "Nhân viên này đã có tài khoản.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // 4. Tạo Mã Tài Khoản mới
                TaiKhoan tkMoiNhat = taiKhoanDAO.timTaiKhoanMoiNhat();
                String maMoiNhat = (tkMoiNhat == null) ? null : tkMoiNhat.getMaTaiKhoan();
                String newMaTaiKhoan = EntityUtil.increaseEntityID(maMoiNhat,
                        EntityIDSymbol.ACCOUNT_PREFIX.getPrefix(),
                        EntityIDSymbol.ACCOUNT_PREFIX.getLength());

                // 5. Mở Dialog
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
                AccountDialog addDialog = new AccountDialog(owner, "Tạo tài khoản", newMaTaiKhoan, maNhanVien);
                addDialog.setVisible(true);

                // 6. Xử lý kết quả
                if (addDialog.isSaved()) {
                    TaiKhoan newTaiKhoan = addDialog.getTaiKhoan();

                    // Giả sử tên hàm của bạn là themTaiKhoan
                    boolean success = taiKhoanDAO.themTaiKhoan(newTaiKhoan);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công.");

                        // Tải lại cả 2 bảng
                        loadTaiKhoanData(); // Bảng TK có thêm 1 người
                        loadNhanVienData(); // Bảng NV mất 1 người (vì đã có TK)

                        tableCardLayout.show(tableCardPanel, "TAI_KHOAN");
                        styleTabLabel(lblTabTaiKhoan, true);
                        styleTabLabel(lblTabNhanVien, false);
                    } else {
                        JOptionPane.showMessageDialog(this, "Tạo tài khoản thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra hoặc tạo tài khoản: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        editButton.addActionListener(e -> {
            // 1. Chuyển sang tab Tài khoản
            tableCardLayout.show(tableCardPanel, "TAI_KHOAN");
            styleTabLabel(lblTabTaiKhoan, true);
            styleTabLabel(lblTabNhanVien, false);

            // 2. Lấy tài khoản đang chọn
            int selectedRow = tblTaiKhoan.getSelectedRow();

            if(selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản từ tab 'Danh sách tài khoản' để sửa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = tblTaiKhoan.convertRowIndexToModel(selectedRow);
            String maTaiKhoan = (String) modelTaiKhoan.getValueAt(modelRow, 0);

            try {
                // 3. Lấy thông tin đầy đủ của tài khoản từ CSDL
                TaiKhoan existingTaiKhoan = taiKhoanDAO.timTaiKhoan(maTaiKhoan);
                if (existingTaiKhoan == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 4. Mở Dialog ở chế độ "Sửa"
                Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
                AccountDialog editDialog = new AccountDialog(owner, "Cập nhật tài khoản", existingTaiKhoan);
                editDialog.setVisible(true);

                // 5. Xử lý kết quả
                if (editDialog.isSaved()) {
                    TaiKhoan updatedTaiKhoan = editDialog.getTaiKhoan();

                    // Gọi DAO để cập nhật
                    TaiKhoan updatedtk = taiKhoanDAO.capNhatTaiKhoan(updatedTaiKhoan);
                    if (updatedtk != null) {
                        JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công.");
                        loadTaiKhoanData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi tải hoặc cập nhật tài khoản: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = tblTaiKhoan.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = tblTaiKhoan.convertRowIndexToModel(selectedRow);

            // 3. Lấy thông tin từ model
            String maTaiKhoan = (String) modelTaiKhoan.getValueAt(modelRow, 0);
            String tenDangNhap = (String) modelTaiKhoan.getValueAt(modelRow, 1);

            // 4. Hiển thị hộp thoại xác nhận
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa tài khoản '" + tenDangNhap + "' (Mã: " + maTaiKhoan + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = taiKhoanDAO.xoaTaiKhoan(maTaiKhoan);

                    if (success) {
                        JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công.");
                        loadTaiKhoanData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Xóa tài khoản thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(this, "Xóa thất bại. Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // --- TẠO CÁC PANEL CHÍNH ---

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý tài khoản", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.BOLD, 20f) : FONT_ACTION);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        add(pnlTop);
    }

    /**
     * Panel bên trái: Tìm kiếm và các nút Thêm/Sửa/Xóa
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setPreferredSize(new Dimension(550, 200));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12, 12, 12, 12), Color.decode("#CED4DA"), 2, 30));

        // Hàng 1: Tìm kiếm
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);

        // 1. ComboBox chọn loại tìm kiếm
        String[] searchOptions = {"Tên đăng nhập", "Mã tài khoản"};
        cmbTimKiem = new JComboBox<>(searchOptions);
        Dimension comboSize = new Dimension(180, 45);
        cmbTimKiem.setPreferredSize(comboSize);
        cmbTimKiem.setMaximumSize(comboSize);
        cmbTimKiem.setMinimumSize(comboSize);
        cmbTimKiem.setFont(FONT_LABEL);
        cmbTimKiem.setAlignmentY(Component.CENTER_ALIGNMENT);

        // 2. Panel chứa các ô nhập liệu (dùng CardLayout)
        cardLayoutTimKiem = new CardLayout();
        pnlTimKiemCards = new JPanel(cardLayoutTimKiem);
        pnlTimKiemCards.setBackground(CustomUI.white);
        pnlTimKiemCards.setAlignmentY(Component.CENTER_ALIGNMENT);

        int newTextWidth = 300;
        int textHeight = comboSize.height;
        Dimension textPanelSize = new Dimension(newTextWidth, textHeight);

        pnlTimKiemCards.setPreferredSize(textPanelSize);
        pnlTimKiemCards.setMaximumSize(textPanelSize);
        pnlTimKiemCards.setMinimumSize(textPanelSize);
        // 3. Tạo các ô nhập liệu với placeholder
        txtTimTenDN = new JTextField();
        configureSearchTextField(txtTimTenDN, SEARCH_TEXT_SIZE, "Nhập tên đăng nhập...");

        txtTimMaTK = new JTextField();
        configureSearchTextField(txtTimMaTK, SEARCH_TEXT_SIZE, "Nhập mã tài khoản...");

        // Thêm các ô nhập vào CardLayout
        pnlTimKiemCards.add(txtTimTenDN, "Tên đăng nhập");
        pnlTimKiemCards.add(txtTimMaTK, "Mã tài khoản");

        // 4. Thêm sự kiện cho ComboBox để chuyển Card
        cmbTimKiem.addActionListener(e -> {
            String selected = (String) cmbTimKiem.getSelectedItem();
            cardLayoutTimKiem.show(pnlTimKiemCards, selected);
        });

        // 5. Thêm các thành phần vào row1
        row1.add(cmbTimKiem);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(pnlTimKiemCards);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(searchButton);
        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalGlue()); // Đẩy hàng 2 xuống

        // Hàng 2: Nút Thêm/Sửa/Xóa
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        row2.setBackground(CustomUI.white);
        row2.add(addButton);
        row2.add(editButton);
        row2.add(deleteButton);

        searchPanel.add(row2);
        searchPanel.add(Box.createVerticalStrut(10));

        searchButton.addActionListener(e ->{
            handleSearch();
        });

        return searchPanel;
    }

    private void handleSearch() {
        String selectedType = (String) cmbTimKiem.getSelectedItem();
        List<TaiKhoan> dsKetQua = new ArrayList<>();
        String searchText = "";

        try {
            if ("Tên đăng nhập".equals(selectedType)) {
                searchText = txtTimTenDN.getForeground().equals(Color.GRAY) ?
                        "" : txtTimTenDN.getText().trim();

                vn.iuh.entity.TaiKhoan tk = taiKhoanDAO.timTaiKhoanBangUserName(searchText);
                dsKetQua.add(tk);

            } else if ("Mã tài khoản".equals(selectedType)) {
                searchText = txtTimMaTK.getForeground().equals(Color.GRAY) ?
                        "" : txtTimMaTK.getText().trim();

                if (searchText.isEmpty()) {
                    loadTaiKhoanData();
                    return;
                } else {
                    TaiKhoan tk = taiKhoanDAO.timTaiKhoan(searchText);
                    if (tk != null) {
                        dsKetQua.add(tk);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modelTaiKhoan.setRowCount(0);

        if (dsKetQua.isEmpty() && !searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản nào phù hợp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (TaiKhoan tk : dsKetQua) {
                modelTaiKhoan.addRow(new Object[]{
                        tk.getMaTaiKhoan(),
                        tk.getTenDangNhap(),
                        convertMaChucVuToTen(tk.getMaChucVu()),
                        tk.getMaNhanVien()
                });
            }
        }
    }

    /**
     * Panel bên phải: Lọc theo chức vụ
     */
    private JPanel createCategoryPanel() {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new GridLayout(2, 2, 15, 15));
        categoryPanel.setBackground(CustomUI.white);
        categoryPanel.setPreferredSize(new Dimension(400, 200));
        categoryPanel.setMaximumSize(new Dimension(400, 200));
        categoryPanel.setBorder(new FlatLineBorder(new Insets(12, 12, 12, 12), Color.decode("#CED4DA"), 2, 30));

        categoryPanel.add(allCategoryButton);
        categoryPanel.add(leTanButton);
        categoryPanel.add(quanLyButton);
        categoryPanel.add(adminButton);

        // TODO: Thêm ActionListener cho các nút lọc (leTanButton, quanLyButton...)

        allCategoryButton.addActionListener(e -> filterTaiKhoanTable("Tất cả"));
        leTanButton.addActionListener(e -> filterTaiKhoanTable("Lễ tân"));
        quanLyButton.addActionListener(e -> filterTaiKhoanTable("Quản lý"));
        adminButton.addActionListener(e -> filterTaiKhoanTable("Admin"));

        return categoryPanel;
    }

    /**
     * Gộp panel Search (trái) và Category (phải)
     */
    private void createSearchAndCategoryPanel() {
        JPanel searchAndCategoryPanel = new JPanel();
        searchAndCategoryPanel.setLayout(new BoxLayout(searchAndCategoryPanel, BoxLayout.X_AXIS));
        searchAndCategoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210)); // Đặt chiều cao max
        searchAndCategoryPanel.setBackground(CustomUI.white);

        searchAndCategoryPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        searchAndCategoryPanel.add(createSearchPanel()); // Panel trái
        //searchAndCategoryPanel.add(Box.createHorizontalGlue()); // Thêm Glue ở giữa
        searchAndCategoryPanel.add(createCategoryPanel()); // Panel phải
        add(searchAndCategoryPanel);
    }

    /**
     * Tạo Panel chính chứa 2 bảng (Tài khoản và Nhân viên)
     */
    private void createListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout(0, 10));
        listPanel.setBackground(CustomUI.white);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        listPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // 1. Tạo thanh chuyển đổi (Switcher)
        JPanel switcherPanel = createTableSwitcher();

        // 2. Tạo CardPanel chứa 2 bảng
        tableCardLayout = new CardLayout();
        tableCardPanel = new JPanel(tableCardLayout);
        tableCardPanel.setBackground(CustomUI.white);

        // 3. Tạo 2 bảng và thêm vào CardPanel
        JScrollPane spTaiKhoan = createTaiKhoanTable();
        JScrollPane spNhanVien = createNhanVienTable();

        tableCardPanel.add(spTaiKhoan, "TAI_KHOAN");
        tableCardPanel.add(spNhanVien, "NHAN_VIEN");

        // 4. Thêm switcher và cardPanel vào
        listPanel.add(switcherPanel, BorderLayout.NORTH);
        listPanel.add(tableCardPanel, BorderLayout.CENTER);

        add(listPanel);
    }

    /**
     * Tạo thanh Tab (bằng JLabel) để chuyển đổi 2 bảng
     */
    private JPanel createTableSwitcher() {
        JPanel switcherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        switcherPanel.setBackground(CustomUI.white);

        lblTabTaiKhoan = new JLabel("Danh sách tài khoản");
        lblTabNhanVien = new JLabel("Danh sách nhân viên");

        // Style cho 2 tab
        styleTabLabel(lblTabTaiKhoan, true);
        styleTabLabel(lblTabNhanVien, false);

        // Sự kiện click
        lblTabTaiKhoan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableCardLayout.show(tableCardPanel, "TAI_KHOAN");
                styleTabLabel(lblTabTaiKhoan, true);
                styleTabLabel(lblTabNhanVien, false);
            }
        });

        lblTabNhanVien.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableCardLayout.show(tableCardPanel, "NHAN_VIEN");
                styleTabLabel(lblTabTaiKhoan, false);
                styleTabLabel(lblTabNhanVien, true);
            }
        });

        switcherPanel.add(lblTabTaiKhoan);
        switcherPanel.add(lblTabNhanVien);
        return switcherPanel;
    }

    private void styleTabLabel(JLabel label, boolean isSelected) {
        label.setFont(FONT_TABLE_SWITCHER);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (isSelected) {
            label.setForeground(CustomUI.blue);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, CustomUI.blue));
        } else {
            label.setForeground(Color.GRAY);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        }
    }

    /**
     * Chuyển đổi Mã Chức Vụ (ví dụ: CV001) sang Tên Chức Vụ (ví dụ: Lễ tân)
     * @param maChucVu Mã chức vụ từ CSDL
     * @return Tên chức vụ tương ứng
     */
    private String convertMaChucVuToTen(String maChucVu) {
        if (maChucVu == null) return "";

        String cleanedMaChucVu = maChucVu.trim().toUpperCase();

        return switch (cleanedMaChucVu) {
            case "CV001" -> "Lễ tân";
            case "CV002" -> "Quản lý";
            case "CV003" -> "Admin";
            default -> maChucVu;
        };
    }

    /**
     * Tạo bảng "Tài khoản"
     */
    private JScrollPane createTaiKhoanTable() {
        String[] columns = {"Mã tài khoản", "Tên đăng nhập", "Chức vụ", "Mã nhân viên"};
        modelTaiKhoan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTaiKhoan = new JTable(modelTaiKhoan);

        sorterTaiKhoan = new TableRowSorter<>(modelTaiKhoan);
        tblTaiKhoan.setRowSorter(sorterTaiKhoan);

        // --- Thêm ComboBox vào cột "Chức vụ" ---
        String[] roles = {"Lễ tân", "Quản lý", "Admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(FONT_TABLE_CELL);

        TableColumn roleColumn = tblTaiKhoan.getColumnModel().getColumn(2); // Cột "Chức vụ"
        roleColumn.setCellEditor(new DefaultCellEditor(roleComboBox));

        // Áp dụng style chung
        configureTable(tblTaiKhoan);

        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
        configureScrollPane(scrollPane);
        return scrollPane;
    }

    /**
     * Lọc bảng tài khoản theo chức vụ (cột 2) và chuyển tab.
     * @param role Chức vụ cần lọc ("Lễ tân", "Quản lý", "Admin") hoặc "Tất cả" để xóa bộ lọc.
     */
    private void filterTaiKhoanTable(String role) {
        loadTaiKhoanData();

        txtTimTenDN.setText("Nhập tên đăng nhập...");
        txtTimTenDN.setForeground(Color.GRAY);
        txtTimMaTK.setText("Nhập mã tài khoản...");
        txtTimMaTK.setForeground(Color.GRAY);

        // 1. Luôn chuyển về tab Tài khoản khi lọc
        tableCardLayout.show(tableCardPanel, "TAI_KHOAN");
        styleTabLabel(lblTabTaiKhoan, true);
        styleTabLabel(lblTabNhanVien, false);

        // 2. Áp dụng bộ lọc
        RowFilter<DefaultTableModel, Object> rf;
        if (role == null || role.equals("Tất cả")) {
            rf = null; // Xóa bộ lọc
        } else {
            // Lọc chính xác theo chuỗi ở cột 2 (Chức vụ)
            // Dùng regex "^" + role + "$" để đảm bảo khớp chính xác
            rf = RowFilter.regexFilter("^" + role + "$", 2);
        }

        if (sorterTaiKhoan != null) {
            sorterTaiKhoan.setRowFilter(rf);
        }
    }

    /**
     * Tạo bảng "Nhân viên"
     */
    private JScrollPane createNhanVienTable() {
        String[] columns = {"Mã nhân viên", "Tên nhân viên", "CCCD", "Ngày sinh", "Điện thoại"};
        modelNhanVien = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblNhanVien = new JTable(modelNhanVien);

        configureTable(tblNhanVien);

        JScrollPane scrollPane = new JScrollPane(tblNhanVien);
        configureScrollPane(scrollPane);
        return scrollPane;
    }

    // --- Tải Dữ liệu ---
    private void loadTaiKhoanData() {
        modelTaiKhoan.setRowCount(0);

        List<TaiKhoan> dsTaiKhoan;
        try {
            dsTaiKhoan = taiKhoanDAO.getAllTaiKhoan();
        }catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (TaiKhoan tk : dsTaiKhoan) {
            Object[] row = {
                    tk.getMaTaiKhoan(),
                    tk.getTenDangNhap(),
                    convertMaChucVuToTen(tk.getMaChucVu()),
                    tk.getMaNhanVien()
            };
            modelTaiKhoan.addRow(row);
        }
    }

    private void loadNhanVienData() {
        modelNhanVien.setRowCount(0); // Xóa dữ liệu cũ

        List<NhanVien> dsNhanVien;
        try {
            dsNhanVien = nhanVienDAO.dsNhanVienChuaCoTaiKhoan();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu nhân viên: "
                    + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (NhanVien nv : dsNhanVien) {
            Object[] row = {
                    nv.getMaNhanVien(),
                    nv.getTenNhanVien(),
                    nv.getCCCD(),
                    nv.getNgaySinh(),
                    nv.getSoDienThoai()
            };
            modelNhanVien.addRow(row);
        }
    }

    // --- CÁC HÀM TIỆN ÍCH ---

    private void configureTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(FONT_TABLE_CELL);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(CustomUI.white);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
        header.setBackground(CustomUI.blue);
        header.setForeground(CustomUI.white);
        header.setFont(FONT_TABLE_HEADER);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Padding

        for (int i = 0; i < table.getColumnCount(); i++) {
//            if (i == 1 || i == 2) { // Tên đăng nhập, Mật khẩu
//                table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
//            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
//            }
        }
    }

    private void configureScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CustomUI.white);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new FlatLineBorder(new Insets(0,0,0,0), Color.decode("#E5E7EB"), 2, 10),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
    }

    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height)); // Cho phép mở rộng
        field.setMinimumSize(new Dimension(120, size.height));
        field.setFont(FONT_LABEL);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (Objects.equals(field.getText(), placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMinimumSize(size);
        btn.setForeground(CustomUI.white);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1D4ED8"));
    }

    private JButton createCategoryButton(String text, String hexColor, Dimension size) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setBackground(Color.decode(hexColor));
        button.setForeground(CustomUI.white);
        button.setFont(FONT_CATEGORY);
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; borderWidth: 2; borderColor: #D1D5DB; focusWidth: 0; innerFocusWidth: 0;");
        button.setFocusPainted(false);
        return button;
    }

    private JButton createActionButtonAsync(String text, String iconPath, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFont(FONT_ACTION);
        button.setBackground(Color.decode(bgHex));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor:" + borderHex);

//        loadIconAsync(iconPath, 20, 20, icon -> {
//            if (icon != null) button.setIcon(icon);
//        });
        return button;
    }

//    // --- CÁC HÀM TẢI ICON (Giữ nguyên) ---
//    private static String iconCacheKey(String path, int w, int h, int arc) {
//        return path + "|" + w + "x" + h + "|arc:" + arc;
//    }
//
//    private static synchronized ImageIcon getCachedIcon(String key) {
//        return ICON_CACHE.get(key);
//    }
//
//    private static void loadIconAsync(String path, int w, int h, Consumer<ImageIcon> callback) {
//        String key = iconCacheKey(path, w, h, 0);
//        synchronized (ICON_CACHE) {
//            ImageIcon cached = ICON_CACHE.get(key);
//            if (cached != null) {
//                SwingUtilities.invokeLater(() -> callback.accept(cached));
//                return;
//            }
//        }
//        SwingWorker<ImageIcon, Void> wk = new SwingWorker<>() {
//            @Override
//            protected ImageIcon doInBackground() {
//                try (InputStream is = QuanLyTaiKhoanPanel.class.getResourceAsStream(path)) {
//                    if (is == null) return null;
//                    BufferedImage img = ImageIO.read(is);
//                    if (img == null) return null;
//                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
//                    ImageIcon ic = new ImageIcon(scaled);
//                    synchronized (ICON_CACHE) {
//                        ICON_CACHE.put(iconCacheKey(path, w, h, 0), ic);
//                    }
//                    return ic;
//                } catch (Exception ex) {
//                    return null;
//                }
//            }
//            @Override
//            protected void done() {
//                try {
//                    ImageIcon ic = get();
//                    if (ic != null) callback.accept(ic);
//                } catch (Exception ignored) {}
//            }
//        };
//        wk.execute();
//    }
}

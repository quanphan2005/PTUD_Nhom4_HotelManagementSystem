package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.Phong;
import vn.iuh.gui.dialog.PhongDialog;
import vn.iuh.gui.dialog.SuaPhongDialog;
import vn.iuh.gui.dialog.ThemPhongDialog;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.RoomService;
import vn.iuh.service.impl.RoomServiceImpl;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class QuanLyPhongPanel extends JPanel {

    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(220, 46);
    // ==================================================================
    private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(190, 52);

    // Fonts tái sử dụng
    private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 18);
    // ==================================================================
    private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

    // Fonts cho thẻ phòng (không dùng nhiều trong phiên bản table nhưng giữ để tương thích)
    private static final Font FONT_ROOM_NAME  = new Font("Arial", Font.BOLD, 30);
    private static final Font FONT_ROOM_SUB   = new Font("Arial", Font.BOLD, 23);

    // Các thành phần trong panel tìm kiếm
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    // Các thành phần trong panel loại phòng
    private JButton onePeopleButton;
    private JButton twoPeopleButton;
    private JButton fourPeopleButton;
    private JButton vipButton;
    private JButton normalButton;
    private JButton allRoomButton;
    private JButton emptyRoomButton;

    // Table components for room list (replacing the previous card-style list)
    private JTable roomTable;
    private DefaultTableModel roomTableModel;

    // Service
    private final RoomService roomService = new RoomServiceImpl();

    // Constructor
    public QuanLyPhongPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
    }

    // Hàm init tập hợp các bước khởi tạo giao diện chính
    private void init() {
        initButtons(); // khởi tạo và cấu hình các button + input

        createTopPanel(); // Panel chứa title (Quản lý phòng)
        add(Box.createVerticalStrut(10));
        createSearchAndCategoryPanel(); // Panel chứa khung tìm kiếm và khung loại phòng
        add(Box.createVerticalStrut(10));

        // Thay vì createListRoomPanel() gốc, ta tạo bảng danh sách phòng
        createRoomTablePanel();

        // Load danh sách tất cả các phòng
        reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());

    }

    // Tạo và cấu hình các nút/ô nhập dùng chung
    private void initButtons() {
        // Cấu hình ô tìm kiếm (placeholder, kích thước)
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Mã phòng");

        // Cấu hình nút tìm
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // Các nút hành động (thêm/sửa/xóa) — tạo ngay nhưng **KHÔNG** tải icon/hình ảnh để tối ưu khởi động
        addButton    = createActionButton("Thêm phòng", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa phòng", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa phòng", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // Các nút category (1 người, 2 người...)
        onePeopleButton   = createCategoryButton("1 người", "#1BA1E2", CATEGORY_BUTTON_SIZE);
        twoPeopleButton   = createCategoryButton("2 người", "#34D399", CATEGORY_BUTTON_SIZE);
        fourPeopleButton  = createCategoryButton("4 người", "#A78BFA", CATEGORY_BUTTON_SIZE);
        vipButton         = createCategoryButton("VIP", "#E3C800", CATEGORY_BUTTON_SIZE);
        normalButton      = createCategoryButton("Thường", "#647687", CATEGORY_BUTTON_SIZE);
        allRoomButton     = createCategoryButton("Toàn bộ", "#3B82F6", CATEGORY_BUTTON_SIZE);
        emptyRoomButton   = createCategoryButton("Phòng trống", "#059669", CATEGORY_BUTTON_SIZE);

        // Gắn listeners cho các nút category
        allRoomButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel()));
        emptyRoomButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getRoomsByStatus("Trống")));
        onePeopleButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getRoomsByPeopleCount(1)));
        if (twoPeopleButton != null) twoPeopleButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getRoomsByPeopleCount(2)));

        try {
            Field threeField = this.getClass().getDeclaredField("threePeopleButton");
            if (threeField != null) {
                threeField.setAccessible(true);
                Object val = threeField.get(this);
                if (val instanceof JButton) {
                    JButton threeBtn = (JButton) val;
                    threeBtn.addActionListener(ev -> reloadRoomsAsync(() -> roomService.getRoomsByPeopleCount(3)));
                }
            }
        } catch (Exception ignored) {}
        if (fourPeopleButton != null) fourPeopleButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getRoomsByPeopleCount(4)));

        normalButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getRoomsByPhanLoai("Thường")));
        vipButton.addActionListener(e -> reloadRoomsAsync(() -> roomService.getRoomsByPhanLoai("Vip")));

        // Sự kiện nút thêm
        addButton.addActionListener(e -> {
            try {
                // Lấy window cha để dialog modal căn theo đó
                Window owner = SwingUtilities.getWindowAncestor(QuanLyPhongPanel.this);
                // Mở dialog thêm phòng
                ThemPhongDialog dialog = new ThemPhongDialog(owner, true, (RoomServiceImpl) roomService);
                dialog.setLocationRelativeTo(owner);
                dialog.setVisible(true);

                // Sau khi dialog đóng, refresh danh sách phòng
                reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this,
                        "Không thể mở dialog thêm phòng: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Sửa phòng: lấy selection hiện tại, kiểm tra rồi mở SuaPhongDialog
        editButton.addActionListener(e -> {
            Phong sel = getSelectedPhong();
            if (sel == null) {
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Vui lòng chọn 1 phòng để sửa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Lấy trạng thái hiện tại của phòng (theo logic dùng trong populateRoomList)
            String currentStatus = "CÒN TRỐNG";
            try {
                CongViec cv = roomService.getCurrentJobForRoom(sel.getMaPhong());
                if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isBlank()) {
                    currentStatus = cv.getTenTrangThai();
                } else if (!sel.isDangHoatDong()) {
                    currentStatus = "BẢO TRÌ";
                }
            } catch (Exception ex) {
                if (!sel.isDangHoatDong()) currentStatus = "BẢO TRÌ";
            }

            // Cho phép sửa nếu phòng đang "Trống" hoặc "Bảo trì"
            boolean allowedToEdit = "CÒN TRỐNG".equalsIgnoreCase(currentStatus) || "BẢO TRÌ".equalsIgnoreCase(currentStatus);

            if (!allowedToEdit) {
                JOptionPane.showMessageDialog(
                        QuanLyPhongPanel.this,
                        "Không thể sửa phòng vì trạng thái hiện tại: " + currentStatus + ".\nChỉ cho phép sửa khi phòng ở trạng thái 'CÒN TRỐNG' hoặc 'BẢO TRÌ'.",
                        "Không thể sửa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Vẫn không cho sửa nếu có đơn đặt phòng trong tương lai
            if (hasFutureBookings(sel)) {
                JOptionPane.showMessageDialog(
                        QuanLyPhongPanel.this,
                        "Không thể sửa phòng vì phòng hiện có đơn đặt phòng trong tương lai!",
                        "Không thể sửa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Mở dialog sửa
            try {
                Window owner = SwingUtilities.getWindowAncestor(QuanLyPhongPanel.this);
                SuaPhongDialog dialog = new SuaPhongDialog(owner, sel, roomService);
                dialog.setVisible(true);
                if (dialog.isSaved()) reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Lỗi khi mở form sửa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xóa phòng: lấy selection, kiểm tra và gọi service xóa giống logic cũ
        deleteButton.addActionListener(e -> {
            Phong sel = getSelectedPhong();
            if (sel == null) {
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Vui lòng chọn 1 phòng để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            boolean canDelete = true;
            String currentStatus = "Không xác định";
            try {
                CongViec cv = roomService.getCurrentJobForRoom(sel.getMaPhong());
                if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isBlank()) {
                    canDelete = false;
                    currentStatus = cv.getTenTrangThai();
                } else if (!sel.isDangHoatDong()) {
                    canDelete = false;
                    currentStatus = "Bảo trì";
                } else {
                    currentStatus = "Trống";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                canDelete = false;
                currentStatus = "Không xác định (lỗi khi kiểm tra trạng thái)";
            }

            if (!canDelete) {
                JOptionPane.showMessageDialog(
                        QuanLyPhongPanel.this,
                        "Không thể xóa phòng vì trạng thái hiện tại: " + currentStatus + ".\nChỉ có thể xóa khi phòng đang ở trạng thái 'CÒN TRỐNG'.",
                        "Không thể xóa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            } else if (hasFutureBookings(sel)) {
                JOptionPane.showMessageDialog(
                        QuanLyPhongPanel.this,
                        "Không thể xóa phòng vì phòng hiện có đơn đặt phòng trong tương lai!",
                        "Không thể xóa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int ans = JOptionPane.showConfirmDialog(QuanLyPhongPanel.this, "Bạn có chắc muốn xóa phòng này không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;

            boolean ok = false;
            try {
                if (roomService instanceof RoomServiceImpl) {
                    ok = ((RoomServiceImpl) roomService).deleteRoomWithHistory(sel.getMaPhong());
                } else {
                    ok = roomService.deleteRoomByID(sel.getMaPhong()); // fallback
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (ok) {
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Xóa thành công.");
                reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
            } else {
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Xóa thất bại.");
            }
        });
    }

    // Cấu hình ô text tìm kiếm với placeholder và style FlatLaf
    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMaximumSize(size);
        field.setMinimumSize(size);
        field.setFont(FONT_LABEL);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 12"); // bo góc FlatLaf

        // Placeholder behavior — khi focus vào/xuống thì đổi text và màu
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Nếu nội dung đang là placeholder thì xóa và đổi màu chữ
                if (Objects.equals(field.getText(), placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Nếu rỗng thì đặt lại placeholder
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    // Cấu hình nút tìm (kích thước, font, màu)
    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMinimumSize(size);
        btn.setForeground(CustomUI.white);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1D4ED8"));
    }

    // Helper tạo nút trong khung loại phòng (các nút lọc theo loại/ số người)
    private JButton createCategoryButton(String text, String hexColor, Dimension size) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        button.setBackground(Color.decode(hexColor));
        button.setForeground(CustomUI.white);
        button.setFont(FONT_CATEGORY);

        // Sử dụng thuộc tính FlatLaf để bo góc, vẽ viền tùy chỉnh
        button.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; borderWidth: 2; borderColor: #D1D5DB; focusWidth: 0; innerFocusWidth: 0;");
        button.setFocusPainted(false);
        return button;
    }

    // Tạo các action button (phiên bản tối giản: không tải icon)
    private JButton createActionButton(String text, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFont(FONT_ACTION);
        try {
            button.setBackground(Color.decode(bgHex));
        } catch (Exception ignored) {}
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);

        // Bo góc và viền màu
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor:" + borderHex);
        // NOTE: intentionally do NOT load any icon here to reduce startup cost
        return button;
    }

    // Tiêu đề
    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý phòng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont : FONT_ROOM_NAME);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        add(pnlTop); // thêm vào panel chính (this)
    }

    // Panel chứa các component tìm kiếm + nút thêm
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        // <-- CHỈNH: chiều cao của search panel giảm xuống 200 để khớp với category panel
        searchPanel.setPreferredSize(new Dimension(650, 200));
        searchPanel.setMaximumSize(new Dimension(650, 200));
        Border paddingBorder = BorderFactory.createEmptyBorder(12, 12, 12, 12);
        searchPanel.setBorder(paddingBorder);
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setOpaque(true);
        // Dùng FlatLineBorder để có viền bo tròn và offset
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        // Thêm tùy chọn tìm: Mã phòng, Tên phòng, Trạng thái
        String[] searchOptions = {"Mã phòng", "Tên phòng", "Trạng thái"};
        JComboBox<String> searchTypeComboBox = new JComboBox<>(searchOptions);

        Dimension searchTypeSize = new Dimension(120, 45);
        searchTypeComboBox.setPreferredSize(searchTypeSize);
        searchTypeComboBox.setMaximumSize(searchTypeSize);
        searchTypeComboBox.setMinimumSize(searchTypeSize);
        searchTypeComboBox.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel inputPanel = new JPanel(new CardLayout());

        Dimension inputSize = new Dimension(380, 45);
        inputPanel.setPreferredSize(inputSize);
        inputPanel.setMaximumSize(inputSize);
        inputPanel.setMinimumSize(inputSize);

        // JTextField cho case "Mã phòng"
        JTextField roomCodeField = new JTextField();
        final String roomPlaceholder = "Mã phòng";
        configureSearchTextField(roomCodeField, new Dimension(380,45), roomPlaceholder);
        roomCodeField.setMaximumSize(new Dimension(380,45));
        roomCodeField.setMinimumSize(new Dimension(380,45));

        // JTextField cho case "Tên phòng" (mới thêm)
        JTextField roomNameField = new JTextField();
        final String namePlaceholder = "Tên phòng";
        configureSearchTextField(roomNameField, new Dimension(380,45), namePlaceholder);
        roomNameField.setMaximumSize(new Dimension(380,45));
        roomNameField.setMinimumSize(new Dimension(380,45));

        // JComboBox cho case "Trạng thái"
        String[] statusOptions = {
                "Trống", "Chờ checkin", "Kiểm tra", "Sử dụng",
                "Checkout trễ", "Dọn dẹp", "Bảo trì"
        };
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(380, 45));
        statusComboBox.setMaximumSize(new Dimension(380, 45));
        statusComboBox.setMinimumSize(new Dimension(380, 45));
        statusComboBox.setFont(new Font("Arial", Font.BOLD, 15));
        statusComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        // Thêm 3 view vào CardLayout để chuyển giữa input text (Mã/Tên) và combobox (Trạng thái)
        inputPanel.add(roomCodeField, "Mã phòng");
        inputPanel.add(roomNameField, "Tên phòng");
        inputPanel.add(statusComboBox, "Trạng thái");

        // Khi thay đổi dropdown thì chuyển view tương ứng trong CardLayout
        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout)(inputPanel.getLayout());
            String selected = (String) searchTypeComboBox.getSelectedItem();
            cl.show(inputPanel, selected);

            // Khi đổi chế độ tìm, ngay lập tức kích hoạt tìm tương ứng
            if ("Mã phòng".equals(selected)) {
                String txt = roomCodeField.getText();
                if (txt == null || txt.trim().isEmpty() || (roomPlaceholder.equals(txt) && roomCodeField.getForeground().equals(Color.GRAY))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                } else {
                    final String q = txt.trim();
                    reloadRoomsAsync(() -> {
                        List<Phong> all = roomService.getAllQuanLyPhongPanel();
                        if (all == null) return Collections.emptyList();
                        String qLower = q.toLowerCase();
                        List<Phong> res = new ArrayList<>();
                        for (Phong p : all) {
                            if (p.getMaPhong() != null && p.getMaPhong().toLowerCase().contains(qLower)) {
                                res.add(p);
                            }
                        }
                        return res;
                    });
                }
            } else if ("Tên phòng".equals(selected)) {
                String txt = roomNameField.getText();
                if (txt == null || txt.trim().isEmpty() || (namePlaceholder.equals(txt) && roomNameField.getForeground().equals(Color.GRAY))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                } else {
                    final String q = txt.trim();
                    reloadRoomsAsync(() -> {
                        List<Phong> all = roomService.getAllQuanLyPhongPanel();
                        if (all == null) return Collections.emptyList();
                        String qLower = q.toLowerCase();
                        List<Phong> res = new ArrayList<>();
                        for (Phong p : all) {
                            if (p.getTenPhong() != null && p.getTenPhong().toLowerCase().contains(qLower)) {
                                res.add(p);
                            }
                        }
                        return res;
                    });
                }
            } else { // Trạng thái
                String status = (String) statusComboBox.getSelectedItem();
                reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
            }
        });

        // Realtime filter: Mã phòng field
        roomCodeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                if (!"Mã phòng".equals((String) searchTypeComboBox.getSelectedItem())) return;

                String txt = roomCodeField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (roomPlaceholder.equals(txt) && roomCodeField.getForeground().equals(Color.GRAY))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    return;
                }
                final String query = txt.trim().toLowerCase();

                reloadRoomsAsync(() -> {
                    List<Phong> all = roomService.getAllQuanLyPhongPanel();
                    if (all == null) return Collections.emptyList();
                    List<Phong> filtered = new ArrayList<>();
                    for (Phong p : all) {
                        if (p.getMaPhong() != null && p.getMaPhong().toLowerCase().contains(query)) {
                            filtered.add(p);
                        }
                    }
                    return filtered;
                });
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // Realtime filter: Tên phòng field (mới thêm)
        roomNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                if (!"Tên phòng".equals((String) searchTypeComboBox.getSelectedItem())) return;

                String txt = roomNameField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (namePlaceholder.equals(txt) && roomNameField.getForeground().equals(Color.GRAY))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    return;
                }
                final String query = txt.trim().toLowerCase();

                reloadRoomsAsync(() -> {
                    List<Phong> all = roomService.getAllQuanLyPhongPanel();
                    if (all == null) return Collections.emptyList();
                    List<Phong> filtered = new ArrayList<>();
                    for (Phong p : all) {
                        if (p.getTenPhong() != null && p.getTenPhong().toLowerCase().contains(query)) {
                            filtered.add(p);
                        }
                    }
                    return filtered;
                });
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // Khi chọn trạng thái trong combobox ==> Lọc theo trạng thái
        statusComboBox.addActionListener(e -> {
            if (!"Trạng thái".equals((String) searchTypeComboBox.getSelectedItem())) return;
            String status = (String) statusComboBox.getSelectedItem();
            reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
        });

        // Row 1: dropdown + input + nút TÌM
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);

        row1.setMaximumSize(new Dimension(650, 60));
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(searchButton);

        // Sự kiện của nút tìm (bắt cả 3 trường hợp)
        searchButton.addActionListener(e -> {
            String mode = (String) searchTypeComboBox.getSelectedItem();
            if ("Mã phòng".equals(mode)) {
                String txt = roomCodeField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (roomPlaceholder.equals(txt) && roomCodeField.getForeground().equals(Color.GRAY))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                } else {
                    final String q = txt.trim().toLowerCase();
                    reloadRoomsAsync(() -> {
                        List<Phong> all = roomService.getAllQuanLyPhongPanel();
                        if (all == null) return Collections.emptyList();
                        List<Phong> filtered = new ArrayList<>();
                        for (Phong p : all) {
                            if (p.getMaPhong() != null && p.getMaPhong().toLowerCase().contains(q)) filtered.add(p);
                        }
                        return filtered;
                    });
                }
            } else if ("Tên phòng".equals(mode)) {
                String txt = roomNameField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (namePlaceholder.equals(txt) && roomNameField.getForeground().equals(Color.GRAY))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                } else {
                    final String q = txt.trim().toLowerCase();
                    reloadRoomsAsync(() -> {
                        List<Phong> all = roomService.getAllQuanLyPhongPanel();
                        if (all == null) return Collections.emptyList();
                        List<Phong> filtered = new ArrayList<>();
                        for (Phong p : all) {
                            if (p.getTenPhong() != null && p.getTenPhong().toLowerCase().contains(q)) filtered.add(p);
                        }
                        return filtered;
                    });
                }
            } else { // Trạng thái
                String status = (String) statusComboBox.getSelectedItem();
                reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
            }
        });

        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(10));

        // ---------- First action row: Thêm & Sửa (căn giữa) ----------
        JPanel actionRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        actionRow1.setBackground(CustomUI.white);

        // Đồng bộ kích thước 3 nút bằng ACTION_BUTTON_SIZE
        Dimension btnSize = new Dimension(ACTION_BUTTON_SIZE.width, ACTION_BUTTON_SIZE.height);
        addButton.setPreferredSize(btnSize);
        editButton.setPreferredSize(btnSize);
        deleteButton.setPreferredSize(btnSize);

        // đặt 2 nút chính (Thêm + Sửa) căn giữa, tăng khoảng cách giữa hai nút để "Thêm" hơi lệch sang trái
        actionRow1.add(addButton);
        actionRow1.add(Box.createHorizontalStrut(24)); // tăng khoảng cách để dịch lệch hơi sang trái
        actionRow1.add(editButton);
        searchPanel.add(actionRow1);

        // giảm khoảng cách dọc giữa 2 hàng (nhỏ hơn trước)
        searchPanel.add(Box.createVerticalStrut(6));

        // ---------- Second action row: Xóa (căn giữa) ----------
        JPanel actionRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        actionRow2.setBackground(CustomUI.white);
        actionRow2.add(deleteButton);
        searchPanel.add(actionRow2);

        // thêm khoảng đệm nhỏ phía dưới để nút Xóa không chạm viền dưới của searchPanel
        searchPanel.add(Box.createVerticalStrut(8));

        return searchPanel;
    }


    // Panel bên phải chứa các nút category (bộ lọc nhanh theo số người/loại)
    private JPanel createCategoryPanel() {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(CustomUI.white);
        categoryPanel.setPreferredSize(new Dimension(655, 200));
        categoryPanel.setMaximumSize(new Dimension(655, 200));
        Border paddingBorder = BorderFactory.createEmptyBorder(12, 8, 12, 8);
        categoryPanel.setBorder(paddingBorder);
        categoryPanel.setOpaque(true);
        categoryPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        // Row 1: Toàn bộ, Phòng trống, 1 người (left-aligned)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(allRoomButton);
        row1.add(emptyRoomButton);
        row1.add(onePeopleButton);
        categoryPanel.add(row1);
        categoryPanel.add(Box.createVerticalStrut(10));

        // Row 2: 2 người, 4 người, VIP (đã bỏ 3 người nên VIP dịch sang đây)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row2.setBackground(CustomUI.white);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(twoPeopleButton);
        row2.add(fourPeopleButton);
        row2.add(vipButton);
        categoryPanel.add(row2);
        categoryPanel.add(Box.createVerticalStrut(10));

        // Row 3: Thường + placeholder để giữ khoảng trống (left-aligned)
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row3.setBackground(CustomUI.white);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(normalButton);
        // Hai placeholder để vẫn giữ bố cục 3 cột (tránh căn giữa)
        row3.add(Box.createRigidArea(new Dimension(CATEGORY_BUTTON_SIZE.width, CATEGORY_BUTTON_SIZE.height)));
        row3.add(Box.createRigidArea(new Dimension(CATEGORY_BUTTON_SIZE.width, CATEGORY_BUTTON_SIZE.height)));
        categoryPanel.add(row3);

        return categoryPanel;
    }

    // Kết hợp searchPanel và categoryPanel vào một hàng ngang lớn
    private void createSearchAndCategoryPanel() {
        JPanel searchAndCategoryPanel = new JPanel();
        searchAndCategoryPanel.setLayout(new BoxLayout(searchAndCategoryPanel, BoxLayout.X_AXIS));
        searchAndCategoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        searchAndCategoryPanel.setBackground(CustomUI.white);
        JPanel leftPanel = createSearchPanel();
        searchAndCategoryPanel.add(leftPanel);

        searchAndCategoryPanel.add(Box.createHorizontalGlue());
        JPanel rightPanel = createCategoryPanel();
        searchAndCategoryPanel.add(rightPanel);

        add(searchAndCategoryPanel);
    }

    // Giờ chuyển phần danh sách phòng sang table (BỎ cột Thao tác)
    private void createRoomTablePanel() {
        // Lưu ý: cột ẩn cuối cùng dùng để lưu object Phong (không hiển thị)
        String[] columnNames = {"Mã phòng", "Tên phòng", "Loại", "Số người", "Giá giờ", "Giá ngày", "Trạng thái", "OBJ"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // toàn bộ cell không editable (không có cột thao tác)
                return false;
            }
        };

        roomTable = new JTable(roomTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);

                if (isRowSelected(row)) {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                    c.setForeground(CustomUI.black);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(CustomUI.ROW_EVEN != null ? CustomUI.ROW_EVEN : Color.WHITE);
                    } else {
                        c.setBackground(CustomUI.ROW_ODD != null ? CustomUI.ROW_ODD : new Color(0xF7F9FB));
                    }
                    c.setForeground(CustomUI.black);
                }

                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));
                return c;
            }
        };

        roomTable.setRowHeight(48);
        roomTable.getTableHeader().setPreferredSize(new Dimension(roomTable.getWidth(), 40));
        roomTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        roomTable.getTableHeader().setBackground(CustomUI.blue);
        roomTable.getTableHeader().setForeground(CustomUI.white);
        roomTable.getTableHeader().setOpaque(true);

        roomTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                comp.setFont(CustomUI.TABLE_FONT);
                return comp;
            }
        });

        // Đăng ký double-click để xem chi tiết phòng -> lấy Phong từ cột ẩn (index 7)
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int viewRow = roomTable.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        int modelRow = roomTable.convertRowIndexToModel(viewRow);
                        Object val = roomTableModel.getValueAt(modelRow, 7); // cột ẩn
                        if (val instanceof Phong) {
                            Phong p = (Phong) val;
                            SwingUtilities.invokeLater(() -> {
                                try { PhongDialog.showDialog(QuanLyPhongPanel.this, p, roomService); }
                                catch (Exception ex) { ex.printStackTrace(); }
                            });
                        }
                    }
                }
            }
        });

        // Kích thước cột
        roomTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        roomTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = roomTable.getWidth();
                TableColumnModel columnModel = roomTable.getColumnModel();
                if (columnModel.getColumnCount() < 8) return;
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.10)); // Mã
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.18)); // Tên
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.12)); // Loại
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.08)); // Số người
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.12)); // Giá giờ
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.12)); // Giá ngày
                columnModel.getColumn(6).setPreferredWidth((int) (tableWidth * 0.12)); // Trạng thái
                // cột 7 là cột OBJ (ẩn) -> đặt width = 0 để ẩn
                columnModel.getColumn(7).setMinWidth(0);
                columnModel.getColumn(7).setMaxWidth(0);
                columnModel.getColumn(7).setPreferredWidth(0);
                columnModel.getColumn(7).setResizable(false);
            }
        });

        JScrollPane scrollPane = new JScrollPane(roomTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 500));

        add(scrollPane);
    }

    // Format giá tiền
    private static String formatPrice(double price) {
        if (price <= 0.0) return "0 VNĐ";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " VNĐ";
    }

    // Reload dữ liệu không đồng bộ
    private void reloadRoomsAsync(Supplier<List<Phong>> loader) {
        SwingWorker<List<Phong>, Void> wk = new SwingWorker<List<Phong>, Void>() {
            @Override
            protected List<Phong> doInBackground() {
                try {
                    List<Phong> list = loader.get();
                    return list;
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Phong> rooms = get();
                    populateRoomList(rooms);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        wk.execute();
    }

    // Điền dữ liệu vào bảng
    private void populateRoomList(List<Phong> rooms) {
        SwingUtilities.invokeLater(() -> {
            roomTableModel.setRowCount(0);

            if (rooms != null && !rooms.isEmpty()) {
                for (Phong p : rooms) {
                    LoaiPhong lp = null;
                    try { lp = roomService.getRoomCategoryByID(p.getMaLoaiPhong()); } catch (Exception ignore) {}
                    int soNguoi = lp != null ? lp.getSoLuongKhach() : 0;
                    String loaiPhongTen = lp != null ? (lp.getPhanLoai() != null && !lp.getPhanLoai().isEmpty() ? lp.getPhanLoai() : lp.getTenLoaiPhong()) : "Thường";

                    double[] latest = roomService.getLatestPriceForLoaiPhong(p.getMaLoaiPhong());
                    String giaGioStr = formatPrice(latest.length > 1 ? latest[1] : 0.0);
                    String giaNgayStr = formatPrice(latest.length > 0 ? latest[0] : 0.0);

                    String trangThai = "Trống";
                    try {
                        CongViec cv = roomService.getCurrentJobForRoom(p.getMaPhong());
                        if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isEmpty()) {
                            trangThai = cv.getTenTrangThai();
                        } else if (!p.isDangHoatDong()) {
                            trangThai = "Bảo trì";
                        }
                    } catch (Exception e) {
                        if (!p.isDangHoatDong()) trangThai = "Bảo trì";
                    }

                    Object[] row = new Object[8];
                    row[0] = p.getMaPhong();
                    row[1] = p.getTenPhong();
                    row[2] = loaiPhongTen;
                    row[3] = soNguoi;
                    row[4] = giaGioStr;
                    row[5] = giaNgayStr;
                    row[6] = trangThai;
                    row[7] = p; // store Phong object in hidden column

                    roomTableModel.addRow(row);
                }
            } else {
                // Khi không có bản ghi, hiển thị 1 hàng thông báo (OBJ null)
                roomTableModel.addRow(new Object[] {"-","Không tìm thấy phòng phù hợp.","-","-","-","-","-", null});
            }
        });
    }

    // helper: lấy Phong đã chọn (dựa trên selection model), trả về null nếu không có
    private Phong getSelectedPhong() {
        int viewRow = roomTable.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = roomTable.convertRowIndexToModel(viewRow);
        Object obj = roomTableModel.getValueAt(modelRow, 7); // cột ẩn
        if (obj instanceof Phong) return (Phong) obj;
        // fallback: nếu cột 0 có mã phòng, try load từ service
        Object codeObj = roomTableModel.getValueAt(modelRow, 0);
        if (codeObj != null) {
            try {
                String ma = codeObj.toString();
                List<Phong> all = roomService.getAllQuanLyPhongPanel();
                if (all != null) {
                    for (Phong p : all) if (ma.equals(p.getMaPhong())) return p;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Giữ lại hàm kiểm tra trạng thái sửa/xóa như cũ
    private boolean hasCurrentJob(Phong p) {
        if (p == null) return false;
        try {
            CongViec cv = roomService.getCurrentJobForRoom(p.getMaPhong());
            if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isBlank()) {
                // CÓ công việc hiện tại ==> Không cho sửa
                return false;
            }
        } catch (Exception ignored) {
        }
        return p.isDangHoatDong();
    }

    private boolean hasFutureBookings(Phong p) {
        if (p == null) return false;
        boolean check = false;
        try { check = roomService.hasFutureBookings(p); } catch (Exception ignored) {}
        return check;
    }

}

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
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.RoomService;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.service.impl.RoomServiceImpl;
import vn.iuh.util.AppEventBus;

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

    // matched to QuanLyKhachHangPanel sizing
    private static final int SEARCH_CONTROL_HEIGHT = 40;
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, SEARCH_CONTROL_HEIGHT);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(110, SEARCH_CONTROL_HEIGHT);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 50);

    // Fonts tái sử dụng
    private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 14); // bold like customer panel
    private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

    // Fonts cho thẻ phòng (không dùng nhiều trong phiên bản table nhưng giữ để tương thích)
    private static final Font FONT_ROOM_NAME  = new Font("Arial", Font.BOLD, 30);
    private static final Font FONT_ROOM_SUB   = new Font("Arial", Font.BOLD, 23);

    // Các thành phần trong panel tìm kiếm (một số biến được promote lên fields để dễ reload)
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    // Fields mới: để có thể reload search panel (category) từ bên ngoài createSearchPanel
    private JComboBox<String> categoryComboBox;
    private Map<String, String> categoryNameToId = new HashMap<>();
    private JTextField roomCodeField;  // alias tới searchTextField
    private JTextField roomNameField;
    private JComboBox<String> searchTypeComboBox;
    private JComboBox<String> statusComboBox;

    // Table components for room list (replacing the previous card-style list)
    private JTable roomTable;
    private DefaultTableModel roomTableModel;

    // Service
    private final RoomService roomService = new RoomServiceImpl();
    private final LoaiPhongService loaiPhongService = new LoaiPhongServiceImpl();

    private final Runnable roomsUpdatedListener = () -> {
        // gọi reload và đồng thời reload search categories
        reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel(), this::reloadCategories);
    };


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
        createSearchAndCategoryPanel(); // giờ chỉ thêm search panel (category panel đã bị loại bỏ)
        add(Box.createVerticalStrut(10));

        // Thay vì createListRoomPanel() gốc, ta tạo bảng danh sách phòng
        createRoomTablePanel();

        // Load danh sách tất cả các phòng + load categories cho search panel
        reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel(), this::reloadCategories);

        AppEventBus.subscribe("ROOMS_UPDATED", roomsUpdatedListener);

    }

    // Tạo và cấu hình các nút/ô nhập dùng chung
    private void initButtons() {
        // Cấu hình ô tìm kiếm (placeholder, kích thước) - bold giống panel khách hàng
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Tìm kiếm...");

        // Cấu hình nút tìm (kích thước giống panel khách hàng)
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // Các nút hành động (thêm/sửa/xóa) -> kích thước giống panel khách hàng
        addButton    = createActionButton("Thêm phòng", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa phòng", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa phòng", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // Sự kiện của nút thêm
        addButton.addActionListener(e -> {
            try {
                Window owner = SwingUtilities.getWindowAncestor(QuanLyPhongPanel.this);
                ThemPhongDialog dialog = new ThemPhongDialog(owner, true, (RoomServiceImpl) roomService);
                dialog.setLocationRelativeTo(owner);
                dialog.setVisible(true);
                // reload cả danh sách phòng và categories (trong trường hợp thêm loại phòng ảnh hưởng)
                reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel(), this::reloadCategories);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this,
                        "Không thể mở dialog thêm phòng: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Sửa phòng
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
                if (dialog.isSaved()) {
                    // reload rooms + categories (categories nếu sửa loại liên quan)
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel(), this::reloadCategories);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Lỗi khi mở form sửa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Xóa phòng
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
                // reload rooms + categories (nếu xóa ảnh hưởng)
                reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel(), this::reloadCategories);
            } else {
                JOptionPane.showMessageDialog(QuanLyPhongPanel.this, "Xóa thất bại.");
            }
        });
    }

    // Cấu hình ô text tìm kiếm với placeholder và style FlatLaf
    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        field.setMinimumSize(new Dimension(120, size.height));
        field.setFont(FONT_LABEL); // bold like customer panel
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Placeholder behavior — khi focus vào/xuống thì đổi text và màu
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

    // Cấu hình nút tìm (kích thước, font, màu)
    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setForeground(CustomUI.white);
        btn.setFont(FONT_LABEL); // bold like customer panel
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1D4ED8"));
        btn.setMargin(new Insets(6, 10, 6, 10));
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

        // Bo góc và viền màu giống các panel khác
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 18; borderWidth: 2; borderColor:" + borderHex);
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

    // Panel tìm kiếm (đã chỉnh theo kiểu QuanLyKhachHangPanel)
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setPreferredSize(new Dimension(0, 200));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setOpaque(true);
        // Đồng bộ border/arc với QuanLyKhachHangPanel (arc:25)
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 25));

        // search type combo (kích thước giống panel khách hàng)
        String[] searchOptions = {"Mã phòng", "Tên phòng", "Loại phòng", "Trạng thái"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        Dimension comboSize = new Dimension(180, SEARCH_CONTROL_HEIGHT);
        searchTypeComboBox.setPreferredSize(comboSize);
        searchTypeComboBox.setMinimumSize(comboSize);
        searchTypeComboBox.setMaximumSize(comboSize);
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // input area (CardLayout) - now flexible: preferred width=0 so BoxLayout lets it expand
        JPanel inputPanel = new JPanel(new CardLayout());
        inputPanel.setPreferredSize(new Dimension(0, SEARCH_CONTROL_HEIGHT));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        inputPanel.setMinimumSize(new Dimension(0, SEARCH_CONTROL_HEIGHT));

        // mã phòng field (reuse searchTextField already configured)
        roomCodeField = searchTextField; // already has max width set in configureSearchTextField

        // tên phòng field (dùng cùng kích thước và placeholder) - make it flexible
        roomNameField = new JTextField();
        configureSearchTextField(roomNameField, SEARCH_TEXT_SIZE, "Tên phòng");
        roomNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));

        // trạng thái combo (dùng cùng kích thước) - make it flexible horizontally
        String[] statusOptions = {
                "Trống", "Chờ checkin", "Kiểm tra", "Sử dụng",
                "Checkout trễ", "Dọn dẹp", "Bảo trì"
        };
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(0, SEARCH_CONTROL_HEIGHT));
        statusComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        statusComboBox.setMinimumSize(new Dimension(0, SEARCH_CONTROL_HEIGHT));
        statusComboBox.setFont(FONT_LABEL);
        statusComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        // category combo (dynamically loaded from LoaiPhong)
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setPreferredSize(new Dimension(0, SEARCH_CONTROL_HEIGHT));
        categoryComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        categoryComboBox.setMinimumSize(new Dimension(0, SEARCH_CONTROL_HEIGHT));
        categoryComboBox.setFont(FONT_LABEL);
        categoryComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        // Add cards
        inputPanel.add(roomCodeField, "Mã phòng");
        inputPanel.add(roomNameField, "Tên phòng");
        inputPanel.add(categoryComboBox, "Loại phòng");
        inputPanel.add(statusComboBox, "Trạng thái");

        // switch card when combobox changed
        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout)(inputPanel.getLayout());
            String selected = (String) searchTypeComboBox.getSelectedItem();
            cl.show(inputPanel, selected);
        });

        // realtime filter for code (giữ logic cũ)
        roomCodeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                if (!"Mã phòng".equals((String) searchTypeComboBox.getSelectedItem())) return;
                String txt = roomCodeField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (roomCodeField.getForeground().equals(Color.GRAY) && "Tìm kiếm...".equals(txt))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    return;
                }
                final String query = txt.trim().toLowerCase();
                reloadRoomsAsync(() -> {
                    List<Phong> all = roomService.getAllQuanLyPhongPanel();
                    if (all == null) return Collections.emptyList();
                    List<Phong> filtered = new ArrayList<>();
                    for (Phong p : all) {
                        if (p.getMaPhong() != null && p.getMaPhong().toLowerCase().contains(query)) filtered.add(p);
                    }
                    return filtered;
                });
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // realtime filter for name (giữ logic cũ, dùng roomNameField)
        roomNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                if (!"Tên phòng".equals((String) searchTypeComboBox.getSelectedItem())) return;
                String txt = roomNameField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (roomNameField.getForeground().equals(Color.GRAY) && "Tên phòng".equals(txt))) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    return;
                }
                final String query = txt.trim().toLowerCase();
                reloadRoomsAsync(() -> {
                    List<Phong> all = roomService.getAllQuanLyPhongPanel();
                    if (all == null) return Collections.emptyList();
                    List<Phong> filtered = new ArrayList<>();
                    for (Phong p : all) {
                        if (p.getTenPhong() != null && p.getTenPhong().toLowerCase().contains(query)) filtered.add(p);
                    }
                    return filtered;
                });
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // category combo action: when user changes category and mode is Loại phòng -> filter
        categoryComboBox.addActionListener(e -> {
            if (!"Loại phòng".equals((String) searchTypeComboBox.getSelectedItem())) return;
            String disp = (String) categoryComboBox.getSelectedItem();
            if (disp == null || disp.isEmpty()) {
                reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                return;
            }
            String id = categoryNameToId.get(disp);
            final String selectedId = id;
            reloadRoomsAsync(() -> {
                List<Phong> all = roomService.getAllQuanLyPhongPanel();
                if (all == null) return Collections.emptyList();
                List<Phong> filtered = new ArrayList<>();
                for (Phong p : all) {
                    if (p.getMaLoaiPhong() != null && p.getMaLoaiPhong().equals(selectedId)) filtered.add(p);
                }
                return filtered;
            });
        });

        // statusComboBox action (giữ logic cũ)
        statusComboBox.addActionListener(e -> {
            if (!"Trạng thái".equals((String) searchTypeComboBox.getSelectedItem())) return;
            String status = (String) statusComboBox.getSelectedItem();
            reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
        });

        // Row 1 layout: combo + spacer + inputPanel + spacer + searchButton
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        // Let inputPanel expand to fill remaining width
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(12));
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        row1.add(searchButton);

        // Search button behavior (giữ logic cũ) + Loại phòng case
        searchButton.addActionListener(e -> {
            String mode = (String) searchTypeComboBox.getSelectedItem();
            if ("Mã phòng".equals(mode)) {
                String txt = roomCodeField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (roomCodeField.getForeground().equals(Color.GRAY) && "Tìm kiếm...".equals(txt))) {
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
                if (txt.isEmpty() || (roomNameField.getForeground().equals(Color.GRAY) && "Tên phòng".equals(txt))) {
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
            } else if ("Loại phòng".equals(mode)) {
                String disp = (String) categoryComboBox.getSelectedItem();
                if (disp == null || disp.isEmpty()) {
                    reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    return;
                }
                String id = categoryNameToId.get(disp);
                final String selectedId = id;
                reloadRoomsAsync(() -> {
                    List<Phong> all = roomService.getAllQuanLyPhongPanel();
                    if (all == null) return Collections.emptyList();
                    List<Phong> filtered = new ArrayList<>();
                    for (Phong p : all) {
                        if (p.getMaLoaiPhong() != null && p.getMaLoaiPhong().equals(selectedId)) filtered.add(p);
                    }
                    return filtered;
                });
            } else { // Trạng thái
                String status = (String) statusComboBox.getSelectedItem();
                reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
            }
        });

        searchPanel.add(row1);
        // tăng khoảng cách để hài hòa với các nút chức năng
        searchPanel.add(Box.createVerticalStrut(20));

        // Action buttons row (centered) - sizes match customer panel
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        actionRow.setBackground(CustomUI.white);
        addButton.setPreferredSize(ACTION_BUTTON_SIZE);
        editButton.setPreferredSize(ACTION_BUTTON_SIZE);
        deleteButton.setPreferredSize(ACTION_BUTTON_SIZE);
        actionRow.add(addButton);
        actionRow.add(editButton);
        actionRow.add(deleteButton);

        searchPanel.add(actionRow);
        searchPanel.add(Box.createVerticalStrut(8));

        // Kick off async load of categories (so panel is responsive)
        reloadCategories();

        return searchPanel;
    }

    // Kết hợp: chỉ thêm search panel (không còn category panel)
    private void createSearchAndCategoryPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        container.setBackground(CustomUI.white);

        JPanel leftPanel = createSearchPanel();
        container.add(leftPanel);

        add(container);
    }

    // Giờ chuyển phần danh sách phòng sang table (BỎ cột Thao tác)
    private void createRoomTablePanel() {
        String[] columnNames = {"Mã phòng", "Tên phòng", "Loại", "Số người", "Giá giờ", "Giá ngày", "Trạng thái", "OBJ"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
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

        roomTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        roomTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = roomTable.getWidth();
                TableColumnModel columnModel = roomTable.getColumnModel();
                if (columnModel.getColumnCount() < 8) return;
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.10));
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.18));
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.12));
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.08));
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.12));
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.12));
                columnModel.getColumn(6).setPreferredWidth((int) (tableWidth * 0.12));
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
    // Overload giữ tương thích
    private void reloadRoomsAsync(Supplier<List<Phong>> loader) {
        reloadRoomsAsync(loader, null);
    }

    // Mở rộng: nhận thêm callback chạy trên EDT sau khi populate (dùng để reload search panel/categories)
    private void reloadRoomsAsync(Supplier<List<Phong>> loader, Runnable postReloadOnEDT) {
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
                    if (postReloadOnEDT != null) {
                        SwingUtilities.invokeLater(postReloadOnEDT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        wk.execute();
    }

    // Hàm load lại categories (search panel) bất đồng bộ
    private void reloadCategories() {
        SwingWorker<List<LoaiPhong>, Void> wk = new SwingWorker<List<LoaiPhong>, Void>() {
            @Override
            protected List<LoaiPhong> doInBackground() {
                try {
                    return loaiPhongService.layTatCaLoaiPhongHienCo();
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<LoaiPhong> list = get();
                    categoryComboBox.removeAllItems();
                    categoryNameToId.clear();
                    if (list != null) {
                        for (LoaiPhong lp : list) {
                            String display = lp.getTenLoaiPhong() + " (" + lp.getSoLuongKhach() + ")";
                            categoryComboBox.addItem(display);
                            categoryNameToId.put(display, lp.getMaLoaiPhong());
                        }
                    }
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

    private boolean hasFutureBookings(Phong p) {
        if (p == null) return false;
        boolean check = false;
        try { check = roomService.hasFutureBookings(p); } catch (Exception ignored) {}
        return check;
    }

}

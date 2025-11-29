    package vn.iuh.gui.panel;

    import com.formdev.flatlaf.FlatClientProperties;
    import com.formdev.flatlaf.ui.FlatLineBorder;
    import vn.iuh.entity.CongViec;
    import vn.iuh.entity.LoaiPhong;
    import vn.iuh.entity.Phong;
    import vn.iuh.gui.base.CustomUI;
    import vn.iuh.gui.dialog.PhongDialog;
    import vn.iuh.gui.dialog.SuaPhongDialog;
    import vn.iuh.gui.dialog.ThemPhongDialog;
    import vn.iuh.service.RoomService;
    import vn.iuh.service.impl.RoomServiceImpl;

    import javax.imageio.ImageIO;
    import javax.swing.*;
    import javax.swing.border.Border;
    import javax.swing.event.DocumentEvent;
    import javax.swing.event.DocumentListener;
    import java.awt.*;
    import java.awt.event.MouseAdapter;
    import java.awt.event.MouseEvent;
    import java.awt.event.MouseListener;
    import java.awt.geom.RoundRectangle2D;
    import java.awt.image.BufferedImage;
    import java.io.InputStream;
    import java.lang.reflect.Field;
    import java.text.NumberFormat;
    import java.util.*;
    import java.util.List;
    import java.util.function.Consumer;
    import java.util.function.Supplier;

    public class QuanLyPhongPanel extends JPanel {

        // Các hằng số dùng chung cho kích thước, font và thông số hiển thị
        private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
        private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
        private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 55);
        private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(190, 52);

        // Fonts tái sử dụng 
        private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 15);
        private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 20);
        private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

        // Fonts cho thẻ phòng
        private static final Font FONT_ROOM_NAME  = new Font("Arial", Font.BOLD, 30);
        private static final Font FONT_ROOM_SUB   = new Font("Arial", Font.BOLD, 23);

        // Kích thước thẻ phòng
        private static final int ROOM_PANEL_WIDTH  = 1300;
        private static final int ROOM_PANEL_HEIGHT = 150;
        private static final int ROOM_PANEL_ARC    = 20; // bán kính bo góc cho FlatLaf

        // Cache cho icons/ảnh scale+rounded
        private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();
        
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

        // Container cho danh sách phòng 
        private final JPanel listRoomPanelContainer = new JPanel();

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
            createListRoomPanel(); // Panel chứa danh sách phòng (populate incremental)

            // Load danh sách tất cả các phòng
            reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());

        }

        // Tạo và cấu hình các nút/ô nhập dùng chung
        private void initButtons() {
            // Cấu hình ô tìm kiếm (placeholder, kích thước)
            configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Mã phòng");

            // Cấu hình nút tìm
            configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

            // Các nút hành động (thêm/sửa/xóa) — tạo ngay nhưng icon load async
            addButton    = createActionButtonAsync("Thêm phòng", "/icons/add.png", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
            editButton   = createActionButtonAsync("Sửa phòng", "/icons/edit.png", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
            deleteButton = createActionButtonAsync("Xóa phòng", "/icons/delete.png", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

            // Các nút category (1 người, 2 người... VIP...)
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

        // Tạo các action button
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

            // Bo góc và viền màu
            button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor:" + borderHex);

            // Load icon
            loadIconAsync(iconPath, 20, 20, icon -> {
                if (icon != null) button.setIcon(icon);
            });

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
            searchPanel.setPreferredSize(new Dimension(650, 200));
            searchPanel.setMaximumSize(new Dimension(650, 200));
            Border paddingBorder = BorderFactory.createEmptyBorder(12, 12, 12, 12);
            searchPanel.setBorder(paddingBorder);
            searchPanel.setBackground(CustomUI.white);
            searchPanel.setOpaque(true);
            // Dùng FlatLineBorder để có viền bo tròn và offset
            searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

            String[] searchOptions = {"Mã phòng", "Trạng thái"};
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

            // Dùng JTextField cho case "Mã phòng"
            JTextField roomCodeField = new JTextField();
            final String roomPlaceholder = "Mã phòng";
            configureSearchTextField(roomCodeField, new Dimension(380,45), roomPlaceholder);
            roomCodeField.setMaximumSize(new Dimension(380,45));
            roomCodeField.setMinimumSize(new Dimension(380,45));

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

            // Thêm 2 view vào CardLayout để chuyển giữa input text và combobox
            inputPanel.add(roomCodeField, "Mã phòng");
            inputPanel.add(statusComboBox, "Trạng thái");

            // Khi thay đổi dropdown thì chuyển view tương ứng trong CardLayout
            searchTypeComboBox.addActionListener(e -> {
                CardLayout cl = (CardLayout)(inputPanel.getLayout());
                String selected = (String) searchTypeComboBox.getSelectedItem();
                cl.show(inputPanel, selected);

                // Khi đổi chế độ tìm, ngay lập tức kích hoạt tìm tương ứng
                if ("Mã phòng".equals(selected)) {
                    String txt = roomCodeField.getText();
                    // Nếu đang là placeholder -> load toàn bộ
                    if (txt == null || txt.trim().isEmpty() || (roomPlaceholder.equals(txt) && roomCodeField.getForeground().equals(Color.GRAY))) {
                        reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    } else {
                        String q = txt.trim();
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
                } else { // Trạng thái
                    String status = (String) statusComboBox.getSelectedItem();
                    reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
                }
            });

            // Mỗi khi user nhập vào ô mã phòng hệ thông sẽ tự loc
            roomCodeField.getDocument().addDocumentListener(new DocumentListener() {
                private void update() {
                    // Chỉ xử lý khi đang ở chế độ "Mã phòng"
                    if (!"Mã phòng".equals((String) searchTypeComboBox.getSelectedItem())) return;

                    String txt = roomCodeField.getText();
                    if (txt == null) txt = "";
                    // Nếu hiện là placeholder (màu xám) -> load tất cả
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
                            // Tìm theo mã phòng
                            if (p.getMaPhong() != null && p.getMaPhong().toLowerCase().contains(query)) {
                                filtered.add(p);
                            }
                        }
                        return filtered;
                    });
                }
                @Override public void insertUpdate(DocumentEvent e) { update(); }
                @Override public void removeUpdate(DocumentEvent e) { update(); }
                @Override public void changedUpdate(DocumentEvent e) { update(); }
            });

            // Khi chọn trạng thái trong combobox ==> Lọctheo trạng thái
            statusComboBox.addActionListener(e -> {
                // Chỉ xử lý khi đang ở chế độ "Trạng thái"
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

            // Sự kiện của nút tìm
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
                } else {
                    String status = (String) statusComboBox.getSelectedItem();
                    reloadRoomsAsync(() -> roomService.getRoomsByStatus(status));
                }
            });

            searchPanel.add(row1);
            searchPanel.add(Box.createVerticalStrut(10));

            // Nút thêm phòng
            JPanel row2 = new JPanel();
            row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
            row2.setBackground(CustomUI.white);
            row2.setMaximumSize(new Dimension(650, ACTION_BUTTON_SIZE.height + 10));

            row2.add(Box.createHorizontalGlue());
            row2.add(addButton);
            row2.add(Box.createHorizontalGlue());

            searchPanel.add(row2);

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

        // Xử lý icon

        private static String iconCacheKey(String path, int w, int h, int arc) {
            return path + "|" + w + "x" + h + "|arc:" + arc;
        }

        // Bo góc cho con
        private static ImageIcon loadRoundedIconSync(Class<?> cls, String path, int width, int height, int arc) {
            String key = iconCacheKey(path, width, height, arc);
            synchronized (ICON_CACHE) {
                if (ICON_CACHE.containsKey(key)) return ICON_CACHE.get(key);
            }
            try (InputStream is = cls.getResourceAsStream(path)) {
                if (is == null) return null;
                BufferedImage orig = ImageIO.read(is);
                if (orig == null) return null;

                BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = scaled.createGraphics();
                try {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.drawImage(orig, 0, 0, width, height, null);
                } finally {
                    g.dispose();
                }

                BufferedImage rounded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = rounded.createGraphics();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Shape clip = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
                    g2.setClip(clip);
                    g2.drawImage(scaled, 0, 0, null);
                } finally {
                    g2.dispose();
                }

                ImageIcon ic = new ImageIcon(rounded);
                synchronized (ICON_CACHE) {
                    ICON_CACHE.put(key, ic);
                }
                return ic;
            } catch (Exception e) {
                return null;
            }
        }

        // Load các icon nhỏ
        private static void loadIconAsync(String path, int w, int h, Consumer<ImageIcon> callback) {
            String key = iconCacheKey(path, w, h, 0);
            synchronized (ICON_CACHE) {
                ImageIcon cached = ICON_CACHE.get(key);
                if (cached != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(cached));
                    return;
                }
            }

            SwingWorker<ImageIcon, Void> wk = new SwingWorker<>() {
                @Override
                protected ImageIcon doInBackground() {
                    try (InputStream is = QuanLyPhongPanel.class.getResourceAsStream(path)) {
                        if (is == null) return null;
                        BufferedImage img = ImageIO.read(is);
                        if (img == null) return null;
                        Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        ImageIcon ic = new ImageIcon(scaled);
                        synchronized (ICON_CACHE) {
                            ICON_CACHE.put(iconCacheKey(path, w, h, 0), ic);
                        }
                        return ic;
                    } catch (Exception ex) {
                        return null;
                    }
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon ic = get();
                        if (ic != null) callback.accept(ic);
                    } catch (Exception ignored) {}
                }
            };
            wk.execute();
        }

        private static void loadRoundedIconAsync(String path, int w, int h, int arc, Consumer<ImageIcon> callback) {
            String key = iconCacheKey(path, w, h, arc);
            synchronized (ICON_CACHE) {
                ImageIcon cached = ICON_CACHE.get(key);
                if (cached != null) {
                    SwingUtilities.invokeLater(() -> callback.accept(cached));
                    return;
                }
            }

            SwingWorker<ImageIcon, Void> wk = new SwingWorker<>() {
                @Override
                protected ImageIcon doInBackground() {
                    return loadRoundedIconSync(QuanLyPhongPanel.class, path, w, h, arc);
                }

                @Override
                protected void done() {
                    try {
                        ImageIcon ic = get();
                        if (ic != null) callback.accept(ic);
                    } catch (Exception ignored) {}
                }
            };
            wk.execute();
        }

        // Panel chứa thông tin phòng
        private JPanel createRoomPanel(String tenPhong, int soNguoi, String loaiPhong,
                                       String giaGio, String giaNgay, String trangThai, String anhPhong) {

            String backgroundColor;
            switch (soNguoi) {
                case 1: backgroundColor = "#1BA1E2"; break;
                case 2: backgroundColor = "#34D399"; break;
                case 3: backgroundColor = "#FB923C"; break;
                case 4: backgroundColor = "#A78BFA"; break;
                default: backgroundColor = "#647687"; break;
            }

            // Chọn ảnh theo loại phòng (VIP hoặc Thường)
            String resolvedAnh = "VIP".equalsIgnoreCase(loaiPhong) ? "/images/vip_room.jpg" : "/images/normal_room.jpg";

            JPanel roomPanel = new JPanel(new BorderLayout());
            // Đặt style FlatLaf (background và bo góc)
            roomPanel.putClientProperty(FlatClientProperties.STYLE,
                    "arc: " + ROOM_PANEL_ARC + "; background: " + backgroundColor + ";");
            roomPanel.setPreferredSize(new Dimension(ROOM_PANEL_WIDTH, ROOM_PANEL_HEIGHT));
            roomPanel.setMaximumSize(new Dimension(ROOM_PANEL_WIDTH, ROOM_PANEL_HEIGHT));

            // Image (bên trái): tạo JLabel placeholder, load ảnh async
            final int imgW = 160;
            final int imgH = ROOM_PANEL_HEIGHT - 4;
            JLabel imgLabel = new JLabel("Loading...", SwingConstants.CENTER);
            imgLabel.setPreferredSize(new Dimension(imgW, imgH));
            imgLabel.setOpaque(true);
            imgLabel.setBackground(new Color(0xdddddd));
            imgLabel.setForeground(Color.BLACK);

            // Load rounded room image async và set icon khi xong
            loadRoundedIconAsync(resolvedAnh, imgW, imgH, ROOM_PANEL_ARC, icon -> {
                SwingUtilities.invokeLater(() -> {
                    imgLabel.setText(null);
                    imgLabel.setIcon(icon);
                    imgLabel.setOpaque(false);
                    imgLabel.setBackground(null);
                });
            });

            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setOpaque(false);
            imagePanel.setPreferredSize(new Dimension(imgW, ROOM_PANEL_HEIGHT));
            imagePanel.add(imgLabel, BorderLayout.CENTER);
            roomPanel.add(imagePanel, BorderLayout.WEST);

            // Content (giữa): tên phòng, số người, loại, giá, trạng thái
            JPanel contentRow = new JPanel();
            contentRow.setLayout(new BoxLayout(contentRow, BoxLayout.X_AXIS));
            contentRow.setOpaque(false);
            contentRow.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            JPanel midPanel = new JPanel();
            midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
            midPanel.setOpaque(false);

            // Tên phòng — nếu VIP thì thêm icon ngôi sao
            JLabel lblName = createWhiteLabel(tenPhong, FONT_ROOM_NAME);
            if ("VIP".equalsIgnoreCase(loaiPhong)) {
                // set star async to avoid blocking
                loadIconAsync("/icons/star.png", 24, 24, icon -> {
                    if (icon != null) {
                        SwingUtilities.invokeLater(() -> lblName.setIcon(icon));
                    }
                });
                lblName.setIconTextGap(10);
            }

            JLabel lblPeople = createWhiteLabel("Số người: " + soNguoi, FONT_ROOM_SUB);
            JLabel lblType   = createWhiteLabel("Loại phòng: " + loaiPhong, FONT_ROOM_SUB);

            midPanel.add(lblName);
            midPanel.add(Box.createVerticalStrut(14));
            midPanel.add(lblPeople);
            midPanel.add(Box.createVerticalStrut(14));
            midPanel.add(lblType);

            // Panel giá
            JPanel pricePanel = new JPanel();
            pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
            pricePanel.setOpaque(false);
            pricePanel.add(Box.createVerticalStrut(lblName.getPreferredSize().height + 14));
            pricePanel.add(createWhiteLabel("Giá theo giờ: " + giaGio, FONT_ROOM_SUB));
            pricePanel.add(Box.createVerticalStrut(14));
            pricePanel.add(createWhiteLabel("Giá theo ngày: " + giaNgay, FONT_ROOM_SUB));

            // Panel trạng thái
            JPanel statusPanel = new JPanel();
            statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
            statusPanel.setOpaque(false);
            statusPanel.add(createWhiteLabel("Trạng thái: " + trangThai, FONT_ROOM_SUB));

            // Ghép các phần vào contentRow
            contentRow.add(midPanel);
            contentRow.add(Box.createHorizontalStrut(30));
            contentRow.add(pricePanel);
            contentRow.add(Box.createHorizontalStrut(30));
            contentRow.add(statusPanel);

            roomPanel.add(contentRow, BorderLayout.CENTER);

            // Buttons (phía phải): nút sửa và xóa
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            buttonPanel.setOpaque(false);

            Dimension btnSize = new Dimension(35, 35);

            JButton editBtn = createIconOnlyButtonAsync("/icons/edit.png", btnSize, "arc: 10; background: #FFFFFF; foreground: #FFFFFF;");
            JButton deleteBtn = createIconOnlyButtonAsync("/icons/delete.png", btnSize, "arc: 10; background: #FFFFFF; foreground: #FFFFFF;");

            buttonPanel.add(editBtn);
            buttonPanel.add(deleteBtn);

            roomPanel.add(buttonPanel, BorderLayout.EAST);

            return roomPanel;
        }

        // Tạo icon nhỏ
        private JButton createIconOnlyButtonAsync(String iconPath, Dimension size, String flatStyle) {
            JButton btn = new JButton();
            btn.setPreferredSize(size);
            btn.putClientProperty(FlatClientProperties.STYLE, flatStyle);
            btn.setFocusPainted(false);

            loadIconAsync(iconPath, 20, 20, icon -> {
                if (icon != null) btn.setIcon(icon);
            });

            return btn;
        }

        private JLabel createWhiteLabel(String text, Font font) {
            JLabel l = new JLabel(text);
            l.setFont(font);
            l.setForeground(Color.WHITE);
            l.setOpaque(false);
            return l;
        }

        // Danh sách các phòng
        private void createListRoomPanel() {
            listRoomPanelContainer.setLayout(new BoxLayout(listRoomPanelContainer, BoxLayout.Y_AXIS));
            listRoomPanelContainer.setBackground(CustomUI.white);
            listRoomPanelContainer.setOpaque(true);
            listRoomPanelContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 2, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));

            JPanel placeholder = new JPanel();
            placeholder.setPreferredSize(new Dimension(0, 40));
            placeholder.setOpaque(false);
            listRoomPanelContainer.add(placeholder);

            JScrollPane scrollPane = new JScrollPane(listRoomPanelContainer,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getViewport().setBackground(CustomUI.white);
            scrollPane.setPreferredSize(new Dimension(0, 500));
            this.add(scrollPane);


            // SwingWorker để tải và hiển thị từng phòng
            SwingWorker<Void, Phong> wk = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<Phong> phongs = roomService.getAllQuanLyPhongPanel();
                    if (phongs == null || phongs.isEmpty()) return null;

                    for (Phong p : phongs) {
                        publish(p);
                        // Dừng UI một chút để hệ thống có thời gian vẽ danh sách phòng
                        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                    }
                    return null;
                }

                @Override
                protected void process(List<Phong> chunks) {
                    for (Phong p : chunks) {
                        // Lấy thông tin loại phòng thông qua Service
                        LoaiPhong lp = null;
                        try {
                            lp = roomService.getRoomCategoryByID(p.getMaLoaiPhong());
                        } catch (Exception ignore) {}

                        int soNguoi = lp != null ? lp.getSoLuongKhach() : 0;
                        String loaiPhongTen = lp != null ? (lp.getPhanLoai() != null && !lp.getPhanLoai().isEmpty()
                                ? lp.getPhanLoai() : lp.getTenLoaiPhong()) : "Thường";

                        // Lấy giá mới nhất qua Service
                        double[] latest = {0.0, 0.0};
                        try {
                            latest = roomService.getLatestPriceForLoaiPhong(p.getMaLoaiPhong());
                        } catch (Exception ignore) {}
                        String giaGioStr = formatPrice(latest.length > 1 ? latest[1] : 0.0);
                        String giaNgayStr = formatPrice(latest.length > 0 ? latest[0] : 0.0);

                        // Trạng thái
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

                        // Chọn ảnh theo phân loại (VIP vs Thường)
                        String anh = ("VIP".equalsIgnoreCase(loaiPhongTen) || "Vip".equalsIgnoreCase(loaiPhongTen))
                                ? "/images/vip_room.jpg" : "/images/normal_room.jpg";

                        // Tạo panel phòng trên
                        JPanel roomPanel = createRoomPanel(p.getTenPhong(), soNguoi, loaiPhongTen,
                                giaGioStr, giaNgayStr, trangThai, anh);

                        attachRoomClickListener(roomPanel, p);
                        attachRoomButtons(roomPanel, p);

                        listRoomPanelContainer.add(roomPanel);
                        listRoomPanelContainer.add(Box.createVerticalStrut(12));
                    }

                    listRoomPanelContainer.add(Box.createVerticalGlue());
                    listRoomPanelContainer.revalidate();
                    listRoomPanelContainer.repaint();
                }
            };

            wk.execute();
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
            SwingWorker<List<Phong>, Void> wk = new SwingWorker<>() {
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

        // Update UI
        private void populateRoomList(List<Phong> rooms) {
            SwingUtilities.invokeLater(() -> {
                listRoomPanelContainer.removeAll();

                JPanel placeholder = new JPanel();
                placeholder.setPreferredSize(new Dimension(0, 12));
                placeholder.setOpaque(false);
                listRoomPanelContainer.add(placeholder);

                if (rooms != null && !rooms.isEmpty()) {
                    for (Phong p : rooms) {
                        LoaiPhong lp = roomService.getRoomCategoryByID(p.getMaLoaiPhong());
                        int soNguoi = lp != null ? lp.getSoLuongKhach() : 0;
                        String loaiPhongTen = lp != null ? (lp.getPhanLoai() != null && !lp.getPhanLoai().isEmpty() ? lp.getPhanLoai() : lp.getTenLoaiPhong()) : "Thường";

                        double[] latest = roomService.getLatestPriceForLoaiPhong(p.getMaLoaiPhong());
                        String giaGioStr = formatPrice(latest.length > 1 ? latest[1] : 0.0);
                        String giaNgayStr = formatPrice(latest.length > 0 ? latest[0] : 0.0);

                        CongViec cv = roomService.getCurrentJobForRoom(p.getMaPhong());
                        String trangThai = "Trống";
                        if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isEmpty()) {
                            trangThai = cv.getTenTrangThai();
                        } else if (!p.isDangHoatDong()) {
                            trangThai = "Bảo trì";
                        }

                        String anh = ("VIP".equalsIgnoreCase(loaiPhongTen) || "Vip".equalsIgnoreCase(loaiPhongTen))
                                ? "/images/vip_room.jpg" : "/images/normal_room.jpg";

                        JPanel roomPanel = createRoomPanel(p.getTenPhong(), soNguoi, loaiPhongTen, giaGioStr, giaNgayStr, trangThai, anh);

                        attachRoomClickListener(roomPanel, p);
                        attachRoomButtons(roomPanel, p);

                        listRoomPanelContainer.add(roomPanel);
                        listRoomPanelContainer.add(Box.createVerticalStrut(12));
                    }
                } else {
                    // Hiển thị placeholder khi không có phòng nào được tìm thấy
                    JLabel empty = new JLabel("Không tìm thấy phòng phù hợp.", SwingConstants.CENTER);
                    empty.setFont(FONT_LABEL);
                    empty.setForeground(Color.GRAY);
                    empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                    listRoomPanelContainer.add(Box.createVerticalStrut(20));
                    listRoomPanelContainer.add(empty);
                    listRoomPanelContainer.add(Box.createVerticalStrut(20));
                }

                listRoomPanelContainer.add(Box.createVerticalGlue());
                listRoomPanelContainer.revalidate();
                listRoomPanelContainer.repaint();
            });
        }

        private void attachRoomClickListener(JPanel roomPanel, Phong p) {
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Xử lí click chuột trái
                    if (!SwingUtilities.isLeftMouseButton(e)) return;
                    // Tránh khi click vào các nút
                    Component clicked = e.getComponent();
                    // Mở dialog thông tin phòng
                    try {
                        PhongDialog.showDialog(QuanLyPhongPanel.this, p, roomService);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            addMouseListenerRecursively(roomPanel, ma);

            roomPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void addMouseListenerRecursively(Component comp, MouseListener ml) {
            if (comp == null) return;
            if (!(comp instanceof JButton)) {
                comp.addMouseListener(ml);
            }
            if (comp instanceof Container) {
                for (Component child : ((Container) comp).getComponents()) {
                    addMouseListenerRecursively(child, ml);
                }
            }
        }

        private void attachRoomButtons(JPanel roomPanel, Phong p) {
            if (roomPanel == null || p == null) return;
            List<JButton> buttons = new ArrayList<>();
            collectButtons(roomPanel, buttons);

            if (!buttons.isEmpty()) {
                JButton editBtn = buttons.get(0);
                editBtn.setToolTipText("Sửa phòng");
                editBtn.addActionListener(ev -> {
                    try {
                        // kiểm tra có được phép sửa không
                        if (!hasCurrentJob(p)) {
                            // xác định trạng thái hiển thị cho người dùng
                            String currentStatus = "Không xác định";
                            try {
                                var cv = roomService.getCurrentJobForRoom(p.getMaPhong());
                                if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isBlank()) {
                                    currentStatus = cv.getTenTrangThai();
                                } else if (!p.isDangHoatDong()) {
                                    currentStatus = "Bảo trì";
                                } else {
                                    currentStatus = "Đang bận";
                                }
                            } catch (Exception ignore) {}

                            JOptionPane.showMessageDialog(
                                    this,
                                    "Không thể sửa phòng vì phòng hiện không ở trạng thái 'CÒN TRỐNG'.\nTrạng thái hiện tại: " + currentStatus,
                                    "Không thể sửa",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        } else if (hasFutureBookings(p)) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Không thể sửa phòng vì phòng hiện có đơn đặt phòng trong tương lai!",
                                    "Không thể sửa",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }

                        Window owner = SwingUtilities.getWindowAncestor(QuanLyPhongPanel.this);
                        // Khởi tạo dialog (modal)
                        SuaPhongDialog dialog = new SuaPhongDialog(owner, p, roomService);
                        dialog.setVisible(true);

                        // Sau khi đóng dialog, nếu user đã lưu -> reload
                        if (dialog.isSaved()) {
                            reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Không thể mở dialog sửa: " + ex.getMessage());
                    }
                });
            }

            if (buttons.size() >= 2) {
                JButton deleteBtn = buttons.get(1);
                deleteBtn.setToolTipText("Xóa phòng");
                deleteBtn.addActionListener(ev -> {
                    boolean canDelete = true;
                    String currentStatus = "Không xác định";
                    try {
                        CongViec cv = roomService.getCurrentJobForRoom(p.getMaPhong());
                        if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isBlank()) {
                            canDelete = false;
                            currentStatus = cv.getTenTrangThai();
                        } else if (!p.isDangHoatDong()) {
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
                                this,
                                "Không thể xóa phòng vì trạng thái hiện tại: " + currentStatus + ".\nChỉ có thể xóa khi phòng đang ở trạng thái 'CÒN TRỐNG'.",
                                "Không thể xóa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    } else if (hasFutureBookings(p)) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Không thể xóa phòng vì phòng hiện có đơn đặt phòng trong tương lai!",
                                "Không thể xóa",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }

                    int ans = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phòng này không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (ans != JOptionPane.YES_OPTION) return;

                    boolean ok = false;
                    try {
                        if (roomService instanceof RoomServiceImpl) {
                            ok = ((RoomServiceImpl) roomService).deleteRoomWithHistory(p.getMaPhong());
                        } else {
                            ok = roomService.deleteRoomByID(p.getMaPhong()); // fallback
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Xóa thành công.");
                        reloadRoomsAsync(() -> roomService.getAllQuanLyPhongPanel());
                    } else {
                        JOptionPane.showMessageDialog(this, "Xóa thất bại.");
                    }
                });
            }


        }

        private void collectButtons(Component comp, List<JButton> out) {
            if (comp == null) return;
            if (comp instanceof JButton) {
                out.add((JButton) comp);
            }
            if (comp instanceof Container) {
                for (Component c : ((Container) comp).getComponents()) {
                    collectButtons(c, out);
                }
            }
        }

        // Kiểm tra phòng có được phép sửa không
        private boolean hasCurrentJob(Phong p) {
            if (p == null) return false;
            try {
                var cv = roomService.getCurrentJobForRoom(p.getMaPhong());
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
            boolean check = roomService.hasFutureBookings(p);
            try {
                if (!check) {
                    // Nếu có đơn đặt phòng trong tương lai thì không cho phép xóa hay sửa
                    return false;
                }
            } catch (Exception ignored) {
            }
            return check;
        }
    }

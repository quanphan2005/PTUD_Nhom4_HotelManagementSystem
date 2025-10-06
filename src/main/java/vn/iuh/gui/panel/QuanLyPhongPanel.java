package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;


public class QuanLyPhongPanel extends JPanel {

    // Các hằng số dùng chung cho kích thước, font và thông số hiển thị
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 55);
    private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(190, 52);

    private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

    // Kích thước thẻ phòng
    private static final int ROOM_PANEL_WIDTH  = 1300;
    private static final int ROOM_PANEL_HEIGHT = 150;
    private static final int ROOM_PANEL_ARC    = 20; // bán kính bo góc cho FlatLaf

    // Các thành phần sẽ được khởi tạo/tái dùng trong nhiều hàm
    // Các thành phần trong panel tìm kiếm
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    // Các thành phần trong panel loại phòng
    private JButton onePeopleButton;
    private JButton twoPeopleButton;
    private JButton threePeopleButton;
    private JButton fourPeopleButton;
    private JButton vipButton;
    private JButton normalButton;
    private JButton allRoomButton;
    private JButton emptyRoomButton;

    // Constructor: cấu hình layout chính và gọi init
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
        createListRoomPanel(); // Panel chứa danh sách phòng
    }

    // Tạo và cấu hình các nút/ô nhập dùng chung
    private void initButtons() {
        // cấu hình ô tìm kiếm (placeholder, kích thước)
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Mã phòng");

        // cấu hình nút tìm
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // Các nút hành động (thêm/sửa/xóa) — gọi helper tạo action button
        addButton    = createActionButton("Thêm phòng", "/icons/add.png", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa phòng", "/icons/edit.png", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa phòng", "/icons/delete.png", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // Các nút category (1 người, 2 người... VIP...)
        onePeopleButton   = createCategoryButton("1 người (10)", "#1BA1E2", CATEGORY_BUTTON_SIZE);
        twoPeopleButton   = createCategoryButton("2 người (10)", "#34D399", CATEGORY_BUTTON_SIZE);
        threePeopleButton = createCategoryButton("3 người (10)", "#FB923C", CATEGORY_BUTTON_SIZE);
        fourPeopleButton  = createCategoryButton("4 người (10)", "#A78BFA", CATEGORY_BUTTON_SIZE);
        vipButton         = createCategoryButton("VIP (10)", "#E3C800", CATEGORY_BUTTON_SIZE);
        normalButton      = createCategoryButton("Thường (10)", "#647687", CATEGORY_BUTTON_SIZE);
        allRoomButton     = createCategoryButton("Toàn bộ (10)", "#3B82F6", CATEGORY_BUTTON_SIZE);
        emptyRoomButton   = createCategoryButton("Phòng trống (10)", "#059669", CATEGORY_BUTTON_SIZE);
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

    // Helper tạo nút hành động lớn (có icon + text) — dùng lại cho Thêm/Sửa/Xóa
    private JButton createActionButton(String text, String iconPath, Dimension size, String bgHex, String borderHex) {
        ImageIcon icon = loadScaledIcon(iconPath, 20, 20); // load icon và scale
        JButton button = new JButton(text, icon);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFont(FONT_ACTION);
        button.setBackground(Color.decode(bgHex));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);

        // FlatLaf style: bo góc và viền màu
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor:" + borderHex);
        return button;
    }

    // Header panel trên cùng: tiêu đề "Quản lý phòng"
    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý phòng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        add(pnlTop); // thêm vào panel chính (this)
    }

    // Panel chứa các controls tìm kiếm (dropdown, input, nút) và 3 nút hành động
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
        searchTypeComboBox.setPreferredSize(new Dimension(120, 45));
        searchTypeComboBox.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel inputPanel = new JPanel(new CardLayout());

        // Dùng JTextField cho case "Mã phòng"
        JTextField roomCodeField = new JTextField();
        configureSearchTextField(roomCodeField, new Dimension(380,45), "Mã phòng");

        // JComboBox cho case "Trạng thái"
        String[] statusOptions = {
                "Trống", "Chờ checkin", "Kiểm tra", "Sử dụng",
                "Checkout trễ", "Dọn dẹp", "Bảo trì"
        };
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(380, 45));
        statusComboBox.setFont(new Font("Arial", Font.BOLD, 15));
        statusComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        // Thêm 2 view vào CardLayout để chuyển giữa input text và combobox
        inputPanel.add(roomCodeField, "Mã phòng");
        inputPanel.add(statusComboBox, "Trạng thái");

        // Khi thay đổi dropdown thì chuyển view tương ứng trong CardLayout
        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout)(inputPanel.getLayout());
            cl.show(inputPanel, (String)searchTypeComboBox.getSelectedItem());
        });

        // Row 1: dropdown + input + nút TÌM
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(searchButton);

        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(10));

        // Row 2: Thêm + Sửa
        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.setBackground(CustomUI.white);
        row2.add(addButton);
        row2.add(Box.createHorizontalGlue());
        row2.add(Box.createHorizontalStrut(20));
        row2.add(editButton);
        searchPanel.add(row2);
        searchPanel.add(Box.createVerticalStrut(10));

        // Row 3: Xóa (căn giữa)
        JPanel row3 = new JPanel();
        row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
        row3.setBackground(CustomUI.white);
        row3.add(Box.createHorizontalGlue());
        row3.add(deleteButton);
        row3.add(Box.createHorizontalGlue());
        searchPanel.add(row3);

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

        // Row 1: Toàn bộ, Phòng trống, 1 người
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.add(allRoomButton);
        row1.add(Box.createHorizontalStrut(15));
        row1.add(emptyRoomButton);
        row1.add(Box.createHorizontalStrut(15));
        row1.add(onePeopleButton);

        categoryPanel.add(row1);
        categoryPanel.add(Box.createVerticalStrut(10));

        // Row 2: 2 người, 3 người, 4 người
        JPanel row2 = new JPanel();
        row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
        row2.setBackground(CustomUI.white);
        row2.add(twoPeopleButton);
        row2.add(Box.createHorizontalStrut(15));
        row2.add(threePeopleButton);
        row2.add(Box.createHorizontalStrut(15));
        row2.add(fourPeopleButton);

        categoryPanel.add(row2);
        categoryPanel.add(Box.createVerticalStrut(10));

        // Row 3: VIP, Thường
        JPanel row3 = new JPanel();
        row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
        row3.setBackground(CustomUI.white);
        row3.add(vipButton);
        row3.add(Box.createHorizontalStrut(15));
        row3.add(normalButton);

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

    // Hàm load và scale icon từ resource, dùng try-with-resources để đóng stream an toàn
    private ImageIcon loadScaledIcon(String path, int width, int height) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) return null; // không tìm thấy resource -> trả về null để không crash
            BufferedImage img = ImageIO.read(is);
            if (img == null) return null;

            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, width, height, null);
            g2.dispose();

            return new ImageIcon(resized);
        } catch (Exception ignore) {
            // UI sẽ hiển thị không có icon
            return null;
        }
    }

    // Tạo ImageIcon bo góc (rounded) từ Image input
    private ImageIcon createRoundedImageIcon(Image src, int width, int height, int arc) {
        if (src == null) return null;

        // Scale vào một BufferedImage trước
        BufferedImage scaledBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledBuf.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }

        // Vẽ lại ảnh vào buffer khác với clip là RoundRectangle2D để tạo bo góc
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buf.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Shape clip = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
            g2.setClip(clip);
            g2.drawImage(scaledBuf, 0, 0, null);
        } finally {
            g2.dispose();
        }

        return new ImageIcon(buf);
    }

    // Tạo một panel mô tả 1 phòng (ảnh trái, thông tin giữa, nút sửa/xóa nằm bên phải)
    private JPanel createRoomPanel(String tenPhong, int soNguoi, String loaiPhong,
                                   String giaGio, String giaNgay, String trangThai, String anhPhong) {

        String backgroundColor;

        // Set màu nền cho thẻ phòng theo số người tối đa của phòng
        switch (soNguoi) {
            case 1: backgroundColor = "#1BA1E2"; break;
            case 2: backgroundColor = "#34D399"; break;
            case 3: backgroundColor = "#FB923C"; break;
            case 4: backgroundColor = "#A78BFA"; break;
            default: backgroundColor = "#647687"; break;
        }

        // Chọn ảnh theo loại phòng (VIP hoặc Thường) — override tham số vừa nhận để đồng nhất resource
        anhPhong = "VIP".equalsIgnoreCase(loaiPhong) ? "/images/vip_room.jpg" : "/images/normal_room.jpg";

        JPanel roomPanel = new JPanel(new BorderLayout());
        // đặt style FlatLaf (background và bo góc)
        roomPanel.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + ROOM_PANEL_ARC + "; background: " + backgroundColor + ";");
        roomPanel.setPreferredSize(new Dimension(ROOM_PANEL_WIDTH, ROOM_PANEL_HEIGHT));
        roomPanel.setMaximumSize(new Dimension(ROOM_PANEL_WIDTH, ROOM_PANEL_HEIGHT));

        // Image (bên trái): tạo JLabel chứa ảnh bo góc hoặc "No Image" fallback
        final int imgW = 160;
        final int imgH = ROOM_PANEL_HEIGHT - 4;
        JLabel imgLabel = createRoomImageLabel(anhPhong, imgW, imgH, ROOM_PANEL_ARC);

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

        // Tên phòng — nếu VIP thì thêm icon ngôi sao (nếu resource có)
        JLabel lblName = createWhiteLabel(tenPhong, 30, true);
        if ("VIP".equalsIgnoreCase(loaiPhong)) {
            ImageIcon starIcon = loadScaledIcon("/icons/star.png", 24, 24);
            if (starIcon != null) lblName.setIcon(starIcon);
            lblName.setIconTextGap(10);
        }

        JLabel lblPeople = createWhiteLabel("Số người: " + soNguoi, 23, true);
        JLabel lblType   = createWhiteLabel("Loại phòng: " + loaiPhong, 23, true);

        midPanel.add(lblName);
        midPanel.add(Box.createVerticalStrut(14));
        midPanel.add(lblPeople);
        midPanel.add(Box.createVerticalStrut(14));
        midPanel.add(lblType);

        // Panel giá: sắp xếp theo cột
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setOpaque(false);
        pricePanel.add(Box.createVerticalStrut(lblName.getPreferredSize().height + 14));
        pricePanel.add(createWhiteLabel("Giá theo giờ: " + giaGio, 23, true));
        pricePanel.add(Box.createVerticalStrut(14));
        pricePanel.add(createWhiteLabel("Giá theo ngày: " + giaNgay, 23, true));

        // Panel trạng thái
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setOpaque(false);
        statusPanel.add(createWhiteLabel("Trạng thái: " + trangThai, 23, true));

        // Ghép các phần vào contentRow
        contentRow.add(midPanel);
        contentRow.add(Box.createHorizontalStrut(30));
        contentRow.add(pricePanel);
        contentRow.add(Box.createHorizontalStrut(30));
        contentRow.add(statusPanel);

        roomPanel.add(contentRow, BorderLayout.CENTER);

        // Buttons (phía phải): nút sửa và xóa dạng icon-only
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        Dimension btnSize = new Dimension(35, 35);

        JButton editBtn = createIconOnlyButton("/icons/edit.png", btnSize, "arc: 10; background: #2563EB; foreground: white;");
        JButton deleteBtn = createIconOnlyButton("/icons/delete.png", btnSize, "arc: 10; background: #DC2626; foreground: white;");

        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        roomPanel.add(buttonPanel, BorderLayout.EAST);

        return roomPanel;
    }

    // Tạo JLabel chứa ảnh phòng, nếu không tìm thấy file ảnh sẽ trả về JLabel "No Image"
    private JLabel createRoomImageLabel(String resourcePath, int width, int height, int arc) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new Exception("Image not found: " + resourcePath);
            BufferedImage orig = ImageIO.read(is);
            if (orig == null) throw new Exception("Cannot read image: " + resourcePath);

            BufferedImage scaledBuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledBuf.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(orig, 0, 0, width, height, null);
            } finally {
                g.dispose();
            }

            ImageIcon rounded = createRoundedImageIcon(scaledBuf, width, height, arc);
            JLabel imgLabel = new JLabel(rounded);
            imgLabel.setPreferredSize(new Dimension(width, height));
            imgLabel.setOpaque(false);
            return imgLabel;
        } catch (Exception e) {
            // Trường hợp không tìm được ảnh: trả về label mặc định để không làm lỗi UI
            JLabel imgLabel = new JLabel("No Image", SwingConstants.CENTER);
            imgLabel.setForeground(Color.BLACK);
            imgLabel.setPreferredSize(new Dimension(width, height));
            imgLabel.setOpaque(true);
            imgLabel.setBackground(Color.LIGHT_GRAY);
            return imgLabel;
        }
    }

    // Tạo nút chỉ chứa icon (dùng cho hàng nút sửa/xóa ở từng thẻ phòng)
    private JButton createIconOnlyButton(String iconPath, Dimension size, String flatStyle) {
        ImageIcon icon = loadScaledIcon(iconPath, 20, 20);
        JButton btn = new JButton(icon);
        btn.setPreferredSize(size);
        btn.putClientProperty(FlatClientProperties.STYLE, flatStyle);
        btn.setFocusPainted(false);
        return btn;
    }

    // Trả về JLabel với font trắng — dùng nhiều lần trong thẻ phòng
    private JLabel createWhiteLabel(String text, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(Color.WHITE);
        l.setOpaque(false);
        return l;
    }

    // Tạo danh sách các thẻ phòng và gói vào JScrollPane để cuộn được
    private void createListRoomPanel() {
        JPanel listRoomPanel = new JPanel();
        listRoomPanel.setLayout(new BoxLayout(listRoomPanel, BoxLayout.Y_AXIS));
        listRoomPanel.setBackground(CustomUI.white);
        listRoomPanel.setOpaque(true);
        listRoomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        listRoomPanel.add(createRoomPanel("T001", 1, "VIP", "50.000 VND", "300.000 VND", "Đang chờ", "/images/vip_room.jpg"));
        listRoomPanel.add(Box.createVerticalStrut(12));
        listRoomPanel.add(createRoomPanel("T002", 2, "Thường", "40.000 VND", "250.000 VND", "Đã đặt", "/images/normal_room.jpg"));
        listRoomPanel.add(Box.createVerticalStrut(12));
        listRoomPanel.add(createRoomPanel("T003", 3, "VIP", "40.000 VND", "250.000 VND", "Đã đặt", "/images/normal_room.jpg"));
        listRoomPanel.add(Box.createVerticalStrut(12));
        listRoomPanel.add(createRoomPanel("T002", 4, "Thường", "40.000 VND", "250.000 VND", "Đã đặt", "/images/normal_room.jpg"));
        listRoomPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(listRoomPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // tăng tốc cuộn
        scrollPane.getViewport().setBackground(CustomUI.white);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        this.add(scrollPane);
    }

}

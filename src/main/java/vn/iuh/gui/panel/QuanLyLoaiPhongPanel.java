package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

// Panel quản lý loại phòng
public class QuanLyLoaiPhongPanel extends JPanel {

    // Các hằng số dùng chung cho kích thước, font và thông số hiển thị
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 55);
    private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(190, 52);

    // Fonts tái sử dụng (tránh tạo nhiều lần)
    private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

    private static final Font FONT_MA         = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_NAME       = new Font("Arial", Font.BOLD, 22);
    private static final Font FONT_PEOPLE     = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_PHANLOAI   = new Font("Arial", Font.BOLD, 18);

    // Kích thước thẻ loại phòng
    private static final int CATEGORY_CARD_WIDTH  = 1300;
    private static final int CATEGORY_CARD_HEIGHT = 150;
    private static final int CATEGORY_CARD_ARC    = 20; // bán kính bo góc cho FlatLaf

    // Simple in-memory cache cho icons/ảnh đã scale+rounded
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    // Các thành phần sẽ được khởi tạo/tái dùng trong nhiều hàm
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    // Các thành phần trong panel loại phòng (nút lọc)
    private JButton onePeopleButton;
    private JButton twoPeopleButton;
    private JButton threePeopleButton;
    private JButton fourPeopleButton;
    private JButton vipButton;
    private JButton normalButton;
    private JButton allCategoryButton;

    // Container cho danh sách thẻ (giữ tham chiếu để populate dần)
    private final JPanel listPanelContainer = new JPanel();

    public QuanLyLoaiPhongPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
    }

    private void init() {
        initButtons();
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndCategoryPanel();
        add(Box.createVerticalStrut(10));
        createListCategoryPanel(); // sẽ populate dần bên trong
    }

    private void initButtons() {
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Mã loại phòng");
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        addButton    = createActionButtonAsync("Thêm loại phòng", "/icons/add.png", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButtonAsync("Sửa loại phòng", "/icons/edit.png", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButtonAsync("Xóa loại phòng", "/icons/delete.png", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        onePeopleButton   = createCategoryButton("1 người (10)", "#1BA1E2", CATEGORY_BUTTON_SIZE);
        twoPeopleButton   = createCategoryButton("2 người (10)", "#34D399", CATEGORY_BUTTON_SIZE);
        threePeopleButton = createCategoryButton("3 người (10)", "#FB923C", CATEGORY_BUTTON_SIZE);
        fourPeopleButton  = createCategoryButton("4 người (10)", "#A78BFA", CATEGORY_BUTTON_SIZE);
        vipButton         = createCategoryButton("VIP (10)", "#E3C800", CATEGORY_BUTTON_SIZE);
        normalButton      = createCategoryButton("Thường (10)", "#647687", CATEGORY_BUTTON_SIZE);
        allCategoryButton = createCategoryButton("Toàn bộ (10)", "#3B82F6", CATEGORY_BUTTON_SIZE);
    }

    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMaximumSize(size);
        field.setMinimumSize(size);
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

    // Async action button (icon loaded async)
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
        loadIconAsync(iconPath, 20, 20, icon -> {
            if (icon != null) button.setIcon(icon);
        });
        return button;
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý loại phòng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont : FONT_NAME);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        add(pnlTop);
    }

    // TẠO KHUNG SEARCH (giữ nguyên bố cục) - đã chỉnh để tránh kéo dãn các component
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setPreferredSize(new Dimension(650, 200));
        searchPanel.setMaximumSize(new Dimension(650, 200));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setOpaque(true);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        String[] searchOptions = {"Mã loại phòng", "Trạng thái"};
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

        JTextField categoryCodeField = new JTextField();
        final String codePlaceholder = "Mã loại phòng";
        configureSearchTextField(categoryCodeField, new Dimension(380,45), codePlaceholder);
        categoryCodeField.setMaximumSize(new Dimension(380,45));
        categoryCodeField.setMinimumSize(new Dimension(380,45));

        String[] statusOptions = {"Thường", "VIP", "Tất cả"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(380, 45));
        statusComboBox.setMaximumSize(new Dimension(380, 45));
        statusComboBox.setMinimumSize(new Dimension(380, 45));
        statusComboBox.setFont(new Font("Arial", Font.BOLD, 15));
        statusComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        inputPanel.add(categoryCodeField, "Mã loại phòng");
        inputPanel.add(statusComboBox, "Trạng thái");

        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout)(inputPanel.getLayout());
            String selected = (String) searchTypeComboBox.getSelectedItem();
            cl.show(inputPanel, selected);
        });

        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setMaximumSize(new Dimension(650, 60));
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(searchButton);

        // nút TÌM behaviour (tương tự như trước)
        searchButton.addActionListener(ev -> {
            String mode = (String) searchTypeComboBox.getSelectedItem();
            if ("Mã loại phòng".equals(mode)) {
                String txt = categoryCodeField.getText();
                if (txt == null) txt = "";
                if (txt.isEmpty() || (codePlaceholder.equals(txt) && categoryCodeField.getForeground().equals(Color.GRAY))) {
                    // reload all (placeholder - hiện chưa có service ở panel loại phòng mẫu)
                    // giữ nguyên hành vi: không làm gì đặc biệt
                } else {
                    // thực hiện tìm trong UI nếu cần (user sẽ tích hợp service)
                }
            } else {
                String status = (String) statusComboBox.getSelectedItem();
                // reload theo trạng thái
            }
        });

        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(10));

        // Row2: chỉ giữ nút Thêm, căn giữa
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

    // CREATE CATEGORY PANEL (phần nút bộ lọc bên phải) — đã chỉnh: mỗi hàng 3 nút, trái->phải
    private JPanel createCategoryPanel() {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(CustomUI.white);
        categoryPanel.setPreferredSize(new Dimension(655, 200));
        categoryPanel.setMaximumSize(new Dimension(655, 200));
        categoryPanel.setOpaque(true);
        categoryPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        // Row 1: Toàn bộ, 1 người, 2 người (left-aligned)
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(allCategoryButton);
        row1.add(onePeopleButton);
        row1.add(twoPeopleButton);
        categoryPanel.add(row1);
        categoryPanel.add(Box.createVerticalStrut(10));

        // Row 2: 3 người, 4 người, VIP (left-aligned)
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row2.setBackground(CustomUI.white);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.add(threePeopleButton);
        row2.add(fourPeopleButton);
        row2.add(vipButton);
        categoryPanel.add(row2);
        categoryPanel.add(Box.createVerticalStrut(10));

        // Row 3: Thường + 2 placeholder (left-aligned to keep 3-col layout)
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row3.setBackground(CustomUI.white);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(normalButton);
        // placeholders giữ kích thước giống nút
        row3.add(Box.createRigidArea(new Dimension(CATEGORY_BUTTON_SIZE.width, CATEGORY_BUTTON_SIZE.height)));
        row3.add(Box.createRigidArea(new Dimension(CATEGORY_BUTTON_SIZE.width, CATEGORY_BUTTON_SIZE.height)));
        categoryPanel.add(row3);

        return categoryPanel;
    }

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

    // ------------- IMAGE UTILITIES (cache + async load) ----------------

    private static String iconCacheKey(String path, int w, int h, int arc) {
        return path + "|" + w + "x" + h + "|arc:" + arc;
    }

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
                try (InputStream is = QuanLyLoaiPhongPanel.class.getResourceAsStream(path)) {
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
                return loadRoundedIconSync(QuanLyLoaiPhongPanel.class, path, w, h, arc);
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

    // ------------- CARD CREATION (giữ nguyên bố cục) ----------------

    private JPanel createCategoryCard(String maLoai, String tenLoaiPhong, int soNguoi, String phanLoai, String imageResource) {
        String backgroundColor;
        switch (soNguoi) {
            case 1: backgroundColor = "#1BA1E2"; break;
            case 2: backgroundColor = "#34D399"; break;
            case 3: backgroundColor = "#FB923C"; break;
            case 4: backgroundColor = "#A78BFA"; break;
            default: backgroundColor = "#647687"; break;
        }

        String anhPhong = (imageResource == null || imageResource.isEmpty()) ? "/images/room_category.jpg" : imageResource;

        JPanel card = new JPanel(new BorderLayout());
        card.putClientProperty(FlatClientProperties.STYLE,
                "arc: " + CATEGORY_CARD_ARC + "; background: " + backgroundColor + ";");
        card.setPreferredSize(new Dimension(CATEGORY_CARD_WIDTH, CATEGORY_CARD_HEIGHT));
        card.setMaximumSize(new Dimension(CATEGORY_CARD_WIDTH, CATEGORY_CARD_HEIGHT));

        final int imgW = 160;
        final int imgH = CATEGORY_CARD_HEIGHT - 4;
        JLabel imgLabel = new JLabel("Loading...", SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(imgW, imgH));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(new Color(0xdddddd));
        imgLabel.setForeground(Color.BLACK);

        // async load rounded image
        loadRoundedIconAsync(anhPhong, imgW, imgH, CATEGORY_CARD_ARC, icon -> {
            SwingUtilities.invokeLater(() -> {
                imgLabel.setText(null);
                imgLabel.setIcon(icon);
                imgLabel.setOpaque(false);
                imgLabel.setBackground(null);
            });
        });

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(imgW, CATEGORY_CARD_HEIGHT));
        imagePanel.add(imgLabel, BorderLayout.CENTER);
        card.add(imagePanel, BorderLayout.WEST);

        JPanel contentRow = new JPanel();
        contentRow.setLayout(new BoxLayout(contentRow, BoxLayout.X_AXIS));
        contentRow.setOpaque(false);
        contentRow.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblMa = new JLabel(maLoai);
        lblMa.setFont(FONT_MA);
        lblMa.setForeground(Color.WHITE);
        lblMa.setOpaque(false);

        JLabel lblName = new JLabel(tenLoaiPhong);
        lblName.setFont(FONT_NAME);
        lblName.setForeground(Color.WHITE);
        lblName.setOpaque(false);

        JLabel lblPeople = new JLabel("Số người tối đa: " + soNguoi);
        lblPeople.setFont(FONT_PEOPLE);
        lblPeople.setForeground(Color.WHITE);
        lblPeople.setOpaque(false);

        JLabel lblCategory = new JLabel("Phân loại: " + phanLoai);
        lblCategory.setFont(FONT_PHANLOAI);
        lblCategory.setForeground(Color.WHITE);
        lblCategory.setOpaque(false);

        infoPanel.add(lblMa);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblPeople);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblCategory);

        contentRow.add(infoPanel);
        contentRow.add(Box.createHorizontalGlue());
        card.add(contentRow, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);

        Dimension btnSize = new Dimension(35, 35);
        JButton editBtn = createIconOnlyButtonAsync("/icons/edit.png", btnSize, "arc: 10; background: #FFFFFF; foreground: #FFFFFF;");
        JButton deleteBtn = createIconOnlyButtonAsync("/icons/delete.png", btnSize, "arc: 10; background: #FFFFFF; foreground: #FFFFFF;");

        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        card.add(buttonPanel, BorderLayout.EAST);

        return card;
    }

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

    // ------------- LIST POPULATION (incremental, không block EDT lâu) -------------

    private void createListCategoryPanel() {
        listPanelContainer.setLayout(new BoxLayout(listPanelContainer, BoxLayout.Y_AXIS));
        listPanelContainer.setBackground(CustomUI.white);
        listPanelContainer.setOpaque(true);
        listPanelContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Add a light placeholder so the scroll area appears immediately
        JPanel placeholder = new JPanel();
        placeholder.setPreferredSize(new Dimension(0, 40));
        placeholder.setOpaque(false);
        listPanelContainer.add(placeholder);

        JScrollPane scrollPane = new JScrollPane(listPanelContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(CustomUI.white);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        this.add(scrollPane);

        // Prepare data items (normally you'd fetch from DB)
        List<CategoryData> dataset = new ArrayList<>();
        dataset.add(new CategoryData("LP00000001", "Phòng thường 1 giường đơn", 1, "Thường", "/images/room_category.jpg"));
        dataset.add(new CategoryData("LP00000002", "Phòng thường 1 giường đôi", 2, "Thường", "/images/room_category.jpg"));
        dataset.add(new CategoryData("LP00000003", "Phòng thường 2 giường đôi", 4, "Thường", "/images/room_category.jpg"));
        dataset.add(new CategoryData("LP00000004", "Phòng vip 1 giường đơn", 1, "Vip", "/images/room_category.jpg"));
        dataset.add(new CategoryData("LP00000005", "Phòng vip 1 giường đôi", 2, "Vip", "/images/room_category.jpg"));
        dataset.add(new CategoryData("LP00000006", "Phòng vip 2 giường đôi", 4, "Vip", "/images/room_category.jpg"));

        // Use SwingWorker to publish items incrementally (keeps EDT responsive)
        SwingWorker<Void, CategoryData> wk = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // publish items one by one with tiny yield so EDT can render
                for (CategoryData d : dataset) {
                    publish(d);
                    // small pause to allow UI painting (adjustable or remove if not desired)
                    try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                }
                return null;
            }

            @Override
            protected void process(List<CategoryData> chunks) {
                // runs on EDT: create components and add them
                for (CategoryData d : chunks) {
                    JPanel card = createCategoryCard(d.code, d.name, d.people, d.type, d.image);
                    listPanelContainer.add(card);
                    listPanelContainer.add(Box.createVerticalStrut(12));
                }
                listPanelContainer.add(Box.createVerticalGlue()); // keep glue at end
                listPanelContainer.revalidate();
                listPanelContainer.repaint();
            }
        };
        wk.execute();
    }

    // small data holder
    private static class CategoryData {
        final String code, name, image, type;
        final int people;
        CategoryData(String code, String name, int people, String type, String image) {
            this.code = code; this.name = name; this.people = people; this.type = type; this.image = image;
        }
    }
}

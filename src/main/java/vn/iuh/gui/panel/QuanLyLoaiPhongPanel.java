package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.SuaLoaiPhongDialog;
import vn.iuh.gui.dialog.ThemLoaiPhongDialog;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.NoiThatService;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.service.impl.NoiThatServiceImpl;
import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.NoiThat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    // ... (giữ nguyên tất cả hằng số, fonts, ICON_CACHE, các trường UI như trước)

    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 55);
    private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(190, 52);

    private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

    private static final Font FONT_MA         = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_NAME       = new Font("Arial", Font.BOLD, 22);
    private static final Font FONT_PEOPLE     = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_PHANLOAI   = new Font("Arial", Font.BOLD, 18);

    private static final int CATEGORY_CARD_WIDTH  = 1300;
    private static final int CATEGORY_CARD_HEIGHT = 150;
    private static final int CATEGORY_CARD_ARC    = 20;

    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    // UI components that we need to access from multiple methods -> make them fields
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    private JButton onePeopleButton;
    private JButton twoPeopleButton;
    private JButton threePeopleButton;
    private JButton fourPeopleButton;
    private JButton vipButton;
    private JButton normalButton;
    private JButton allCategoryButton;

    private final JPanel listPanelContainer = new JPanel();

    // ---- New: services used by panel ----
    private final LoaiPhongService loaiPhongService;
    private final NoiThatService noiThatService;

    // ---- New: search & filter controls as fields so listeners can access them ----
    private JComboBox<String> searchTypeComboBox;
    private JTextField categoryCodeField;
    private JComboBox<String> statusComboBox;
    private static final String CODE_PLACEHOLDER = "Mã loại phòng";

    // ---- New: caching and active filters ----
    private List<CategoryData> fullDataset = new ArrayList<>();
    private Integer activePeopleFilter = null; // 1,2,3,4 or null
    private String activeTypeFilter = null; // "Thường" or "VIP" or null
    private JButton activeCategoryButton = null;

    public QuanLyLoaiPhongPanel() {
        // khởi tạo services (implements cần có trong project)
        LoaiPhongService lps = null;
        NoiThatService nts = null;
        try {
            lps = new LoaiPhongServiceImpl();
        } catch (Throwable t) {
            // fall back null -> panel vẫn hoạt động với dataset tĩnh
            lps = null;
        }
        try {
            nts = new NoiThatServiceImpl();
        } catch (Throwable t) {
            nts = null;
        }
        this.loaiPhongService = lps;
        this.noiThatService = nts;

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
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, CODE_PLACEHOLDER);
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        addButton    = createActionButtonAsync("Thêm loại phòng", "/icons/add.png", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButtonAsync("Sửa loại phòng", "/icons/edit.png", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButtonAsync("Xóa loại phòng", "/icons/delete.png", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // gắn sự kiện cho nút Thêm: mở dialog ThemLoaiPhongDialog
        addButton.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            // dialog constructor: ThemLoaiPhongDialog(Frame owner, LoaiPhongService, NoiThatService)
            try {
                ThemLoaiPhongDialog dlg = new ThemLoaiPhongDialog(owner, loaiPhongService, noiThatService);
                dlg.setVisible(true);
            } catch (Throwable ex) {
                // nếu service null hoặc lỗi, vẫn mở dialog (constructor có thể xử lý null service)
                try {
                    ThemLoaiPhongDialog dlg = new ThemLoaiPhongDialog(owner, loaiPhongService, noiThatService);
                    dlg.setVisible(true);
                } catch (Throwable ignore) {}
            } finally {
                // reload danh sách sau khi dialog đóng (ngay cả khi lỗi)
                reloadListFromService();
            }
        });

        onePeopleButton   = createCategoryButton("1 người (10)", "#1BA1E2", CATEGORY_BUTTON_SIZE);
        twoPeopleButton   = createCategoryButton("2 người (10)", "#34D399", CATEGORY_BUTTON_SIZE);
        threePeopleButton = createCategoryButton("3 người (10)", "#FB923C", CATEGORY_BUTTON_SIZE);
        fourPeopleButton  = createCategoryButton("4 người (10)", "#A78BFA", CATEGORY_BUTTON_SIZE);
        vipButton         = createCategoryButton("VIP (10)", "#E3C800", CATEGORY_BUTTON_SIZE);
        normalButton      = createCategoryButton("Thường (10)", "#647687", CATEGORY_BUTTON_SIZE);
        allCategoryButton = createCategoryButton("Toàn bộ (10)", "#3B82F6", CATEGORY_BUTTON_SIZE);

        // Attach behavior for category buttons: set filters and apply
        onePeopleButton.addActionListener(e -> setActiveCategoryFilter(1, null, onePeopleButton));
        twoPeopleButton.addActionListener(e -> setActiveCategoryFilter(2, null, twoPeopleButton));
        threePeopleButton.addActionListener(e -> setActiveCategoryFilter(3, null, threePeopleButton));
        fourPeopleButton.addActionListener(e -> setActiveCategoryFilter(4, null, fourPeopleButton));
        vipButton.addActionListener(e -> setActiveCategoryFilter(null, "Vip", vipButton));
        normalButton.addActionListener(e -> setActiveCategoryFilter(null, "Thường", normalButton));
        allCategoryButton.addActionListener(e -> setActiveCategoryFilter(null, null, allCategoryButton));
    }

    private void setActiveCategoryFilter(Integer people, String type, JButton btn) {
        this.activePeopleFilter = people;
        this.activeTypeFilter = type;
        // update button visuals (simple approach: border highlight)
        if (activeCategoryButton != null) {
            activeCategoryButton.setBorder(null);
        }
        activeCategoryButton = btn;
        if (activeCategoryButton != null) {
            activeCategoryButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        }
        applyFilters();
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

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setPreferredSize(new Dimension(650, 200));
        searchPanel.setMaximumSize(new Dimension(650, 200));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setOpaque(true);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        String[] searchOptions = {"Mã loại phòng", "Trạng thái"};
        searchTypeComboBox = new JComboBox<>(searchOptions);

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

        // make categoryCodeField a field
        categoryCodeField = new JTextField();
        configureSearchTextField(categoryCodeField, new Dimension(380,45), CODE_PLACEHOLDER);
        categoryCodeField.setMaximumSize(new Dimension(380,45));
        categoryCodeField.setMinimumSize(new Dimension(380,45));

        String[] statusOptions = {"Thường", "VIP", "Tất cả"};
        statusComboBox = new JComboBox<>(statusOptions);
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
            // no-op: dynamic filtering handles searches in real-time
        });

        // Dynamic filtering: when user types in mã loại phòng -> filter immediately
        categoryCodeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Dynamic filtering: when user changes status selection -> filter immediately
        statusComboBox.addActionListener(ev -> {
            String sel = (String) statusComboBox.getSelectedItem();
            if (sel == null || "Tất cả".equals(sel)) {
                // do not override activeTypeFilter unless user explicitly chooses the status box
                activeTypeFilter = null;
            } else {
                // map displayed values to internal representation matches DB sample ("Thường" / "Vip")
                if (sel.equalsIgnoreCase("VIP")) activeTypeFilter = "Vip";
                else activeTypeFilter = sel;
            }
            applyFilters();
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

    // ------------- IMAGE UTILITIES (giữ nguyên) ----------------
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

    // ------------- CARD CREATION (thay đổi: gắn action cho editBtn) ----------------

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

        // ---- gắn sự kiện cho nút SỬA: mở SuaLoaiPhongDialog với dữ liệu từ service ----
        editBtn.addActionListener(evt -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            LoaiPhong lp = null;
            try {
                if (loaiPhongService != null) {
                    lp = loaiPhongService.getRoomCategoryByIDV2(maLoai);
                }
            } catch (Exception ex) {
                lp = null;
            }

            if (lp == null) {

                String fallbackName = tenLoaiPhong != null ? tenLoaiPhong : "";
                LoaiPhong tmp = new LoaiPhong();
                tmp.setMaLoaiPhong(maLoai);
                tmp.setTenLoaiPhong(fallbackName);
                // thông báo cho user
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin đầy đủ cho loại phòng. Vẫn mở form với dữ liệu sẵn có.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                lp = tmp;
            }

            List<NoiThat> furniture = new ArrayList<>();
            try {
                if (noiThatService != null) {
                    furniture = noiThatService.getNoiThatByLoaiPhong(maLoai);
                }
            } catch (Exception ignored) { furniture = new ArrayList<>(); }

            try {
                SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, lp, furniture);
                dlg.setVisible(true);
            } catch (Throwable ex) {
                // fallback attempt
                try {
                    SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, lp, furniture);
                    dlg.setVisible(true);
                } catch (Throwable ignore) {}
            } finally {
                reloadListFromService();
            }
        });


        // ---- gắn sự kiện cho nút XÓA (nếu bạn muốn xử lý xóa) ----
        deleteBtn.addActionListener(evt -> {
            if (loaiPhongService == null) {
                JOptionPane.showMessageDialog(this, "Không có service để xóa", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa loại phòng " + maLoai + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // Lấy mã phiên/nhân viên hiện tại. Thay bằng cách lấy session thực tế nếu có.
            String maPhien = System.getProperty("user.name");
            if (maPhien == null) maPhien = "UNKNOWN";

            try {
                // gọi trực tiếp impl (nếu interface LoaiPhongService không khai báo phương thức này)
                boolean deleted = ((vn.iuh.service.impl.LoaiPhongServiceImpl) loaiPhongService)
                        .deleteRoomCategoryWithAudit(maLoai, maPhien);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Xóa loại phòng thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa loại phòng", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (vn.iuh.exception.BusinessException be) {
                JOptionPane.showMessageDialog(this, be.getMessage(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa loại phòng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                reloadListFromService();
            }
        });

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

    // ------------- LIST POPULATION (load from DB via service; no sample data) -------------
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

        // load items (use service)
        reloadListFromService();
    }

    /**
     * Reload danh sách loại phòng bằng cách gọi service (bắt lỗi và không sử dụng dữ liệu mẫu).
     * Nếu service == null -> hiển thị thông báo và để list rỗng.
     */
    private void reloadListFromService() {
        // clear current items
        listPanelContainer.removeAll();

        // show temporary "loading" row
        JLabel loading = new JLabel("Đang tải danh sách loại phòng...", SwingConstants.CENTER);
        loading.setPreferredSize(new Dimension(0, 40));
        loading.setForeground(Color.GRAY);
        listPanelContainer.add(loading);
        listPanelContainer.revalidate();
        listPanelContainer.repaint();

        SwingWorker<List<CategoryData>, Void> wk = new SwingWorker<>() {
            private Exception error = null;

            @Override
            protected List<CategoryData> doInBackground() {
                List<CategoryData> dataset = new ArrayList<>();
                if (loaiPhongService == null) {
                    throw new IllegalStateException("LoaiPhongService chưa được khởi tạo.");
                }

                try {
                    List<RoomCategoryResponse> list = loaiPhongService.getAllRoomCategories();
                    if (list == null) return dataset; // return empty list if null
                    for (RoomCategoryResponse r : list) {
                        // Map fields from RoomCategoryResponse to CategoryData
                        String code = r.getMaLoaiPhong();
                        String name = r.getTenLoaiPhong();
                        int people = r.getSoLuongKhach();
                        String type = r.getPhanLoai();
                        // image: keep default or allow RoomCategoryResponse to carry image path in future
                        String image = "/images/room_category.jpg";
                        dataset.add(new CategoryData(code, name, people, type, image));
                    }
                    return dataset;
                } catch (Exception ex) {
                    error = ex;
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                listPanelContainer.removeAll();
                try {
                    if (error != null) {
                        // show non-blocking error label and log stacktrace to console
                        JLabel lbl = new JLabel("Lỗi khi tải danh sách loại phòng: " + error.getMessage(), SwingConstants.CENTER);
                        lbl.setForeground(Color.RED);
                        lbl.setPreferredSize(new Dimension(0, 40));
                        listPanelContainer.add(lbl);
                        error.printStackTrace();
                    } else {
                        List<CategoryData> dataset = get();
                        // cache full dataset for filtering
                        fullDataset = dataset == null ? new ArrayList<>() : dataset;
                        // render using current filters
                        renderList(fullDataset);
                    }
                } catch (Exception ex) {
                    JLabel lbl = new JLabel("Lỗi khi hiển thị danh sách loại phòng", SwingConstants.CENTER);
                    lbl.setForeground(Color.RED);
                    lbl.setPreferredSize(new Dimension(0, 40));
                    listPanelContainer.add(lbl);
                    ex.printStackTrace();
                } finally {
                    listPanelContainer.revalidate();
                    listPanelContainer.repaint();
                }
            }
        };

        wk.execute();
    }

    // New: apply filters to cached fullDataset and render
    private void applyFilters() {
        if (fullDataset == null) return;

        String txt = categoryCodeField.getText();
        boolean isPlaceholder = categoryCodeField.getForeground().equals(Color.GRAY) && CODE_PLACEHOLDER.equals(txt);
        String codeFilter = (!isPlaceholder && txt != null && !txt.isBlank()) ? txt.trim().toLowerCase() : null;

        List<CategoryData> filtered = new ArrayList<>();
        for (CategoryData d : fullDataset) {
            boolean ok = true;
            if (codeFilter != null) {
                // filter by mã loại phòng contains
                if (d.code == null || !d.code.toLowerCase().contains(codeFilter)) {
                    ok = false;
                }
            }
            if (ok && activeTypeFilter != null) {
                if (d.type == null || !d.type.equalsIgnoreCase(activeTypeFilter)) ok = false;
            }
            if (ok && activePeopleFilter != null) {
                if (d.people != activePeopleFilter) ok = false;
            }
            if (ok) filtered.add(d);
        }

        renderList(filtered);
    }

    private void renderList(List<CategoryData> dataset) {
        listPanelContainer.removeAll();
        if (dataset == null || dataset.isEmpty()) {
            JLabel empty = new JLabel("Không có loại phòng nào phù hợp.", SwingConstants.CENTER);
            empty.setPreferredSize(new Dimension(0, 40));
            empty.setForeground(Color.GRAY);
            listPanelContainer.add(empty);
            listPanelContainer.revalidate();
            listPanelContainer.repaint();
            return;
        }

        for (CategoryData d : dataset) {
            JPanel card = createCategoryCard(d.code, d.name, d.people, d.type, d.image);
            listPanelContainer.add(card);
            listPanelContainer.add(Box.createVerticalStrut(12));
        }
        listPanelContainer.add(Box.createVerticalGlue());
        listPanelContainer.revalidate();
        listPanelContainer.repaint();
    }

    // small data holder (giữ nguyên)
    private static class CategoryData {
        final String code, name, image, type;
        final int people;
        CategoryData(String code, String name, int people, String type, String image) {
            this.code = code; this.name = name; this.people = people; this.type = type; this.image = image;
        }
    }
}

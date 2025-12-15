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
import vn.iuh.util.DatabaseUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class QuanLyLoaiPhongPanel extends JPanel {

    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    // *** Thay đổi kích thước nút: thu nhỏ để vừa vặn hơn trong search panel
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(220, 46);
    private static final Dimension CATEGORY_BUTTON_SIZE = new Dimension(190, 52);

    private static final Font FONT_LABEL      = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION     = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_CATEGORY   = new Font("Arial", Font.BOLD, 18);

    private static final Font FONT_MA         = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_NAME       = new Font("Arial", Font.BOLD, 22);
    private static final Font FONT_PEOPLE     = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_PHANLOAI   = new Font("Arial", Font.BOLD, 18);

    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    private JButton onePeopleButton;
    private JButton twoPeopleButton;
    // removed threePeopleButton as requested
    private JButton fourPeopleButton;
    private JButton vipButton;
    private JButton normalButton;
    private JButton allCategoryButton;

    // Table components to replace card list
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    // services
    private LoaiPhongService loaiPhongService;
    private NoiThatService noiThatService;
    // NEW: flags for lazy init
    private volatile boolean initStarted = false;
    private volatile boolean servicesReady = false;

    private JComboBox<String> searchTypeComboBox;
    private JTextField categoryCodeField;
    private JTextField categoryNameField; // <-- mới: ô tìm theo tên
    private JComboBox<String> statusComboBox;
    private static final String CODE_PLACEHOLDER = "Mã loại phòng";
    private static final String NAME_PLACEHOLDER = "Tên loại phòng";

    private List<CategoryData> fullDataset = new ArrayList<>();
    private Integer activePeopleFilter = null;
    private String activeTypeFilter = null;
    private JButton activeCategoryButton = null;

    public QuanLyLoaiPhongPanel() {
        // không khởi tạo service ngay tại startup để giảm blocking UI
        this.loaiPhongService = null;
        this.noiThatService = null;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();

        // lazy init: khi panel được thêm vào hierarchy và hiển thị lần đầu, tạo services & load data ở background
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                // kiểm tra panel đã hiển thị (showing) và chưa init lần nào
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        ensureLazyInit(); // gọi lazy init
                    }
                }
            }
        });
    }


    private void init() {
        initButtons();
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndCategoryPanel();
        add(Box.createVerticalStrut(10));
        // create table-based list (replaces previous card list)
        createListCategoryPanel();
    }

    private void initButtons() {
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, CODE_PLACEHOLDER);
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // tạo action button KHÔNG tải icon - giờ cả 3 nút có cùng kích thước ACTION_BUTTON_SIZE
        addButton    = createActionButton("Thêm loại phòng", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa loại phòng", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa loại phòng", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // Thêm action cho Thêm
        addButton.addActionListener(e -> {
            // ensure init started and then wait for servicesReady (up to timeout)
            ensureLazyInit();

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    int waited = 0;
                    while (!servicesReady && waited < 5000) {
                        Thread.sleep(100);
                        waited += 100;
                    }
                    return null;
                }
                @Override protected void done() {
                    Frame owner = (Frame) SwingUtilities.getWindowAncestor(QuanLyLoaiPhongPanel.this);
                    try {
                        ThemLoaiPhongDialog dlg = new ThemLoaiPhongDialog(owner, loaiPhongService, noiThatService);
                        dlg.setLocationRelativeTo(owner);
                        dlg.setVisible(true);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        try {
                            // fallback: thử lại 1 lần nữa
                            Frame owner2 = (Frame) SwingUtilities.getWindowAncestor(QuanLyLoaiPhongPanel.this);
                            ThemLoaiPhongDialog dlg = new ThemLoaiPhongDialog(owner2, loaiPhongService, noiThatService);
                            dlg.setLocationRelativeTo(owner2);
                            dlg.setVisible(true);
                        } catch (Throwable ignore) { ignore.printStackTrace(); }
                    } finally {
                        // luôn reload sau khi dialog đóng
                        // Nếu services chưa sẵn sàng thì reloadListFromService() sẽ làm gì? nó trả về empty,
                        // nhưng vì ta chờ servicesReady ở trên, normal case servicesReady==true.
                        reloadListFromService();
                    }
                }
            }.execute();
        });

        editButton.addActionListener(e -> {
            final CategoryData sel = getSelectedCategoryData();
            if (sel == null) {
                JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Vui lòng chọn 1 loại phòng để sửa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ensure services ready
            ensureLazyInit();

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    int waited = 0;
                    while (!servicesReady && waited < 5000) { Thread.sleep(100); waited += 100; }
                    return null;
                }
                @Override protected void done() {
                    // mở form sửa trên EDT
                    SwingUtilities.invokeLater(() -> {
                        try {
                            openCategoryDetail(sel);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                            // fallback: try again
                            try { openCategoryDetail(sel); } catch (Throwable ignore) { ignore.printStackTrace(); }
                        } finally {
                            // reload sau khi dialog đóng
                            reloadListFromService();
                        }
                    });
                }
            }.execute();
        });

        // Xóa: nếu không chọn hàng -> thông báo; nếu có -> thực hiện xóa như trước
        deleteButton.addActionListener(e -> {
            CategoryData sel = getSelectedCategoryData();
            if (sel == null) {
                JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Vui lòng chọn 1 loại phòng để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (loaiPhongService == null) {
                JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Không có service để xóa", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(QuanLyLoaiPhongPanel.this, "Bạn có chắc muốn xóa loại phòng " + sel.code + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            String maPhien = System.getProperty("user.name"); if (maPhien == null) maPhien = "UNKNOWN";
            try {
                boolean deleted = ((LoaiPhongServiceImpl) loaiPhongService).deleteRoomCategoryWithAudit(sel.code, maPhien);
                if (deleted) JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Xóa loại phòng thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                else JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Không thể xóa loại phòng", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Lỗi khi xóa loại phòng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                reloadListFromService();
            }
        });

        onePeopleButton   = createCategoryButton("1 người", "#1BA1E2", CATEGORY_BUTTON_SIZE);
        twoPeopleButton   = createCategoryButton("2 người", "#34D399", CATEGORY_BUTTON_SIZE);
        // threePeopleButton removed intentionally per request
        fourPeopleButton  = createCategoryButton("4 người", "#A78BFA", CATEGORY_BUTTON_SIZE);
        vipButton         = createCategoryButton("VIP", "#E3C800", CATEGORY_BUTTON_SIZE);
        normalButton      = createCategoryButton("Thường", "#647687", CATEGORY_BUTTON_SIZE);
        allCategoryButton = createCategoryButton("Toàn bộ", "#3B82F6", CATEGORY_BUTTON_SIZE);

        onePeopleButton.addActionListener(e -> setActiveCategoryFilter(1, null, onePeopleButton));
        twoPeopleButton.addActionListener(e -> setActiveCategoryFilter(2, null, twoPeopleButton));
        fourPeopleButton.addActionListener(e -> setActiveCategoryFilter(4, null, fourPeopleButton));
        vipButton.addActionListener(e -> setActiveCategoryFilter(null, "VIP", vipButton));
        normalButton.addActionListener(e -> setActiveCategoryFilter(null, "Thường", normalButton));
        allCategoryButton.addActionListener(e -> setActiveCategoryFilter(null, null, allCategoryButton));
    }

    private void setActiveCategoryFilter(Integer people, String type, JButton btn) {
        this.activePeopleFilter = people;
        this.activeTypeFilter = type;
        // thay vì setBorder, dùng clientProperty "active" để RoundedButton vẽ viền phù hợp
        if (activeCategoryButton != null) {
            activeCategoryButton.putClientProperty("active", Boolean.FALSE);
            activeCategoryButton.repaint();
        }
        activeCategoryButton = btn;
        if (activeCategoryButton != null) {
            activeCategoryButton.putClientProperty("active", Boolean.TRUE);
            activeCategoryButton.repaint();
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
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (Objects.equals(field.getText(), placeholder)) { field.setText(""); field.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(placeholder); }
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
        RoundedButton button = new RoundedButton(text, 20);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);

        // set colors
        try {
            Color bg = Color.decode(hexColor);
            button.setBackground(bg);
            // chọn màu chữ dựa trên độ tương phản (luminance)
            double lum = (bg.getRed() * 0.299 + bg.getGreen() * 0.587 + bg.getBlue() * 0.114);
            button.setForeground(lum > 186 ? Color.BLACK : Color.WHITE);
        } catch (Exception ex) {
            button.setBackground(Color.GRAY);
            button.setForeground(Color.WHITE);
        }

        button.setFont(FONT_CATEGORY);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);

        // ensure default not active
        button.putClientProperty("active", Boolean.FALSE);

        return button;
    }

    private JButton createActionButton(String text, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFont(FONT_ACTION);
        try { button.setBackground(Color.decode(bgHex)); } catch (Exception e) { button.setBackground(new Color(0x888888)); }
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor:" + borderHex);

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
        // giữ insets nhưng viền vẫn rõ ràng
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        String[] searchOptions = {"Mã loại phòng", "Tên loại phòng", "Trạng thái"};
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

        categoryCodeField = new JTextField();
        configureSearchTextField(categoryCodeField, new Dimension(380,45), CODE_PLACEHOLDER);
        categoryCodeField.setMaximumSize(new Dimension(380,45));
        categoryCodeField.setMinimumSize(new Dimension(380,45));

        // name search field
        categoryNameField = new JTextField();
        configureSearchTextField(categoryNameField, new Dimension(380,45), NAME_PLACEHOLDER);
        categoryNameField.setMaximumSize(new Dimension(380,45));
        categoryNameField.setMinimumSize(new Dimension(380,45));

        String[] statusOptions = {"Thường", "VIP", "Tất cả"};
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(380, 45));
        statusComboBox.setMaximumSize(new Dimension(380, 45));
        statusComboBox.setMinimumSize(new Dimension(380, 45));
        statusComboBox.setFont(new Font("Arial", Font.BOLD, 15));
        statusComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        inputPanel.add(categoryCodeField, "Mã loại phòng");
        inputPanel.add(categoryNameField, "Tên loại phòng");
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

        categoryCodeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        categoryNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        statusComboBox.addActionListener(ev -> {
            String sel = (String) statusComboBox.getSelectedItem();
            if (sel == null || "Tất cả".equals(sel)) activeTypeFilter = null;
            else if (sel.equalsIgnoreCase("VIP")) activeTypeFilter = "VIP";
            else activeTypeFilter = sel;
            // khi thay đổi statusComboBox, clear any activeCategoryButton highlight (optional)
            if (activeCategoryButton != null) {
                activeCategoryButton.putClientProperty("active", Boolean.FALSE);
                activeCategoryButton.repaint();
                activeCategoryButton = null;
            }
            applyFilters();
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

        // addButton nằm bên trái của editButton; cả cặp được căn giữa
        actionRow1.add(addButton);
        actionRow1.add(Box.createHorizontalStrut(12));
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

    private JPanel createCategoryPanel() {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(CustomUI.white);
        categoryPanel.setPreferredSize(new Dimension(655, 200));
        categoryPanel.setMaximumSize(new Dimension(655, 200));
        categoryPanel.setOpaque(true);
        categoryPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(allCategoryButton); row1.add(onePeopleButton); row1.add(twoPeopleButton);
        categoryPanel.add(row1); categoryPanel.add(Box.createVerticalStrut(10));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row2.setBackground(CustomUI.white);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        // removed threePeopleButton - add fourPeople, vip and normal here (normal moved next to VIP)
        row2.add(fourPeopleButton);
        row2.add(vipButton);
        row2.add(normalButton);
        categoryPanel.add(row2); categoryPanel.add(Box.createVerticalStrut(10));

        // no separate row for 'normalButton' now; keep small spacing below
        // (if you want further reflow, we can adjust sizes or add/remove rigid areas)

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

    private void createListCategoryPanel() {
        String[] cols = {"Mã loại", "Tên loại", "Phân loại", "Số người", "Giá ngày", "Giá giờ"};
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // JTable override prepareRenderer để cập nhật ngay background khi chọn dòng
        categoryTable = new JTable(categoryTableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
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

        categoryTable.setRowHeight(48);
        categoryTable.getTableHeader().setPreferredSize(new Dimension(categoryTable.getWidth(), 40));
        categoryTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        categoryTable.getTableHeader().setBackground(CustomUI.blue);
        categoryTable.getTableHeader().setForeground(CustomUI.white);
        categoryTable.getTableHeader().setOpaque(true);

        // reuse default renderer to ensure alignment & border on non-overridden scenarios
        categoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                comp.setFont(CustomUI.TABLE_FONT);
                return comp;
            }
        });

        // double-click to view/edit
        categoryTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = categoryTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        CategoryData d = getCategoryDataAtViewRow(row);
                        if (d != null) {
                            // ensure services ready before opening detail
                            ensureLazyInit();
                            new SwingWorker<Void, Void>() {
                                @Override protected Void doInBackground() throws Exception {
                                    int waited = 0;
                                    while (!servicesReady && waited < 5000) { Thread.sleep(100); waited += 100; }
                                    return null;
                                }
                                @Override protected void done() {
                                    SwingUtilities.invokeLater(() -> openCategoryDetail(d));
                                }
                            }.execute();
                        }
                    }
                } else {
                    // single click: repaint to show selection immediately and keep buttons enabled
                    SwingUtilities.invokeLater(() -> categoryTable.repaint());
                }
            }
        });

        categoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        categoryTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = categoryTable.getWidth();
                TableColumnModel columnModel = categoryTable.getColumnModel();
                if (columnModel.getColumnCount() < 6) return;
                columnModel.getColumn(0).setPreferredWidth((int)(tableWidth*0.12));
                columnModel.getColumn(1).setPreferredWidth((int)(tableWidth*0.35));
                columnModel.getColumn(2).setPreferredWidth((int)(tableWidth*0.12));
                columnModel.getColumn(3).setPreferredWidth((int)(tableWidth*0.11));
                columnModel.getColumn(4).setPreferredWidth((int)(tableWidth*0.15));
                columnModel.getColumn(5).setPreferredWidth((int)(tableWidth*0.15));
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        add(scrollPane);

        // load data (this will return empty until servicesReady; ensureLazyInit() will call reload when ready)
        reloadListFromService();
    }

    private void openCategoryDetail(CategoryData d) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);

        // If services not ready yet, wait in background (avoid blocking EDT)
        if (!servicesReady) {
            ensureLazyInit();
            new SwingWorker<OpenPayload, Void>() {
                @Override protected OpenPayload doInBackground() throws Exception {
                    int waited = 0;
                    while (!servicesReady && waited < 5000) { Thread.sleep(100); waited += 100; }
                    LoaiPhong lp = null;
                    try { if (loaiPhongService != null) lp = loaiPhongService.getRoomCategoryByIDV2(d.code); } catch (Exception ignored) { lp = null; }
                    if (lp == null) {
                        lp = new LoaiPhong(); lp.setMaLoaiPhong(d.code); lp.setTenLoaiPhong(d.name);
                    }
                    List<NoiThat> furniture = new ArrayList<>();
                    try { if (noiThatService != null) furniture = noiThatService.getNoiThatByLoaiPhong(d.code); } catch (Exception ignored) {}
                    return new OpenPayload(lp, furniture);
                }
                @Override protected void done() {
                    try {
                        OpenPayload p = get();
                        try {
                            SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, p.lp, p.furniture);
                            dlg.setLocationRelativeTo(owner);
                            dlg.setVisible(true);
                        } catch (Throwable ex) {
                            try { SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, p.lp, p.furniture); dlg.setVisible(true); } catch (Throwable ignore) {}
                        } finally { reloadListFromService(); }
                    } catch (Exception ex) { ex.printStackTrace(); reloadListFromService(); }
                }
            }.execute();
            return;
        }

        // services ready => proceed normally
        LoaiPhong lp = null;
        try {
            if (loaiPhongService != null) lp = loaiPhongService.getRoomCategoryByIDV2(d.code);
        } catch (Exception ignored) { lp = null; }
        if (lp == null) {
            lp = new LoaiPhong(); lp.setMaLoaiPhong(d.code); lp.setTenLoaiPhong(d.name);
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu đầy đủ, mở form với dữ liệu sẵn có.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        List<NoiThat> furniture = new ArrayList<>();
        try { if (noiThatService != null) furniture = noiThatService.getNoiThatByLoaiPhong(d.code); } catch (Exception ignored) { }
        try {
            SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, lp, furniture);
            dlg.setLocationRelativeTo(owner);
            dlg.setVisible(true);
        } catch (Throwable ex) {
            try { SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, lp, furniture); dlg.setVisible(true); } catch (Throwable ignore) {}
        } finally { reloadListFromService(); }
    }

    // small holder used by background open
    private static class OpenPayload {
        final LoaiPhong lp;
        final List<NoiThat> furniture;
        OpenPayload(LoaiPhong lp, List<NoiThat> furniture) { this.lp = lp; this.furniture = furniture; }
    }

    // Thay thế method reloadListFromService() bằng phiên bản dựa trên Supplier
    public void reloadListFromService() {
        // xóa table hiện tại (placeholder)
        if (categoryTableModel != null) categoryTableModel.setRowCount(0);

        // supplier sẽ tạo service mới mỗi lần (tránh dùng service cũ có connection đã đóng)
        Supplier<List<CategoryData>> loader = () -> {
            List<CategoryData> dataset = new ArrayList<>();
            LoaiPhongService svc = null;
            try {
                // tạo service tạm, dùng xong để GC (DAO bên trong sẽ tạo connection mới)
                svc = new LoaiPhongServiceImpl();

                List<RoomCategoryResponse> list = null;
                try {
                    list = svc.getAllRoomCategories();
                } catch (Exception e) {
                    // nếu lỗi, trả về rỗng — SwingWorker sẽ in stacktrace
                    e.printStackTrace();
                    return dataset;
                }
                if (list == null) return dataset;

                for (RoomCategoryResponse r : list) {
                    String code = r.getMaLoaiPhong();
                    String name = r.getTenLoaiPhong();
                    int people = r.getSoLuongKhach();
                    String type = r.getPhanLoai();

                    // lấy giá — gọi trên cùng service tạm để các DAO share cùng connection (ít gọi nhiều connection)
                    String giaNgayStr = "-";
                    String giaGioStr  = "-";
                    try {
                        BigDecimal giaNgay = null;
                        BigDecimal giaGio  = null;
                        try {
                            giaNgay = svc.layGiaTheoLoaiPhong(code, true);
                            giaGio  = svc.layGiaTheoLoaiPhong(code, false);
                        } catch (Exception ex) {
                            // có thể ném do giá ko tìm thấy hoặc lỗi kết nối -> tiếp tục với "-"
                            ex.printStackTrace();
                        }
                        if (giaNgay != null) giaNgayStr = formatPrice(giaNgay);
                        if (giaGio  != null) giaGioStr  = formatPrice(giaGio);
                    } catch (Throwable ignore) {}

                    dataset.add(new CategoryData(code, name, people, type, giaNgayStr, giaGioStr));
                }
                return dataset;
            } finally {
                // svc không có close(); nếu bạn có method close ở service/dao có thể gọi ở đây.
                // Để tránh giữ reference lâu, ta không lưu svc vào trường lớp.
            }
        };

        // gọi reload async (giống reloadRoomsAsync)
        reloadCategoriesAsync(loader);
    }

    // helper tương tự reloadRoomsAsync ở panel phòng
    private void reloadCategoriesAsync(Supplier<List<CategoryData>> loader) {
        SwingWorker<List<CategoryData>, Void> wk = new SwingWorker<>() {
            @Override protected List<CategoryData> doInBackground() {
                try {
                    return loader.get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return new ArrayList<>();
                }
            }
            @Override protected void done() {
                try {
                    List<CategoryData> dataset = get();
                    fullDataset = dataset == null ? new ArrayList<>() : dataset;
                    applyFilters(); // cập nhật table trên EDT
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        wk.execute();
    }

    // filter and update table
    private void applyFilters() {
        if (fullDataset == null) return;
        String selectedSearch = (searchTypeComboBox != null) ? (String) searchTypeComboBox.getSelectedItem() : "Mã loại phòng";

        // code filter
        String txtCode = categoryCodeField.getText();
        boolean isPlaceholderCode = categoryCodeField.getForeground().equals(Color.GRAY) && CODE_PLACEHOLDER.equals(txtCode);
        String codeFilter = (!isPlaceholderCode && txtCode != null && !txtCode.isBlank()) ? txtCode.trim().toLowerCase() : null;

        // name filter
        String txtName = categoryNameField.getText();
        boolean isPlaceholderName = categoryNameField.getForeground().equals(Color.GRAY) && NAME_PLACEHOLDER.equals(txtName);
        String nameFilter = (!isPlaceholderName && txtName != null && !txtName.isBlank()) ? txtName.trim().toLowerCase() : null;

        List<CategoryData> filtered = new ArrayList<>();
        for (CategoryData d : fullDataset) {
            boolean ok = true;
            // apply search type for code/name
            if ("Mã loại phòng".equals(selectedSearch)) {
                if (codeFilter != null) { if (d.code == null || !d.code.toLowerCase().contains(codeFilter)) ok = false; }
            } else if ("Tên loại phòng".equals(selectedSearch)) {
                if (nameFilter != null) { if (d.name == null || !d.name.toLowerCase().contains(nameFilter)) ok = false; }
            } // nếu là "Trạng thái" thì statusComboBox đã cập nhật activeTypeFilter

            // apply activeTypeFilter (luôn áp dụng, không phụ thuộc vào selectedSearch)
            if (ok && activeTypeFilter != null) {
                if (d.type == null || !d.type.equalsIgnoreCase(activeTypeFilter)) ok = false;
            }

            if (ok && activePeopleFilter != null) { if (d.people != activePeopleFilter) ok = false; }
            if (ok) filtered.add(d);
        }
        populateCategoryTable(filtered);
    }

    private void populateCategoryTable(List<CategoryData> dataset) {
        if (categoryTableModel == null) return;
        SwingUtilities.invokeLater(() -> {
            categoryTableModel.setRowCount(0);
            if (dataset == null || dataset.isEmpty()) {
                categoryTableModel.addRow(new Object[] {"-","Không có loại phòng phù hợp.","-","-", "-", "-"});
                return;
            }
            for (CategoryData d : dataset) {
                Object[] row = new Object[6];
                row[0] = d.code;
                row[1] = d.name;
                row[2] = d.type;
                row[3] = d.people;
                row[4] = d.priceDay;
                row[5] = d.priceHour;
                categoryTableModel.addRow(row);
            }
            // clear selection after reload so users must re-select explicit row
            categoryTable.clearSelection();
        });
    }

    // helper: lấy CategoryData theo mã từ fullDataset
    private CategoryData getCategoryDataByCode(String code) {
        if (code == null) return null;
        if (fullDataset == null) return null;
        for (CategoryData d : fullDataset) {
            if (code.equals(d.code)) return d;
        }
        return null;
    }

    private CategoryData getCategoryDataAtViewRow(int viewRow) {
        if (viewRow < 0) return null;
        int modelRow = categoryTable.convertRowIndexToModel(viewRow);
        Object codeObj = categoryTableModel.getValueAt(modelRow, 0);
        if (codeObj == null) return null;
        return getCategoryDataByCode(codeObj.toString());
    }

    private CategoryData getSelectedCategoryData() {
        int viewRow = categoryTable.getSelectedRow();
        if (viewRow < 0) return null;
        return getCategoryDataAtViewRow(viewRow);
    }

    // small data holder (đã thêm giá ngày & giá giờ)
    private static class CategoryData {
        final String code, name, type;
        final int people;
        final String priceDay;
        final String priceHour;
        CategoryData(String code, String name, int people, String type, String priceDay, String priceHour) {
            this.code = code; this.name = name; this.people = people; this.type = type;
            this.priceDay = priceDay == null ? "-" : priceDay;
            this.priceHour = priceHour == null ? "-" : priceHour;
        }
    }

    private synchronized void ensureLazyInit() {
        if (initStarted) return;
        initStarted = true;

        // tạo services trong background để không block UI thread
        new SwingWorker<Void, Void>() {
            private Exception setupEx = null;
            @Override
            protected Void doInBackground() {
                try {
                    // tạo impl có thể mở kết nối, load config... thực hiện ở background
                    try {
                        loaiPhongService = new LoaiPhongServiceImpl();
                    } catch (Throwable t) {
                        loaiPhongService = null;
                        // optional: log t
                    }
                    try {
                        noiThatService = new NoiThatServiceImpl();
                    } catch (Throwable t) {
                        noiThatService = null;
                    }
                } catch (Exception ex) {
                    setupEx = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                // sau khi tạo xong service, set servicesReady = true rồi load dữ liệu
                servicesReady = true;
                // reloadListFromService đã dùng SwingWorker để load data -> an toàn gọi trực tiếp
                reloadListFromService();

                if (setupEx != null) {
                    // không block UI; chỉ thông báo qua console. Nếu muốn, hiện non-blocking label ở UI.
                    setupEx.printStackTrace();
                }
            }
        }.execute();
    }

    private String formatPrice(BigDecimal p) {
        if (p == null) return "-";
        try {
            // làm tròn tới đồng và hiển thị không có phần thập phân
            BigDecimal rounded = p.setScale(0, RoundingMode.HALF_UP);
            // định dạng với dấu phân cách hàng nghìn
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(rounded.longValue()) + " đ";
        } catch (Exception e) {
            return p.toPlainString();
        }
    }

    private static class RoundedButton extends JButton {
        private final int arc;
        public RoundedButton(String text, int arc) {
            super(text);
            this.arc = arc;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = getBackground() != null ? getBackground() : new Color(0x888888);
            ButtonModel model = getModel();

            if (model.isPressed()) {
                // make pressed slightly darker
                bg = bg.darker();
            } else if (model.isRollover()) {
                // slightly brighter on hover
                bg = bg.brighter();
            }

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            // draw border: thick when active, thin otherwise
            Object active = getClientProperty("active");
            if (Boolean.TRUE.equals(active)) {
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
            } else {
                g2.setStroke(new BasicStroke(1f));
                // subtle border color
                Color borderColor = new Color(0,0,0,30);
                g2.setColor(borderColor);
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, arc, arc);
            }

            g2.dispose();

            // let UI draw the text and focus/selected state (component is non-opaque so no background)
            super.paintComponent(g);
        }

        @Override
        public void setBorder(Border border) {
            // ignore external border changes to keep rounded shape consistent
        }
    }

}

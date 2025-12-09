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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

// Panel quản lý loại phòng (tối giản: thay RoundedButton bằng JButton thuần, vẫn giữ bo góc bằng FlatLaf)
public class QuanLyLoaiPhongPanel extends JPanel {

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

    // Table components to replace card list
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    // services
    private LoaiPhongService loaiPhongService;
    private NoiThatService noiThatService;
    private volatile boolean lazyInitialized = false;

    private JComboBox<String> searchTypeComboBox;
    private JTextField categoryCodeField;
    private JComboBox<String> statusComboBox;
    private static final String CODE_PLACEHOLDER = "Mã loại phòng";

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

        // tạo action button KHÔNG tải icon
        addButton    = createActionButton("Thêm loại phòng", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa loại phòng", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa loại phòng", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        addButton.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            try {
                ThemLoaiPhongDialog dlg = new ThemLoaiPhongDialog(owner, loaiPhongService, noiThatService);
                dlg.setVisible(true);
            } catch (Throwable ex) {
                try { ThemLoaiPhongDialog dlg = new ThemLoaiPhongDialog(owner, loaiPhongService, noiThatService); dlg.setVisible(true); }
                catch (Throwable ignore) {}
            } finally { reloadListFromService(); }
        });

        onePeopleButton   = createCategoryButton("1 người", "#1BA1E2", CATEGORY_BUTTON_SIZE);
        twoPeopleButton   = createCategoryButton("2 người", "#34D399", CATEGORY_BUTTON_SIZE);
        threePeopleButton = createCategoryButton("3 người", "#FB923C", CATEGORY_BUTTON_SIZE);
        fourPeopleButton  = createCategoryButton("4 người", "#A78BFA", CATEGORY_BUTTON_SIZE);
        vipButton         = createCategoryButton("VIP", "#E3C800", CATEGORY_BUTTON_SIZE);
        normalButton      = createCategoryButton("Thường", "#647687", CATEGORY_BUTTON_SIZE);
        allCategoryButton = createCategoryButton("Toàn bộ", "#3B82F6", CATEGORY_BUTTON_SIZE);

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
        if (activeCategoryButton != null) activeCategoryButton.setBorder(null);
        activeCategoryButton = btn;
        if (activeCategoryButton != null) activeCategoryButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
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

    /**
     * Tạo category button bằng JButton thuần.
     * Giữ bo góc bằng thuộc tính FlatLaf ("arc: 20") và set màu/foreground dựa trên độ sáng nền.
     */
    private JButton createCategoryButton(String text, String hexColor, Dimension size) {
        JButton button = new JButton(text);
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
        // Sử dụng FlatLaf client property để bo góc và viền
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor: #D1D5DB; focusWidth: 0; innerFocusWidth: 0;");
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

        categoryCodeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        statusComboBox.addActionListener(ev -> {
            String sel = (String) statusComboBox.getSelectedItem();
            if (sel == null || "Tất cả".equals(sel)) activeTypeFilter = null;
            else if (sel.equalsIgnoreCase("VIP")) activeTypeFilter = "Vip";
            else activeTypeFilter = sel;
            applyFilters();
        });

        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(10));

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
        row2.add(threePeopleButton); row2.add(fourPeopleButton); row2.add(vipButton);
        categoryPanel.add(row2); categoryPanel.add(Box.createVerticalStrut(10));

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row3.setBackground(CustomUI.white);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(normalButton);
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

    // --- Thay thế createListCategoryPanel: tạo table và scrollpane (không có cột Ảnh) ---
    private void createListCategoryPanel() {
        String[] cols = {"Mã loại", "Tên loại", "Phân loại", "Số người", "Thao tác"};
        categoryTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
        };

        categoryTable = new JTable(categoryTableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? CustomUI.ROW_EVEN : CustomUI.ROW_ODD);
                else c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                return c;
            }
        };

        categoryTable.setRowHeight(48);
        categoryTable.getTableHeader().setPreferredSize(new Dimension(categoryTable.getWidth(), 40));
        categoryTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        categoryTable.getTableHeader().setBackground(CustomUI.blue);
        categoryTable.getTableHeader().setForeground(CustomUI.white);
        categoryTable.getTableHeader().setOpaque(true);

        categoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                comp.setFont(CustomUI.TABLE_FONT);
                return comp;
            }
        });

        // action column renderer/editor
        categoryTable.getColumn("Thao tác").setCellRenderer(new CategoryActionRenderer());
        categoryTable.getColumn("Thao tác").setCellEditor(new CategoryActionEditor());

        // double-click to view/edit
        categoryTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = categoryTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        Object val = categoryTableModel.getValueAt(row, 4);
                        if (val instanceof CategoryData) {
                            CategoryData d = (CategoryData) val;
                            SwingUtilities.invokeLater(() -> openCategoryDetail(d));
                        }
                    }
                }
            }
        });

        categoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        categoryTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = categoryTable.getWidth();
                TableColumnModel columnModel = categoryTable.getColumnModel();
                if (columnModel.getColumnCount() < 5) return;
                columnModel.getColumn(0).setPreferredWidth((int)(tableWidth*0.15));
                columnModel.getColumn(1).setPreferredWidth((int)(tableWidth*0.35));
                columnModel.getColumn(2).setPreferredWidth((int)(tableWidth*0.12));
                columnModel.getColumn(3).setPreferredWidth((int)(tableWidth*0.12));
                columnModel.getColumn(4).setPreferredWidth((int)(tableWidth*0.26));
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        add(scrollPane);

        // load data
        reloadListFromService();
    }

    private void openCategoryDetail(CategoryData d) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
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
            dlg.setVisible(true);
        } catch (Throwable ex) {
            try { SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, lp, furniture); dlg.setVisible(true); } catch (Throwable ignore) {}
        } finally { reloadListFromService(); }
    }

    // reload uses existing SwingWorker implementation but now populates table model
    private void reloadListFromService() {
        // show temporary placeholder while loading
        if (categoryTableModel != null) categoryTableModel.setRowCount(0);

        SwingWorker<List<CategoryData>, Void> wk = new SwingWorker<>() {
            private Exception error = null;
            @Override protected List<CategoryData> doInBackground() {
                List<CategoryData> dataset = new ArrayList<>();
                if (loaiPhongService == null) {
                    return dataset; // empty
                }
                try {
                    List<RoomCategoryResponse> list = loaiPhongService.getAllRoomCategories();
                    if (list == null) return dataset;
                    for (RoomCategoryResponse r : list) {
                        String code = r.getMaLoaiPhong();
                        String name = r.getTenLoaiPhong();
                        int people = r.getSoLuongKhach();
                        String type = r.getPhanLoai();
                        dataset.add(new CategoryData(code, name, people, type));
                    }
                    return dataset;
                } catch (Exception ex) { error = ex; return new ArrayList<>(); }
            }
            @Override protected void done() {
                try {
                    List<CategoryData> dataset = get();
                    fullDataset = dataset == null ? new ArrayList<>() : dataset;
                    applyFilters();
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
        String txt = categoryCodeField.getText();
        boolean isPlaceholder = categoryCodeField.getForeground().equals(Color.GRAY) && CODE_PLACEHOLDER.equals(txt);
        String codeFilter = (!isPlaceholder && txt != null && !txt.isBlank()) ? txt.trim().toLowerCase() : null;
        List<CategoryData> filtered = new ArrayList<>();
        for (CategoryData d : fullDataset) {
            boolean ok = true;
            if (codeFilter != null) { if (d.code == null || !d.code.toLowerCase().contains(codeFilter)) ok = false; }
            if (ok && activeTypeFilter != null) { if (d.type == null || !d.type.equalsIgnoreCase(activeTypeFilter)) ok = false; }
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
                categoryTableModel.addRow(new Object[] {"-","Không có loại phòng phù hợp.","-","-", null});
                return;
            }
            for (CategoryData d : dataset) {
                Object[] row = new Object[5];
                row[0] = d.code;
                row[1] = d.name;
                row[2] = d.type;
                row[3] = d.people;
                row[4] = d; // CategoryData for actions
                categoryTableModel.addRow(row);
            }
        });
    }

    // action column renderer/editor
    private class CategoryActionRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnEdit = new JButton("Sửa");
        private final JButton btnDelete = new JButton("Xóa");
        public CategoryActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));
            btnEdit.setFont(CustomUI.smallFont); btnDelete.setFont(CustomUI.smallFont);
            btnEdit.setPreferredSize(new Dimension(80,30)); btnDelete.setPreferredSize(new Dimension(80,30));
            // colors
            btnEdit.setBackground(new Color(30,144,255)); btnEdit.setForeground(Color.WHITE); btnEdit.setOpaque(true); btnEdit.setFocusPainted(false);
            btnDelete.setBackground(new Color(220,35,35)); btnDelete.setForeground(Color.WHITE); btnDelete.setOpaque(true); btnDelete.setFocusPainted(false);
            add(btnEdit); add(btnDelete);
            setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) setBackground(table.getSelectionBackground()); else setBackground(table.getBackground());
            return this;
        }
    }

    private class CategoryActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        private final JButton btnEdit = new JButton("Sửa");
        private final JButton btnDelete = new JButton("Xóa");
        private CategoryData current;
        public CategoryActionEditor() {
            btnEdit.setFont(CustomUI.smallFont); btnDelete.setFont(CustomUI.smallFont);
            btnEdit.setPreferredSize(new Dimension(80,30)); btnDelete.setPreferredSize(new Dimension(80,30));
            btnEdit.setBackground(new Color(30,144,255)); btnEdit.setForeground(Color.WHITE); btnEdit.setOpaque(true); btnEdit.setFocusPainted(false);
            btnDelete.setBackground(new Color(220,35,35)); btnDelete.setForeground(Color.WHITE); btnDelete.setOpaque(true); btnDelete.setFocusPainted(false);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                if (current == null) return;
                SwingUtilities.invokeLater(() -> openCategoryDetail(current));
            });

            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (current == null) return;
                if (loaiPhongService == null) { JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Không có service để xóa", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                int confirm = JOptionPane.showConfirmDialog(QuanLyLoaiPhongPanel.this, "Bạn có chắc muốn xóa loại phòng " + current.code + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
                String maPhien = System.getProperty("user.name"); if (maPhien == null) maPhien = "UNKNOWN";
                try {
                    boolean deleted = ((LoaiPhongServiceImpl) loaiPhongService).deleteRoomCategoryWithAudit(current.code, maPhien);
                    if (deleted) JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Xóa loại phòng thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    else JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Không thể xóa loại phòng", "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) { JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Lỗi khi xóa loại phòng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); ex.printStackTrace(); }
                finally { reloadListFromService(); }
            });

            panel.add(btnEdit); panel.add(btnDelete);
            panel.setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            try { current = value instanceof CategoryData ? (CategoryData) value : null; } catch (Exception e) { current = null; }
            return panel;
        }

        @Override public Object getCellEditorValue() { return current; }
    }

    // small data holder (đã bỏ trường ảnh để tránh tải ảnh)
    private static class CategoryData {
        final String code, name, type;
        final int people;
        CategoryData(String code, String name, int people, String type) { this.code = code; this.name = name; this.people = people; this.type = type; }
    }

    /**
     * Khởi tạo lười: tạo service trong background (không chặn EDT) rồi tải dữ liệu.
     * Gọi an toàn nhiều lần — thực hiện duy nhất 1 lần.
     */
    private synchronized void ensureLazyInit() {
        if (lazyInitialized) return;
        lazyInitialized = true;

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
                // sau khi tạo xong service, load dữ liệu (cũng trong SwingWorker riêng nếu cần)
                // reloadListFromService đã dùng SwingWorker để load data -> an toàn gọi trực tiếp
                reloadListFromService();

                if (setupEx != null) {
                    // không block UI; chỉ thông báo qua console. Nếu muốn, hiện non-blocking label ở UI.
                    setupEx.printStackTrace();
                }
            }
        }.execute();
    }

}

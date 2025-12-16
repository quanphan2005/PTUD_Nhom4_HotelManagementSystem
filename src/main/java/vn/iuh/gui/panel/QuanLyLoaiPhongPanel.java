// (Toàn bộ file giống như trước, chỉ dán lại phần đã chỉnh sửa hoàn chỉnh)
package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.ChiTietLoaiPhongDialog;
import vn.iuh.gui.dialog.SuaLoaiPhongDialog;
import vn.iuh.gui.dialog.ThemLoaiPhongDialog;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.NoiThatService;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.service.impl.NoiThatServiceImpl;
import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.NoiThat;
import vn.iuh.util.AppEventBus;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class QuanLyLoaiPhongPanel extends JPanel {

    // ADJUSTED sizes: make search components wider and action buttons larger/harmonized
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(110, 45); // increased
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 50); // harmonized with room panel

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
    // CHANGED: use JComboBox for name search (list of existing room category names)
    private JComboBox<String> categoryNameComboBox;
    private JComboBox<String> statusComboBox;
    private static final String CODE_PLACEHOLDER = "Mã loại phòng";
    private static final String NAME_PLACEHOLDER = "Tên loại phòng";

    private List<CategoryData> fullDataset = new ArrayList<>();
    // removed people/category buttons -> no more activePeopleFilter

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
        createSearchAndCategoryPanel(); // now will only add the expanded search panel
        add(Box.createVerticalStrut(10));
        // create table-based list (replaces previous card list)
        createListCategoryPanel();
    }

    private void initButtons() {
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE);
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
                        reloadListFromService();
                        AppEventBus.publish("ROOMS_UPDATED");
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
                private Exception checkEx = null;
                private boolean hasBooking = false;

                @Override protected Void doInBackground() throws Exception {
                    int waited = 0;
                    while (!servicesReady && waited < 5000) { Thread.sleep(100); waited += 100; }
                    try {
                        // đảm bảo có một instance LoaiPhongServiceImpl để gọi method kiểm tra
                        LoaiPhongServiceImpl svcImpl;
                        if (loaiPhongService instanceof LoaiPhongServiceImpl) {
                            svcImpl = (LoaiPhongServiceImpl) loaiPhongService;
                        } else {
                            svcImpl = new LoaiPhongServiceImpl();
                        }
                        hasBooking = svcImpl.hasCurrentOrFutureBookingsForLoaiPhong(sel.code);
                    } catch (Exception ex) {
                        checkEx = ex;
                    }
                    return null;
                }

                @Override protected void done() {
                    // chạy trên EDT
                    if (checkEx != null) {
                        checkEx.printStackTrace();
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this,
                                "Lỗi khi kiểm tra trạng thái đặt phòng: " + checkEx.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (hasBooking) {
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this,
                                "Không thể sửa: loại phòng đang được sử dụng hoặc có đơn đặt trong tương lai.",
                                "Không cho phép", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // nếu không có booking -> mở dialog chỉnh sửa (giữ logic hiện tại)
                    new SwingWorker<OpenPayload, Void>() {
                        @Override protected OpenPayload doInBackground() throws Exception {
                            int waited = 0;
                            while (!servicesReady && waited < 5000) { Thread.sleep(100); waited += 100; }
                            LoaiPhong lp = null;
                            try { if (loaiPhongService != null) lp = loaiPhongService.getRoomCategoryByIDV2(sel.code); } catch (Exception ignored) { lp = null; }
                            if (lp == null) { lp = new LoaiPhong(); lp.setMaLoaiPhong(sel.code); lp.setTenLoaiPhong(sel.name); }
                            List<NoiThat> furniture = new ArrayList<>();
                            try { if (noiThatService != null) furniture = noiThatService.getNoiThatByLoaiPhong(sel.code); } catch (Exception ignored) {}
                            return new OpenPayload(lp, furniture);
                        }
                        @Override protected void done() {
                            try {
                                OpenPayload p = get();
                                Frame owner = (Frame) SwingUtilities.getWindowAncestor(QuanLyLoaiPhongPanel.this);
                                SuaLoaiPhongDialog dlg = new SuaLoaiPhongDialog(owner, loaiPhongService, noiThatService, p.lp, p.furniture);
                                dlg.setLocationRelativeTo(owner);
                                dlg.setVisible(true);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                reloadListFromService();
                                AppEventBus.publish("ROOMS_UPDATED");
                            }
                        }
                    }.execute();
                }
            }.execute();
        });

        deleteButton.addActionListener(e -> {
            CategoryData sel = getSelectedCategoryData();
            if (sel == null) {
                JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Vui lòng chọn 1 loại phòng để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // ensure service ready
            ensureLazyInit();

            // hỏi xác nhận sơ bộ
            int confirm = JOptionPane.showConfirmDialog(QuanLyLoaiPhongPanel.this, "Bạn có chắc muốn xóa loại phòng " + sel.code + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            new SwingWorker<Void, Void>() {
                private Exception checkEx = null;
                private boolean hasBooking = false;
                private boolean deleted = false;
                private Exception deleteEx = null;

                @Override protected Void doInBackground() throws Exception {
                    int waited = 0;
                    while (!servicesReady && waited < 5000) { Thread.sleep(100); waited += 100; }

                    try {
                        vn.iuh.service.impl.LoaiPhongServiceImpl svcImpl;
                        if (loaiPhongService instanceof vn.iuh.service.impl.LoaiPhongServiceImpl) {
                            svcImpl = (vn.iuh.service.impl.LoaiPhongServiceImpl) loaiPhongService;
                        } else {
                            svcImpl = new vn.iuh.service.impl.LoaiPhongServiceImpl();
                        }
                        hasBooking = svcImpl.hasCurrentOrFutureBookingsForLoaiPhong(sel.code);
                        if (!hasBooking) {
                            String maPhien = System.getProperty("user.name"); if (maPhien == null) maPhien = "UNKNOWN";
                            deleted = svcImpl.deleteRoomCategoryWithAudit(sel.code, maPhien);
                        }
                    } catch (Exception ex) {
                        // phân biệt lỗi kiểm tra và lỗi xóa bằng deleteEx/checkEx
                        if (hasBooking) {
                            checkEx = ex;
                        } else {
                            deleteEx = ex;
                        }
                    }
                    return null;
                }

                @Override protected void done() {
                    if (checkEx != null) {
                        checkEx.printStackTrace();
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this,
                                "Lỗi khi kiểm tra booking: " + checkEx.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (hasBooking) {
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this,
                                "Không thể xóa: loại phòng đang được sử dụng hoặc có đơn đặt trong tương lai.",
                                "Không cho phép", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (deleteEx != null) {
                        deleteEx.printStackTrace();
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this,
                                "Lỗi khi xóa loại phòng: " + deleteEx.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        reloadListFromService();
                        return;
                    }
                    if (deleted) {
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Xóa loại phòng thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(QuanLyLoaiPhongPanel.this, "Không thể xóa loại phòng", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                    reloadListFromService();
                    AppEventBus.publish("ROOMS_UPDATED");
                }
            }.execute();
        });
    }

    private void configureSearchTextField(JTextField field, Dimension size) {
        field.setPreferredSize(size);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        field.setMinimumSize(new Dimension(120, size.height));
        field.setFont(FONT_LABEL);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        field.setForeground(Color.GRAY);
        field.setText(CODE_PLACEHOLDER);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (Objects.equals(field.getText(), CODE_PLACEHOLDER) || Objects.equals(field.getText(), NAME_PLACEHOLDER)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(CODE_PLACEHOLDER);
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

    // ---------- UPDATED: expanded search panel that spans full width ----------
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        // allow horizontal expansion
        searchPanel.setPreferredSize(new Dimension(0, 180));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setOpaque(true);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 30));

        // NOTE: "Phân loại" replaces previous "Trạng thái"
        String[] searchOptions = {"Mã loại phòng", "Tên loại phòng", "Phân loại"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        // larger combo so it looks balanced
        Dimension searchTypeSize = new Dimension(180, 45);
        searchTypeComboBox.setPreferredSize(searchTypeSize);
        searchTypeComboBox.setMinimumSize(searchTypeSize);
        searchTypeComboBox.setMaximumSize(new Dimension(220, 45));
        searchTypeComboBox.setFont(new Font("Arial", Font.BOLD, 14));

        // input area with flexible width
        JPanel inputPanel = new JPanel(new CardLayout());
        inputPanel.setPreferredSize(new Dimension(0, 45));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        inputPanel.setMinimumSize(new Dimension(0, 45));

        categoryCodeField = new JTextField();
        configureSearchTextField(categoryCodeField, SEARCH_TEXT_SIZE);
        // allow it to expand horizontally
        categoryCodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        categoryCodeField.setMinimumSize(new Dimension(120, 45));

        // CHANGED: combo box for existing category names
        categoryNameComboBox = new JComboBox<>();
        categoryNameComboBox.setEditable(false);
        categoryNameComboBox.setPreferredSize(SEARCH_TEXT_SIZE);
        categoryNameComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        categoryNameComboBox.setMinimumSize(new Dimension(120, 45));
        categoryNameComboBox.setFont(FONT_LABEL);
        // initial placeholder item
        categoryNameComboBox.addItem("Tất cả");
        categoryNameComboBox.addActionListener(ev -> applyFilters());

        String[] statusOptions = {"Tất cả", "Thường", "VIP"};
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setPreferredSize(new Dimension(160, 45));
        statusComboBox.setMaximumSize(new Dimension(200, 45));
        statusComboBox.setMinimumSize(new Dimension(120, 45));
        statusComboBox.setFont(new Font("Arial", Font.BOLD, 15));
        statusComboBox.putClientProperty(FlatClientProperties.STYLE, "arc:12");

        inputPanel.add(categoryCodeField, "Mã loại phòng");
        inputPanel.add(categoryNameComboBox, "Tên loại phòng"); // use combo here
        inputPanel.add(statusComboBox, "Phân loại"); // renamed

        // switch card when combobox changed
        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout)(inputPanel.getLayout());
            String selected = (String) searchTypeComboBox.getSelectedItem();
            cl.show(inputPanel, selected);
        });

        // place components on one row: searchTypeComboBox + gap + inputPanel + gap + searchButton
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(12));
        // adjust searchButton style/size
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        row1.add(searchButton);

        // Document listeners: keep for code field
        categoryCodeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // status/phanloai selection triggers filter
        statusComboBox.addActionListener(ev -> applyFilters());

        searchPanel.add(row1);
        // smaller vertical spacing to bring buttons closer but still airy
        searchPanel.add(Box.createVerticalStrut(30));

        // ---------- Action buttons row: center aligned and harmonized ----------
        JPanel actionRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        actionRow1.setBackground(CustomUI.white);

        // set sizes
        Dimension btnSize = new Dimension(ACTION_BUTTON_SIZE.width, ACTION_BUTTON_SIZE.height);
        addButton.setPreferredSize(btnSize);
        editButton.setPreferredSize(btnSize);
        deleteButton.setPreferredSize(btnSize);

        actionRow1.add(addButton);
        actionRow1.add(Box.createHorizontalStrut(12));
        actionRow1.add(editButton);
        actionRow1.add(Box.createHorizontalStrut(12));
        actionRow1.add(deleteButton);

        searchPanel.add(actionRow1);
        // slightly larger bottom spacing before the table
        searchPanel.add(Box.createVerticalStrut(8));

        return searchPanel;
    }

    private void createSearchAndCategoryPanel() {
        // Now only add the expanded search panel (no separate category panel)
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        container.setBackground(CustomUI.white);

        JPanel leftPanel = createSearchPanel();
        container.add(leftPanel);
        // fill remaining horizontal space
        container.add(Box.createHorizontalGlue());

        add(container);
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
                                    // open ChiTietLoaiPhongDialog (detail view) on EDT
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            Frame owner = (Frame) SwingUtilities.getWindowAncestor(QuanLyLoaiPhongPanel.this);
                                            LoaiPhongService svcLp = loaiPhongService;
                                            NoiThatService svcNt = noiThatService;
                                            if (svcLp == null) svcLp = new LoaiPhongServiceImpl();
                                            if (svcNt == null) svcNt = new NoiThatServiceImpl();
                                            ChiTietLoaiPhongDialog dlg = new ChiTietLoaiPhongDialog(owner, svcLp, svcNt, d.code);
                                            dlg.setLocationRelativeTo(owner);
                                            dlg.setVisible(true);
                                        } catch (Throwable ex) {
                                            ex.printStackTrace();
                                        } finally {
                                            // keep original behavior: reload list after closing detail
                                            reloadListFromService();
                                        }
                                    });
                                }
                            }.execute();
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
                            // có thể ném do giá ko tìm thấy hoặc lỗi kết nối -> tiếp tục với "-"`
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
                    // update the categoryNameComboBox model with unique names from dataset
                    SwingUtilities.invokeLater(() -> {
                        // preserve currently selected name if possible
                        String prev = null;
                        if (categoryNameComboBox.getSelectedItem() != null) prev = categoryNameComboBox.getSelectedItem().toString();
                        categoryNameComboBox.removeAllItems();
                        categoryNameComboBox.addItem("Tất cả"); // default
                        Set<String> names = new LinkedHashSet<>();
                        if (fullDataset != null) {
                            for (CategoryData cd : fullDataset) {
                                if (cd.name != null) names.add(cd.name);
                            }
                        }
                        for (String n : names) categoryNameComboBox.addItem(n);
                        // try to reselect previous selection if still present
                        if (prev != null) {
                            DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) categoryNameComboBox.getModel();
                            boolean found = false;
                            for (int i = 0; i < m.getSize(); i++) if (prev.equals(m.getElementAt(i))) { found = true; break; }
                            if (found) categoryNameComboBox.setSelectedItem(prev);
                        }
                        applyFilters();
                    });
                    applyFilters(); // cập nhật table trên EDT (applyFilters will use updated fullDataset)
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

        // name filter: read from combo selection
        String nameFilter = null;
        if (categoryNameComboBox != null) {
            Object selName = categoryNameComboBox.getSelectedItem();
            if (selName != null) {
                String sn = selName.toString();
                if (!"Tất cả".equalsIgnoreCase(sn)) nameFilter = sn.trim().toLowerCase();
            }
        }

        // type filter from statusComboBox (Phân loại)
        String selStatus = null;
        if (statusComboBox != null) {
            Object s = statusComboBox.getSelectedItem();
            if (s != null) {
                String st = s.toString();
                if (!"Tất cả".equalsIgnoreCase(st)) selStatus = st.equalsIgnoreCase("VIP") ? "VIP" : st;
            }
        }

        List<CategoryData> filtered = new ArrayList<>();
        for (CategoryData d : fullDataset) {
            boolean ok = true;
            // apply search type for code/name
            if ("Mã loại phòng".equals(selectedSearch)) {
                if (codeFilter != null) { if (d.code == null || !d.code.toLowerCase().contains(codeFilter)) ok = false; }
            } else if ("Tên loại phòng".equals(selectedSearch)) {
                if (nameFilter != null) { if (d.name == null || !d.name.toLowerCase().contains(nameFilter)) ok = false; }
            } // nếu là "Phân loại" thì selStatus đã cập nhật

            // apply selStatus (if any)
            if (ok && selStatus != null) {
                if (d.type == null || !d.type.equalsIgnoreCase(selStatus)) ok = false;
            }

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
}

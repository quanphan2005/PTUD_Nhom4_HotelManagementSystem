package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.dto.response.ServiceCategoryResponse;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.dialog.ChiTietLoaiDichVuDialog;
import vn.iuh.gui.dialog.SuaLoaiDichVuDialog;
import vn.iuh.gui.dialog.ThemLoaiDichVuDialog;
import vn.iuh.service.ServiceCategoryService;
import vn.iuh.service.impl.ServiceCategoryServiceImpl;
import vn.iuh.service.impl.ServiceImpl;
import vn.iuh.util.AppEventBus;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class QuanLyLoaiDichVuPanel extends JPanel {

    // dùng cùng kích thước với QuanLyDichVuPanel cho nhất quán
    private static final int SEARCH_CONTROL_HEIGHT = 45; // phù hợp với SEARCH_TEXT_SIZE.height (45)

    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(110, SEARCH_CONTROL_HEIGHT);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 50); // giống QuanLyKhachHangPanel
    private static final int TOP_PANEL_HEIGHT = 50;

    private static final Font FONT_LABEL  = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_TITLE  = new Font("Arial", Font.BOLD, 18);

    // Controls for the new search bar
    private JComboBox<String> searchTypeComboBox;      // left: criteria combo (ví dụ "Tên loại dịch vụ")
    private JComboBox<String> categorySearchComboBox;  // center: list of all categories (will expand)
    private final JButton searchButton = new JButton("TÌM"); // right

    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    // Demo data (thực tế thay bằng service/DAO)
    private final java.util.List<LoaiDichVu> categories = new ArrayList<>();
    // map demo: maLoai -> so luong dich vu (bạn gán số thật khi nối DB)
    private final Map<String, Integer> serviceCountMap = new HashMap<>();

    private final ServiceCategoryService categoryService = new ServiceCategoryServiceImpl();
    private QuanLyDichVuPanel servicePanel;


    public QuanLyLoaiDichVuPanel(QuanLyDichVuPanel servicePanel) {
        this.servicePanel = servicePanel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        initSampleData();
        init();

        AppEventBus.subscribe("SERVICE_CHANGED", () -> {
            SwingUtilities.invokeLater(() -> {
                initSampleData();
                rebuildCategoryCombo();
                populateTable(categories);
            });
        });

    }

    private void init() {
        initButtons();

        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndActionPanel();
        add(Box.createVerticalStrut(10));
        createTablePanel();

        populateTable(categories);
    }

    // Simple DTO for UI demo
    private static class LoaiDichVu {
        private String ma;
        private String ten;
        private Date thoiGianTao;
        private boolean daXoa;
        LoaiDichVu(String ma, String ten) { this.ma = ma; this.ten = ten; this.thoiGianTao = new Date(); this.daXoa = false; }
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public Date getThoiGianTao() { return thoiGianTao; }
        public boolean isDaXoa() { return daXoa; }
        public void setTen(String ten) { this.ten = ten; }
        public void setDaXoa(boolean d) { this.daXoa = d; }
        @Override public String toString() { return ten; }
    }

    private void initSampleData() {
        categories.clear();
        serviceCountMap.clear();

        try {
            // gọi service để lấy danh sách loại + số lượng dịch vụ
            List<ServiceCategoryResponse> items = categoryService.getAllServiceCategoriesWithCount();

            if (items == null || items.isEmpty()) {
                // fallback demo cũ nếu DB trống
                categories.add(new LoaiDichVu("LDV00000001", "Chăm sóc cá nhân"));
                categories.add(new LoaiDichVu("LDV00000002", "Ăn uống"));
                categories.add(new LoaiDichVu("LDV00000003", "Giặt ủi"));
                categories.add(new LoaiDichVu("LDV00000004", "Vận chuyển và du lịch"));
                for (LoaiDichVu l : categories) serviceCountMap.put(l.getMa(), 0);
                return;
            }

            // chuyển về model UI
            for (ServiceCategoryResponse it : items) {
                LoaiDichVu l = new LoaiDichVu(it.getMaLoai(), it.getTenLoai());
                categories.add(l);
                serviceCountMap.put(it.getMaLoai(), it.getSoLuong());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            // fallback demo như trên
            categories.add(new LoaiDichVu("LDV00000001", "Chăm sóc cá nhân"));
            categories.add(new LoaiDichVu("LDV00000002", "Ăn uống"));
            categories.add(new LoaiDichVu("LDV00000003", "Giặt ủi"));
            categories.add(new LoaiDichVu("LDV00000004", "Vận chuyển và du lịch"));
            for (LoaiDichVu l : categories) serviceCountMap.put(l.getMa(), 0);
        }
    }


    private void initButtons() {
        // search button
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        addButton = createActionButton("Thêm loại dịch vụ", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton = createActionButton("Sửa loại dịch vụ", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa loại dịch vụ", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        addButton.addActionListener(e -> onAdd());
        editButton.addActionListener(e -> onEdit());
        deleteButton.addActionListener(e -> onDelete());
    }

    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMinimumSize(size);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_LABEL);
        btn.setFocusPainted(false);
        try { btn.setBackground(Color.decode("#1D4ED8")); } catch (Exception ignored) {}
        btn.addActionListener(e -> applyFilters());
    }

    private JButton createActionButton(String text, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFont(FONT_ACTION);
        try { button.setBackground(Color.decode(bgHex)); } catch (Exception ignored) { button.setBackground(new Color(0x888888)); }
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 18; borderWidth: 2; borderColor:" + borderHex);
        return button;
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý loại dịch vụ", SwingConstants.CENTER);
        lblTop.setForeground(Color.WHITE);
        lblTop.setFont(FONT_TITLE);

        pnlTop.setBackground(new Color(59,130,246));
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        add(pnlTop);
    }

    /**
     * Tạo panel tìm kiếm + action tương đồng với QuanLyDichVuPanel về viền và chiều cao thành phần
     */
    private void createSearchAndActionPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBackground(CustomUI.white);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(CustomUI.white);
        // dùng cùng style viền như QuanLyDichVuPanel
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 25));
        searchPanel.setPreferredSize(new Dimension(0, 200));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        // Row 1: xếp ngang - left combo, center expanding combo, right button
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentY(Component.CENTER_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT + 4));
        row1.setPreferredSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT + 4));

        // left: (fixed width) - tiêu chí tìm kiếm (hiện chỉ 1 mục nhưng giữ consistent)
        String[] searchOptions = {"Tên loại dịch vụ"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        Dimension leftComboSize = new Dimension(180, SEARCH_CONTROL_HEIGHT);
        searchTypeComboBox.setPreferredSize(leftComboSize);
        searchTypeComboBox.setMinimumSize(leftComboSize);
        searchTypeComboBox.setMaximumSize(leftComboSize);
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // center: category combo (chiều cao đồng bộ với left combo và nút)
        categorySearchComboBox = new JComboBox<>();
        rebuildCategoryCombo();
        categorySearchComboBox.setFont(FONT_LABEL);
        categorySearchComboBox.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        // fix height, allow width expand
        categorySearchComboBox.setPreferredSize(new Dimension(10, SEARCH_CONTROL_HEIGHT));
        categorySearchComboBox.setMinimumSize(new Dimension(10, SEARCH_CONTROL_HEIGHT));
        categorySearchComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        categorySearchComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // right: search button (height matches controls)
        searchButton.setPreferredSize(SEARCH_BUTTON_SIZE);
        searchButton.setMaximumSize(SEARCH_BUTTON_SIZE);
        searchButton.setMinimumSize(SEARCH_BUTTON_SIZE);
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        // assemble row1 with spacing similar to QuanLyDichVuPanel
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(categorySearchComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(searchButton);

        searchPanel.add(Box.createVerticalStrut(8));
        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(12));

        // Row 2: action buttons centered
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        row2.setBackground(CustomUI.white);
        addButton.setPreferredSize(ACTION_BUTTON_SIZE);
        editButton.setPreferredSize(ACTION_BUTTON_SIZE);
        deleteButton.setPreferredSize(ACTION_BUTTON_SIZE);
        row2.add(addButton);
        row2.add(editButton);
        row2.add(deleteButton);

        searchPanel.add(row2);
        searchPanel.add(Box.createVerticalStrut(8));

        // events
        categorySearchComboBox.addActionListener(e -> applyFilters());
        searchTypeComboBox.addActionListener(e -> applyFilters());

        container.add(searchPanel);
        add(container);
    }

    private void rebuildCategoryCombo() {
        if (categorySearchComboBox == null) return;
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        m.addElement("Chọn loại");
        for (LoaiDichVu l : categories) m.addElement(l.getTen());
        categorySearchComboBox.setModel(m);
    }

    private void createTablePanel() {
        // Cột: Mã loại | Tên loại | Số lượng dịch vụ | OBJ(hidden)
        String[] cols = {"Mã loại", "Tên loại", "Số lượng dịch vụ", "OBJ"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);
                if (isRowSelected(row)) {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                    c.setForeground(CustomUI.black);
                } else {
                    c.setBackground(row % 2 == 0 ? (CustomUI.ROW_EVEN != null ? CustomUI.ROW_EVEN : Color.WHITE) : (CustomUI.ROW_ODD != null ? CustomUI.ROW_ODD : new Color(0xF7F9FB)));
                    c.setForeground(CustomUI.black);
                }
                if (c instanceof JLabel) ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                return c;
            }
        };

        table.setRowHeight(48);
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 40));
        table.getTableHeader().setFont(CustomUI.HEADER_FONT);
        table.getTableHeader().setBackground(CustomUI.blue);
        table.getTableHeader().setForeground(CustomUI.white);
        table.getTableHeader().setOpaque(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                comp.setFont(CustomUI.TABLE_FONT);
                return comp;
            }
        });

        // double click -> show simple detail dialog (cột OBJ chứa object)
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        Object obj = tableModel.getValueAt(modelRow, 3);
                        if (obj instanceof LoaiDichVu) {
                            LoaiDichVu l = (LoaiDichVu) obj;
                            int cnt = serviceCountMap.getOrDefault(l.getMa(), 0);
                            // truyền một instance ServiceService (nếu bạn có sẵn, dùng instance đó)
                            ChiTietLoaiDichVuDialog dlg = new ChiTietLoaiDichVuDialog(
                                    SwingUtilities.getWindowAncestor(QuanLyLoaiDichVuPanel.this),
                                    l.getMa(),
                                    l.getTen(),
                                    cnt,
                                    new ServiceImpl() // hoặc truyền service từ panel nếu có
                            );
                            dlg.setVisible(true);
                        }

                    }
                }
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = table.getWidth();
                TableColumnModel columnModel = table.getColumnModel();
                if (columnModel.getColumnCount() < 4) return;
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.14)); // mã
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.62)); // tên
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.12)); // số lượng
                columnModel.getColumn(3).setMinWidth(0); // OBJ hidden
                columnModel.getColumn(3).setMaxWidth(0);
                columnModel.getColumn(3).setPreferredWidth(0);
                columnModel.getColumn(3).setResizable(false);
            }
        });

        JScrollPane scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(0, 500));
        add(scroll);
    }

    private void populateTable(List<LoaiDichVu> list) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (list == null || list.isEmpty()) {
                tableModel.addRow(new Object[]{"-", "Không có loại dịch vụ.", "-", null});
                return;
            }
            for (LoaiDichVu l : list) {
                Object[] row = new Object[4];
                row[0] = l.getMa();
                row[1] = l.getTen();
                Integer cnt = serviceCountMap.getOrDefault(l.getMa(), 0);
                row[2] = cnt;
                row[3] = l; // hidden object
                tableModel.addRow(row);
            }
        });
    }

    private void onAdd() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ThemLoaiDichVuDialog dlg = new ThemLoaiDichVuDialog(owner, categoryService, () -> {
            // reload UI panel loại
            initSampleData();
            rebuildCategoryCombo();
            populateTable(categories);

            // nếu có tham chiếu tới panel dịch vụ, refresh combobox của nó
            if (servicePanel != null) {
                servicePanel.rebuildCategorySearchCombo();
            }
        });
        dlg.setVisible(true);
    }


    private void onEdit() {
        LoaiDichVu sel = getSelected();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 loại để sửa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Disable UI controls while checking and set wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        addButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // run DB checks in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Exception error = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    ServiceImpl svc = new ServiceImpl();
                    // lấy tất cả dịch vụ cùng giá (DAO trả ServiceResponse có maLoai)
                    List<ServiceResponse> all = svc.layTatCaDichVuCungGia();
                    if (all == null || all.isEmpty()) return true; // không có dịch vụ => ok

                    // kiểm tra từng dịch vụ thuộc loại sel
                    for (ServiceResponse sr : all) {
                        if (sr == null) continue;
                        String maLoaiDv = sr.getMaLoaiDichVu();
                        String maDv = sr.getMaDichVu();
                        if (maLoaiDv != null && maLoaiDv.equals(sel.getMa())) {
                            // nếu có đang dùng => không cho sửa
                            boolean inUse = svc.isServiceCurrentlyUsed(maDv);
                            if (inUse) return false;
                        }
                    }
                    return true; // tất cả dịch vụ thuộc loại đều không dùng
                } catch (Exception ex) {
                    error = ex;
                    return null;
                }
            }

            @Override
            protected void done() {
                // restore UI state
                setCursor(Cursor.getDefaultCursor());
                addButton.setEnabled(true);
                editButton.setEnabled(true);
                deleteButton.setEnabled(true);

                if (error != null) {
                    error.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyLoaiDichVuPanel.this,
                            "Lỗi khi kiểm tra trạng thái dịch vụ: " + error.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Boolean ok;
                try {
                    ok = get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyLoaiDichVuPanel.this,
                            "Lỗi nội bộ: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (ok == null) {
                    JOptionPane.showMessageDialog(QuanLyLoaiDichVuPanel.this,
                            "Không thể kiểm tra trạng thái dịch vụ (kết quả rỗng).",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!ok) {
                    // Có ít nhất 1 dịch vụ đang dùng -> không cho sửa
                    JOptionPane.showMessageDialog(QuanLyLoaiDichVuPanel.this,
                            "Không thể sửa loại này vì có ít nhất một dịch vụ thuộc loại đang được sử dụng.",
                            "Không thể sửa",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Nếu tới đây: tất cả dịch vụ thuộc loại đều không được sử dụng -> mở dialog sửa loại
                Window owner = SwingUtilities.getWindowAncestor(QuanLyLoaiDichVuPanel.this);
                SuaLoaiDichVuDialog dlg = new SuaLoaiDichVuDialog(
                        owner,
                        sel.getMa(),
                        sel.getTen(),
                        categoryService,
                        () -> {
                            // callback onSaved: reload panel loại + panel dịch vụ (nếu tham chiếu có)
                            // reload panel loại
                            initSampleData();
                            rebuildCategoryCombo();
                            populateTable(categories);

                            // reload panel dịch vụ nếu tham chiếu
                            if (servicePanel != null) {
                                servicePanel.reloadAllData();
                            }
                        },
                        Main.getCurrentLoginSession()
                );
                dlg.setVisible(true);
            }
        };

        worker.execute();
    }

    private void onDelete() {
        LoaiDichVu sel = getSelected();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 loại để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ans = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa loại " + sel.getTen() + " không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;

        try {
            // 1) Lấy danh sách tất cả dịch vụ (cùng giá) từ ServiceImpl/ServiceService
            ServiceImpl service = new ServiceImpl();
            List<ServiceResponse> all = service.layTatCaDichVuCungGia();

            // 2) Kiểm tra xem có dịch vụ nào thuộc loại sel hay không
            boolean hasAnyInCategory = false;
            if (all != null) {
                for (ServiceResponse sr : all) {
                    if (sr != null && sel.getMa().equals(sr.getMaLoaiDichVu())) {
                        hasAnyInCategory = true;
                        break;
                    }
                }
            }

            if (hasAnyInCategory) {
                JOptionPane.showMessageDialog(this,
                        "Không thể xóa loại dịch vụ vì vẫn còn dịch vụ thuộc loại này.",
                        "Không thể xóa",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3) Nếu không có dịch vụ nào trong loại -> gọi service để xóa
            boolean ok = false;
            try {
                ok = categoryService.deleteServiceCategoryV2(sel.getMa());
            } catch (IllegalStateException ise) {
                // nếu service impl ném (ví dụ do ràng buộc khác), hiển thị thông báo
                JOptionPane.showMessageDialog(this, ise.getMessage(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa loại dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (ok) {
                JOptionPane.showMessageDialog(this, "Xóa loại dịch vụ thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // reload UI panel loại
                initSampleData();
                rebuildCategoryCombo();
                populateTable(categories);

                // reload service panel (nếu có tham chiếu)
                if (servicePanel != null) servicePanel.reloadAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra/xóa loại dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LoaiDichVu getSelected() {
        int view = table.getSelectedRow();
        if (view < 0) return null;
        int modelRow = table.convertRowIndexToModel(view);
        Object obj = tableModel.getValueAt(modelRow, 3);
        if (obj instanceof LoaiDichVu) return (LoaiDichVu) obj;
        return null;
    }

    private void applyFilters() {
        String sel = (categorySearchComboBox != null) ? (String) categorySearchComboBox.getSelectedItem() : null;
        List<LoaiDichVu> out = new ArrayList<>();
        if (sel == null || sel.equals("Chọn loại")) {
            out.addAll(categories);
        } else {
            String selLower = sel.toLowerCase();
            for (LoaiDichVu l : categories) {
                if (l.getTen() != null && l.getTen().toLowerCase().contains(selLower)) out.add(l);
            }
        }
        populateTable(out);
    }

    // --- public helper so bạn có thể gán số lượng dịch vụ từ DAO bên ngoài ---
    public void setServiceCount(String maLoai, int count) {
        serviceCountMap.put(maLoai, count);
        populateTable(categories);
    }
}

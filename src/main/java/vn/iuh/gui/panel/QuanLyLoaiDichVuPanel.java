package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * QuanLyLoaiDichVuPanel - cập nhật UI theo yêu cầu:
 * - Thanh tìm kiếm tương tự QuanLyDichVuPanel (viền, chiều cao, nút/combobox đồng bộ)
 * - Bảng hiển thị: Mã loại | Tên loại | Số lượng dịch vụ
 *
 * NOTE: UI-only. serviceCountMap để map số lượng dịch vụ (set sau khi tích hợp DB).
 */
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

    public QuanLyLoaiDichVuPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        initSampleData();
        init();
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
        categories.add(new LoaiDichVu("LDV00000001", "Chăm sóc cá nhân"));
        categories.add(new LoaiDichVu("LDV00000002", "Ăn uống"));
        categories.add(new LoaiDichVu("LDV00000003", "Giặt ủi"));
        categories.add(new LoaiDichVu("LDV00000004", "Vận chuyển và du lịch"));

        // demo counts (mặc định)
        serviceCountMap.clear();
        for (LoaiDichVu l : categories) serviceCountMap.put(l.getMa(), 0);
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
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            JOptionPane.showMessageDialog(QuanLyLoaiDichVuPanel.this,
                                    "Mã: " + l.getMa() + "\nTên: " + l.getTen() + "\nTạo: " + df.format(l.getThoiGianTao()),
                                    "Chi tiết loại dịch vụ", JOptionPane.INFORMATION_MESSAGE);
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
        JTextField tfMa = new JTextField("LDV" + String.format("%08d", categories.size() + 1));
        JTextField tfTen = new JTextField();
        Object[] inputs = {"Mã loại:", tfMa, "Tên loại:", tfTen};
        int ans = JOptionPane.showConfirmDialog(this, inputs, "Thêm loại dịch vụ", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) return;
        String ma = tfMa.getText().trim();
        String ten = tfTen.getText().trim();
        if (ten.isEmpty()) ten = "(chưa đặt tên)";
        LoaiDichVu l = new LoaiDichVu(ma, ten);
        categories.add(l);
        // khi thêm, set count mặc định 0 — bạn sẽ cập nhật từ DB sau
        serviceCountMap.put(ma, 0);
        rebuildCategoryCombo();
        populateTable(categories);
    }

    private void onEdit() {
        LoaiDichVu sel = getSelected();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 loại để sửa", "Thông báo", JOptionPane.INFORMATION_MESSAGE); return; }
        JTextField tfTen = new JTextField(sel.getTen());
        Object[] inputs = {"Mã (không đổi):", new JLabel(sel.getMa()), "Tên loại:", tfTen};
        int ans = JOptionPane.showConfirmDialog(this, inputs, "Sửa loại dịch vụ", JOptionPane.OK_CANCEL_OPTION);
        if (ans != JOptionPane.OK_OPTION) return;
        sel.setTen(tfTen.getText().trim());
        rebuildCategoryCombo();
        populateTable(categories);
    }

    private void onDelete() {
        LoaiDichVu sel = getSelected();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 loại để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE); return; }
        int ans = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa loại " + sel.getTen() + " không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;
        categories.remove(sel);
        serviceCountMap.remove(sel.getMa());
        rebuildCategoryCombo();
        populateTable(categories);
    }

    private LoaiDichVu getSelected() {
        int view = table.getSelectedRow();
        if (view < 0) return null;
        int modelRow = table.convertRowIndexToModel(view);
        Object obj = tableModel.getValueAt(modelRow, 3);
        if (obj instanceof LoaiDichVu) return (LoaiDichVu) obj;
        return null;
    }

    /**
     * Filter logic:
     * - If user selects a type in the center combo (not "Chọn loại") => filter by that name
     * - If "Chọn loại" selected => show all
     */
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

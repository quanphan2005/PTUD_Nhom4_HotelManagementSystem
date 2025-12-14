package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.dto.response.ServiceResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.ChiTietDichVuDialog;
import vn.iuh.gui.dialog.SuaDichVuDialog;
import vn.iuh.gui.dialog.ThemDichVuDialog;
import vn.iuh.service.ServiceService;
import vn.iuh.service.impl.ServiceImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Objects;

/**
 * QuanLyDichVuPanel - sửa phần search giống QuanLyKhachHangPanel
 *
 * Thay đổi chính:
 * - Kích thước nút hành động giống QuanLyKhachHangPanel (290x50)
 * - Khi chọn "Loại dịch vụ" thì hiển thị combobox chứa các loại hiện có để tìm nhanh
 */
public class QuanLyDichVuPanel extends JPanel {

    // Kích thước / font reuse (tương tự QuanLyPhongPanel / QuanLyKhachHangPanel)
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, 45);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    // =========== thay đổi: giống QuanLyKhachHangPanel ===========
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 50);
    // ===========================================================
    private static final int TOP_PANEL_HEIGHT = 50;

    private static final Font FONT_LABEL    = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION   = new Font("Arial", Font.BOLD, 18);
    private static final Font FONT_TITLE    = new Font("Arial", Font.BOLD, 30);

    // Search controls (đã thay đổi)
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JComboBox<String> searchTypeComboBox;
    // combo dùng khi tìm theo loại
    private JComboBox<String> categorySearchComboBox;

    // Action buttons
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;

    // Table
    private JTable serviceTable;
    private DefaultTableModel serviceTableModel;

    // Demo data (inner classes below)
    private final java.util.List<LoaiDichVu> categories = new ArrayList<>();
    private final java.util.List<DichVu> services = new ArrayList<>();

    private final ServiceService serviceService = new ServiceImpl();

    public QuanLyDichVuPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        initSampleData();
        init();
    }

    private void init() {
        initButtons();

        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndActionPanel(); // mới: combine search + actions into one area
        add(Box.createVerticalStrut(10));

        createServiceTablePanel();

        // initial populate
        populateServiceList(services);
    }

    // ------------------- demo data models (simple POJOs) -------------------
    private static class LoaiDichVu {
        private final String ma;
        private final String ten;
        public LoaiDichVu(String ma, String ten) { this.ma = ma; this.ten = ten; }
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        @Override public String toString() { return ten; }
    }

    private static class DichVu {
        private String ma;
        private String ten;
        private int tonKho;
        private String maLoai;
        private double gia;

        public DichVu(String ma, String ten, int tonKho, String maLoai, double gia) {
            this.ma = ma; this.ten = ten; this.tonKho = tonKho; this.maLoai = maLoai; this.gia = gia;
        }
        public String getMa() { return ma; }
        public String getTen() { return ten; }
        public int getTonKho() { return tonKho; }
        public String getMaLoai() { return maLoai; }
        public double getGia() { return gia; }

        public void setTen(String ten) { this.ten = ten; }
        public void setTonKho(int tonKho) { this.tonKho = tonKho; }
        public void setMaLoai(String maLoai) { this.maLoai = maLoai; }
        public void setGia(double gia) { this.gia = gia; }
    }
    // -----------------------------------------------------------------------

    private void initSampleData() {
        categories.clear();
        services.clear();

        try {
            // 1) lấy danh sách dịch vụ + giá từ service (service gọi DAO)
            List<ServiceResponse> items = serviceService.layTatCaDichVuCungGia();

            // 2) lấy map ma_loai -> ten_loai từ service (service gọi LoaiDichVuDAO)
            Map<String, String> maThanhTen = serviceService.layMapMaThanhTenLoaiDichVu();
            if (maThanhTen == null) maThanhTen = new java.util.LinkedHashMap<>();

            // 3) build categories (unique) + services GUI model
            java.util.Set<String> seen = new java.util.LinkedHashSet<>();
            if (items != null) {
                for (ServiceResponse it : items) {
                    String maLoai = it.getMaLoaiDichVu();
                    String tenLoai = maThanhTen.get(maLoai);
                    if (maLoai != null && !seen.contains(maLoai)) {
                        seen.add(maLoai);
                        categories.add(new LoaiDichVu(maLoai, tenLoai != null ? tenLoai : maLoai));
                    }
                    double gia = it.getGiaHienTai() != null ? it.getGiaHienTai() : 0.0;
                    services.add(new DichVu(it.getMaDichVu(), it.getTenDichVu(), it.getTonKho(), it.getMaLoaiDichVu(), gia));
                }
            }

            // fallback nếu không có dữ liệu
            if (services.isEmpty()) {
                // giữ demo cũ (tùy bạn)
                initDemoFallback();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            initDemoFallback();
        }
    }

    // helper fallback (cũ) — bạn có thể giữ hoặc xóa
    private void initDemoFallback() {
        categories.clear();
        services.clear();
        categories.add(new LoaiDichVu("LDV00000001", "Chăm sóc cá nhân"));
        categories.add(new LoaiDichVu("LDV00000002", "Ăn uống"));
        categories.add(new LoaiDichVu("LDV00000003", "Giặt ủi"));
        categories.add(new LoaiDichVu("LDV00000004", "Vận chuyển & du lịch"));

        services.add(new DichVu("DV00000001", "Gội đầu", 0, "LDV00000001", 250000));
        services.add(new DichVu("DV00000002", "Massage", 0, "LDV00000001", 600000));
        services.add(new DichVu("DV00000003", "Ăn sáng", 100, "LDV00000002", 120000));
        services.add(new DichVu("DV00000004", "Ăn trưa", 100, "LDV00000002", 250000));
        services.add(new DichVu("DV00000005", "Ăn tối", 100, "LDV00000002", 250000));
        services.add(new DichVu("DV00000006", "Giặt ủi", 50, "LDV00000003", 180000));
        services.add(new DichVu("DV00000007", "Đưa đón sân bay", 0, "LDV00000004", 350000));
        services.add(new DichVu("DV00000008", "Tour du lịch", 0, "LDV00000004", 1200000));
    }


    private void initButtons() {
        // configure search field and button (placeholder: Tên dịch vụ)
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Tên dịch vụ");
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        addButton = createActionButton("Thêm dịch vụ", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton = createActionButton("Sửa dịch vụ", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa dịch vụ", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        // actions (demo)
        addButton.addActionListener(e -> onAddService());
        editButton.addActionListener(e -> onEditService());
        deleteButton.addActionListener(e -> onDeleteService());
    }

    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMaximumSize(size);
        field.setMinimumSize(size);
        field.setFont(FONT_LABEL); // bold as requested
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (Objects.equals(field.getText(), placeholder)) { field.setText(""); field.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(placeholder); }
            }
        });

        // realtime filter when user types
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { applyFilters(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
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
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 20; borderWidth: 2; borderColor:" + borderHex);
        return button;
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý dịch vụ", SwingConstants.CENTER);
        lblTop.setForeground(Color.WHITE);
        lblTop.setFont(FONT_TITLE);

        pnlTop.setBackground(new Color(59,130,246)); // fallback blue
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        add(pnlTop);
    }

    /**
     * Tạo panel tìm kiếm + action giống phong cách QuanLyKhachHangPanel:
     * - Combo chọn kiểu tìm (Tên dịch vụ / Loại dịch vụ)
     * - Khi "Loại dịch vụ" được chọn -> hiển thị combobox loại để tìm
     * - TextField (placeholder bold) + nút TÌM
     * - Hàng nút hành động (Thêm / Sửa / Xóa) căn giữa
     */
    private void createSearchAndActionPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBackground(CustomUI.white);

        // search panel left
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 25));
        searchPanel.setPreferredSize(new Dimension(0, 200));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        // search type combo (Tên dịch vụ / Loại dịch vụ)
        String[] searchOptions = {"Tên dịch vụ", "Loại dịch vụ"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        Dimension comboSize = new Dimension(180, 45);
        searchTypeComboBox.setPreferredSize(comboSize);
        searchTypeComboBox.setMinimumSize(comboSize);
        searchTypeComboBox.setMaximumSize(comboSize);
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Input panel with CardLayout: text field OR category combobox
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel(new CardLayout());
        inputPanel.setPreferredSize(SEARCH_TEXT_SIZE);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_TEXT_SIZE.height));

        // ensure searchTextField placeholder is "Tên dịch vụ"
        // (already configured in initButtons)
        searchTextField.setPreferredSize(SEARCH_TEXT_SIZE);
        searchTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_TEXT_SIZE.height));
        searchTextField.setAlignmentY(Component.CENTER_ALIGNMENT);

        // categorySearchComboBox for quick find when "Loại dịch vụ" selected
        categorySearchComboBox = new JComboBox<>();
        rebuildCategorySearchCombo(); // fill values from categories
        categorySearchComboBox.setPreferredSize(new Dimension(SEARCH_TEXT_SIZE.width, SEARCH_TEXT_SIZE.height));
        categorySearchComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_TEXT_SIZE.height));
        categorySearchComboBox.setFont(FONT_LABEL);
        categorySearchComboBox.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        inputPanel.add(searchTextField, "TEXT");
        inputPanel.add(categorySearchComboBox, "CATEGORY");

        // default show TEXT
        CardLayout cl = (CardLayout) inputPanel.getLayout();
        cl.show(inputPanel, "TEXT");

        // assemble row: combo + input + button
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(12));
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);
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

        // Behavior: switch input when searchType changed
        searchTypeComboBox.addActionListener(e -> {
            String sel = (String) searchTypeComboBox.getSelectedItem();
            CardLayout card = (CardLayout) inputPanel.getLayout();
            if ("Loại dịch vụ".equals(sel)) {
                // reload latest categories from DB each time user chọn chế độ Loại
                rebuildCategorySearchCombo();
                card.show(inputPanel, "CATEGORY");
                // reset combo selection to "Chọn loại" to show all
                if (categorySearchComboBox.getItemCount() > 0) {
                    categorySearchComboBox.setSelectedIndex(0);
                }
                applyFilters(); // update view according to selection
            } else {
                card.show(inputPanel, "TEXT");
                applyFilters();
            }
        });


        // when user types in textfield -> realtime filter (already wired by configureSearchTextField)
        // when user selects a category -> filter by that category
        categorySearchComboBox.addActionListener(e -> {
            // only react if currently in CATEGORY mode
            if (!"Loại dịch vụ".equals((String) searchTypeComboBox.getSelectedItem())) return;
            applyFilters();
        });

        // search button triggers applyFilters() (already wired)

        container.add(searchPanel);
        add(container);
    }

    // thay thế phương thức cũ bằng phương thức này và đổi thành public
    public void rebuildCategorySearchCombo() {
        SwingUtilities.invokeLater(() -> {
            DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
            m.addElement("Chọn loại");

            try {
                // lấy map ma -> ten từ DB (service gọi DAO)
                Map<String, String> maThanhTen = serviceService.layMapMaThanhTenLoaiDichVu();
                // clear cũ và build lại danh sách nội bộ categories để map name->id khi cần
                categories.clear();
                if (maThanhTen != null && !maThanhTen.isEmpty()) {
                    for (Map.Entry<String, String> en : maThanhTen.entrySet()) {
                        String ma = en.getKey();
                        String ten = en.getValue() != null ? en.getValue() : ma;
                        m.addElement(ten);
                        categories.add(new LoaiDichVu(ma, ten));
                    }
                } else {
                    // fallback: nếu DB trả về rỗng thì giữ categories cũ (nếu có) hoặc demo fallback
                    for (LoaiDichVu l : categories) m.addElement(l.getTen());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // nếu lỗi DB thì vẫn giữ dữ liệu cũ (hoặc demo)
                for (LoaiDichVu l : categories) m.addElement(l.getTen());
            }

            categorySearchComboBox.setModel(m);
            // mặc định chọn "Chọn loại"
            if (categorySearchComboBox.getItemCount() > 0) categorySearchComboBox.setSelectedIndex(0);
        });
    }

    private void createServiceTablePanel() {
        String[] columnNames = {"Mã dịch vụ", "Tên dịch vụ", "Tồn kho", "Loại", "Giá", "OBJ"};
        serviceTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        serviceTable = new JTable(serviceTableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(new Font("Arial", Font.PLAIN, 14));
                if (isRowSelected(row)) {
                    c.setBackground(new Color(0xE6F0FF)); // selected-like
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7F9FB));
                    c.setForeground(Color.BLACK);
                }
                if (c instanceof JLabel) ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, new Color(0xE5E7EB)));
                return c;
            }
        };

        serviceTable.setRowHeight(48);
        serviceTable.getTableHeader().setPreferredSize(new Dimension(serviceTable.getWidth(), 40));
        serviceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        serviceTable.getTableHeader().setBackground(new Color(59,130,246));
        serviceTable.getTableHeader().setForeground(Color.WHITE);
        serviceTable.getTableHeader().setOpaque(true);

        serviceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, new Color(0xE5E7EB)));
                comp.setFont(new Font("Arial", Font.PLAIN, 14));
                return comp;
            }
        });

        // double click -> show details (simple dialog)
        serviceTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int viewRow = serviceTable.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        int modelRow = serviceTable.convertRowIndexToModel(viewRow);
                        Object obj = serviceTableModel.getValueAt(modelRow, 5); // hidden OBJ
                        if (obj instanceof DichVu) {
                            DichVu d = (DichVu) obj;
                            ServiceResponse sr = new ServiceResponse();
                            // set fields — giả sử ServiceResponse có setters hoặc một constructor; nếu không, bạn có thể sửa dialog để nhận ma/ten/ton/gia trực tiếp
                            sr.setMaDichVu(d.getMa());
                            sr.setTenDichVu(d.getTen());
                            sr.setTonKho(d.getTonKho());
                            sr.setMaLoaiDichVu(d.getMaLoai());
                            sr.setGiaHienTai(d.getGia());

                            // Show dialog (this is a Component within a Window)
                            Window w = SwingUtilities.getWindowAncestor(QuanLyDichVuPanel.this);
                            ChiTietDichVuDialog dialog = new ChiTietDichVuDialog(w, sr, serviceService);
                            dialog.setVisible(true);
                        }
                    }
                }
            }
        });

        serviceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        serviceTable.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int tableWidth = serviceTable.getWidth();
                TableColumnModel columnModel = serviceTable.getColumnModel();
                if (columnModel.getColumnCount() < 6) return;
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.12)); // mã
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.34)); // tên
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.10)); // tồn kho
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.18)); // loại
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.20)); // giá
                // cột OBJ ẩn
                columnModel.getColumn(5).setMinWidth(0);
                columnModel.getColumn(5).setMaxWidth(0);
                columnModel.getColumn(5).setPreferredWidth(0);
                columnModel.getColumn(5).setResizable(false);
            }
        });

        JScrollPane scrollPane = new JScrollPane(serviceTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        add(scrollPane);
    }

    // ------------------ populate / helpers ------------------
    private void populateServiceList(List<DichVu> list) {
        SwingUtilities.invokeLater(() -> {
            serviceTableModel.setRowCount(0);
            if (list != null && !list.isEmpty()) {
                for (DichVu d : list) {
                    Object[] row = new Object[6];
                    row[0] = d.getMa();
                    row[1] = d.getTen();
                    row[2] = d.getTonKho();
                    row[3] = findLoaiNameByMa(d.getMaLoai());
                    row[4] = formatPrice(d.getGia());
                    row[5] = d; // hidden
                    serviceTableModel.addRow(row);
                }
            } else {
                serviceTableModel.addRow(new Object[] {"-", "Không tìm thấy dịch vụ phù hợp.", "-", "-", "-", null});
            }
        });
    }

    private String findLoaiNameByMa(String ma) {
        if (ma == null) return "";
        for (LoaiDichVu l : categories) if (l.getMa().equals(ma)) return l.getTen();
        return "";
    }

    private String findLoaiMaByName(String name) {
        if (name == null) return null;
        for (LoaiDichVu l : categories) if (l.getTen().equals(name)) return l.getMa();
        return null;
    }

    private static String formatPrice(double price) {
        if (price <= 0.0) return "0 VNĐ";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " VNĐ";
    }

    private void onAddService() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        // truyền serviceService đã có sẵn, maPhienDangNhap hiện tại bạn cần truyền (nếu có) - tạm truyền null
        ThemDichVuDialog dialog = new ThemDichVuDialog(owner, serviceService, () -> {
            // reload data khi thành công
            initSampleData();
            rebuildCategorySearchCombo();
            populateServiceList(services);
        });
        dialog.setVisible(true);
    }


    private void onEditService() {
        DichVu sel = getSelectedDichVu();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dịch vụ để sửa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // --- mới: kiểm tra service có đang được sử dụng không ---
        try {
            boolean inUse = serviceService.isServiceCurrentlyUsed(sel.getMa());
            if (inUse) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể sửa thông tin dịch vụ do dịch vụ này đang được sử dụng",
                        "Không thể sửa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi kiểm tra trạng thái dịch vụ: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // map GUI.DichVu -> ServiceResponse
        ServiceResponse sr = new ServiceResponse();
        sr.setMaDichVu(sel.getMa());
        sr.setTenDichVu(sel.getTen());
        sr.setTonKho(sel.getTonKho());
        sr.setMaLoaiDichVu(sel.getMaLoai());
        sr.setGiaHienTai(sel.getGia());
        // mở dialog
        Window w = SwingUtilities.getWindowAncestor(this);
        SuaDichVuDialog dlg = new SuaDichVuDialog(w, sr, serviceService, () -> {
            initSampleData();
            rebuildCategorySearchCombo();
            populateServiceList(services);
        });
        dlg.setVisible(true);
    }

    private void onDeleteService() {
        DichVu sel = getSelectedDichVu();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dịch vụ để xóa", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 1) kiểm tra dịch vụ có đang được sử dụng không (service layer)
        try {
            boolean inUse = serviceService.isServiceCurrentlyUsed(sel.getMa());
            if (inUse) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể xóa dịch vụ do dịch vụ này đang được sử dụng",
                        "Không thể xóa",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi kiểm tra trạng thái dịch vụ: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 2) xác nhận xóa
        int ans = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dịch vụ " + sel.getTen() + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;

        // 3) gọi service để xóa (transaction + log)
        try {
            boolean ok = serviceService.xoaDichVu(sel.getMa());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // reload dữ liệu
                initSampleData();
                rebuildCategorySearchCombo();
                populateServiceList(services);
            } else {
                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thất bại", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalStateException ise) {
            // trường hợp service.impl cũng kiểm tra và ném IllegalStateException
            JOptionPane.showMessageDialog(this, ise.getMessage(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa dịch vụ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DichVu getSelectedDichVu() {
        int viewRow = serviceTable.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = serviceTable.convertRowIndexToModel(viewRow);
        Object obj = serviceTableModel.getValueAt(modelRow, 5);
        if (obj instanceof DichVu) return (DichVu) obj;
        return null;
    }

    private void showServiceDetailDialog(DichVu d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mã: ").append(d.getMa()).append("\n");
        sb.append("Tên: ").append(d.getTen()).append("\n");
        sb.append("Tồn kho: ").append(d.getTonKho()).append("\n");
        sb.append("Loại: ").append(findLoaiNameByMa(d.getMaLoai())).append("\n");
        sb.append("Giá: ").append(formatPrice(d.getGia())).append("\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Chi tiết dịch vụ", JOptionPane.INFORMATION_MESSAGE);
    }

    private String[] getCategoryNames() {
        List<String> out = new ArrayList<>();
        for (LoaiDichVu l : categories) out.add(l.getTen());
        return out.toArray(new String[0]);
    }

    private int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception ex) { return fallback; }
    }
    private double tryParseDouble(String s, double fallback) {
        try { return Double.parseDouble(s); } catch (Exception ex) { return fallback; }
    }

    // ------------------ Search/filter logic ------------------
    private void applyFilters() {
        String mode = (searchTypeComboBox != null) ? (String) searchTypeComboBox.getSelectedItem() : "Tên dịch vụ";

        // If mode is category and categorySearchComboBox exists, use its selection
        List<DichVu> filtered = new ArrayList<>();
        if ("Loại dịch vụ".equals(mode) && categorySearchComboBox != null) {
            String sel = (String) categorySearchComboBox.getSelectedItem();
            if (sel == null || sel.equals("Chọn loại")) {
                filtered.addAll(services);
            } else {
                String selLower = sel.toLowerCase();
                for (DichVu d : services) {
                    String loaiName = findLoaiNameByMa(d.getMaLoai());
                    if (loaiName != null && loaiName.toLowerCase().contains(selLower)) filtered.add(d);
                }
            }
            populateServiceList(filtered);
            return;
        }

        // otherwise (Tên dịch vụ)
        String q = searchTextField.getText();
        if (q == null) q = "";
        boolean isPlaceholder = searchTextField.getForeground().equals(Color.GRAY) && Objects.equals(q, "Tên dịch vụ");
        String query = (!isPlaceholder) ? q.trim().toLowerCase() : "";

        if (query.isEmpty()) {
            filtered.addAll(services);
        } else {
            for (DichVu d : services) {
                if (d.getTen() != null && d.getTen().toLowerCase().contains(query)) filtered.add(d);
            }
        }
        populateServiceList(filtered);
    }

    public void reloadAllData() {
        // đảm bảo chạy trên EDT
        SwingUtilities.invokeLater(() -> {
            initSampleData();              // gọi lại dữ liệu từ service/DAO
            rebuildCategorySearchCombo();  // cập nhật combobox loại trong panel dịch vụ
            populateServiceList(services); // fill lại bảng
        });
    }
}

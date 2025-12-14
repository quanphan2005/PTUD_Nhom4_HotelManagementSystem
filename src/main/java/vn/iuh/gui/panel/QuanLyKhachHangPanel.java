package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.SuaKhachHangDialog;
import vn.iuh.gui.dialog.ThemKhachHangDialog;
import vn.iuh.gui.dialog.ThongTinKhachHangDialog;
import vn.iuh.service.impl.CustomerServiceImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * QuanLyKhachHangPanel (đã tối giản: loại bỏ hoàn toàn icon, đồng bộ style bảng,
 * tìm kiếm theo Tên / CCCD, table đặt tương tự QuanLyLoaiPhongPanel, nội dung bảng không in đậm)
 *
 * Lưu ý: toàn bộ text trong phần search panel (combo, placeholder, nút) là BOLD.
 *       Table sử dụng CustomUI.TABLE_FONT (không in đậm).
 */
public class QuanLyKhachHangPanel extends JPanel {

    // Chuẩn hoá chiều cao cho các control tìm kiếm
    private static final int SEARCH_CONTROL_HEIGHT = 40; // chiều cao cố định cho combo, textfield, button
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, SEARCH_CONTROL_HEIGHT);

    // Kích thước cụ thể (width) cho combo và nút tìm — text field
    private static final int SEARCH_TYPE_WIDTH = 180;
    private static final int SEARCH_BUTTON_WIDTH = 110;
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(SEARCH_BUTTON_WIDTH, SEARCH_CONTROL_HEIGHT);

    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 50);

    // Top panel height
    private static final int TOP_PANEL_HEIGHT = 40;

    // Fonts & Colors tái sử dụng
    // FONT_LABEL sử dụng BOLD để đảm bảo phần search panel in đậm
    private static final Font FONT_LABEL  = new Font("Arial", Font.BOLD, 14); // BOLD for search panel
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);

    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

    private JComboBox<String> searchTypeComboBox;

    // Table + model
    private JTable table;
    private DefaultTableModel tableModel;

    // In-memory dataset (sample data)
    private final List<CustomerData> fullDataset = new ArrayList<>();

    private final CustomerServiceImpl customerService = new CustomerServiceImpl();
    public QuanLyKhachHangPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
    }

    private void init() {
        initButtons();
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndActionPanel();
        add(Box.createVerticalStrut(10));
        // Table added similar to QuanLyLoaiPhongPanel (direct scrollPane add)
        createListCustomerPanel();
        // load sample data into dataset and table
        loadCustomersFromService();
        applyFilters(); // initial populate
    }

    private void initButtons() {
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Tìm kiếm...");
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // Action buttons - NO ICONS to speed up startup
        addButton    = createActionButton("Thêm khách hàng", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa khách hàng", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa khách hàng", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        addButton.addActionListener(e -> {
            ThemKhachHangDialog dlg = new ThemKhachHangDialog(SwingUtilities.getWindowAncestor(this),
                    customerService,
                    () -> {
                        // callback: reload data và apply filter
                        loadCustomersFromService();
                        applyFilters();
                    });
            dlg.setVisible(true);
        });

        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để sửa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            if (ma == null || ma.equals("-")) {
                JOptionPane.showMessageDialog(this, "Mã khách hàng không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CustomerServiceImpl svc = new CustomerServiceImpl();
            try {
                if (svc.hasCurrentOrFutureBookings(ma)) {
                    JOptionPane.showMessageDialog(this, "Không thể sửa: khách hàng đang có đơn đặt phòng hiện tại hoặc trong tương lai.", "Không cho phép", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra đơn đặt: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // mở dialog sửa, truyền callback reload
            SuaKhachHangDialog dlg = new SuaKhachHangDialog(SwingUtilities.getWindowAncestor(this), svc, ma, () -> {
                // callback khi sửa thành công -> reload
                loadCustomersFromService();
                applyFilters();
            });
            dlg.setVisible(true);
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ma = (String) table.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khách hàng " + ma + " và các đơn liên quan không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            CustomerServiceImpl svc = new CustomerServiceImpl();
            try {
                boolean ok = svc.deleteCustomerByIDV2(ma);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    // reload bảng từ service
                    fullDataset.clear();
                    var ds = svc.layTatCaKhachHang();
                    if (ds != null) {
                        for (var kh : ds) {
                            fullDataset.add(new CustomerData(kh.getMaKhachHang(), kh.getTenKhachHang(), kh.getCCCD(), kh.getSoDienThoai()));
                        }
                    }
                    applyFilters();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa không thành công.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalStateException ise) {
                JOptionPane.showMessageDialog(this, ise.getMessage(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa khách hàng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMinimumSize(new Dimension(120, size.height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        // Use the BOLD font here to make search panel text bold
        field.setFont(FONT_LABEL);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (Objects.equals(field.getText(), placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });

        // Realtime filter when user types
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { applyFilters(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
    }

    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setForeground(CustomUI.white);
        btn.setFont(FONT_LABEL); // make button label consistent with search panel (bold)
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1D4ED8"));
        btn.setMargin(new Insets(6, 10, 6, 10));
        btn.addActionListener(e -> applyFilters());
    }

    // create action button (no icon)
    private JButton createActionButton(String text, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFont(FONT_ACTION);
        try { button.setBackground(Color.decode(bgHex)); } catch (Exception ignored) { button.setBackground(new Color(0x888888)); }
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 18; borderWidth: 2; borderColor:" + borderHex);
        return button;
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý khách hàng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        // keep header style consistent with other panels
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        add(pnlTop);
    }

    private void createSearchAndActionPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBackground(CustomUI.white);

        // Left: search controls
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 25));
        searchPanel.setPreferredSize(new Dimension(0, 200));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        // search type combo (Tên khách hàng / CCCD)
        String[] searchOptions = {"Tên khách hàng", "CCCD"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        Dimension comboSize = new Dimension(SEARCH_TYPE_WIDTH, SEARCH_CONTROL_HEIGHT);
        searchTypeComboBox.setPreferredSize(comboSize);
        searchTypeComboBox.setMinimumSize(comboSize);
        searchTypeComboBox.setMaximumSize(comboSize);
        // make combo text bold
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Row: combo + searchTextField + button
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentY(Component.CENTER_ALIGNMENT);
        searchTextField.setPreferredSize(SEARCH_TEXT_SIZE);
        searchTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        // ensure placeholder font bold as configured earlier
        searchTextField.setAlignmentY(Component.CENTER_ALIGNMENT);

        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(searchTextField);
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

        container.add(searchPanel);
        add(container);
    }

    private void createListCustomerPanel() {
        // create model & table similar to QuanLyLoaiPhongPanel
        String[] columns = {"Mã khách hàng", "Tên khách hàng", "CCCD", "Điện thoại"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // JTable subclass that mirrors ReservationManagementPanel's prepareRenderer behavior
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                // reuse table font constant
                c.setFont(CustomUI.TABLE_FONT);

                // selection handling: immediate visual update when row is selected
                if (isRowSelected(row)) {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                    c.setForeground(CustomUI.black);
                } else {
                    // alternating row colors
                    if (row % 2 == 0) {
                        c.setBackground(CustomUI.ROW_EVEN != null ? CustomUI.ROW_EVEN : Color.WHITE);
                    } else {
                        c.setBackground(CustomUI.ROW_ODD != null ? CustomUI.ROW_ODD : new Color(0xF7F9FB));
                    }
                    c.setForeground(CustomUI.black);
                }

                // center align cell text and add thin cell border to match style
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }
                // matte border (bottom + right)
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

                return c;
            }
        };

        // Visual polish to match ReservationManagementPanel
        table.setRowHeight(48); // giống ReservationManagementPanel (bạn có thể đổi lại 40 nếu cần)
        table.setFont(CustomUI.TABLE_FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        table.setShowGrid(true);
        table.setGridColor(CustomUI.tableBorder);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setBackground(CustomUI.blue);
        header.setForeground(CustomUI.white);
        header.setFont(CustomUI.HEADER_FONT);
        header.setReorderingAllowed(false);

        // center align columns (kept from original) — still useful for header formatting
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // ensure immediate repaint when selection changes or mouse clicks
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                table.repaint();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String ma = (String) table.getValueAt(row, 0); // cột mã khách hàng
                        ThongTinKhachHangDialog dlg = new ThongTinKhachHangDialog(SwingUtilities.getWindowAncestor(table),
                                new CustomerServiceImpl(), ma);
                        dlg.setVisible(true);
                    }
                }
            }
        });


        // put table into scroll pane
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 500));

        add(scrollPane);
    }

    // trong QuanLyKhachHangPanel (thay loadSampleData cũ)
    private void loadCustomersFromService() {
        fullDataset.clear();
        try {
            List<vn.iuh.entity.KhachHang> ds = customerService.layTatCaKhachHang();
            if (ds != null) {
                for (vn.iuh.entity.KhachHang k : ds) {
                    fullDataset.add(new CustomerData(
                            k.getMaKhachHang(),
                            k.getTenKhachHang(),
                            k.getCCCD(),
                            k.getSoDienThoai()
                    ));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // fallback: giữ sample nếu cần hoặc để trống
        }
    }

    // Apply filters based on searchTypeComboBox and searchTextField content
    private void applyFilters() {
        if (tableModel == null) return;
        String mode = (searchTypeComboBox != null) ? (String) searchTypeComboBox.getSelectedItem() : "Tên khách hàng";
        String q = searchTextField.getText();
        if (q == null) q = "";
        boolean isPlaceholder = searchTextField.getForeground().equals(Color.GRAY) && Objects.equals(q, "Tìm kiếm...");
        String query = (!isPlaceholder) ? q.trim().toLowerCase() : "";

        List<CustomerData> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(fullDataset);
        } else {
            for (CustomerData c : fullDataset) {
                if ("CCCD".equals(mode)) {
                    if (c.cccd != null && c.cccd.toLowerCase().contains(query)) filtered.add(c);
                } else { // default name search
                    if (c.name != null && c.name.toLowerCase().contains(query)) filtered.add(c);
                }
            }
        }
        populateCustomerTable(filtered);
    }

    private void populateCustomerTable(List<CustomerData> list) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (list == null || list.isEmpty()) {
                tableModel.addRow(new Object[] {"-", "Không có khách hàng phù hợp.", "-", "-"});
                return;
            }
            for (CustomerData c : list) {
                tableModel.addRow(new Object[] { c.code, c.name, c.cccd, c.phone });
            }
        });
    }

    // small data holder
    private static class CustomerData {
        final String code;
        final String name;
        final String cccd;
        final String phone;
        CustomerData(String code, String name, String cccd, String phone) {
            this.code = code; this.name = name; this.cccd = cccd; this.phone = phone;
        }
    }
}

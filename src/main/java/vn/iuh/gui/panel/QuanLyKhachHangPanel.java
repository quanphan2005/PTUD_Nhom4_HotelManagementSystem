package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.gui.base.CustomUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    // Fonts & Colors tái sử dụng (tránh tạo mới trong render loop)
    private static final Font FONT_LABEL  = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);
    private static final Font TABLE_FONT = FONT_LABEL;
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 15);
    private static final Color ROW_ALT_COLOR = new Color(247, 249, 250);
    private static final Color ROW_SELECTED_COLOR = new Color(210, 230, 255);

    // Simple in-memory cache cho icons (key = path + size)
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;

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
        createListCustomerPanel();
    }

    private void initButtons() {
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Tên khách hàng");
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        // Tạo button mà **không** block UI để load icon
        addButton    = createActionButton("Thêm khách hàng", "/icons/add.png", ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton   = createActionButton("Sửa khách hàng", "/icons/edit.png", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");
        deleteButton = createActionButton("Xóa khách hàng", "/icons/delete.png", ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");
    }

    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        // width sẽ được linh hoạt: đặt preferred width nhưng cho phép mở rộng tối đa
        field.setPreferredSize(size);
        field.setMinimumSize(new Dimension(120, size.height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height)); // cho phép mở rộng chiều rộng
        field.setFont(FONT_LABEL);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        // Thay đổi placeholder
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

        // Căn giữa theo chiều dọc khi được đặt vào container
        field.setAlignmentY(Component.CENTER_ALIGNMENT);
    }

    // Hàm để tạo nút tìm
    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setForeground(CustomUI.white);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1D4ED8"));
        btn.setMargin(new Insets(6, 10, 6, 10));
        btn.setAlignmentY(Component.CENTER_ALIGNMENT);
    }

    // Hàm đẻ tạo các nút thêm/xóa/sửa khách hàng (không block EDT khi load icon)
    private JButton createActionButton(String text, String iconPath, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFont(FONT_ACTION);
        button.setBackground(Color.decode(bgHex));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 18; borderWidth: 2; borderColor:" + borderHex);

        // Nếu đã cache icon thì dùng ngay, còn không load async (không block)
        String key = iconCacheKey(iconPath, 20, 20);
        ImageIcon cached = getCachedIcon(key);
        if (cached != null) {
            button.setIcon(cached);
        } else {
            // placeholder (null) -> tải ảnh trong background rồi setIcon
            loadIconAsync(iconPath, 20, 20, icon -> {
                if (icon != null) button.setIcon(icon);
            });
        }
        return button;
    }

    // Tạo panel chứa tiêu đề 'Quản lý khách hàng"
    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý khách hàng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        add(pnlTop);
    }

    // Tạo panel tìm kiếm (panel chứa các nút thêm xóa sửa khách hàng, nút tìm kiếm, text field tìm kiếm)
    private void createSearchAndActionPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBackground(CustomUI.white);

        JPanel searchPanel = createSearchPanel();
        // KHÔNG đặt max quá nhỏ nữa — cho phép đủ chỗ cho nút action
        searchPanel.setPreferredSize(new Dimension(0, 180));   // tăng chiều cao để chứa action buttons (50px)
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        container.add(searchPanel);
        add(container);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12,12,12,12), Color.decode("#CED4DA"), 2, 25));

        String[] searchOptions = {"Tên khách hàng", "Mã khách hàng"};
        JComboBox<String> searchTypeComboBox = new JComboBox<>(searchOptions);
        Dimension comboSize = new Dimension(SEARCH_TYPE_WIDTH, SEARCH_CONTROL_HEIGHT); // chuẩn hoá chiều cao và width cố định cho combo
        searchTypeComboBox.setPreferredSize(comboSize);
        searchTypeComboBox.setMinimumSize(comboSize);
        searchTypeComboBox.setMaximumSize(comboSize);
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // inputPanel cho textfield: cho phép mở rộng chiều rộng để phủ hết panel
        JPanel inputPanel = new JPanel(new CardLayout());
        inputPanel.setBackground(CustomUI.white);
        Dimension inputPreferred = new Dimension(520, SEARCH_CONTROL_HEIGHT);
        inputPanel.setPreferredSize(inputPreferred);
        inputPanel.setMinimumSize(new Dimension(200, SEARCH_CONTROL_HEIGHT));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        inputPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(520, SEARCH_CONTROL_HEIGHT));
        nameField.setMinimumSize(new Dimension(120, SEARCH_CONTROL_HEIGHT));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        configureSearchTextField(nameField, new Dimension(520, SEARCH_CONTROL_HEIGHT), "Tên khách hàng");

        JTextField idField = new JTextField();
        idField.setPreferredSize(new Dimension(520, SEARCH_CONTROL_HEIGHT));
        idField.setMinimumSize(new Dimension(120, SEARCH_CONTROL_HEIGHT));
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        configureSearchTextField(idField, new Dimension(520, SEARCH_CONTROL_HEIGHT), "Mã khách hàng");

        inputPanel.add(nameField, "Tên khách hàng");
        inputPanel.add(idField, "Mã khách hàng");

        // Sự kiện cho searchTypeComboBox để thay đổi khung textfield khi lựa chọn trong searchTypeComboBox thay đổi
        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout) inputPanel.getLayout();
            cl.show(inputPanel, (String) searchTypeComboBox.getSelectedItem());
        });

        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);

        // Căn giữa theo chiều dọc
        row1.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Đảm bảo searchButton có kích thước cố định
        searchButton.setPreferredSize(SEARCH_BUTTON_SIZE);
        searchButton.setMinimumSize(SEARCH_BUTTON_SIZE);
        searchButton.setMaximumSize(SEARCH_BUTTON_SIZE);
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Tạo khoảng tách hợp lý và thêm các control
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(searchButton);

        // Cho thêm một khoảng trên để row1 không dính sát viền
        searchPanel.add(Box.createVerticalStrut(8));
        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(14));

        // Hàng 2: căn giữa 3 nút hành động
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8)); // tăng vertical gap để rõ hơn
        row2.setBackground(CustomUI.white);
        // đảm bảo các nút action có kích thước cố định và hiển thị đầy đủ
        addButton.setPreferredSize(ACTION_BUTTON_SIZE);
        editButton.setPreferredSize(ACTION_BUTTON_SIZE);
        deleteButton.setPreferredSize(ACTION_BUTTON_SIZE);

        row2.add(addButton);
        row2.add(editButton);
        row2.add(deleteButton);

        searchPanel.add(row2);
        searchPanel.add(Box.createVerticalStrut(8));
        return searchPanel;
    }

    // Synchronous loader có cache (dùng bởi SwingWorker)
    private static synchronized ImageIcon loadScaledIconSync(String path, int width, int height) {
        String key = iconCacheKey(path, width, height);
        ImageIcon cached = ICON_CACHE.get(key);
        if (cached != null) return cached;

        try (InputStream is = QuanLyKhachHangPanel.class.getResourceAsStream(path)) {
            if (is == null) return null;
            BufferedImage img = ImageIO.read(is);
            if (img == null) return null;
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon ic = new ImageIcon(scaled);
            ICON_CACHE.put(key, ic);
            return ic;
        } catch (Exception e) {
            return null;
        }
    }

    private static synchronized ImageIcon getCachedIcon(String key) {
        return ICON_CACHE.get(key);
    }

    private static String iconCacheKey(String path, int w, int h) {
        return path + "|" + w + "x" + h;
    }

    // Load icon bất đồng bộ rồi gọi callback trên EDT
    private static void loadIconAsync(String path, int w, int h, java.util.function.Consumer<ImageIcon> callback) {
        // nếu đã cache -> trả về ngay trên EDT
        String key = iconCacheKey(path, w, h);
        ImageIcon cached = getCachedIcon(key);
        if (cached != null) {
            SwingUtilities.invokeLater(() -> callback.accept(cached));
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                return loadScaledIconSync(path, w, h);
            }
            @Override
            protected void done() {
                try {
                    ImageIcon ic = get();
                    if (ic != null) callback.accept(ic);
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void createListCustomerPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(CustomUI.white);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 750));
        wrap.setPreferredSize(new Dimension(0, 750));

        String[] columns = {"Mã khách hàng", "Tên khách hàng", "CCCD", "Điện thoại"};
        Object[][] data = {
                {"KH00000001", "Nguyễn Văn A", "079123456789", "0912345678"},
                {"KH00000002", "Trần Thị B",   "079987654321", "0987654321"},
                {"KH00000003", "Lê Văn C",     "079456789123", "0905123456"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000005", "Huỳnh Văn E",  "079741258963", "0978123456"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"},
                {"KH00000004", "Phạm Thị D",   "079321654987", "0934567890"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Không cho phép chỉnh sửa thông tin trong các ô của table
        };

        JTable table = new JTable(model) { // Tạo JTable mới dựa trên model
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                // prepareRenderer được gọi mỗi khi JTable vẽ 1 cell.
                Component c = super.prepareRenderer(renderer, row, column);

                // reuse font constant (không new font mỗi cell)
                c.setFont(TABLE_FONT);

                if (!isRowSelected(row)) {
                    // reuse color constant
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT_COLOR);
                } else {
                    c.setBackground(ROW_SELECTED_COLOR);
                }
                return c;
            }
        };

        table.setRowHeight(48);
        table.setFont(TABLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setBackground(CustomUI.blue);
        header.setForeground(CustomUI.white);
        header.setFont(HEADER_FONT);
        header.setReorderingAllowed(false);

        // Căn giữa cho thông tin trong các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CustomUI.white);

        wrap.add(scrollPane, BorderLayout.CENTER);
        this.add(wrap);
    }
}

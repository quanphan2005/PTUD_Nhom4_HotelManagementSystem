package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.constraint.Fee;
import vn.iuh.dao.HoaDonDAO;
//import vn.iuh.dto.event.create.InvoiceCreationEvent;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.dto.response.InvoiceResponse;
import vn.iuh.entity.HoaDon;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.DateChooser;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.dialog.InvoiceDialog2;
import vn.iuh.dao.*;
import vn.iuh.entity.*;
import vn.iuh.util.FeeValue;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QuanLyHoaDonPanel extends JPanel{
    // --- Kích thước (dựa trên QuanLyLoaiPhongPanel) ---
    private static final int SEARCH_CONTROL_HEIGHT = 45;
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(400, SEARCH_CONTROL_HEIGHT);
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(90, 40);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(290, 55);
    private static final int TOP_PANEL_HEIGHT = 50;

    // --- Fonts (dựa trên QuanLyLoaiPhongPanel) ---
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 20);
    private static final Font TABLE_FONT = FONT_LABEL;
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 15);

    // --- Colors ---
    private static final Color ROW_ALT_COLOR = new Color(250, 247, 249);
    private static final Color ROW_SELECTED_COLOR = new Color(210, 230, 255);

    // --- Icon Cache ---
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    // --- Thành phần UI ---
    private JComboBox<String> searchTypeComboBox;
    private JTextField txtSearchMaHoaDon;
    private DateChooser datePickerStart;
    private DateChooser datePickerEnd;
    private JPanel inputPanel; // Panel CardLayout
    private final JButton searchButton = new JButton("TÌM");

    private DefaultTableModel tableModel;
    private JTable table;

    // --- DAO ---
    private HoaDonDAO hoaDonDAO;
    private ChiTietHoaDonDAO chiTietHoaDonDAO;
    private DonGoiDichVuDao donGoiDichVuDAO;
    private PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private PhuPhiDAO phuPhiDAO;
    private KhachHangDAO khachHangDAO;
    private NhanVienDAO nhanVienDAO;
    private DatPhongDAO datPhongDAO;

    public QuanLyHoaDonPanel() {
        this.phuPhiDAO = new PhuPhiDAO();
        this.datPhongDAO = new DatPhongDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.donGoiDichVuDAO = new DonGoiDichVuDao();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.hoaDonDAO = new HoaDonDAO();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
        loadInvoiceData();
    }

    private void init() {
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndActionPanel();
        add(Box.createVerticalStrut(10));
        createInvoiceListPanel();

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Tự động gọi refreshData() mỗi khi panel này được hiển thị
                refreshData();
            }
        });
    }

    // Panel tiêu đề
    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý hóa đơn", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont : FONT_LABEL);
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        add(pnlTop);
    }

    private void createSearchAndActionPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        mainPanel.setBackground(CustomUI.white);

        mainPanel.add(createLeftSearchPanel());
        mainPanel.add(Box.createHorizontalGlue());
        mainPanel.add(Box.createHorizontalStrut(20));
        mainPanel.add(createRightDatePanel());

        add(mainPanel);
    }

    /**
     * Tạo panel bên trái (ComboBox, Nút Thêm)
     */
    private JPanel createLeftSearchPanel() {
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setBackground(CustomUI.white);
        pnlLeft.setBorder(new FlatLineBorder(new Insets(12, 12, 12, 12), Color.decode("#CED4DA"), 2, 30));
        pnlLeft.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        pnlLeft.setPreferredSize(new Dimension(600, 100));

        // --- Hàng 1: Tìm kiếm ---
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentY(Component.CENTER_ALIGNMENT);

        // ComboBox
        String[] searchOptions = {"Mã hóa đơn", "Theo ngày"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        searchTypeComboBox.setPreferredSize(new Dimension(180, SEARCH_CONTROL_HEIGHT));
        searchTypeComboBox.setMaximumSize(new Dimension(180, SEARCH_CONTROL_HEIGHT));
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Input Panel (CardLayout)
        inputPanel = new JPanel(new CardLayout(5, 5));
        inputPanel.setBackground(CustomUI.white);
        inputPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Card 1: Tìm theo Mã
        txtSearchMaHoaDon = new JTextField();
        configureSearchTextField(txtSearchMaHoaDon, SEARCH_TEXT_SIZE, "Nhập mã hóa đơn...");
        inputPanel.add(txtSearchMaHoaDon, "Mã hóa đơn");

        // Card 2: Placeholder cho tìm theo ngày
        JLabel datePlaceholder = new JLabel("Sử dụng bộ lọc thời gian bên phải ->");
        datePlaceholder.setFont(FONT_LABEL);
        datePlaceholder.setForeground(Color.GRAY);
        datePlaceholder.setHorizontalAlignment(SwingConstants.CENTER);
        datePlaceholder.setPreferredSize(SEARCH_TEXT_SIZE);
        inputPanel.add(datePlaceholder, "Theo ngày");

        // Nút Tìm
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        searchButton.addActionListener(e -> handleSearch());
        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(10));
        row1.add(searchButton);

        pnlLeft.add(Box.createVerticalStrut(8));
        pnlLeft.add(row1);
        pnlLeft.add(Box.createVerticalGlue());

        searchTypeComboBox.addActionListener(e -> updateSearchPanelState());

        return pnlLeft;
    }

    /**
     * Tạo panel bên phải (Lọc ngày)
     */
    private JPanel createRightDatePanel() {
        JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBackground(CustomUI.white);
        pnlRight.setBorder(new FlatLineBorder(new Insets(12, 12, 12, 12), Color.decode("#CED4DA"), 2, 30));
        pnlRight.setPreferredSize(new Dimension(500, 200));
        pnlRight.setMaximumSize(new Dimension(500, 200));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Bộ lọc thời gian");
        lblTitle.setFont(FONT_ACTION);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ngày bắt đầu
        JPanel pnlStart = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlStart.setBackground(CustomUI.white);
        JLabel lblStartTime = new JLabel("Thời gian bắt đầu:");
        lblStartTime.setFont(FONT_LABEL);
        datePickerStart = new DateChooser();
        datePickerStart.setPreferredSize(new Dimension(250, SEARCH_CONTROL_HEIGHT));
        pnlStart.add(lblStartTime);
        pnlStart.add(datePickerStart);

        // Ngày kết thúc
        JPanel pnlEnd = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlEnd.setBackground(CustomUI.white);
        JLabel lblEndTime = new JLabel("Thời gian kết thúc:");
        lblEndTime.setFont(FONT_LABEL);
        datePickerEnd = new DateChooser();
        datePickerEnd.setPreferredSize(new Dimension(250, SEARCH_CONTROL_HEIGHT));
        pnlEnd.add(lblEndTime);
        pnlEnd.add(datePickerEnd);

        pnlRight.add(lblTitle);
        pnlRight.add(Box.createVerticalStrut(15));
        pnlRight.add(pnlStart);
        pnlRight.add(Box.createVerticalStrut(5));
        pnlRight.add(pnlEnd);

        // Vô hiệu hóa mặc định
        updateSearchPanelState();

        return pnlRight;
    }

    /**
     * Cập nhật trạng thái của các ô input dựa trên ComboBox
     */
    private void updateSearchPanelState() {
        String selected = (String) searchTypeComboBox.getSelectedItem();
        boolean isDateSearch = "Theo ngày".equals(selected);

        CardLayout cl = (CardLayout) inputPanel.getLayout();
        cl.show(inputPanel, selected);

        txtSearchMaHoaDon.setEnabled(!isDateSearch);
        datePickerStart.setEnabled(isDateSearch);
        datePickerEnd.setEnabled(isDateSearch);
    }

    public void refreshData() {
        searchTypeComboBox.setSelectedIndex(0);

        txtSearchMaHoaDon.setText("Nhập mã hóa đơn...");
        txtSearchMaHoaDon.setForeground(Color.GRAY);

        updateSearchPanelState();
        loadInvoiceData();
    }
    /**
     * Tạo bảng danh sách hóa đơn
     */
    private void createInvoiceListPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(CustomUI.white);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        String[] columns = {"Mã hóa đơn", "Kiểu hóa đơn", "Mã đặt phòng", "Mã khách hàng"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(TABLE_FONT);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT_COLOR);
                } else {
                    c.setBackground(ROW_SELECTED_COLOR);
                }
                return c;
            }
        };

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    int selectedRow = table.getSelectedRow();
                    if(selectedRow != -1){
                        String maHoaDon = (String) tableModel.getValueAt(selectedRow , 0);
                        showInvoiceDetails(maHoaDon);
                    }
                }
            }
        });

        // Áp dụng style cho bảng
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

        // Căn giữa
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CustomUI.white);

        wrap.add(scrollPane, BorderLayout.CENTER);
        add(wrap); // Thêm bảng vào panel chính
    }
    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        field.setMinimumSize(new Dimension(120, size.height));
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

        return button;
    }

    private void handleSearch() {
        String searchType = (String) searchTypeComboBox.getSelectedItem();

        if ("Mã hóa đơn".equals(searchType)) {
            String maHD = txtSearchMaHoaDon.getText();
            if (maHD.isEmpty() || maHD.equals("Nhập mã hóa đơn...")) {
                loadInvoiceData();
            } else {
                loadInvoiceData(maHD);
            }
        } else { // "Theo ngày"
            LocalDate startLocal = datePickerStart.getDate();
            LocalDate endLocal   = datePickerEnd.getDate();

            if (startLocal == null || endLocal == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn cả ngày bắt đầu và kết thúc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            java.util.Date startDate = java.util.Date.from(startLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            java.time.LocalDateTime endLocalTime = endLocal.atTime(23, 59, 59);
            java.util.Date endDate = java.util.Date.from(endLocalTime.atZone(ZoneId.systemDefault()).toInstant());
            // 4. Kiểm tra logic ngày
            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu không thể sau ngày kết thúc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadInvoiceData(startDate, endDate);
        }
    }

    private void loadInvoiceData(Object... params){
        tableModel.setRowCount(0);
        List<HoaDon> dsHoaDon = null;

        try {
            if (params.length == 0) {
                dsHoaDon = hoaDonDAO.layDanhSachHoaDon();
            } else if (params.length == 1) {
                HoaDon hd = hoaDonDAO.timHoaDon((String) params[0]);
                if (hd != null) dsHoaDon = List.of(hd);
            } else if (params.length == 2) {
                java.util.Date fromUtil = (java.util.Date) params[0];
                java.util.Date toUtil   = (java.util.Date) params[1];
                Timestamp from = new Timestamp(fromUtil.getTime());
                Timestamp to = new Timestamp(toUtil.getTime());
                dsHoaDon = hoaDonDAO.danhSachHoaDonTrongKhoang(from, to);
            }

            if (dsHoaDon != null) {
                for (HoaDon hd : dsHoaDon) {
                    tableModel.addRow(new Object[]{
                            hd.getMaHoaDon(), hd.getKieuHoaDon(),
                            hd.getMaDonDatPhong(), hd.getMaKhachHang()
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInvoiceDetails(String maHoaDon){
        try{
            HoaDon hoaDon = hoaDonDAO.timHoaDon(maHoaDon);
            if(hoaDon == null){
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn" + maHoaDon);
            }
            String maDonDatPhong = hoaDon.getMaDonDatPhong();

            ThongTinPhuPhi ttpp = FeeValue.getInstance().get(Fee.THUE);

            DonDatPhong ddp = datPhongDAO.getDonDatPhongById(maDonDatPhong);

            KhachHang khachHang = khachHangDAO.timKhachHang(hoaDon.getMaKhachHang());

            NhanVien nhanVien = nhanVienDAO.layNVTheoMaPhienDangNhap(Main.getCurrentLoginSession());

            List<ChiTietHoaDon> dsChiTiet = chiTietHoaDonDAO.layChiTietHoaDonBangMaHoaDon(maHoaDon);

            List<PhongDungDichVu> dsPhongDungDichVu = donGoiDichVuDAO.timDonGoiDichVuBangDonDatPhong(hoaDon.getMaDonDatPhong());

            List<PhongTinhPhuPhi> dsPhongTinhPhuPhi = phongTinhPhuPhiDAO.getPhuPhiTheoMaHoaDon(maHoaDon);

            InvoiceResponse invoiceResponse = new InvoiceResponse(
                    hoaDon.getMaPhienDangNhap(),
                    ddp,
                    khachHang,
                    hoaDon,
                    nhanVien,
                    dsChiTiet,
                    dsPhongDungDichVu,
                    dsPhongTinhPhuPhi
            );
            SwingUtilities.invokeLater(() -> {
                InvoiceDialog2 dialog = new InvoiceDialog2(invoiceResponse);
                dialog.setVisible(true);
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


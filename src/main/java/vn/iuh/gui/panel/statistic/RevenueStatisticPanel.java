package vn.iuh.gui.panel.statistic;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dto.response.InvoiceStatistic;
import vn.iuh.entity.NhanVien;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.DateChooser;
import vn.iuh.gui.base.RoleChecking;
import vn.iuh.service.impl.RevenueStatisticService;
import vn.iuh.util.ExportWriter;
import vn.iuh.util.PriceFormat;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class RevenueStatisticPanel extends RoleChecking {
    private JPanel pnlMain;
    private JLabel lblEmployee;
    private JLabel lblDisplayType;
    private DateChooser datePickerEnd;
    private DateChooser datePickerStart;
    private JPanel pnlResult;
    private DefaultTableModel model;
    private JPanel pnlVisualDisplay;
    private CardLayout cardLayout;
    private JPanel pnlTextDisplay;
    private FilterStatistic baseFilter;
    private JComboBox<String> cmbEmployee;
    private List<NhanVien> danhSachNhanVien;
    private final RevenueStatisticService revenueStatisticService;
    private NhanVienDAO nhanVienDAO;
    private JLabel lblTotalInvoiceValue;
    private JLabel lblFeeValue;
    private JLabel lblTotalRevenueValue ;
    private JLabel lblServiceValue;
    private JLabel lblRoomValue;
    private JLabel lblTaxValue;
    private List<InvoiceStatistic> danhSachKetQua;
    private JComboBox<String> cmbOption;
    private JLabel lblOption;
    private JPanel pnlOption;
    private JPanel chartPanel;
    private JTextField txtFolderPath;
    private JButton btnChooseFolder;
    private JButton btnExport;
    private JLabel lblFolderChooser;
    private JPanel pnlFolderChooser;

    private void init(){
        createTopPanel();
        createMainPanel();
    }

    @Override
    protected void buildAdminUI() {
        setLayout(new BorderLayout());
        init();
        loadDanhSachNhanVien();
    }

    public void createMainPanel() {
        pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        createFilterPanel();
        createResultPanel();
        this.add(pnlMain, BorderLayout.CENTER);
    }

    public RevenueStatisticPanel() {
        super();
        this.nhanVienDAO = new NhanVienDAO();
        this.revenueStatisticService = new RevenueStatisticService();
        this.danhSachKetQua = new ArrayList<>();
        //run khi card được show
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadDanhSachNhanVien();
            }
        });
    }
    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("THỐNG KÊ DOANH THU", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);

        pnlTop.setPreferredSize(new Dimension(0, 40));
        pnlTop.setMinimumSize(new Dimension(0, 40));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        add(pnlTop, BorderLayout.NORTH);
    }

    private void createFilterPanel(){
        JPanel pnlFilter = new JPanel(new GridLayout(3, 2, 5, 5));

        // Ô [0,0] StartTime
        JPanel pnlStartTime = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblStartTime = new JLabel("Thời gian bắt đầu: ");
        lblStartTime.setFont(CustomUI.smallFont);
        datePickerStart = new DateChooser();

        pnlStartTime.add(lblStartTime);
        pnlStartTime.add(datePickerStart);
        pnlStartTime.setBackground(CustomUI.white);

        // Ô [1,0] EndTime
        JPanel pnlEndTime = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblEndTime = new JLabel("Thời gian kết thúc:");
        lblEndTime.setFont(CustomUI.smallFont);
        datePickerEnd = new DateChooser();
        pnlEndTime.add(lblEndTime);
        pnlEndTime.add(datePickerEnd);
        pnlEndTime.setBackground(CustomUI.white);

        // Ô [0,1] Employee + Button
        JPanel pnlEmployee = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblEmployee = new JLabel("Chọn nhân viên     ");
        lblEmployee.setFont(CustomUI.smallFont);
        lblEmployee.setForeground(CustomUI.black);
        cmbEmployee = new JComboBox<>();
        cmbEmployee.setPreferredSize(new Dimension(100, 30));

        cmbEmployee.addActionListener(e -> {
            handleBaseCase();
        });

        JButton btnReLoad = new JButton("Tải lại");
        btnReLoad.addActionListener(e ->{
            handleBaseCase();
        });
        btnReLoad.setSize(new Dimension(20, 10));
        btnReLoad.setFont(CustomUI.smallFont);
        btnReLoad.setForeground(CustomUI.white);
        btnReLoad.setBackground(CustomUI.purple);
        btnExport = new JButton("Xuất file");
        btnExport.setFont(CustomUI.smallFont);
        btnExport.setSize(new Dimension(20, 10));
        btnExport.setForeground(CustomUI.white);
        btnExport.setBackground(CustomUI.green);

        btnExport.addActionListener(e ->{
            this.handleBtnExport();
        });
        txtFolderPath = new JTextField();
        txtFolderPath.setFont(CustomUI.verySmallFont);
        txtFolderPath.setPreferredSize(new Dimension(250, 35));
        txtFolderPath.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        ImageIcon folderIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/folder.png")));
        btnChooseFolder = createFolderIconButton(folderIcon, txtFolderPath);
        btnChooseFolder.setFont(CustomUI.smallFont);
        btnChooseFolder.setBackground(CustomUI.blue);
        btnChooseFolder.setForeground(CustomUI.white);

        lblFolderChooser = new JLabel("Chọn thư mục");
        lblFolderChooser.setFont(CustomUI.smallFont);

        pnlFolderChooser = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFolderChooser.add(lblFolderChooser);
        pnlFolderChooser.add(btnChooseFolder);
        pnlFolderChooser.add(txtFolderPath);
        pnlFolderChooser.add(btnExport);
        pnlFolderChooser.setOpaque(false);



        pnlEmployee.add(lblEmployee);
        pnlEmployee.add(cmbEmployee);
        pnlEmployee.setBackground(CustomUI.white);

        // Ô [1,1] RadioButton group
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblDisplayType = new JLabel("Chọn dạng hiển thị");
        lblDisplayType.setFont(CustomUI.smallFont);
        lblDisplayType.setForeground(CustomUI.black);
        JRadioButton radGraph = new JRadioButton("Dạng biểu đồ");
        JRadioButton radTable = new JRadioButton("Dạng bảng");
        ButtonGroup group = new ButtonGroup();
        group.add(radGraph);
        radGraph.addActionListener(e ->{
            Map<String, BigDecimal> grouped = groupInvoicesByOption(danhSachKetQua);
            showBarChart(grouped, "Thống kê doanh thu");
        });
        group.add(radTable);
        pnlRadio.add(lblDisplayType);
        pnlRadio.add(radGraph);
        pnlRadio.add(radTable);
        pnlRadio.setBackground(CustomUI.white);

        lblOption = new JLabel("Tùy chọn");
        lblOption.setForeground(CustomUI.black);
        lblOption.setFont(CustomUI.smallFont);
        String[] options = {"Hôm nay", "Hôm qua", "Tuần này", "Tháng này", "Quý này","Năm nay"};
        cmbOption = new JComboBox<>(options);
        cmbOption.setEnabled(true);
        cmbOption.setPreferredSize(new Dimension(100, 30));
        pnlOption = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlOption.add(lblOption);
        pnlOption.add(cmbOption);
        pnlOption.setBackground(CustomUI.white);

        cmbOption.addActionListener( e->{
            String option = (String) cmbOption.getSelectedItem();
            setDateRangeByOption(option);
            handleBaseCase();
        });
        
        

        // Thêm ActionListener để chuyển đổi giữa hai card panel
        radGraph.addActionListener(e -> cardLayout.show(pnlVisualDisplay, "chart"));
        radTable.addActionListener(e -> cardLayout.show(pnlVisualDisplay, "table"));
        radTable.setSelected(true); // Mặc định hiển thị bảng

        // Thêm tất cả vào pnlFilter
        pnlFilter.add(pnlStartTime);   // [0,0]
        pnlFilter.add(pnlEmployee);    // [0,1]
        pnlFilter.add(pnlEndTime);     // [1,0]
        pnlFilter.add(pnlRadio);       // [1,1]
        pnlFilter.add(pnlOption);
        pnlFilter.add(pnlFolderChooser);

        pnlFilter.setBackground(CustomUI.white);
        pnlMain.add(pnlFilter);
    }

    private JButton createFolderIconButton(ImageIcon icon, JTextField txt) {
        JButton btn = new JButton(icon);
        btn.setPreferredSize(CustomUI.BUTTON_SIZE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(CustomUI.mine);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(240, 240, 240));
            }
        });

        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txt.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        return btn;
    }


    private void loadDanhSachNhanVien(){
        danhSachNhanVien = nhanVienDAO.layDanhSachNhanVien();
        cmbEmployee.removeAllItems();
        cmbEmployee.addItem("Tất cả");
        for(NhanVien nv : danhSachNhanVien){
            cmbEmployee.addItem(nv.getTenNhanVien());
        }
    }

    private void exportFileExcel(){
        String folderPath = txtFolderPath.getText().trim();
        if (!folderPath.isEmpty()) {
            try {
                String fileName = "ThongKeDoanhThu";
                String filePath = folderPath + "/ThongKeDoanhThu.xlsx";
                ExportWriter.exportTableToExcel(model, filePath,fileName);
                JOptionPane.showMessageDialog(this, "Xuất file Excel thành công:\n" + filePath);
            } catch (Exception ex1) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + ex1.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex1.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thư mục để lưu file Excel!", "Thiếu đường dẫn", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleBtnExport(){
        int rowCount = this.model.getRowCount();
        if(rowCount > 0){
            exportFileExcel();
        }
        else {
            JOptionPane.showMessageDialog(null, "Chưa có kết quả thống kê");
        }
    }

    private FilterStatistic validateInput(){
        LocalDate startDate = datePickerStart.getDate();
        LocalDate endDate = datePickerEnd.getDate();
        LocalDate today = LocalDate.now();
        String tenNhanVien = (String) cmbEmployee.getSelectedItem();
        String maNhanVien;
        if("Tất cả".equalsIgnoreCase(tenNhanVien) || tenNhanVien == null){
            maNhanVien = null;
        }
        else {
            maNhanVien = this.danhSachNhanVien.stream()
                    .filter(e -> tenNhanVien.equalsIgnoreCase(e.getTenNhanVien()))
                    .map(NhanVien::getMaNhanVien)
                    .toList().getFirst();
        }
        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc");
        }
        if (startDate.isAfter(today)) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu không được sau ngày hiện tại");
        }
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu không được sau ngày kết thúc");
        }

        return new FilterStatistic(Timestamp.valueOf(startDate.atTime(LocalTime.MIN)),
                Timestamp.valueOf(endDate.atTime(LocalTime.MAX)), maNhanVien);
    }

    private void handleReload(FilterStatistic newFilter){
        try {
            List<InvoiceStatistic> danhSachKetQua  = revenueStatisticService.layThongKeVoiDieuKien(newFilter);
            fillTable(danhSachKetQua);
            updateStatisticLabels(danhSachKetQua);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void setDateRangeByOption(String option) {
        if(option == null) return;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today;
        LocalDate endDate = today;

        switch (option) {
            case "Hôm nay":
                startDate = today;
                endDate = today;
                break;

            case "Hôm qua":
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;

            case "Tuần này":
                startDate = today.with(DayOfWeek.MONDAY);
                endDate = today.with(DayOfWeek.SUNDAY);
                break;

            case "Tháng này":
                startDate = today.withDayOfMonth(1);
                endDate = today.withDayOfMonth(today.lengthOfMonth());
                break;

            case "Quý này":
                int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                int startMonth = (currentQuarter - 1) * 3 + 1;
                int endMonth = startMonth + 2;

                startDate = LocalDate.of(today.getYear(), startMonth, 1);
                endDate = LocalDate.of(today.getYear(), endMonth,
                        YearMonth.of(today.getYear(), endMonth).lengthOfMonth());
                break;

            case "Năm nay":
                startDate = LocalDate.of(today.getYear(), 1, 1);
                endDate = LocalDate.of(today.getYear(), 12, 31);
                break;
        }

        // Gán vào 2 datePicker
        datePickerStart.setDate(startDate);
        datePickerEnd.setDate(endDate);
    }


    private void updateStatisticLabels(List<InvoiceStatistic> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            lblTotalInvoiceValue.setText("0");
            lblFeeValue.setText("0");
            lblTotalRevenueValue.setText("0");
            lblServiceValue.setText("0");
            lblRoomValue.setText("0");
            lblTaxValue.setText("0");
            return;
        }

        BigDecimal totalFee = invoices.stream()
                .map(InvoiceStatistic::getPhuPhi)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = invoices.stream()
                .map(InvoiceStatistic::getTongHoaDon)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalService = invoices.stream()
                .map(InvoiceStatistic::getTienDichVu)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRoom = invoices.stream()
                .map(InvoiceStatistic::getTienPhong)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = invoices.stream()
                .map(InvoiceStatistic::getTienThue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        lblTotalInvoiceValue.setText(String.valueOf(invoices.size()));
        lblFeeValue.setText(formatter.format(totalFee));
        lblTotalRevenueValue.setText(formatter.format(totalRevenue));
        lblServiceValue.setText(formatter.format(totalService));
        lblRoomValue.setText(formatter.format(totalRoom));
        lblTaxValue.setText(formatter.format(totalTax));
    }

    private void createResultPanel(){
        pnlResult = new JPanel(new BorderLayout());
        pnlVisualDisplay = new JPanel();
        cardLayout = new CardLayout();
        pnlVisualDisplay.setLayout(cardLayout);
        createTableResult();
        createCharPanel();
        pnlResult.add(pnlVisualDisplay, BorderLayout.CENTER);

        // Tạo panel text display với GridBagLayout, 2 hàng 3 cột
        pnlTextDisplay = new JPanel(new GridBagLayout());
        pnlTextDisplay.setBackground(CustomUI.lightGray);
        pnlTextDisplay.setPreferredSize(new Dimension(0, 120));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 30, 5, 30);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Hàng 1
        gbc.gridy = 0;
        gbc.gridx = 0;
        JLabel lblTotalInvoice = new JLabel("Tổng số hóa đơn:");
        lblTotalInvoice.setFont(CustomUI.smallFont);
        lblTotalInvoice.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTotalInvoice, gbc);
        gbc.gridx = 1;
        JLabel lblFee = new JLabel("Tiền phụ phí:");
        lblFee.setFont(CustomUI.smallFont);
        lblFee.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblFee, gbc);
        gbc.gridx = 2;
        JLabel lblTotalRevenue = new JLabel("Tổng doanh thu:");
        lblTotalRevenue.setFont(CustomUI.smallFont);
        lblTotalRevenue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTotalRevenue, gbc);

        // Hàng 2
        gbc.insets = new Insets(25, 30, 5, 30);
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel lblService = new JLabel("Doanh thu dịch vụ:");
        lblService.setFont(CustomUI.smallFont);
        lblService.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblService, gbc);
        gbc.gridx = 1;
        JLabel lblRoom = new JLabel("Tiền phòng:");
        lblRoom.setFont(CustomUI.smallFont);
        lblRoom.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblRoom, gbc);
        gbc.gridx = 2;
        JLabel lblTax = new JLabel("Tổng thu thuế:");
        lblTax.setFont(CustomUI.smallFont);
        lblTax.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTax, gbc);

        // Hàng 1 - Giá trị
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 200, 5, 30);
        gbc.weightx = 1.0;
        lblTotalInvoiceValue = new JLabel("30");
        lblTotalInvoiceValue.setFont(CustomUI.smallFont);
        lblTotalInvoiceValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTotalInvoiceValue, gbc); // Tổng số hóa đơn
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 180, 5, 30);
        lblFeeValue = new JLabel("25.380.000");
        lblFeeValue.setFont(CustomUI.smallFont);
        lblFeeValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblFeeValue, gbc); // Tiền cọc
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 200, 5, 30);
        lblTotalRevenueValue = new JLabel("1.153.570");
        lblTotalRevenueValue.setFont(CustomUI.smallFont);
        lblTotalRevenueValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTotalRevenueValue, gbc); // Tổng doanh thu

        // Hàng 2 - Giá trị
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(25, 200, 5, 30);
        gbc.weightx = 1.0;
        lblServiceValue = new JLabel("396.200");
        lblServiceValue.setFont(CustomUI.smallFont);
        lblServiceValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblServiceValue, gbc); // Dịch vụ
        gbc.gridx = 1;
        gbc.insets = new Insets(25, 180, 5, 30);
        lblRoomValue = new JLabel("652.500");
        lblRoomValue.setFont(CustomUI.smallFont);
        lblRoomValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblRoomValue, gbc); // Tiền phòng
        gbc.gridx = 2;
        gbc.insets = new Insets(25, 200, 5, 30);
        lblTaxValue = new JLabel("0");
        lblTaxValue.setFont(CustomUI.smallFont);
        lblTaxValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTaxValue, gbc); // Thuế

        pnlResult.add(pnlTextDisplay, BorderLayout.SOUTH);
        pnlMain.add(pnlResult);
    }

    private void createTableResult(){
        String[] cols = {"Mã hóa đơn", "Khách hàng", "Ngày lập", "Tiền phòng","Dịch vụ", "Phụ phí","Thuế", "Tổng tiền"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // toàn bộ cell không editable (không có cột thao tác)
                return false;
            }
        };

        JTable roomTable = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
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

        roomTable.setRowHeight(48);
        roomTable.getTableHeader().setPreferredSize(new Dimension(roomTable.getWidth(), 40));
        roomTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        roomTable.getTableHeader().setBackground(CustomUI.blue);
        roomTable.getTableHeader().setForeground(CustomUI.white);
        roomTable.getTableHeader().setOpaque(true);

        roomTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                comp.setFont(CustomUI.TABLE_FONT);
                return comp;
            }
        });

        JScrollPane scrollPane = new JScrollPane(roomTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        pnlVisualDisplay.add(scrollPane, "table");
    }

    private void fillTable(List<InvoiceStatistic> danhSachHoaDon){
        model.setRowCount(0);
        for(InvoiceStatistic hd : danhSachHoaDon){
            model.addRow(hd.getObject());
        }
    }

    private void showBarChart(Map<String, BigDecimal> data ,String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        data.forEach((key, value) -> dataset.addValue(value.doubleValue(), "Doanh thu", key));
        JFreeChart barChart = ChartFactory.createBarChart(
                title,
                "Thời gian",
                "Tổng tiền (VNĐ)",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        this.chartPanel.add(chartPanel, BorderLayout.CENTER);
        cardLayout.show(pnlVisualDisplay, "chart");
    }

    private Map<String, BigDecimal> groupInvoicesByOption(List<InvoiceStatistic> list) {
        String option = (String) this.cmbOption.getSelectedItem();
        return list.stream().collect(Collectors.groupingBy(
                invoice -> {
                    LocalDate date = invoice.getNgayLap().toLocalDateTime().toLocalDate();
                    switch (option) {
                        case "Hôm nay":
                        case "Hôm qua":
                            return date.toString();

                        case "Tuần này":
                            return date.getDayOfWeek().toString();

                        case "Tháng này":
                            return String.valueOf(date.getDayOfMonth());

                        case "Quý này":
                            return date.getMonth().toString();

                        case "Năm nay":
                            return date.getMonth().toString();

                        default:
                            return date.toString();
                    }
                },
                Collectors.reducing(
                        BigDecimal.ZERO,
                        InvoiceStatistic::getTongHoaDon,
                        BigDecimal::add
                )
        ));
    }

    private void createCharPanel(){
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        pnlVisualDisplay.add(chartPanel, "chart");
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VND";
        value = PriceFormat.lamTronDenHangNghin(value);
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(value);
    }

    private void handleBaseCase(){
        FilterStatistic newFilter = validateInput();
        if(!newFilter.equals(baseFilter)){
            handleReload(newFilter);
        }
    }
}

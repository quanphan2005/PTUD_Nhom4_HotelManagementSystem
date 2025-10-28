package vn.iuh.gui.panel.statistic;

import com.formdev.flatlaf.FlatClientProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import vn.iuh.dao.LoaiPhongDAO;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.DateChooser;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.util.ExportWriter;
import vn.iuh.util.PriceFormat;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

public class RoomProductivityPanel extends JPanel {
    private JPanel pnlTop;
    private JLabel lblTop;
    private JPanel pnlMain;
    private DefaultTableModel model;
    private final LoaiPhongDAO loaiPhongDAO;
    private DateChooser startChooser;
    private DateChooser endChooser;
    private JPanel pnlPhong;
    private JPanel pnlTo;
    private JPanel filterPanel;
    private JPanel pnlKhoang;
    private JRadioButton rdoKhoang;
    private JPanel pnlNam;
    private JRadioButton rdoNam;
    private JComboBox<String> cboNam;
    private JComboBox<String> cboQuy;
    private JPanel pnlLoaiPhong;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboMaPhong;
    private JButton btnTaiLai;
    private JButton btnXuat;
    private JLabel lblStartDate;
    private JLabel lblEndDate;
    private JLabel lblLoaiPhong;
    private JPanel pnlButton;
    private List<RoomStatistic> danhSachKetQua;
    private LoaiPhongService loaiPhongService;
    private List<LoaiPhong> danhSachLoaiPhong;
    private DefaultPieDataset datasetUsage;
    private DefaultPieDataset datasetRevenue;
    private JPanel chartPanel;
    private JFreeChart chartUsage;
    private JFreeChart chartRevenue;
    private ChartPanel usagePanel;
    private ChartPanel revenuePanel;
    private JScrollPane pnlScroll;
    private FillterRoomStatistic baseFilter;
    private JTextField txtFolderPath;
    private JButton btnChooseFolder;
    private JCheckBox btnCheckBox;

    private void init(){
        createTopPanel();
        createMainPanel();
    }

    public void createMainPanel() {
        pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlScroll = new JScrollPane();
        pnlScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pnlScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pnlScroll.setViewportView(pnlMain);
        pnlScroll.getVerticalScrollBar().setUnitIncrement(16);
        createFilterPanel2();
        createResultPanel2();
        createTableResult();
        this.add(pnlScroll, BorderLayout.CENTER);
    }
    public RoomProductivityPanel() {
        setLayout(new BorderLayout());
        this.loaiPhongDAO = new LoaiPhongDAO();
        this.loaiPhongService = new LoaiPhongServiceImpl();
        init();
        //run khi card được show
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadRoomCategoryList();
            }
        });
    }
    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("THỐNG KÊ HIỆU SUẤT PHÒNG", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);

        pnlTop.setPreferredSize(new Dimension(0, 40));
        pnlTop.setMinimumSize(new Dimension(0, 40));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        add(pnlTop);
    }

    private void createFilterPanel2(){
        filterPanel = new JPanel(new GridLayout(2, 2, 15, 10)); // 3 dòng, 3 cột, khoảng cách ngang-dọc
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CustomUI.tableBorder),
                BorderFactory.createEmptyBorder(5,10, 5,10)));

        // === Hàng 1 ===
        pnlKhoang = new JPanel();
        pnlKhoang.setOpaque(false);
        lblStartDate = new JLabel("Ngày bắt đầu");
        lblStartDate.setFont(CustomUI.smallFont);
        startChooser = new DateChooser();
        endChooser = new DateChooser();
        pnlKhoang.add(lblStartDate);
        pnlKhoang.add(startChooser);
        pnlTo = new JPanel();
        pnlTo.setOpaque(false);
        lblEndDate = new JLabel("Ngày kết thúc");
        lblEndDate.setFont(CustomUI.smallFont);
        pnlTo.add(lblEndDate);
        pnlTo.add(endChooser);

        pnlNam = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlNam.setOpaque(false);

        btnCheckBox = new JCheckBox("Tùy chọn");
        btnCheckBox.setFont(CustomUI.smallFont);
        btnCheckBox.addActionListener( e-> {
            if(btnCheckBox.isSelected()){
                cboNam.setEnabled(true);
                cboQuy.setEnabled(true);
                this.startChooser.setEnabled(false);
                this.endChooser.setEnabled(false);
            }
            else{
                cboNam.setEnabled(false);
                cboQuy.setEnabled(false);
                this.startChooser.setEnabled(true);
                this.endChooser.setEnabled(true);
            }
        });
        pnlNam.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
        cboNam = new JComboBox<>(new String[]{"2023", "2024", "2025", "2026"});
        cboNam.setPreferredSize(new Dimension(100, 30));
        cboNam.addActionListener(e ->{
            handlePeriodTime();
            handleBaseCase();
        });

        cboQuy = new JComboBox<>(new String[]{"Quý 1", "Quý 2", "Quý 3", "Quý 4"});
        cboQuy.setPreferredSize(new Dimension(100, 30));
        cboQuy.addActionListener(e ->{
            handlePeriodTime();
            handleBaseCase();
        });
        pnlNam.add(btnCheckBox);
        pnlNam.add(cboNam);
        pnlNam.add(cboQuy);

        cboNam.setEnabled(false);
        cboQuy.setEnabled(false);


        // === Hàng 2 ===
        pnlLoaiPhong = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlLoaiPhong.setOpaque(false);
        lblLoaiPhong = new JLabel("Loại phòng");
        lblLoaiPhong.setFont(CustomUI.smallFont);
        pnlLoaiPhong.add(lblLoaiPhong);
        cboLoaiPhong = new JComboBox<>();
        cboLoaiPhong.setPreferredSize(new Dimension(200,30 ));
        pnlLoaiPhong.add(cboLoaiPhong);

        pnlPhong = new JPanel();
        pnlPhong.add(pnlLoaiPhong);
        pnlPhong.setOpaque(false);

        cboLoaiPhong.addActionListener(e ->{
            handleBaseCase();
        });



        btnTaiLai = new JButton("Tải lại");
        btnTaiLai.setPreferredSize(new Dimension(100, 30));
        btnTaiLai.setBackground(CustomUI.blue);
        btnTaiLai.setFont(CustomUI.verySmallFont);
        btnTaiLai.setForeground(CustomUI.white);

        btnTaiLai.addActionListener(e ->{
            handleBaseCase();
        });

        txtFolderPath = new JTextField();
        txtFolderPath.setPreferredSize(new Dimension(200, 30));

        btnChooseFolder = new JButton("Chọn thư mục");
        btnChooseFolder.setFont(CustomUI.smallFont);
        btnChooseFolder.setBackground(CustomUI.blue);
        btnChooseFolder.setForeground(CustomUI.white);

        btnChooseFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtFolderPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        btnXuat = new JButton("Xuất Excel");
        btnXuat.setBackground(CustomUI.darkGreen);
        btnXuat.setFont(CustomUI.verySmallFont);
        btnXuat.setForeground(CustomUI.white);
        btnXuat.setPreferredSize(new Dimension(100, 30));
        pnlButton = new JPanel();
        pnlButton.setOpaque(false);
        pnlButton.add(btnTaiLai);
        pnlButton.add(btnXuat);
        pnlButton.add(btnChooseFolder);

        btnXuat.addActionListener( e->{
            handleBtnExport();
        });


        filterPanel.add(pnlKhoang);
        filterPanel.add(pnlTo);
        filterPanel.add(pnlNam);

        filterPanel.add(pnlPhong);
        filterPanel.add(pnlButton);

        pnlMain.add(filterPanel);
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

    private void handleBaseCase(){
        FillterRoomStatistic filter = validateTime();
        if(filter != null && !filter.equals(this.baseFilter)){
            getListRoomCategoryByFilter(filter);
        }
        reloadForAllCategory();
    }

    private void exportFileExcel(){
        String folderPath = txtFolderPath.getText().trim();
        if (!folderPath.isEmpty()) {
            try {
                String fileName = "ThongKeHieuSuatPhong";
                String filePath = folderPath + "/ThongKeHieuSuatPhong.xlsx";
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

    public void loadRoomCategoryList() {
        this.danhSachLoaiPhong = loaiPhongDAO.layTatCaLoaiPhong();

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Tất cả");
        for(LoaiPhong lp : danhSachLoaiPhong) {
            model.addElement(lp.getTenLoaiPhong());
        }

        // Set model một lần duy nhất
        this.cboLoaiPhong.setModel(model);
    }

    private void reloadForAllCategory() {
        String tenLoaiPhong = (String) cboLoaiPhong.getSelectedItem();
        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        double tongThoiGianDat = 0;
        datasetUsage.clear();
        datasetRevenue.clear();

        if ("Tất cả".equalsIgnoreCase(tenLoaiPhong)) {
            // Gom theo tên loại phòng
            Map<String, RoomStatistic> mapLoaiPhong = new HashMap<>();

            for (RoomStatistic rs : this.danhSachKetQua) {
                String key = rs.getMaLoaiPhong();
                if (!mapLoaiPhong.containsKey(key)) {
                    mapLoaiPhong.put(key, new RoomStatistic());
                    mapLoaiPhong.get(key).setMaLoaiPhong(key);
                    mapLoaiPhong.get(key).setTenLoaiPhong(rs.getTenLoaiPhong());
                    mapLoaiPhong.get(key).setSoLuotDat(0);
                    mapLoaiPhong.get(key).setThoiGianDat(0);
                    mapLoaiPhong.get(key).setDoanhThu(BigDecimal.ZERO);
                }

                RoomStatistic loai = mapLoaiPhong.get(key);
                loai.setSoLuotDat(loai.getSoLuotDat() + rs.getSoLuotDat());
                loai.setThoiGianDat(loai.getThoiGianDat() + rs.getThoiGianDat());
                loai.setDoanhThu(loai.getDoanhThu().add(rs.getDoanhThu()));
            }

            // Đưa dữ liệu vào dataset
            for (RoomStatistic loai : mapLoaiPhong.values()) {
                tongDoanhThu = tongDoanhThu.add(loai.getDoanhThu());
                tongThoiGianDat += loai.getThoiGianDat();
                datasetUsage.setValue(loai.getTenLoaiPhong(), loai.getThoiGianDat());
                datasetRevenue.setValue(loai.getTenLoaiPhong(), loai.getDoanhThu());
            }

            List<RoomStatistic> list = new ArrayList<>(mapLoaiPhong.values());
            list = list.stream().sorted(Comparator.comparingDouble(RoomStatistic::getThoiGianDat).reversed()).toList();
            fillTable(list);
        } else {
            // Chỉ lấy theo phòng trong loại được chọn
            for (RoomStatistic rs : this.danhSachKetQua) {
                if (tenLoaiPhong.equalsIgnoreCase(rs.getTenLoaiPhong())) {
                    tongDoanhThu = tongDoanhThu.add(rs.getDoanhThu());
                    tongThoiGianDat += rs.getThoiGianDat();
                    datasetUsage.setValue(rs.getTenPhong(), rs.getThoiGianDat());
                    datasetRevenue.setValue(rs.getTenPhong(), rs.getDoanhThu());
                }
            }
            fillTable(null);
        }
        refreshCharts();
    }

    private void handlePeriodTime(){
        int year = Integer.parseInt((String) Objects.requireNonNull(this.cboNam.getSelectedItem()));
        String selectedQuarter = (String) Objects.requireNonNull(this.cboQuy.getSelectedItem());

        LocalDate startDate = null;
        LocalDate endDate = null;

        switch (selectedQuarter) {
            case "Quý 1" -> {
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 3, 31);
            }
            case "Quý 2" -> {
                startDate = LocalDate.of(year, 4, 1);
                endDate = LocalDate.of(year, 6, 30);
            }
            case "Quý 3" -> {
                startDate = LocalDate.of(year, 7, 1);
                endDate = LocalDate.of(year, 9, 30);
            }
            case "Quý 4" -> {
                startDate = LocalDate.of(year, 10, 1);
                endDate = LocalDate.of(year, 12, 31);
            }
            default -> throw new IllegalArgumentException("Quý không hợp lệ: " + selectedQuarter);
        }
        this.startChooser.setDate(startDate);
        this.endChooser.setDate(endDate);
    }

    private void getListRoomCategoryByFilter(FillterRoomStatistic filter){
        this.danhSachKetQua = loaiPhongService.getListRoomCategoryByFilter(filter);
    }

    private FillterRoomStatistic validateTime(){
        LocalDate startDate = startChooser.getDate();
        LocalDate endDate = endChooser.getDate();
        LocalDate today = LocalDate.now();

        if (startDate == null || endDate == null) {
           JOptionPane.showMessageDialog(null,"Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc");
           return null;
        }
        if (startDate.isAfter(today)) {
            JOptionPane.showMessageDialog(null,"Ngày bắt đầu không được sau ngày hiện tại");
            return null;
        }
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(null,"Ngày bắt đầu không được sau ngày kết thúc");
            return null;
        }

        return new FillterRoomStatistic(
            Timestamp.valueOf(startDate.atTime(LocalTime.MIN)),
                Timestamp.valueOf(endDate.atTime(LocalTime.MAX))
        );
    }

    private void createResultPanel2() {
        // Panel chứa 2 biểu đồ (chia đôi ngang)
        chartPanel = new JPanel(new GridLayout(1, 2, 20, 10));
        chartPanel.setBackground(CustomUI.white);

        // === Dataset cho 2 biểu đồ ===
        datasetUsage = new DefaultPieDataset();
        datasetRevenue = new DefaultPieDataset();

        // === Biểu đồ 1: Tỉ lệ sử dụng phòng ===
        chartUsage = ChartFactory.createPieChart(
                "Biểu đồ thống kê tỉ lệ thời gian sử dụng phòng",
                datasetUsage,
                true, true, false
        );

        // === Biểu đồ 2: Tỉ lệ doanh thu phòng ===
        chartRevenue = ChartFactory.createPieChart(
                "Biểu đồ thống kê doanh thu loại phòng",
                datasetRevenue,
                true, true, false
        );

        // Panel chứa từng biểu đồ
        usagePanel = new ChartPanel(chartUsage);
        revenuePanel = new ChartPanel(chartRevenue);
        usagePanel.setPreferredSize(new Dimension(400, 300));
        revenuePanel.setPreferredSize(new Dimension(400, 300));

        chartPanel.add(usagePanel);
        chartPanel.add(revenuePanel);

        pnlMain.add(chartPanel);
    }

    private void refreshCharts() {
        // Refresh plot để hiển thị lại label và legend
        PiePlot plotUsage = (PiePlot) chartUsage.getPlot();
        plotUsage.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1}h ({2})",
                new DecimalFormat("0"),
                new DecimalFormat("0.00%")
        ));

        PiePlot plotRevenue = (PiePlot) chartRevenue.getPlot();
        plotRevenue.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})",
                new DecimalFormat("#,###"),
                new DecimalFormat("0.00%")
        ));
        // Force repaint
        usagePanel.repaint();
        revenuePanel.repaint();
    }


    private void fillTable(List<RoomStatistic> listCategory){
        model.setRowCount(0);
        if(listCategory == null){
            List<RoomStatistic> danhSachKetQua = this.danhSachKetQua;
            String selectedCategory = (String) this.cboLoaiPhong.getSelectedItem();
            if(!"Tất cả".equalsIgnoreCase(selectedCategory)){
                danhSachKetQua = this.danhSachKetQua.stream()
                        .filter(rs -> rs.getTenLoaiPhong().equalsIgnoreCase(selectedCategory))
                        .sorted(Comparator.comparing(RoomStatistic::getSoLuotDat).reversed())
                                .toList();
            }
            for(RoomStatistic rs : danhSachKetQua){
                model.addRow(rs.getObject());
            }
            if(danhSachKetQua.isEmpty()){
                JOptionPane.showMessageDialog(null, "Không tìm thấy kết quả");
            }
        }
        else {
            if(danhSachKetQua.isEmpty()){
                JOptionPane.showMessageDialog(null, "Không tìm thấy kết quả");
            }
            for(RoomStatistic rs : listCategory){
                model.addRow(new Object[]{
                        rs.getMaLoaiPhong(),
                        rs.getTenLoaiPhong(),
                        null,
                        null,
                        rs.getSoLuotDat() +" lượt",
                        rs.getThoiGianDat() + " giờ",
                        formatCurrency(rs.getDoanhThu())
                });
            }
        }
    }


    private void createTableResult(){
        String[] cols = {"Mã loại phòng", "Tên loại phòng", "Mã phòng", "Tên phòng", "Số lượt đặt phòng", "Số giờ sử dụng", "Tổng doanh thu"};
        model = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Không cho phép chỉnh sửa thông tin trong các ô của table
        };

        JTable table = new JTable(model) { // Tạo JTable mới dựa trên model
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                // prepareRenderer được gọi mỗi khi JTable vẽ 1 cell.
                Component c = super.prepareRenderer(renderer, row, column);

                // reuse font constant (không new font mỗi cell)
                c.setFont(CustomUI.TABLE_FONT);

                if (!isRowSelected(row)) {
                    // reuse color constant
                    c.setBackground(row % 2 == 0 ? CustomUI.ROW_ODD : CustomUI.ROW_EVEN);
                } else {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                }
                return c;
            }
        };

        table.setRowHeight(48);
        table.setFont(CustomUI.TABLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setBackground(CustomUI.TABLE_HEADER_BACKGROUND);
        header.setForeground(CustomUI.TABLE_HEADER_FOREGROUND);
        header.setFont(CustomUI.HEADER_FONT);
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

        pnlMain.add(scrollPane);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VND";
        value = PriceFormat.lamTronDenHangNghin(value);
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(value);
    }
}

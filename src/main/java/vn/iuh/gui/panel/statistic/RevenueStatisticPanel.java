package vn.iuh.gui.panel.statistic;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.DateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RevenueStatisticPanel extends JPanel {
    private JPanel pnlTop;
    private JLabel lblTop;
    private JPanel pnlFilter;
    private JPanel pnlMain;
    private JLabel lblEmployee;
    private JLabel lblDisplayType;
    private DateChooser datePickerEnd;
    private DateChooser datePickerStart;
    private JPanel pnlResult;
    private DefaultTableModel model;
    private JTable table;
    private JScrollPane scrollTable;
    private JPanel pnlVisualDisplay;
    private CardLayout cardLayout;

    private void init(){
        createTopPanel();
        createMainPanel();
    }

    public void createMainPanel() {
        pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        createFilterPanel();
        createResultPanel();
        this.add(pnlMain, BorderLayout.CENTER);
    }
    public RevenueStatisticPanel() {
        setLayout(new BorderLayout());
        init();
    }
    private void createTopPanel(){
        pnlTop = new JPanel();
        lblTop = new JLabel("Thống kê doanh thu");
        lblTop.setFont(CustomUI.normalFont);
        lblTop.setForeground(CustomUI.white);
        pnlTop.setBackground(CustomUI.lightBlue);
        pnlTop.add(lblTop);
        pnlTop.setPreferredSize(new Dimension(0, 35));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");
        this.add(pnlTop, BorderLayout.NORTH);
    }

    private void createFilterPanel(){
        JPanel pnlFilter = new JPanel(new GridLayout(2, 2, 5, 5));

        // Ô [0,0] StartTime
        JPanel pnlStartTime = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblStartTime = new JLabel("Thời gian bắt đầu:");
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
        lblEmployee = new JLabel("Chọn nhân viên");
        lblEmployee.setFont(CustomUI.smallFont);
        lblEmployee.setForeground(CustomUI.black);
        JComboBox<String> cmbEmployee = new JComboBox<>(new String[]{"Alice", "Bob", "Charlie"});
        JButton btnReLoad = new JButton("Tải lại");
        btnReLoad.setSize(new Dimension(20, 10));
        btnReLoad.setFont(CustomUI.smallFont);
        btnReLoad.setForeground(CustomUI.white);
        btnReLoad.setBackground(CustomUI.purple);
        JButton btnSearch = new JButton("Xuất file");
        btnSearch.setFont(CustomUI.smallFont);
        btnSearch.setSize(new Dimension(20, 10));
        btnSearch.setForeground(CustomUI.white);
        btnSearch.setBackground(CustomUI.lightGreen);
        pnlEmployee.add(lblEmployee);
        pnlEmployee.add(cmbEmployee);
        pnlEmployee.add(btnReLoad);
        pnlEmployee.add(btnSearch);
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
        group.add(radTable);
        pnlRadio.add(lblDisplayType);
        pnlRadio.add(radGraph);
        pnlRadio.add(radTable);
        pnlRadio.setBackground(CustomUI.white);

        // Thêm tất cả vào pnlFilter
        pnlFilter.add(pnlStartTime);   // [0,0]
        pnlFilter.add(pnlEmployee);    // [0,1]
        pnlFilter.add(pnlEndTime);     // [1,0]
        pnlFilter.add(pnlRadio);       // [1,1]

        pnlFilter.setBackground(CustomUI.white);
        pnlMain.add(pnlFilter);
    }
    private void createResultPanel(){
//        pnlMain.add(Box.createVerticalStrut(600));
        pnlResult = new JPanel();


        pnlVisualDisplay = new JPanel();
        cardLayout = new CardLayout();
        pnlVisualDisplay.setLayout(cardLayout);
//        pnlVisualDisplay.add(scrollTable, "table");
    }

    private void createTableResult(){
        String[] cols = {"Mã hóa đơn", "Khách hàng", "Mã phòng", "Nhân viên", "Ngày lập", "Tiền phòng","Dịch vụ", "Thuế", "Tổng tiền"};
        model = new DefaultTableModel(null, cols);
        table = new JTable(model);
        scrollTable = new JScrollPane(table);
        cardLayout.show(scrollTable, "table");
    }

    private void createCharPanel(){

    }


}

package vn.iuh.gui.panel.statistic;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.DateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;


public class RevenueStatisticPanel extends JPanel {
    private static final Font FONT_LABEL  = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);
    private static final Font TABLE_FONT = FONT_LABEL;
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 15);
    private static final Color ROW_ALT_COLOR = new Color(247, 249, 250);
    private static final Color ROW_SELECTED_COLOR = new Color(210, 230, 255);

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
    private JPanel pnlTextDisplay;



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
        btnSearch.setBackground(CustomUI.green);
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

        // Thêm ActionListener để chuyển đổi giữa hai card panel
        radGraph.addActionListener(e -> cardLayout.show(pnlVisualDisplay, "chart"));
        radTable.addActionListener(e -> cardLayout.show(pnlVisualDisplay, "table"));
        radTable.setSelected(true); // Mặc định hiển thị bảng

        // Thêm tất cả vào pnlFilter
        pnlFilter.add(pnlStartTime);   // [0,0]
        pnlFilter.add(pnlEmployee);    // [0,1]
        pnlFilter.add(pnlEndTime);     // [1,0]
        pnlFilter.add(pnlRadio);       // [1,1]

        pnlFilter.setBackground(CustomUI.white);
        pnlMain.add(pnlFilter);
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
        JLabel lblDeposit = new JLabel("Tiền cọc:");
        lblDeposit.setFont(CustomUI.smallFont);
        lblDeposit.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblDeposit, gbc);
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
        JLabel lblTotalInvoiceValue = new JLabel("30");
        lblTotalInvoiceValue.setFont(CustomUI.smallFont);
        lblTotalInvoiceValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTotalInvoiceValue, gbc); // Tổng số hóa đơn
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 180, 5, 30);
        JLabel lblDepositValue = new JLabel("25.380.000");
        lblDepositValue.setFont(CustomUI.smallFont);
        lblDepositValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblDepositValue, gbc); // Tiền cọc
        gbc.gridx = 2;
        gbc.insets = new Insets(5, 200, 5, 30);
        JLabel lblTotalRevenueValue = new JLabel("1.153.570");
        lblTotalRevenueValue.setFont(CustomUI.smallFont);
        lblTotalRevenueValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTotalRevenueValue, gbc); // Tổng doanh thu

        // Hàng 2 - Giá trị
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(25, 200, 5, 30);
        gbc.weightx = 1.0;
        JLabel lblServiceValue = new JLabel("396.200");
        lblServiceValue.setFont(CustomUI.smallFont);
        lblServiceValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblServiceValue, gbc); // Dịch vụ
        gbc.gridx = 1;
        gbc.insets = new Insets(25, 180, 5, 30);
        JLabel lblRoomValue = new JLabel("652.500");
        lblRoomValue.setFont(CustomUI.smallFont);
        lblRoomValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblRoomValue, gbc); // Tiền phòng
        gbc.gridx = 2;
        gbc.insets = new Insets(25, 200, 5, 30);
        JLabel lblTaxValue = new JLabel("0");
        lblTaxValue.setFont(CustomUI.smallFont);
        lblTaxValue.setForeground(CustomUI.black);
        pnlTextDisplay.add(lblTaxValue, gbc); // Thuế

        pnlResult.add(pnlTextDisplay, BorderLayout.SOUTH);
        pnlMain.add(pnlResult);
    }

    private void createTableResult(){
        String[] cols = {"Mã hóa đơn", "Khách hàng", "Nhân viên", "Ngày lập", "Tiền phòng","Dịch vụ", "Thuế", "Tổng tiền"};
        DefaultTableModel model = new DefaultTableModel(null, cols) {
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

        pnlVisualDisplay.add(scrollPane, "table");
    }

    private void createCharPanel(){
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout());
        JLabel lblChart = new JLabel("Biểu đồ doanh thu", SwingConstants.CENTER);
        lblChart.setFont(CustomUI.normalFont);
        chartPanel.add(lblChart, BorderLayout.CENTER);
        pnlVisualDisplay.add(chartPanel, "chart");
    }
}

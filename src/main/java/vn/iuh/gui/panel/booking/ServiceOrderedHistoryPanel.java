package vn.iuh.gui.panel.booking;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.dto.repository.RoomUsageServiceInfo;
import vn.iuh.dto.response.RoomUsageServiceResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.GoiDichVuService;
import vn.iuh.service.impl.GoiDichVuServiceImpl;
import vn.iuh.util.PriceFormat;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ServiceOrderedHistoryPanel extends JPanel {
    private String maChiTietDatPhong;
    private GoiDichVuService goiDichVuService;

    // Info labels
    private JLabel lblMaDatPhong;
    private JLabel lblTongDichVu;
    private JLabel lblTongTien;

    // Table
    private JTable tblServices;
    private DefaultTableModel servicesModel;

    private DecimalFormat priceFormatter = PriceFormat.getPriceFormatter();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ServiceOrderedHistoryPanel(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.goiDichVuService = new GoiDichVuServiceImpl();

        setLayout(new BorderLayout());
        init();
        loadServices();
    }

    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createTopPanel();
        createInfoPanel();
        createServicesTable();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CustomUI.blue);
        topPanel.setPreferredSize(new Dimension(0, 50));
        topPanel.setMinimumSize(new Dimension(0, 50));
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        topPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        // Title
        JLabel lblTitle = new JLabel("LỊCH SỬ GỌI DỊCH VỤ", SwingConstants.CENTER);
        lblTitle.setForeground(CustomUI.white);
        lblTitle.setFont(CustomUI.bigFont);

        topPanel.add(lblTitle, BorderLayout.CENTER);

        add(topPanel);
    }

    private void createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        infoPanel.setMinimumSize(new Dimension(0, 80));
        infoPanel.setPreferredSize(new Dimension(0, 80));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize labels
        lblMaDatPhong = new JLabel();
        lblMaDatPhong.setFont(CustomUI.smallFont);

        lblTongDichVu = new JLabel();
        lblTongDichVu.setFont(CustomUI.smallFont);

        lblTongTien = new JLabel();
        lblTongTien.setFont(CustomUI.smallFont);

        // Row 1: Mã đặt phòng and Tổng dịch vụ
        addInfoRow(infoPanel, gbc, 0, 0, "Mã đặt phòng:", lblMaDatPhong);

        // Row 2: Tổng đơn gọi dịch vụ + Tổng tiền
        addInfoRow(infoPanel, gbc, 1, 0, "Tổng đơn gọi dịch vụ:", lblTongDichVu);
        addInfoRow(infoPanel, gbc, 1, 2, "Tổng tiền:", lblTongTien);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        wrapper.add(infoPanel, BorderLayout.CENTER);

        add(wrapper);
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, int startCol, String labelText, JLabel valueLabel) {
        gbc.gridy = row;
        gbc.gridx = startCol;
        gbc.weightx = 0.0;

        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = startCol + 1;
        gbc.weightx = 0.5;

        valueLabel.setFont(CustomUI.smallFont);
        panel.add(valueLabel, gbc);
    }

    private void createServicesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Create collapsible title panel
        JPanel titlePanel = createCollapsibleTitlePanel("Danh sách dịch vụ");

        // Create table
        String[] columnNames = {"Đơn DV", "Phòng", "Dịch vụ", "Số lượng", "Đơn giá", "Được tặng", "Thành tiền"};
        servicesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblServices = createStyledTable(servicesModel);

        // Set dynamic column widths
        tblServices.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int tableWidth = tblServices.getWidth();
                TableColumnModel columnModel = tblServices.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth((int) (tableWidth * 0.15)); // 10% - Đơn DV
                columnModel.getColumn(1).setPreferredWidth((int) (tableWidth * 0.12)); // 15% - Phòng
                columnModel.getColumn(2).setPreferredWidth((int) (tableWidth * 0.20)); // 20% - Dịch vụ
                columnModel.getColumn(3).setPreferredWidth((int) (tableWidth * 0.10)); // 15% - Số lượng
                columnModel.getColumn(4).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Đơn giá
                columnModel.getColumn(5).setPreferredWidth((int) (tableWidth * 0.13)); // 10% - Được tặng
                columnModel.getColumn(6).setPreferredWidth((int) (tableWidth * 0.15)); // 15% - Thành tiền
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblServices);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 400)); // Set initial height

        tablePanel.add(titlePanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel);
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CustomUI.ROW_ODD : CustomUI.ROW_EVEN);
                } else {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                }
                return c;
            }
        };

        table.setFont(CustomUI.TABLE_FONT);
        table.setRowHeight(40);
        table.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        table.setGridColor(CustomUI.tableBorder);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Enhanced header styling
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 40));
        table.getTableHeader().setFont(CustomUI.HEADER_FONT);
        table.getTableHeader().setBackground(CustomUI.blue);
        table.getTableHeader().setForeground(CustomUI.white);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        return table;
    }

    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            component.setFont(CustomUI.TABLE_FONT);

            if (isSelected) {
                component.setBackground(CustomUI.ROW_SELECTED_COLOR);
                component.setForeground(Color.BLACK);
            } else {
                if (row % 2 == 0) {
                    component.setBackground(CustomUI.ROW_EVEN);
                } else {
                    component.setBackground(CustomUI.ROW_ODD);
                }
                component.setForeground(Color.BLACK);
            }

            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));

            return component;
        }
    }

    private JPanel createCollapsibleTitlePanel(String title) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CustomUI.COLLAPSIBLE_BG);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(CustomUI.normalFont);

        // Add arrow icon to indicate collapsible state
        JLabel lblArrow = new JLabel("▼");
        lblArrow.setFont(new Font("Arial", Font.BOLD, 12));
        lblArrow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setBackground(CustomUI.COLLAPSIBLE_BG);
        leftPanel.add(lblArrow);
        leftPanel.add(lblTitle);

        titlePanel.add(leftPanel, BorderLayout.WEST);
        titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Make the title panel clickable to toggle collapse/expand
        titlePanel.addMouseListener(new MouseAdapter() {
            boolean isCollapsed = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                isCollapsed = !isCollapsed;
                lblArrow.setText(isCollapsed ? "►" : "▼");

                // Find the parent panel and toggle its scroll pane visibility
                Container parent = titlePanel.getParent();
                if (parent != null) {
                    Component[] components = parent.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JScrollPane) {
                            comp.setVisible(!isCollapsed);
                            parent.revalidate();
                            parent.repaint();
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                titlePanel.setBackground(CustomUI.COLLAPSIBLE_HOVER);
                leftPanel.setBackground(CustomUI.COLLAPSIBLE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                titlePanel.setBackground(CustomUI.COLLAPSIBLE_BG);
                leftPanel.setBackground(CustomUI.COLLAPSIBLE_BG);
            }
        });

        return titlePanel;
    }

    private void loadServices() {
        servicesModel.setRowCount(0);

        // Set mã đặt phòng
        lblMaDatPhong.setText(maChiTietDatPhong);

        // Fetch services from service
        List<RoomUsageServiceResponse> services = goiDichVuService.timTatCaDonGoiDichVuBangMaChiTietDatPhong(maChiTietDatPhong);

        if (services == null || services.isEmpty()) {
            lblTongDichVu.setText("0");
            lblTongTien.setText("0 VND");
            return;
        }

        // Calculate totals
        int totalServices = services.size();
        double totalAmount = 0;

        for (RoomUsageServiceResponse service : services) {
            Object[] rowData = new Object[7];
            rowData[0] = service.getRoomUsageServiceId();
            rowData[1] = service.getRoomName();
            rowData[2] = service.getServiceName();
            rowData[3] = service.getQuantity();
            rowData[4] = priceFormatter.format(service.getPrice()) + " VND";

            // Display icon for gifted services
            if (service.isGifted()) {
                rowData[5] = "Có";
            } else {
                rowData[5] = "Không";
            }

            // Calculate total for this service
            double serviceTotal = service.getTotalPrice();
            rowData[6] = priceFormatter.format(serviceTotal) + " VND";

            servicesModel.addRow(rowData);

            totalAmount = totalAmount + serviceTotal;
        }

        // Update totals
        lblTongDichVu.setText(String.valueOf(totalServices));
        lblTongTien.setText(priceFormatter.format(totalAmount) + " VND");
    }
}


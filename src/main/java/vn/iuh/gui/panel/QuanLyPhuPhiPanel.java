package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.dao.*;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.entity.NhanVien;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.PhuPhiDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class QuanLyPhuPhiPanel extends JPanel {
    private static final int SEARCH_CONTROL_HEIGHT = 40;
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, SEARCH_CONTROL_HEIGHT);

    private static final int SEARCH_TYPE_WIDTH = 180;
    private static final int SEARCH_BUTTON_WIDTH = 110;
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(SEARCH_BUTTON_WIDTH, SEARCH_CONTROL_HEIGHT);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(220, 50);
    private static final int TOP_PANEL_HEIGHT = 40;

    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);
    private static final Font TABLE_FONT = FONT_LABEL;
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 15);
    private static final Color ROW_ALT_COLOR = new Color(250, 247, 249);
    private static final Color ROW_SELECTED_COLOR = new Color(210, 230, 255);
    private PhuPhiDAO phuPhiDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<ThongTinPhuPhi> dsPhuPhi;

    public QuanLyPhuPhiPanel() {
        this.phuPhiDAO = new PhuPhiDAO();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
        loadDataToTable();
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý phụ phí", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        add(pnlTop);
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);

        try {
            this.dsPhuPhi = phuPhiDAO.getDanhSachPhuPhi();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu phụ phí: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (ThongTinPhuPhi thongTinPhuPhi : this.dsPhuPhi) {
            Object[] row = {
                    thongTinPhuPhi.getMaPhuPhi(),
                    thongTinPhuPhi.getTenPhuPhi(),
                    thongTinPhuPhi.isLaPhanTram() ? "TỈ LỆ PHẦM TRĂM" : "VNĐ",
                    thongTinPhuPhi.getGiaHienTai(),
            };
            tableModel.addRow(row);
        }
    }

    private void init() {
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createAdditionalFeeListPanel();

//        this.addComponentListener(new java.awt.event.ComponentAdapter() {
//            @Override
//            public void componentShown(java.awt.event.ComponentEvent e) {
//                // Tự động gọi refreshData() mỗi khi panel này được hiển thị
//                refreshData();
//            }
//        });

    }
    private void createAdditionalFeeListPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(CustomUI.white);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        String[] columns = {"Mã phụ phí", "Tên phụ phí", "Đơn vị tính","Giá hiện tại"};

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
                    int selectedRowView = table.getSelectedRow();
                    if(selectedRowView != -1){
                        int selectedRowModel = table.convertRowIndexToModel(selectedRowView);

                        ThongTinPhuPhi selectedPhuPhi = dsPhuPhi.get(selectedRowModel);

                        Window owner = SwingUtilities.getWindowAncestor(QuanLyPhuPhiPanel.this);
                        PhuPhiDialog dialog = new PhuPhiDialog(owner, selectedPhuPhi);
                        dialog.setVisible(true);

                        loadDataToTable();
                    }
                }
            }
        });

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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CustomUI.white);

        wrap.add(scrollPane, BorderLayout.CENTER);
        add(wrap);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test QuanLyNhanVienPanel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);

            JPanel panel = new QuanLyPhuPhiPanel();
            frame.add(panel);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}



package vn.iuh.gui.dialog;

import vn.iuh.dto.response.RoomCategoryPriceHistory;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.NoiThat;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.NoiThatService;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class ChiTietLoaiPhongDialog extends JDialog {

    private final LoaiPhongService loaiPhongService;
    private final NoiThatService noiThatService;
    private final String maLoaiPhong;

    private final JLabel lblCode = new JLabel();
    private final JLabel lblName = new JLabel();
    private final JLabel lblPeople = new JLabel();
    private final JLabel lblType = new JLabel();

    private final DefaultListModel<String> furnitureModel = new DefaultListModel<>();
    private final JList<String> listFurniture = new JList<>(furnitureModel);

    private final JLabel lblGiaGio = new JLabel();
    private final JLabel lblGiaNgay = new JLabel();

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[] {"Ngày thay đổi", "Giá giờ cũ", "Giá ngày cũ", "Giá giờ mới", "Giá ngày mới", "Nhân viên"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable historyTable = new JTable(historyModel);

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ChiTietLoaiPhongDialog(Frame owner,
                                  LoaiPhongService loaiPhongService,
                                  NoiThatService noiThatService,
                                  String maLoaiPhong) {
        super(owner, "Chi tiết loại phòng - " + (maLoaiPhong == null ? "" : maLoaiPhong), true);
        this.loaiPhongService = loaiPhongService;
        this.noiThatService = noiThatService;
        this.maLoaiPhong = maLoaiPhong;

        initUI();
        setPreferredSize(new Dimension(1400, 600));
        pack();
        setLocationRelativeTo(owner);

        loadDataAsync();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(root);

        // Top: info + price
        JPanel top = new JPanel(new BorderLayout(8,8));
        JPanel info = new JPanel(new GridLayout(2, 4, 8, 8));
        info.setPreferredSize(new Dimension(0, 80));
        Font valueFont = CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.BOLD, 14f) : new Font("Arial", Font.BOLD, 14);

        info.add(makeInfoPanel("Mã loại:", lblCode, valueFont));
        info.add(makeInfoPanel("Tên loại:", lblName, valueFont));
        info.add(makeInfoPanel("Số người tối đa:", lblPeople, valueFont));
        info.add(makeInfoPanel("Phân loại:", lblType, valueFont));
        top.add(info, BorderLayout.CENTER);

        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setBorder(BorderFactory.createTitledBorder("Giá hiện tại"));
        lblGiaGio.setFont(valueFont);
        lblGiaNgay.setFont(valueFont);
        pricePanel.add(lblGiaGio);
        pricePanel.add(Box.createVerticalStrut(6));
        pricePanel.add(lblGiaNgay);
        top.add(pricePanel, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);

        // Center: furniture + history
        JPanel center = new JPanel(new GridLayout(1,2,12,12));

        // Left: furniture — make it compact and pretty
        JPanel left = new JPanel(new BorderLayout(6,6));
        left.setBorder(BorderFactory.createTitledBorder("Nội thất (mẫu)"));

        listFurniture.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
                lbl.setFont(CustomUI.TABLE_FONT != null ? CustomUI.TABLE_FONT.deriveFont(13f) : lbl.getFont().deriveFont(13f));
                return lbl;
            }
        });
        listFurniture.setFixedCellHeight(30);
        left.add(new JScrollPane(listFurniture), BorderLayout.CENTER);
        center.add(left);

        // Right: history table
        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setBorder(BorderFactory.createTitledBorder("Lịch sử thay đổi giá"));
        historyTable.setModel(historyModel);
        styleTable(historyTable);
        historyTable.setRowHeight(40);
        JScrollPane sp = new JScrollPane(historyTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        right.add(sp, BorderLayout.CENTER);
        center.add(right);

        root.add(center, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel makeInfoPanel(String label, JLabel valueLabel, Font valueFont) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(label);
        l.setFont(CustomUI.HEADER_FONT != null ? CustomUI.HEADER_FONT.deriveFont(Font.PLAIN, 12f) : new Font("Arial", Font.PLAIN, 12));
        valueLabel.setFont(valueFont);
        p.add(l, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 40));
        table.getTableHeader().setFont(CustomUI.HEADER_FONT);
        table.getTableHeader().setBackground(CustomUI.blue);
        table.getTableHeader().setForeground(CustomUI.white);
        table.getTableHeader().setOpaque(true);

        // căn giữa, zebra, font, border
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final DecimalFormat df = new DecimalFormat("#,###");
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                comp.setFont(CustomUI.TABLE_FONT);
                if (isSelected) {
                    comp.setBackground(CustomUI.ROW_SELECTED_COLOR);
                    comp.setForeground(CustomUI.black);
                } else {
                    comp.setBackground(row % 2 == 0 ? CustomUI.ROW_EVEN : CustomUI.ROW_ODD);
                    comp.setForeground(CustomUI.black);
                }
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                try {
                    int modelCol = t.convertColumnIndexToModel(column);
                    if (modelCol >= 1 && modelCol <= 4 && value instanceof String) {
                        // already formatted string from dialog
                    }
                } catch (Exception ignored) {}
                return comp;
            }
        });

        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void loadDataAsync() {
        SwingWorker<Void, Void> wk = new SwingWorker<>() {
            private LoaiPhong lp = null;
            private List<NoiThat> furn = Collections.emptyList();
            private double latestGiaGio = 0.0;
            private double latestGiaNgay = 0.0;
            private List<HistoryRow> historyRows = new ArrayList<>();

            @Override
            protected Void doInBackground() {
                try {
                    // 1) lấy LoaiPhong từ service
                    try {
                        if (loaiPhongService != null) lp = loaiPhongService.getRoomCategoryByIDV2(maLoaiPhong);
                    } catch (Throwable ignore) { lp = null; }

                    // 2) lấy danh sách nội thất qua service
                    try {
                        if (noiThatService != null) furn = noiThatService.getNoiThatByLoaiPhong(maLoaiPhong);
                    } catch (Throwable ignore) { furn = Collections.emptyList(); }

                    // 3) lấy lịch sử giá qua DAO (DAO chứa truy vấn DB)
                    // dùng service (không gọi DAO trực tiếp)
                    try {
                        // lấy latest giá
                        Map<String, Double> latest = loaiPhongService.getLatestPriceMap(maLoaiPhong);
                        latestGiaGio = latest.getOrDefault("gia_gio", 0.0);
                        latestGiaNgay = latest.getOrDefault("gia_ngay", 0.0);

                        // lấy history kèm actor (đã format trong DTO)
                        List<RoomCategoryPriceHistory> phList = loaiPhongService.getPriceHistoryWithActor(maLoaiPhong);
                        for (RoomCategoryPriceHistory p : phList) {
                            historyRows.add(new HistoryRow(
                                    p.getTime(),
                                    p.getGiaGioCu(),
                                    p.getGiaNgayCu(),
                                    p.getGiaGioMoi(),
                                    p.getGiaNgayMoi(),
                                    p.getActorName()
                            ));
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }

                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                // update UI components on EDT
                if (lp != null) {
                    lblCode.setText(Optional.ofNullable(lp.getMaLoaiPhong()).orElse(""));
                    lblName.setText(Optional.ofNullable(lp.getTenLoaiPhong()).orElse(""));
                    lblPeople.setText(String.valueOf(lp.getSoLuongKhach()));
                    lblType.setText(Optional.ofNullable(lp.getPhanLoai()).orElse("Thường"));
                } else {
                    lblCode.setText(maLoaiPhong);
                    lblName.setText("-");
                    lblPeople.setText("-");
                    lblType.setText("-");
                }

                // furniture list
                furnitureModel.clear();
                if (furn != null && !furn.isEmpty()) {
                    for (NoiThat n : furn) {
                        String name = n.getTenNoiThat() != null ? n.getTenNoiThat() : "-";
                        String code = n.getMaNoiThat() != null ? n.getMaNoiThat() : "-";
                        furnitureModel.addElement(name + "  —  (" + code + ")");
                    }
                } else {
                    furnitureModel.addElement("Không có nội thất mẫu.");
                }

                // latest prices
                lblGiaGio.setText("Giá giờ: " + formatVnd(latestGiaGio));
                lblGiaNgay.setText("Giá ngày: " + formatVnd(latestGiaNgay));

                // history table
                historyModel.setRowCount(0);
                for (HistoryRow r : historyRows) {
                    String time = r.time == null ? "" : sdf.format(r.time);
                    historyModel.addRow(new Object[] {
                            time,
                            formatVnd(r.gioCu),
                            formatVnd(r.ngayCu),
                            formatVnd(r.gioMoi),
                            formatVnd(r.ngayMoi),
                            r.actor == null ? "" : r.actor
                    });
                }

                // set preferred widths
                try {
                    historyTable.getColumnModel().getColumn(0).setPreferredWidth(160);
                    historyTable.getColumnModel().getColumn(1).setPreferredWidth(120);
                    historyTable.getColumnModel().getColumn(2).setPreferredWidth(120);
                    historyTable.getColumnModel().getColumn(3).setPreferredWidth(120);
                    historyTable.getColumnModel().getColumn(4).setPreferredWidth(120);
                    historyTable.getColumnModel().getColumn(5).setPreferredWidth(200);
                } catch (Throwable ignored) {}
            }
        };
        wk.execute();
    }

    private double safeDouble(Double v, double fallback) {
        return v == null ? fallback : v;
    }

    private String formatVnd(double v) {
        try {
            if (v <= 0.0) return "0 đ";
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(Math.round(v)) + " đ";
        } catch (Exception e) {
            return String.valueOf(v) + " đ";
        }
    }

    private static class HistoryRow {
        final Date time;
        final double gioCu, ngayCu, gioMoi, ngayMoi;
        final String actor;
        HistoryRow(Date time, double gioCu, double ngayCu, double gioMoi, double ngayMoi, String actor) {
            this.time = time; this.gioCu = gioCu; this.ngayCu = ngayCu; this.gioMoi = gioMoi; this.ngayMoi = ngayMoi; this.actor = actor;
        }
    }
}

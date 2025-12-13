package vn.iuh.gui.dialog;

import vn.iuh.dto.repository.ChangeRoomRecord;
import vn.iuh.dto.response.ReservationDetailResponse;
import vn.iuh.dto.response.ReservationInfoDetailResponse;
import vn.iuh.service.BookingService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.service.impl.DoiPhongServiceImpl;
import vn.iuh.dao.PhongDAO;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.entity.Phong;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.util.DatabaseUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

// Dialog hiển thị lịch sử đổi phòng
public class LichSuDoiPhongDialog extends JDialog {

    private final String maDonDatPhong;
    private final DoiPhongServiceImpl doiPhongService;
    private final BookingService bookingService;
    private final PhongDAO phongDAO;
    private final NhanVienDAO nhanVienDAO;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public LichSuDoiPhongDialog(Frame owner, String maDonDatPhong) {
        super(owner, "Lịch sử đổi phòng - Đơn " + maDonDatPhong, true);
        this.maDonDatPhong = maDonDatPhong;
        this.doiPhongService = new DoiPhongServiceImpl();
        this.bookingService = new BookingServiceImpl();
        this.phongDAO = new PhongDAO();
        this.nhanVienDAO = new NhanVienDAO();

        initComponents();
        setSize(980, 520);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // lấy data
        List<ChangeRoomRecord> records = doiPhongService.layLichSuDoiPhongTheoDon(maDonDatPhong);
        if (records == null) records = List.of();

        // xác định phòng ban đầu
        LinkedHashSet<String> originalRooms = new LinkedHashSet<>();
        try {
            ReservationInfoDetailResponse res = bookingService.getReservationDetailInfo(maDonDatPhong);
            if (res != null && res.getDetails() != null && !res.getDetails().isEmpty()) {
                List<ReservationDetailResponse> details = res.getDetails();
                long min = Long.MAX_VALUE;
                for (ReservationDetailResponse d : details) {
                    try {
                        Timestamp tin = d.getTimeIn();
                        if (tin != null && tin.getTime() < min) min = tin.getTime();
                    } catch (Throwable ignore) { }
                }
                if (min == Long.MAX_VALUE) {
                    for (ReservationDetailResponse d : details) {
                        String roomId = safeRoomIdFromDetail(d);
                        if (roomId != null) originalRooms.add(roomId);
                    }
                } else {
                    for (ReservationDetailResponse d : details) {
                        try {
                            Timestamp tin = d.getTimeIn();
                            if (tin != null && Math.abs(tin.getTime() - min) <= 2000L) {
                                String roomId = safeRoomIdFromDetail(d);
                                if (roomId != null) originalRooms.add(roomId);
                            }
                        } catch (Throwable ignore) { }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        getContentPane().setLayout(new BorderLayout(8, 8));

        // Header — nhẹ nhàng, dùng font chuẩn
        JLabel header = new JLabel("Lịch sử đổi phòng — Đơn: " + maDonDatPhong);
        header.setFont(CustomUI.HEADER_FONT.deriveFont(14f));
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        getContentPane().add(header, BorderLayout.NORTH);

        if (originalRooms.isEmpty()) {
            JPanel p = new JPanel(new BorderLayout(8, 8));
            JLabel lbl = new JLabel("<html><b>Không có phòng ban đầu để hiện lịch sử đổi phòng cho đơn:</b> " + maDonDatPhong + "</html>");
            lbl.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            p.add(lbl, BorderLayout.CENTER);
            JButton close = new JButton("Đóng");
            close.addActionListener(e -> dispose());
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footer.add(close);
            p.add(footer, BorderLayout.SOUTH);
            getContentPane().add(p, BorderLayout.CENTER);
            return;
        }

        // Tabbed: dùng font vừa phải, không quá nhiều spacing
        JTabbedPane tabbed = new JTabbedPane();
        tabbed.setFont(CustomUI.TABLE_FONT.deriveFont(13f));
        tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        for (String originalRoomId : originalRooms) {
            String roomLabel = getRoomName(originalRoomId);

            JPanel panel = new JPanel(new BorderLayout(6,6));
            panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

            DefaultTableModel model = new DefaultTableModel(new Object[]{
                    "Thời điểm", "Phòng cũ", "Phòng mới", "Loại", "Nhân viên"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };

            // build chain (giữ nguyên logic đã sửa — chronological chain)
            List<ChangeRoomRecord> related = new ArrayList<>();
            if (records != null && !records.isEmpty()) {
                List<ChangeRoomRecord> sorted = new ArrayList<>(records);
                sorted.sort(Comparator.comparing(ChangeRoomRecord::getTime, Comparator.nullsLast(Comparator.naturalOrder())));

                Set<String> addedKeys = new HashSet<>();
                String cur = originalRoomId;
                long curTime = Long.MIN_VALUE;
                boolean progressed = true;
                while (progressed) {
                    progressed = false;
                    for (ChangeRoomRecord r : sorted) {
                        if (r == null || r.getOldRoom() == null) continue;
                        if (r.getTime() == null) continue;
                        if (!r.getOldRoom().equalsIgnoreCase(cur)) continue;
                        long rTime = r.getTime().getTime();
                        if (rTime <= curTime) continue;
                        String key = (r.getOldRoom() == null ? "" : r.getOldRoom().toUpperCase())
                                + "|" + (r.getNewRoom() == null ? "" : r.getNewRoom().toUpperCase())
                                + "|" + rTime;
                        if (addedKeys.contains(key)) continue;
                        related.add(r);
                        addedKeys.add(key);
                        String newRoom = r.getNewRoom();
                        if (newRoom != null && !newRoom.isEmpty()) {
                            cur = newRoom;
                            curTime = rTime;
                            progressed = true;
                            break;
                        } else {
                            progressed = false;
                            break;
                        }
                    }
                }
            }

            if (related.isEmpty()) {
                model.addRow(new Object[]{"", getRoomName(originalRoomId), "", "Không có bản ghi đổi phòng", ""});
            } else {
                for (ChangeRoomRecord r : related) {
                    String time = r.getTime() == null ? "" : sdf.format(r.getTime());
                    String oldName = r.getOldRoom() == null ? "" : getRoomName(r.getOldRoom());
                    String newName = r.getNewRoom() == null ? "" : getRoomName(r.getNewRoom());
                    String type = r.getType() == null ? "" : r.getType();
                    String actor = lookupActorNameForRecord(r);
                    model.addRow(new Object[]{ time, oldName, newName, type, actor });
                }
            }

            JTable table = createStyledTable(model);
            table.setRowHeight(36); // hơi thấp hơn, tránh rối mắt
            table.getTableHeader().setFont(CustomUI.HEADER_FONT.deriveFont(13f));

            // maintain column widths but not too wide
            table.getColumnModel().getColumn(0).setPreferredWidth(130);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(180);
            table.getColumnModel().getColumn(3).setPreferredWidth(160);
            table.getColumnModel().getColumn(4).setPreferredWidth(160);

            panel.add(new JScrollPane(table), BorderLayout.CENTER);

            // minimal header per tab content (kept small)
            JLabel lbl = new JLabel("Phòng (ban đầu): " + roomLabel);
            lbl.setFont(CustomUI.TABLE_FONT.deriveFont(13f));
            lbl.setBorder(BorderFactory.createEmptyBorder(4,4,6,4));
            panel.add(lbl, BorderLayout.NORTH);

            // Tab title: bold short label to make it clearer
            String tabTitle = "<html><b>" + roomLabel + "</b></html>";
            tabbed.addTab(tabTitle, panel);
        }

        getContentPane().add(tabbed, BorderLayout.CENTER);

        // bottom close button
        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 34));
        btnClose.addActionListener(e -> dispose());
        ft.add(btnClose);
        getContentPane().add(ft, BorderLayout.SOUTH);
    }

    private String safeRoomIdFromDetail(ReservationDetailResponse d) {
        if (d == null) return null;
        try { String id = d.getRoomId(); if (id != null && !id.isEmpty()) return id; } catch (Throwable ignore) {}
        try { String rn = d.getRoomName(); if (rn != null && !rn.isEmpty()) return rn; } catch (Throwable ignore) {}
        return null;
    }

    private String getRoomName(String roomIdOrName) {
        if (roomIdOrName == null) return "";
        try {
            Phong p = phongDAO.timPhong(roomIdOrName);
            if (p != null && p.getTenPhong() != null && !p.getTenPhong().isEmpty()) return p.getTenPhong();
        } catch (Exception ignore) {}
        return roomIdOrName;
    }

    private String lookupActorNameForRecord(ChangeRoomRecord r) {
        try {
            if (r == null || r.getTime() == null) return "";
            String oldRoom = r.getOldRoom() == null ? "" : r.getOldRoom();
            String newRoom = r.getNewRoom() == null ? "" : r.getNewRoom();
            String patternExact = "Đổi phòng cho đơn " + maDonDatPhong + ": " + oldRoom + " -> " + newRoom + "%";
            String patternFallback = "Đổi phòng cho đơn " + maDonDatPhong + ":%";
            String sql = "SELECT ls.ma_phien_dang_nhap, ls.mo_ta, ls.thoi_gian_tao " +
                    "FROM LichSuThaoTac ls " +
                    "WHERE (ls.mo_ta LIKE ? OR ls.mo_ta LIKE ?) " +
                    "  AND ABS(DATEDIFF(SECOND, ls.thoi_gian_tao, ?)) <= 5 " +
                    "ORDER BY ABS(DATEDIFF(SECOND, ls.thoi_gian_tao, ?)) ASC";
            try (Connection conn = DatabaseUtil.getConnect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, patternExact);
                ps.setString(2, patternFallback);
                ps.setTimestamp(3, r.getTime());
                ps.setTimestamp(4, r.getTime());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String maPhien = rs.getString("ma_phien_dang_nhap");
                        if (maPhien != null && !maPhien.isEmpty()) {
                            String name = nhanVienDAO.findTenNhanVienByPhienDangNhap(maPhien);
                            if (name != null && !name.isEmpty()) return name;
                        }
                    }
                }
            }
        } catch (Throwable ex) { ex.printStackTrace(); }
        return "";
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? CustomUI.ROW_ODD : CustomUI.ROW_EVEN);
                else c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                return c;
            }
        };

        table.setFont(CustomUI.TABLE_FONT);
        table.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR);
        table.setGridColor(CustomUI.tableBorder);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 36));
        table.getTableHeader().setFont(CustomUI.HEADER_FONT.deriveFont(13f));
        table.getTableHeader().setBackground(CustomUI.blue);
        table.getTableHeader().setForeground(CustomUI.white);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CustomUI.tableBorder));

        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        return table;
    }

    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setFont(CustomUI.TABLE_FONT);
            if (isSelected) { component.setBackground(CustomUI.ROW_SELECTED_COLOR); component.setForeground(CustomUI.black); }
            else { component.setBackground(row % 2 == 0 ? CustomUI.ROW_EVEN : CustomUI.ROW_ODD); component.setForeground(CustomUI.black); }
            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));
            return component;
        }
    }

    public static void showDialog(Frame owner, String maDon) {
        LichSuDoiPhongDialog d = new LichSuDoiPhongDialog(owner, maDon);
        d.setVisible(true);
    }
}

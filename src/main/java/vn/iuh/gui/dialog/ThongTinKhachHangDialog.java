package vn.iuh.gui.dialog;

import vn.iuh.dto.response.BookingResponseV2;
import vn.iuh.entity.KhachHang;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ThongTinKhachHangDialog extends JDialog {

    private final CustomerService customerService;
    private final String maKhachHang;

    private JLabel lblMa;
    private JLabel lblTen;
    private JLabel lblCCCD;
    private JLabel lblPhone;

    private JTable bookingTable;
    private DefaultTableModel bookingModel;

    private static final SimpleDateFormat DATE_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ThongTinKhachHangDialog(Window owner, CustomerService customerService, String maKhachHang) {
        super(owner, "Chi tiết khách hàng", ModalityType.APPLICATION_MODAL);
        this.customerService = customerService;
        this.maKhachHang = maKhachHang;

        initUI();
        loadData();
        pack();
        setSize(1000, 620);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(12,12));

        // top: thông tin khách
        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(BorderFactory.createTitledBorder("Thông tin khách hàng"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,8,6,8);
        gc.anchor = GridBagConstraints.WEST;

        lblMa = new JLabel("-");
        lblTen = new JLabel("-");
        lblCCCD = new JLabel("-");
        lblPhone = new JLabel("-");

        // make labels slightly larger to be readable
        Font infoFont = CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.PLAIN, 14f) : new Font("Arial", Font.PLAIN, 14);
        lblMa.setFont(infoFont);
        lblTen.setFont(infoFont);
        lblCCCD.setFont(infoFont);
        lblPhone.setFont(infoFont);

        gc.gridx = 0; gc.gridy = 0;
        top.add(new JLabel("Mã khách hàng:"), gc);
        gc.gridx = 1;
        top.add(lblMa, gc);

        gc.gridx = 0; gc.gridy++;
        top.add(new JLabel("Họ tên:"), gc);
        gc.gridx = 1;
        top.add(lblTen, gc);

        gc.gridx = 0; gc.gridy++;
        top.add(new JLabel("CCCD:"), gc);
        gc.gridx = 1;
        top.add(lblCCCD, gc);

        gc.gridx = 0; gc.gridy++;
        top.add(new JLabel("Điện thoại:"), gc);
        gc.gridx = 1;
        top.add(lblPhone, gc);

        add(top, BorderLayout.NORTH);

        // center: table đơn đặt (format giống bảng danh sách khách hàng)
        String[] cols = {"Mã đơn", "Ngày đặt", "TG nhận phòng", "TG trả phòng", "Loại đơn", "Tiền đặt cọc"};
        bookingModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        bookingTable = new JTable(bookingModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                // font
                c.setFont(CustomUI.TABLE_FONT != null ? CustomUI.TABLE_FONT : new Font("Arial", Font.PLAIN, 14));
                // selection colors
                if (isRowSelected(row)) {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR != null ? CustomUI.ROW_SELECTED_COLOR : new Color(0xE6F0FF));
                    c.setForeground(CustomUI.black != null ? CustomUI.black : Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? (CustomUI.ROW_EVEN != null ? CustomUI.ROW_EVEN : Color.WHITE)
                            : (CustomUI.ROW_ODD != null ? CustomUI.ROW_ODD : new Color(0xF7F9FB)));
                    c.setForeground(CustomUI.black != null ? CustomUI.black : Color.BLACK);
                }
                if (c instanceof JLabel) ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder != null ? CustomUI.tableBorder : new Color(0xE5E7EB)));
                return c;
            }
        };

        bookingTable.setRowHeight(48);
        bookingTable.setFont(CustomUI.TABLE_FONT != null ? CustomUI.TABLE_FONT : new Font("Arial", Font.PLAIN, 14));
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setSelectionBackground(CustomUI.ROW_SELECTED_COLOR != null ? CustomUI.ROW_SELECTED_COLOR : new Color(0xE6F0FF));
        bookingTable.setShowGrid(true);
        bookingTable.setGridColor(CustomUI.tableBorder != null ? CustomUI.tableBorder : new Color(0xE5E7EB));
        bookingTable.setIntercellSpacing(new Dimension(1,1));
        bookingTable.setFillsViewportHeight(true);

        JTableHeader header = bookingTable.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setBackground(CustomUI.blue != null ? CustomUI.blue : new Color(59,130,246));
        header.setForeground(Color.WHITE);
        header.setFont(CustomUI.HEADER_FONT != null ? CustomUI.HEADER_FONT : new Font("Arial", Font.BOLD, 14));
        header.setReorderingAllowed(false);

        // center renderer for columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < bookingTable.getColumnCount(); i++) {
            bookingTable.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane sp = new JScrollPane(bookingTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(0, 320));
        sp.setBorder(BorderFactory.createTitledBorder("Danh sách đơn đặt phòng"));

        add(sp, BorderLayout.CENTER);

        // bottom: đóng
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadData() {
        // load khách
        try {
            KhachHang kh = customerService.getCustomerByID(maKhachHang);
            if (kh != null) {
                lblMa.setText(nonNull(kh.getMaKhachHang()));
                lblTen.setText(nonNull(kh.getTenKhachHang()));
                lblCCCD.setText(nonNull(kh.getCCCD()));
                lblPhone.setText(nonNull(kh.getSoDienThoai()));
            } else {
                // show placeholder if not found
                lblMa.setText(maKhachHang != null ? maKhachHang : "-");
            }

            // load bookings
            bookingModel.setRowCount(0);
            List<BookingResponseV2> bookings = customerService.layDonDatPhongCuaKhach(maKhachHang);
            if (bookings == null || bookings.isEmpty()) {
                bookingModel.addRow(new Object[] {"-", "Không có đơn đặt nào.", "-", "-", "-", "-"});
            } else {
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
                nf.setMaximumFractionDigits(0);
                for (BookingResponseV2 b : bookings) {
                    String ngayDat = b.getThoiGianTao() != null ? DATE_TIME_FMT.format(b.getThoiGianTao()) : "-";
                    String tgNhan = b.getTgNhanPhong() != null ? DATE_TIME_FMT.format(b.getTgNhanPhong()) : "-";
                    String tgTra  = b.getTgTraPhong() != null ? DATE_TIME_FMT.format(b.getTgTraPhong()) : "-";
                    String loai = b.getLoai() != null ? b.getLoai() : "-";
                    String tien = (b.getTienDatCoc() != null) ? (nf.format(b.getTienDatCoc()) + " VNĐ") : "-";
                    bookingModel.addRow(new Object[] { b.getMaDonDatPhong(), ngayDat, tgNhan, tgTra, loai, tien });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin khách/đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String nonNull(String s) {
        return s == null ? "-" : s;
    }
}

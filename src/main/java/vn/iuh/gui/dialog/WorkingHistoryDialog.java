package vn.iuh.gui.dialog;
import vn.iuh.constraint.ActionType;
import vn.iuh.dao.HoaDonDAO;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.PhienDangNhapDAO;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.NhanVien;
import vn.iuh.entity.PhienDangNhap;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.util.IconUtil;
import vn.iuh.util.PriceFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkingHistoryDialog extends JDialog {
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final PhienDangNhapDAO phienDangNhapDAO = new PhienDangNhapDAO();
    private final LichSuThaoTacDAO lichSuThaoTacDAO = new LichSuThaoTacDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private final Map<String, WorkingSessionDTO> sessionCache = new HashMap<>();
    private WorkingSessionDTO currentViewSession;

    // ===== UI =====
    private JLabel lblEmpId, lblEmpName, lblPhone;
    private JLabel lblPreOrders, lblCheckIn, lblCheckOut;
    private JTable tblHistory;
    private DefaultTableModel model;
    private JButton btnBackToPrevious, btnToCurrent;
    private JLabel title, previousTitle;
    private JPanel pnlTop;
    private JLabel lblTotalReceipt;
    private BigDecimal previousTotal;

    // ===== CONSTRUCTOR =====
    public WorkingHistoryDialog(JFrame parent) {
        super(parent, "Thống kê ca làm", true);
        initUI();
    }

    // ===== INIT UI =====
    private void initUI() {
        setLayout(new BorderLayout());
        // ===== TOP =====
        pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBorder(new EmptyBorder(10,10,0,10));
        title = new JLabel("LỊCH SỬ CA LÀM", SwingConstants.CENTER);
        previousTitle = new JLabel("LỊCH SỬ CA LÀM TRƯỚC", SwingConstants.CENTER);
        title.setFont(CustomUI.bigFont);
        previousTitle.setFont(CustomUI.bigFont);

        btnBackToPrevious = new JButton(IconUtil.createMenuWHIcon("/icons/left-arrow.png"));
        btnToCurrent = new JButton(IconUtil.createMenuWHIcon("/icons/right-arrow.png"));

        btnBackToPrevious.addActionListener(e -> showPreviousSession());
        btnToCurrent.addActionListener(e -> showCurrentSession());

        pnlTop.add(title, BorderLayout.CENTER);
        pnlTop.add(btnBackToPrevious, BorderLayout.WEST);
        pnlTop.setBorder(new EmptyBorder(20,0,0,0));
        add(pnlTop, BorderLayout.NORTH);

        // ===== MAIN =====
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        // ===== INFO =====
        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlInfo.setBorder(new EmptyBorder(15, 20,15, 20));
        pnlInfo.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));

        lblEmpId = new JLabel();
        lblEmpName = new JLabel();
        lblPhone = new JLabel();
        lblPreOrders = new JLabel();
        lblCheckIn = new JLabel();
        lblCheckOut = new JLabel();

        pnlInfo.add(lblEmpId);
        pnlInfo.add(lblPreOrders);
        pnlInfo.add(lblEmpName);
        pnlInfo.add(lblCheckIn);
        pnlInfo.add(lblPhone);
        pnlInfo.add(lblCheckOut);

        pnlMain.add(pnlInfo);

        // ===== TABLE =====
        String[] columns = {"Thao tác", "Nội dung", "Thời gian"};
        model = new DefaultTableModel(columns, 0);
        tblHistory = new JTable(model);

        JScrollPane sp = new JScrollPane(tblHistory);
        sp.setBorder(BorderFactory.createTitledBorder("Lịch sử thao tác"));
        pnlMain.add(sp);


        lblTotalReceipt = new JLabel();
        lblTotalReceipt.setFont(CustomUI.subMenuFont);
        pnlMain.add(lblTotalReceipt);

        // ===== BOTTOM =====
        JButton btnClose = new JButton("ĐÓNG");
        btnClose.addActionListener(e -> dispose());

        JPanel pnlBottom = new JPanel();
        pnlBottom.add(btnClose);
        pnlMain.add(pnlBottom);

        add(pnlMain, BorderLayout.CENTER);

        setSize(720, 520);
        setLocationRelativeTo(null);
    }
    private void renderTotalReceipt(BigDecimal total) {
        if (total == null) total = BigDecimal.ZERO;
        lblTotalReceipt.setText("TỔNG THU CA LÀM: " + PriceFormat.lamTronDenHangNghin(total) + " VNĐ");
    }

    // ===== OPEN =====
    public void open(String currentSessionId) {
        WorkingSessionDTO dto = loadSession(currentSessionId);
        showSession(dto);
        setVisible(true);
    }

    // ===== LOAD & CACHE =====
    private WorkingSessionDTO loadSession(String maPhien) {

        boolean isCurrent = isCurrentSession(maPhien);

        if (!isCurrent && sessionCache.containsKey(maPhien)) {
            return sessionCache.get(maPhien);
        }

        PhienDangNhap session = phienDangNhapDAO.timPhienDangNhap(maPhien);
        NhanVien nv = nhanVienDAO.layNVTheoMaPhienDangNhap(maPhien);

        List<LichSuThaoTac> lichSu =
                lichSuThaoTacDAO.timThaoTacTheoPhienDN(maPhien);

        BigDecimal total =
                hoaDonDAO.getTotalReceiptPaidInvoiceByWorkingHistory(maPhien);

        WorkingSessionDTO dto = new WorkingSessionDTO(session, nv, lichSu);
        dto.totalReceipt = total;

        if (!isCurrent) {
            sessionCache.put(maPhien, dto);
        }

        return dto;
    }

    private boolean isCurrentSession(String maPhien) {
        return maPhien.equals(Main.getCurrentLoginSession());
    }

    // ===== SHOW SESSION =====
    private void showSession(WorkingSessionDTO dto) {
        this.currentViewSession = dto;
        renderNhanVien(dto.getNhanVien());
        renderLichSu(dto.getLichSu());
        renderTotalReceipt(dto.totalReceipt);
    }

    // ===== PREVIOUS / CURRENT =====
    private void showPreviousSession() {
        PhienDangNhap prev = phienDangNhapDAO.layPhienDangNhapTruocDo();
        if (prev == null) return;

        showSession(loadSession(prev.getMaPhienDangNhap()));

        pnlTop.remove(btnBackToPrevious);
        pnlTop.add(btnToCurrent, BorderLayout.WEST);
        pnlTop.remove(title);
        pnlTop.add(previousTitle, BorderLayout.CENTER);
        pnlTop.revalidate();
        pnlTop.repaint();
    }

    private void showCurrentSession() {
        showSession(loadSession(Main.getCurrentLoginSession()));

        pnlTop.remove(btnToCurrent);
        pnlTop.add(btnBackToPrevious, BorderLayout.WEST);
        pnlTop.remove(previousTitle);
        pnlTop.add(title, BorderLayout.CENTER);
        pnlTop.revalidate();
        pnlTop.repaint();
    }


    // ===== RENDER =====
    private void renderNhanVien(NhanVien nv) {
        lblEmpId.setText("Mã nhân viên: " + nv.getMaNhanVien());
        lblEmpName.setText("Họ tên: " + nv.getTenNhanVien());
        lblPhone.setText("SĐT: " + nv.getSoDienThoai());
    }

    private void renderLichSu(List<LichSuThaoTac> ds) {
        model.setRowCount(0);

        int booking = 0, checkin = 0, checkout = 0;

        for (LichSuThaoTac ls : ds) {
            if (ActionType.BOOKING.actionName.equals(ls.getTenThaoTac())) booking++;
            if (ActionType.CHECKIN.actionName.equals(ls.getTenThaoTac())) checkin++;
            if (ActionType.CHECKOUT.actionName.equals(ls.getTenThaoTac())) checkout++;

            model.addRow(new Object[]{
                    ls.getTenThaoTac(),
                    ls.getMoTa(),
                    ls.getThoiGianTao()
            });
        }

        lblPreOrders.setText("Đơn đặt trước: " + booking);
        lblCheckIn.setText("Check-in: " + checkin);
        lblCheckOut.setText("Check-out: " + checkout);
    }

    // ===== DTO =====
    private static class WorkingSessionDTO {
        private final PhienDangNhap session;
        private final NhanVien nhanVien;
        private final List<LichSuThaoTac> lichSu;
        private BigDecimal totalReceipt;

        public WorkingSessionDTO(PhienDangNhap session,
                                 NhanVien nhanVien,
                                 List<LichSuThaoTac> lichSu) {
            this.session = session;
            this.nhanVien = nhanVien;
            this.lichSu = lichSu;
        }

        public PhienDangNhap getSession() {
            return session;
        }

        public NhanVien getNhanVien() {
            return nhanVien;
        }

        public List<LichSuThaoTac> getLichSu() {
            return lichSu;
        }

        public BigDecimal getTotalReceipt() {
            return totalReceipt;
        }

        public void setTotalReceipt(BigDecimal totalReceipt) {
            this.totalReceipt = totalReceipt;
        }
    }
}

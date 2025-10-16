package vn.iuh.gui.dialog;

import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.PhienDangNhapDAO;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.NhanVien;
import vn.iuh.entity.PhienDangNhap;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class WorkingHistoryDialog extends JDialog {
    private PhienDangNhap previousSession;
    private String maPhienDangNhapHienTai;
    private  List<LichSuThaoTac> lichSuThaoTac;
    private  List<LichSuThaoTac> lichSuCaLamTruoc;
    private  NhanVien nhanVienDangNhap;
    private JLabel lblEmpId, lblEmpName, lblPhone, lblPreOrders, lblCheckIn, lblCheckOut;
    private JTable tblInvoices;
    private JButton btnConfirmLogout;
    private JPanel pnlMain;
    private  final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final PhienDangNhapDAO phienDangNhapDAO;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
    private DefaultTableModel model;
    private  NhanVien nhanVienTruoc;
    private JPanel pnlTop;
    private JButton btnBackToPrevious;
    private JButton btnToCurrent;


    public WorkingHistoryDialog(JFrame parent) {
        super(parent, "Thống kê ca làm", true);
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
        this.phienDangNhapDAO = new PhienDangNhapDAO();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        pnlTop = new JPanel();
        pnlTop.setLayout(new BorderLayout());
        JLabel title = new JLabel("LỊCH SỬ CA LÀM", SwingConstants.CENTER);
        title.setFont(CustomUI.bigFont);
        btnBackToPrevious = new JButton("\uFE0F");
        btnBackToPrevious.addActionListener( e ->{
            setCaLamTruoc();
        });

        btnToCurrent = new JButton("\uFE0F");
        btnToCurrent.addActionListener(e ->{
            resfreshThongTinNV(this.nhanVienDangNhap);
            refreshDanhSach(this.lichSuThaoTac);
            this.pnlTop.remove(btnToCurrent);
            this.pnlTop.add(btnBackToPrevious, BorderLayout.WEST);
            this.pnlTop.repaint();
        });

        pnlTop.add(title, BorderLayout.CENTER);
        pnlTop.add(btnBackToPrevious, BorderLayout.WEST);
        add(pnlTop, BorderLayout.NORTH);

        // === Panel thông tin nhân viên ===
        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 10,10));
        pnlInfo.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));
        lblEmpId = new JLabel("Mã nhân viên: " + nhanVienDangNhap.getMaNhanVien());
        lblEmpName = new JLabel("Họ và tên: " + nhanVienDangNhap.getTenNhanVien());
        lblPhone = new JLabel("Số điện thoại: " + nhanVienDangNhap.getSoDienThoai());
        lblPreOrders = new JLabel("Tổng số lượng đơn đặt trước: 1");
        lblCheckIn = new JLabel("Tổng số lượng phòng check-in: 1");
        lblCheckOut = new JLabel("Tổng số lượng phòng check-out: 3");

        pnlInfo.add(lblEmpId);
        pnlInfo.add(lblPreOrders);
        pnlInfo.add(lblEmpName);
        pnlInfo.add(lblCheckIn);
        pnlInfo.add(lblPhone);
        pnlInfo.add(lblCheckOut);

        pnlMain.add(Box.createHorizontalStrut(20));

        pnlMain.add(pnlInfo);

        // === Bảng hóa đơn ===
        String[] columnNames = {"Thao tác", "Nội dung", "Thời gian thực hiện"};

        model = new DefaultTableModel(null, columnNames);
        tblInvoices = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tblInvoices);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lịch sử thao tác"));
        this.lichSuThaoTac = lichSuThaoTacDAO.timThaoTacTheoPhienDN(Main.getCurrentLoginSession());
        pnlMain.add(scrollPane);
        pnlMain.add(Box.createHorizontalStrut(20));

        // === Tổng tiền + Nút xác nhận ===
        JPanel pnlBottom = new JPanel();

        btnConfirmLogout = new JButton("XÁC NHẬN VÀ ĐĂNG XUẤT");
        btnConfirmLogout.setBackground(Color.RED);
        btnConfirmLogout.setForeground(Color.WHITE);
        btnConfirmLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirmLogout.setFocusPainted(false);
        btnConfirmLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pnlBottom.add(btnConfirmLogout);
        pnlMain.add(pnlBottom);

        setSize(700, 500);
        add(pnlMain, BorderLayout.CENTER);
    }

    private void resfreshThongTinNV(NhanVien nv){
        if(nv != null){
            this.lblEmpId.setText("Mã nhân viên: " + nv.getMaNhanVien());
            this.lblEmpName.setText("Tên nhân viên: " + nv.getTenNhanVien());
            this.lblPhone.setText("Số điện thoại: " + nv.getSoDienThoai());
        }
        else {
            initComponents();
        }
    }


    private void refreshDanhSach(List<LichSuThaoTac> ds){
        this.model.setRowCount(0);
        for(LichSuThaoTac ls : ds){
            this.model.addRow(new Object[]{
                    ls.getTenThaoTac(),
                    ls.getMoTa(),
                    ls.getThoiGianTao()
            });
        }
    }

    public void open(){
        this.lichSuThaoTac = lichSuThaoTacDAO.timThaoTacTheoPhienDN(Main.getCurrentLoginSession());
        if(this.maPhienDangNhapHienTai == null){
            this.maPhienDangNhapHienTai = Main.getCurrentLoginSession();
            nhanVienDangNhap = nhanVienDAO.layNVTheoMaPhienDangNhap(maPhienDangNhapHienTai);
            initComponents();
        }
        refreshDanhSach(lichSuThaoTac);
    }

    private void setCaLamTruoc(){
        if(this.previousSession == null){
            this.previousSession = phienDangNhapDAO.layPhienDangNhapTruocDo();
            this.lichSuCaLamTruoc = lichSuThaoTacDAO.timThaoTacTheoPhienDN(this.previousSession.getMaPhienDangNhap());
            nhanVienTruoc = nhanVienDAO.layNVTheoMaPhienDangNhap(this.previousSession.getMaPhienDangNhap());
        }
        refreshDanhSach(lichSuCaLamTruoc);
        resfreshThongTinNV(nhanVienTruoc);
        this.pnlTop.remove(btnBackToPrevious);
        this.pnlTop.add(btnToCurrent, BorderLayout.WEST);
        this.pnlTop.repaint();
    }

    public void updateWorkingHistory(){
        this.maPhienDangNhapHienTai = Main.getCurrentLoginSession();
        this.nhanVienTruoc = this.nhanVienDangNhap;
        this.lichSuCaLamTruoc = this.lichSuThaoTac;
        this.nhanVienDangNhap = nhanVienDAO.layNVTheoMaPhienDangNhap(maPhienDangNhapHienTai);
        this.resfreshThongTinNV(nhanVienDangNhap);
    }

    public String getMaPhienDangNhapHienTai() {
        return maPhienDangNhapHienTai;
    }
}

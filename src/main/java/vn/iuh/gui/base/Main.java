//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package vn.iuh.gui.base;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import vn.iuh.constraint.PanelName;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.entity.NhanVien;
import vn.iuh.gui.dialog.LogOutDialog;
import vn.iuh.gui.dialog.UserInfoDialog;
import vn.iuh.gui.dialog.WorkingHistoryDialog;
import vn.iuh.gui.panel.*;
import vn.iuh.gui.panel.booking.PreReservationManagementPanel;
import vn.iuh.gui.panel.booking.BookingManagementPanel;
import vn.iuh.gui.panel.booking.ReservationManagementPanel;
import vn.iuh.gui.panel.statistic.RevenueStatisticPanel;
import vn.iuh.gui.panel.statistic.RoomProductivityPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main extends JFrame {
    private JPanel pMain;
    private vn.iuh.gui.base.Menu cmpMenu;
    private JScrollPane scrollPanel;
    private JPanel cmpTopHeading;
    private JPanel pnlLeftWrapper;
    private JPanel pnlTopWrapper;
    private JPanel pnlLeftTopWrapper;
    private static CardLayout mainLayout;
    private static JPanel pnlCenter;
    private UserInfoPanel userInfo;
    private JPanel pnlTop;
    private JLabel lblSystemName;
    private JButton btnLogOut;
    private JPanel pnlCenterWrapper;
    private RoundedWrapperPanel pnlWrapperCenter;
    private static JPanel pnlRoot;
    private JPanel pnlMainUI;
    private JPanel pnlCenterPos;
    private static String maPhienDangNhap;
    private static BellButton btnBell;
    private JPopupMenu notificationPopup;
    private JPanel notificationPanel;
    private JButton btnWorkingHistory;
    private WorkingHistoryDialog workingHistoryDialog   ;
    private JPanel pnlTopRight;
    private NhanVienDAO nhanVienDAO;
    private TaiKhoanDAO taiKhoanDAO;
    private QuanLyPhuPhiPanel pnlQuanLyPhuPhi;
    private QuanLyTaiKhoanPanel pnlQuanLyTaiKhoan;
    private RevenueStatisticPanel pnlStatistic;
    private RoomProductivityPanel pnlRoomProductivity;
    public void init() {
        this.taiKhoanDAO = new TaiKhoanDAO();
        this.nhanVienDAO = new NhanVienDAO();
        //Set hiển thị mặc định toàn màn hình
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //Hiển thị chính giữa
        this.setLocationRelativeTo(null);
        //Tắt ứng dụng khi tắt giao diện
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Hệ thống khách sạn Hai Quân Đức Thịnh");
//        this.setIconImage();
    }

    public static BellButton getBtnBell() {
        return btnBell;
    }


    public Main() {
        this.init();
        //Là wrapperPanel bọc tất cả thành phần trong ứng dụng, nằm ngay trên JFrame
        this.pMain = new JPanel();
        this.pMain.setLayout(new BorderLayout());

        mainLayout = new CardLayout();
        pnlRoot = new JPanel(mainLayout);

        LoginPanelV2 loginPanel = new LoginPanelV2(this);
        pnlRoot.add(loginPanel, "Login");

        pnlMainUI = new JPanel(new BorderLayout());

        pnlMainUI.add(createASideBar(), BorderLayout.WEST);
        pnlMainUI.add(createTopCounting(), BorderLayout.NORTH);
        pnlMainUI.add(createCenterPanel(), BorderLayout.CENTER);
        initializeMainPanels();
        pnlRoot.add(pnlMainUI, "MainUI");

        showRootCard("Login");

        this.pMain.add(pnlRoot, BorderLayout.CENTER);
        this.add(pMain);
    }

    private JPanel createCenterPanel(){
        //Tạo panelCenter dùng để cho phần thao tác chính, chứa các màn hình khác với cardLayout 
        mainLayout = new CardLayout();
        pnlCenter = new JPanel(mainLayout);
        pnlWrapperCenter = new RoundedWrapperPanel();
        pnlWrapperCenter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5,5));
        pnlWrapperCenter.add(pnlCenter);
        //this.pMain.add(pnlWrapperCenter, BorderLayout.CENTER);

        return pnlWrapperCenter;
    }

    private JPanel createASideBar() {
        //Tạo 1 panel bên trái bọc 2 thành phần userInfor và Menu
        pnlLeftWrapper = new JPanel();
        pnlLeftWrapper.setLayout(new BorderLayout());
        
        //Tạo phần menu
        cmpMenu = new Menu(this);
        
        //Tạo phần thanh cuộn cho menu
        scrollPanel = new JScrollPane(cmpMenu,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPanel.setBorder(null);
        scrollPanel.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16);

        String session = Main.getCurrentLoginSession();
        if(session == null) {
            userInfo = new UserInfoPanel("Guest", "Pleasa log in");
            JLabel lblDetail = userInfo.getLblDetail();
            lblDetail.setText("");
            lblDetail.setCursor(null);
        }
        //Tạo phần khung nhỏ hiển thị tài khoảng đang đăng nhập
        //userInfo = new UserInfoPanel("Quản Lý", "Dickese Ng");
        
        //Bọc phần userInfor bằng 1 panel khác để có thể tùy chỉnh border cách đều
        pnlLeftTopWrapper = new JPanel();
        pnlLeftTopWrapper.add(userInfo);
        
        
        pnlLeftWrapper.add(pnlLeftTopWrapper, BorderLayout.NORTH);
        pnlLeftWrapper.add(scrollPanel);
        
        
        return pnlLeftWrapper;
    }

    public void refreshSidebar() {
        String session = Main.getCurrentLoginSession();
        if (session == null) return;

        NhanVien nv = nhanVienDAO.layNVTheoMaPhienDangNhap(session);
        if (nv == null) {
            System.err.println("Lỗi refreshSidebar: Không tìm thấy NV cho phiên " + session);
            return;
        }

        String role = taiKhoanDAO.getChucVuBangMaNhanVien(nv.getMaNhanVien());

        UserInfoPanel newUserInfo = new UserInfoPanel(convertMaChucVuToTen(role), nv.getTenNhanVien());

        JLabel lblDetail = newUserInfo.getLblDetail();
        lblDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDetail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(pnlLeftWrapper);
                UserInfoDialog userInfoDialog = new UserInfoDialog(parentFrame, nv);
                userInfoDialog.setLocationRelativeTo(parentFrame);
                userInfoDialog.setVisible(true);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblDetail.setForeground(Color.CYAN);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblDetail.setForeground(Color.LIGHT_GRAY);
            }
        });

        pnlLeftTopWrapper.remove(this.userInfo);
        pnlLeftTopWrapper.add(newUserInfo);
        pnlLeftTopWrapper.revalidate();
        pnlLeftTopWrapper.repaint();

        // cập nhật tham chiếu
        this.userInfo = newUserInfo;

        if (pnlQuanLyPhuPhi != null) {
            pnlQuanLyPhuPhi.checkRoleAndLoadData();
        }

        if (pnlQuanLyTaiKhoan != null) {
            pnlQuanLyTaiKhoan.checkRoleAndLoadData();
        }

        if (pnlStatistic != null) {
            pnlStatistic.checkRoleAndLoadData();
        }

        if (pnlRoomProductivity != null) {
            pnlRoomProductivity.checkRoleAndLoadData();
        }

    }

    private String convertMaChucVuToTen(String maChucVu) {
        if (maChucVu == null) return "Lễ tân";
        return switch (maChucVu.trim().toUpperCase()) {
            case "CV001" -> "Lễ tân";
            case "CV002" -> "Quản lý";
            case "CV003" -> "Admin";
            default -> "Lễ tân";
        };
    }
    private JPanel createTopCounting(){
        //Tạo phần top bằng 1 panel được custom lại
        cmpTopHeading = new HeadingTop();
        pnlTop = new JPanel();
        pnlTop.setBackground(CustomUI.darkBlue);
        pnlTop.setLayout(new BorderLayout());
        lblSystemName = new JLabel("HAI QUÂN ĐỨC THỊNH");
        lblSystemName.setFont(CustomUI.bigFont);
        lblSystemName.setForeground(CustomUI.white);
        pnlCenterWrapper = new JPanel();
        pnlCenterWrapper.add(lblSystemName);
        pnlCenterWrapper.setBackground(CustomUI.darkBlue);


        pnlTopRight = new JPanel();
        pnlTopRight.setBackground(pnlTop.getBackground());
        btnBell = new BellButton();
        btnLogOut  = new JButton("Đăng xuất");
        btnLogOut.setBackground(CustomUI.red);
        btnLogOut.setForeground(CustomUI.white);
        btnLogOut.setFont(CustomUI.smallFont);
        btnLogOut.addActionListener(e -> LogOutDialog.handleLogout(this));
        createWoringHistoryButton();
        pnlTopRight.add(btnWorkingHistory);
        pnlTopRight.add(btnBell);

        JPanel pnlRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlRightButtons.setOpaque(false);

        pnlRightButtons.add(btnWorkingHistory);
        pnlRightButtons.add(btnBell);
        pnlRightButtons.add(btnLogOut);

        pnlTop.add(cmpTopHeading, BorderLayout.WEST);
        pnlTop.add(pnlCenterWrapper, BorderLayout.CENTER);
        pnlTop.add(pnlRightButtons, BorderLayout.EAST);

        //this.pMain.add(pnlTop, BorderLayout.NORTH);
        return pnlTop;
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new FlatMacLightLaf());
        SwingUtilities.invokeLater(() ->{
            (new Main()).setVisible(true);
        });
    }

    //Truyển vào tên của panel, khi gọi sẽ hiển thị nó lên phía trên trong cardLayout
    public static void showCard(String name) {
        System.out.println(name);
        mainLayout.show(pnlCenter, name);
    }

    public static void showRootCard(String name) {
        if (pnlRoot != null) {
            CardLayout cl = (CardLayout) pnlRoot.getLayout();
            cl.show(pnlRoot, name);
        }
    }

    public static void addCard(JPanel panel, String name){
        pnlCenter.add(panel, name);
    }

    public static String getCurrentLoginSession() {
        return maPhienDangNhap;
    }
    public static void setCurrenLoginSession(String id){
        maPhienDangNhap = id;
    }
    //Tạo các màn hình con cho cardLayout (màn hình chức năng)
    public void initializeMainPanels(){
        JPanel pink = new JPanel();
        pink.setBackground(Color.cyan);
        JPanel blue = new JPanel();
        blue.setBackground(Color.blue);
        BookingManagementPanel bookingManagementPanel = new BookingManagementPanel();
        ReservationManagementPanel reservationManagementPanel = new ReservationManagementPanel();
//        PreReservationManagementPanel preReservationManagementPanel = new PreReservationManagementPanel();
        pnlStatistic = new RevenueStatisticPanel();
        QuanLyHoaDonPanel pnlQuanLyHoaDon = new QuanLyHoaDonPanel();
        QuanLyNhanVienPanel pnlQuanLyNhanVien = new QuanLyNhanVienPanel();
        pnlQuanLyTaiKhoan = new QuanLyTaiKhoanPanel();
        pnlQuanLyPhuPhi = new QuanLyPhuPhiPanel();
        QuanLyPhongPanel pnlQuanLyPhong = new QuanLyPhongPanel();
        QuanLyKhachHangPanel pnlQuanLyKhachHang = new QuanLyKhachHangPanel();
        QuanLyLoaiPhongPanel pnlQuanLyLoaiPhong = new QuanLyLoaiPhongPanel();
        pnlRoomProductivity = new RoomProductivityPanel();
        pnlCenter.add(pnlQuanLyPhong, "Quản lý phòng");
        pnlCenter.add(pnlQuanLyLoaiPhong, "Quản lý loại phòng");
        pnlCenter.add(pnlQuanLyKhachHang, "Quản lý khách hàng");
        pnlCenter.add(bookingManagementPanel, PanelName.BOOKING_MANAGEMENT.getName());
        pnlCenter.add(reservationManagementPanel, PanelName.RESERVATION_MANAGEMENT.getName());
//        pnlCenter.add(preReservationManagementPanel, PanelName.PRE_RESERVATION_MANAGEMENT.getName());
        pnlCenter.add(pnlQuanLyTaiKhoan, "Quản lý tài khoản");
        pnlCenter.add(pnlStatistic, "Thống kê doanh thu");
        pnlCenter.add(pnlQuanLyHoaDon, "Tìm hóa đơn");
        pnlCenter.add(pnlQuanLyNhanVien, "Quản lý nhân viên");
        pnlCenter.add(pnlRoomProductivity, "Thống kê hiệu suất");
        pnlCenter.add(pnlQuanLyPhuPhi, "Quản lý phụ phí");
//        showCard("Quản lý đặt phòng");

        showCenterCard("Quản lý đặt phòng");
    }

    private JButton createWoringHistoryButton(){
        btnWorkingHistory = new JButton("\uD83D\uDDD3\uFE0F");
        btnWorkingHistory.setSize(60,60);
        btnWorkingHistory.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        btnWorkingHistory.setForeground(CustomUI.white);
        btnWorkingHistory.setFocusPainted(false);
        btnWorkingHistory.setBorderPainted(false);
        btnWorkingHistory.setContentAreaFilled(true);
        btnWorkingHistory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnWorkingHistory.setBackground(this.pnlTop.getBackground());
        workingHistoryDialog = new WorkingHistoryDialog(this);
        btnWorkingHistory.addActionListener( e -> {
            if(!getCurrentLoginSession().equalsIgnoreCase(workingHistoryDialog.getMaPhienDangNhapHienTai()) &&
            workingHistoryDialog.getMaPhienDangNhapHienTai() != null)
            {
                workingHistoryDialog.updateWorkingHistory();
                System.out.println("Khác");
            }
            workingHistoryDialog.open();
            workingHistoryDialog.setVisible(true);
        });
        return btnWorkingHistory;
    }

    /** Hiện panel chức năng trong main UI */
    public static void showCenterCard(String name) {
        if (pnlRoot != null) {
            CardLayout cl = (CardLayout) pnlCenter.getLayout();
            cl.show(pnlCenter, name);
        }
    }


}

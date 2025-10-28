package vn.iuh.gui.base;


import vn.iuh.constraint.UserRole;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.entity.NhanVien;

import javax.swing.*;
import java.awt.*;

public abstract class RoleChecking extends JPanel{

    public RoleChecking() {
        super(new BorderLayout());
        setBackground(CustomUI.white);
    }

    public final void checkRoleAndLoadData() {
        removeAll();

        UserRole vaiTro = getCurrentUserRole();

        if (vaiTro == UserRole.QUAN_LY) {
            // NẾU LÀ QUẢN LÝ: Xây dựng UI
            // Hàm này sẽ đặt lại layout (ví dụ: BoxLayout)
            // và gọi init(), loadData()...
            buildAdminUI();
        } else {
            // NẾU LÀ LỄ TÂN: Hiển thị thông báo lỗi
            // Đảm bảo layout là BorderLayout để căn giữa
            setLayout(new BorderLayout());
            add(createAccessDeniedPanel(), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private UserRole getCurrentUserRole() {
        TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
        NhanVienDAO nhanVienDAO = new NhanVienDAO();

        String maPhienDangNhap = Main.getCurrentLoginSession();
        if (maPhienDangNhap == null) return UserRole.KHONG_XAC_DINH;

        NhanVien nv = nhanVienDAO.layNVTheoMaPhienDangNhap(maPhienDangNhap);
        if (nv == null) return UserRole.KHONG_XAC_DINH;

        String role = taiKhoanDAO.getChucVuBangMaNhanVien(nv.getMaNhanVien());
        if (role != null) {
            return UserRole.fromString(role.trim());
        }

        return UserRole.KHONG_XAC_DINH;
    }


    private JPanel createAccessDeniedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lblThongBao = new JLabel("Lễ tân không có quyền truy cập chức năng này.");
        lblThongBao.setFont(new Font("Arial", Font.BOLD, 18));
        lblThongBao.setHorizontalAlignment(SwingConstants.CENTER);
        lblThongBao.setForeground(Color.RED);
        panel.add(lblThongBao, BorderLayout.CENTER);
        return panel;
    }

    protected abstract void buildAdminUI();
}

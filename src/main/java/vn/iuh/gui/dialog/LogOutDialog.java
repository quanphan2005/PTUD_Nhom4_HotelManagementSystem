package vn.iuh.gui.dialog;

import vn.iuh.entity.PhienDangNhap;
import vn.iuh.gui.base.Main;
import vn.iuh.service.ShiftAssignmentService;
import vn.iuh.service.impl.PhienDangNhapServiceImpl;

import javax.swing.*;
import java.sql.Timestamp;

public class LogOutDialog extends JDialog{
    public static void handleLogout(JFrame currentFrame) {
        int confirm = JOptionPane.showConfirmDialog(
                currentFrame,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try{
                String maPhien = Main.getCurrentLoginSession();
                if(maPhien != null){
                    ShiftAssignmentService service = new PhienDangNhapServiceImpl();
                    PhienDangNhap phien = service.getShiftAssignmentByID(maPhien);
                    if(phien != null){
                        phien.setTgKetThuc(new Timestamp(System.currentTimeMillis()));
                        service.updateFinishingTime(maPhien, new Timestamp(System.currentTimeMillis()));
                    }
                }
            }catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(currentFrame,
                        "Lỗi khi kết thúc phiên đăng nhập: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
            Main.showRootCard("Login");
        }
    }
}

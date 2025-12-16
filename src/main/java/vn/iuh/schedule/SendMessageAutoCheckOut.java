package vn.iuh.schedule;

import org.quartz.*;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.PhongDAO;
import vn.iuh.dao.ThongBaoDAO;
import vn.iuh.entity.ThongBao;
import vn.iuh.gui.base.Main;
import vn.iuh.util.AudioPlayer;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SendMessageAutoCheckOut implements Job {
    private final ThongBaoDAO thongBaoDAO = new ThongBaoDAO();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap dataMap = context.getMergedJobDataMap();
            boolean isBatch = dataMap.getBoolean("isBatch");

            ThongBao thongBaoMoi;
            if (isBatch) {
                String roomNames = dataMap.getString("roomNames");
                thongBaoMoi = createThongBaoGop(roomNames);
            } else {
                String roomId = dataMap.getString("roomId");
                thongBaoMoi = createThongBao(roomId);
            }
            thongBaoDAO.themThongBao(thongBaoMoi);
            Main.getBtnBell().addNotification(thongBaoMoi);
//            AudioPlayer.playDefaultNotification();
        } finally {
            try {
                context.getScheduler().deleteJob(context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

    private ThongBao createThongBaoGop(String roomNames) {
        synchronized (thongBaoDAO) {  // Đảm bảo thread-safe
            var latest = thongBaoDAO.timThongBaoMoiNhat();
            String maThongBaoMoiNhat = latest == null ? null : latest.getMaThongBao();
            String maThongBao = EntityUtil.increaseEntityID(
                    maThongBaoMoiNhat,
                    EntityIDSymbol.NOTIFICATION_PREFIX.getPrefix(),
                    EntityIDSymbol.NOTIFICATION_PREFIX.getLength()
            );

            return new ThongBao(maThongBao,
                    "Các phòng " + roomNames + " đã tự động check-out",
                    Main.getCurrentLoginSession(),
                    Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    private ThongBao createThongBao(String roomId) {
        synchronized (thongBaoDAO) {  // Đảm bảo thread-safe
            var latest = thongBaoDAO.timThongBaoMoiNhat();
            String maThongBaoMoiNhat = latest == null ? null : latest.getMaThongBao();
            String maThongBao = EntityUtil.increaseEntityID(
                    maThongBaoMoiNhat,
                    EntityIDSymbol.NOTIFICATION_PREFIX.getPrefix(),
                    EntityIDSymbol.NOTIFICATION_PREFIX.getLength()
            );

            var phongDAO = new PhongDAO();
            var tenPhong = phongDAO.timPhong(roomId).getTenPhong();

            return new ThongBao(maThongBao,
                    "Phòng " + tenPhong + " tự động check-out",
                    Main.getCurrentLoginSession(),
                    Timestamp.valueOf(LocalDateTime.now()));
        }
    }

}

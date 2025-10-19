package vn.iuh.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
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
    private final PhongDAO phongDAO = new PhongDAO();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String roomId = context.getMergedJobDataMap().get("roomId").toString();
            ThongBao thongBaoMoi = createThongBao(roomId);
            thongBaoDAO.themThongBao(thongBaoMoi);
            Main.getBtnBell().addNotification(thongBaoMoi.getNoiDung());
            AudioPlayer.playDefaultNotification();
        } finally {
            // Xoá job sau khi hoàn thành
            try {
                context.getScheduler().deleteJob(context.getJobDetail().getKey());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }


    private ThongBao createThongBao(String roomId){

        var latest = thongBaoDAO.timThongBaoMoiNhat();
        String maThongBaoMoiNhat = latest == null ? null : latest.getMaThongBao();
        String maThongBao = EntityUtil.increaseEntityID(
                maThongBaoMoiNhat,
                EntityIDSymbol.NOTIFICATION_PREFIX.getPrefix(),
                EntityIDSymbol.NOTIFICATION_PREFIX.getLength()
        );

        var tenPhong = phongDAO.timPhong(roomId).getTenPhong();

        return new ThongBao(maThongBao,
                "Phòng " + tenPhong + " tự động check-out"
                ,Main.getCurrentLoginSession(), Timestamp.valueOf(LocalDateTime.now()));
    }

}

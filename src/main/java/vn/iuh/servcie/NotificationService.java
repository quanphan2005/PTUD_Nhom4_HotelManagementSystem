package vn.iuh.servcie;

import vn.iuh.entity.ThongBao;

public interface NotificationService {
    ThongBao getNotificationByID(String id);
    ThongBao createNotification(ThongBao thongBao);
    ThongBao updateNotification(ThongBao thongBao);
    boolean deleteNotificationByID(String id);
}

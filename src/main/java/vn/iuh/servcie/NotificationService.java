package vn.iuh.servcie;

import vn.iuh.entity.Notification;

public interface NotificationService {
    Notification getNotificationByID(String id);
    Notification createNotification(Notification notification);
    Notification updateNotification(Notification notification);
    boolean deleteNotificationByID(String id);
}

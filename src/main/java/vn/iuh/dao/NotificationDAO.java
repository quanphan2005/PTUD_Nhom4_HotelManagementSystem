package vn.iuh.dao;

import vn.iuh.entity.Notification;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class NotificationDAO {
    private final Connection connection;

    public NotificationDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public NotificationDAO(Connection connection) {
        this.connection = connection;
    }

    public Notification getNotificationByID(String id) {
        String query = "SELECT * FROM Notification WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToNotification(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public Notification createNotification(Notification notification) {
        String query = "INSERT INTO Notification (id, create_time, noti_message, shift_assignment_id) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, notification.getId());
            ps.setTimestamp(2, notification.getCreateTime() != null ? new Timestamp(notification.getCreateTime().getTime()) : null);
            ps.setString(3, notification.getNotiMessage());
            ps.setString(4, notification.getShiftAssignmentId());

            ps.executeUpdate();
            return notification;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Notification updateNotification(Notification notification) {
        String query = "UPDATE Notification SET create_time = ?, noti_message = ?, shift_assignment_id = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, notification.getCreateTime() != null ? new Timestamp(notification.getCreateTime().getTime()) : null);
            ps.setString(2, notification.getNotiMessage());
            ps.setString(3, notification.getShiftAssignmentId());
            ps.setTimestamp(4, notification.getCreateAt() != null ? new Timestamp(notification.getCreateAt().getTime()) : null);
            ps.setString(5, notification.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getNotificationByID(notification.getId());
            } else {
                System.out.println("No notification found with ID: " + notification.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteNotificationByID(String id) {
        if (getNotificationByID(id) == null) {
            System.out.println("No notification found with ID: " + id);
            return false;
        }

        String query = "UPDATE Notification SET is_deleted = 1 WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Notification has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        try {
            notification.setId(rs.getString("id"));
            notification.setCreateTime(rs.getTimestamp("create_time")); // datetime -> java.util.Date
            notification.setNotiMessage(rs.getString("noti_message"));
            notification.setShiftAssignmentId(rs.getString("shift_assignment_id"));
            notification.setCreateAt(rs.getTimestamp("create_at"));
            notification.setIsDeleted(rs.getBoolean("is_deleted"));
            return notification;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Notification: " + e);
        }
    }
}


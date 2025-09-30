package vn.iuh.dao;

import vn.iuh.entity.ThongBao;
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

    public ThongBao getNotificationByID(String id) {
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

    public ThongBao createNotification(ThongBao thongBao) {
        String query = "INSERT INTO Notification (id, create_time, noti_message, shift_assignment_id) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, thongBao.getMaThongBao());
            ps.setTimestamp(2, thongBao.getCreateTime() != null ? new Timestamp(thongBao.getCreateTime().getTime()) : null);
            ps.setString(3, thongBao.getNoiDung());
            ps.setString(4, thongBao.getMaPhienDangNhap());

            ps.executeUpdate();
            return thongBao;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ThongBao updateNotification(ThongBao thongBao) {
        String query = "UPDATE Notification SET create_time = ?, noti_message = ?, shift_assignment_id = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, thongBao.getCreateTime() != null ? new Timestamp(thongBao.getCreateTime().getTime()) : null);
            ps.setString(2, thongBao.getNoiDung());
            ps.setString(3, thongBao.getMaPhienDangNhap());
            ps.setTimestamp(4, thongBao.getThoiGianTao() != null ? new Timestamp(thongBao.getThoiGianTao().getTime()) : null);
            ps.setString(5, thongBao.getMaThongBao());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getNotificationByID(thongBao.getMaThongBao());
            } else {
                System.out.println("No notification found with ID: " + thongBao.getMaThongBao());
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

    private ThongBao mapResultSetToNotification(ResultSet rs) throws SQLException {
        ThongBao thongBao = new ThongBao();
        try {
            thongBao.setMaThongBao(rs.getString("id"));
            thongBao.setCreateTime(rs.getTimestamp("create_time")); // datetime -> java.util.Date
            thongBao.setNoiDung(rs.getString("noti_message"));
            thongBao.setMaPhienDangNhap(rs.getString("shift_assignment_id"));
            thongBao.setThoiGianTao(rs.getTimestamp("create_at"));
            thongBao.setIsDeleted(rs.getBoolean("is_deleted"));
            return thongBao;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Notification: " + e);
        }
    }
}


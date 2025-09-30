package vn.iuh.dao;

import vn.iuh.entity.ShiftAssignment;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class ShiftAssignmentDAO {
    private final Connection connection;

    public ShiftAssignmentDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public ShiftAssignmentDAO(Connection connection) {
        this.connection = connection;
    }

    public ShiftAssignment getShiftAssignmentByID(String id) {
        String query = "SELECT * FROM ShiftAssignment WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToShiftAssignment(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public ShiftAssignment createShiftAssignment(ShiftAssignment shift) {
        String query = "INSERT INTO ShiftAssignment " +
                "(id, counter_number, start_time, end_time, account_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, shift.getId());
            ps.setInt(2, shift.getCounterNumber());
            ps.setTimestamp(3, shift.getStartTime() != null ? new Timestamp(shift.getStartTime().getTime()) : null);
            ps.setTimestamp(4, shift.getEndTime() != null ? new Timestamp(shift.getEndTime().getTime()) : null);
            ps.setString(5, shift.getAccountId());

            ps.executeUpdate();
            return shift;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ShiftAssignment updateShiftAssignment(ShiftAssignment shift) {
        String query = "UPDATE ShiftAssignment SET counter_number = ?, start_time = ?, end_time = ?, account_id = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, shift.getCounterNumber());
            ps.setTimestamp(2, shift.getStartTime() != null ? new Timestamp(shift.getStartTime().getTime()) : null);
            ps.setTimestamp(3, shift.getEndTime() != null ? new Timestamp(shift.getEndTime().getTime()) : null);
            ps.setString(4, shift.getAccountId());
            ps.setTimestamp(5, shift.getCreateAt() != null ? new Timestamp(shift.getCreateAt().getTime()) : null);
            ps.setString(6, shift.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getShiftAssignmentByID(shift.getId());
            } else {
                System.out.println("No shift assignment found with ID: " + shift.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteShiftAssignmentByID(String id) {
        if (getShiftAssignmentByID(id) == null) {
            System.out.println("No shift assignment found with ID: " + id);
            return false;
        }

        String query = "UPDATE ShiftAssignment SET is_deleted = 1 WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Shift assignment has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private ShiftAssignment mapResultSetToShiftAssignment(ResultSet rs) throws SQLException {
        ShiftAssignment shift = new ShiftAssignment();
        try {
            shift.setId(rs.getString("id"));
            shift.setCounterNumber(rs.getInt("counter_number"));
            shift.setStartTime(rs.getTimestamp("start_time"));
            shift.setEndTime(rs.getTimestamp("end_time"));
            shift.setAccountId(rs.getString("account_id"));
            shift.setCreateAt(rs.getTimestamp("create_at"));
            shift.setIsDeleted(rs.getBoolean("is_deleted"));
            return shift;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ShiftAssignment: " + e);
        }
    }
}

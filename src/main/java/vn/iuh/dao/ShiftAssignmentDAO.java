package vn.iuh.dao;

import vn.iuh.entity.PhienDangNhap;
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

    public PhienDangNhap getShiftAssignmentByID(String id) {
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

    public PhienDangNhap createShiftAssignment(PhienDangNhap shift) {
        String query = "INSERT INTO ShiftAssignment " +
                "(id, counter_number, start_time, end_time, account_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, shift.getMaPhienDangNhap());
            ps.setInt(2, shift.getSoQuay());
            ps.setTimestamp(3, shift.getTgBatDau() != null ? new Timestamp(shift.getTgBatDau().getTime()) : null);
            ps.setTimestamp(4, shift.getTgKetThuc() != null ? new Timestamp(shift.getTgKetThuc().getTime()) : null);
            ps.setString(5, shift.getMaTaiKhoan());

            ps.executeUpdate();
            return shift;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public PhienDangNhap updateShiftAssignment(PhienDangNhap shift) {
        String query = "UPDATE ShiftAssignment SET counter_number = ?, start_time = ?, end_time = ?, account_id = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, shift.getSoQuay());
            ps.setTimestamp(2, shift.getTgBatDau() != null ? new Timestamp(shift.getTgBatDau().getTime()) : null);
            ps.setTimestamp(3, shift.getTgKetThuc() != null ? new Timestamp(shift.getTgKetThuc().getTime()) : null);
            ps.setString(4, shift.getMaTaiKhoan());
            ps.setTimestamp(5, shift.getThoiGianTao() != null ? new Timestamp(shift.getThoiGianTao().getTime()) : null);
            ps.setString(6, shift.getMaPhienDangNhap());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getShiftAssignmentByID(shift.getMaPhienDangNhap());
            } else {
                System.out.println("No shift assignment found with ID: " + shift.getMaPhienDangNhap());
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

    private PhienDangNhap mapResultSetToShiftAssignment(ResultSet rs) throws SQLException {
        PhienDangNhap shift = new PhienDangNhap();
        try {
            shift.setMaPhienDangNhap(rs.getString("id"));
            shift.setSoQuay(rs.getInt("counter_number"));
            shift.setTgBatDau(rs.getTimestamp("start_time"));
            shift.setTgKetThuc(rs.getTimestamp("end_time"));
            shift.setMaTaiKhoan(rs.getString("account_id"));
            shift.setThoiGianTao(rs.getTimestamp("create_at"));
            shift.setIsDeleted(rs.getBoolean("is_deleted"));
            return shift;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ShiftAssignment: " + e);
        }
    }
}

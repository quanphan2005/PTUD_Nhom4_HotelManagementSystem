package vn.iuh.dao;

import vn.iuh.entity.ReservationForm;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class ReservationFormDAO {
    private final Connection connection;

    public ReservationFormDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public ReservationFormDAO(Connection connection) {
        this.connection = connection;
    }

    public ReservationForm getReservationFormByID(String id) {
        String query = "SELECT * FROM ReservationForm WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToReservationForm(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public ReservationForm createReservationForm(ReservationForm reservationForm) {
        String query = "INSERT INTO ReservationForm " +
                "(id, reserve_date, note, check_in_date, check_out_date, initial_price, deposit_price, " +
                "is_advanced, customer_id, shift_assignment_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, reservationForm.getId());
            ps.setTimestamp(2, reservationForm.getReserveDate() != null ? new Timestamp(reservationForm.getReserveDate().getTime()) : null);
            ps.setString(3, reservationForm.getNote());
            ps.setTimestamp(4, reservationForm.getCheckInDate() != null ? new Timestamp(reservationForm.getCheckInDate().getTime()) : null);
            ps.setTimestamp(5, reservationForm.getCheckOutDate() != null ? new Timestamp(reservationForm.getCheckOutDate().getTime()) : null);
            ps.setDouble(6, reservationForm.getInitialPrice());
            ps.setDouble(7, reservationForm.getDepositPrice());
            ps.setBoolean(8, reservationForm.getIsAdvanced());
            ps.setString(9, reservationForm.getCustomerId());
            ps.setString(10, reservationForm.getShiftAssignmentId());

            ps.executeUpdate();
            return reservationForm;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ReservationForm updateReservationForm(ReservationForm reservationForm) {
        String query = "UPDATE ReservationForm SET reserve_date = ?, note = ?, check_in_date = ?, check_out_date = ?, " +
                "initial_price = ?, deposit_price = ?, is_advanced = ?, customer_id = ?, shift_assignment_id = ?" +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, reservationForm.getReserveDate() != null ? new Timestamp(reservationForm.getReserveDate().getTime()) : null);
            ps.setString(2, reservationForm.getNote());
            ps.setTimestamp(3, reservationForm.getCheckInDate() != null ? new Timestamp(reservationForm.getCheckInDate().getTime()) : null);
            ps.setTimestamp(4, reservationForm.getCheckOutDate() != null ? new Timestamp(reservationForm.getCheckOutDate().getTime()) : null);
            ps.setDouble(5, reservationForm.getInitialPrice());
            ps.setDouble(6, reservationForm.getDepositPrice());
            ps.setBoolean(7, reservationForm.getIsAdvanced());
            ps.setString(8, reservationForm.getCustomerId());
            ps.setString(9, reservationForm.getShiftAssignmentId());
            ps.setString(10, reservationForm.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getReservationFormByID(reservationForm.getId());
            } else {
                System.out.println("No reservation form found with ID: " + reservationForm.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteReservationFormByID(String id) {
        if (getReservationFormByID(id) == null) {
            System.out.println("No reservation form found with ID: " + id);
            return false;
        }

        String query = "UPDATE ReservationForm SET is_deleted = 1 WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Reservation form has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private ReservationForm mapResultSetToReservationForm(ResultSet rs) throws SQLException {
        ReservationForm reservationForm = new ReservationForm();
        try {
            reservationForm.setId(rs.getString("id"));
            reservationForm.setReserveDate(rs.getTimestamp("reserve_date"));
            reservationForm.setNote(rs.getString("note"));
            reservationForm.setCheckInDate(rs.getTimestamp("check_in_date"));
            reservationForm.setCheckOutDate(rs.getTimestamp("check_out_date"));
            reservationForm.setInitialPrice(rs.getDouble("initial_price"));
            reservationForm.setDepositPrice(rs.getDouble("deposit_price"));
            reservationForm.setIsAdvanced(rs.getBoolean("is_advanced"));
            reservationForm.setCustomerId(rs.getString("customer_id"));
            reservationForm.setShiftAssignmentId(rs.getString("shift_assignment_id"));
            return reservationForm;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ReservationForm: " + e);
        }
    }
}

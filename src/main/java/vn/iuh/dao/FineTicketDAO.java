package vn.iuh.dao;

import vn.iuh.entity.FineTicket;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FineTicketDAO {
    private final Connection connection;

    public FineTicketDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public FineTicketDAO(Connection connection) {
        this.connection = connection;
    }

    public FineTicket getFineTicketByID(String id) {
        String query = "SELECT * FROM FineTicket WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            var rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToFineTicket(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public FineTicket createFineTicket(FineTicket fineTicket) {
        String query = "INSERT INTO FineTicket (id, create_time, ticket_description, total_fine, reservation_form_id, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, fineTicket.getId());
            ps.setTimestamp(2, fineTicket.getCreateTime());
            ps.setString(3, fineTicket.getTicketDescription());
            ps.setDouble(4, fineTicket.getTotalFine());
            ps.setString(5, fineTicket.getReservationFormId());

            ps.executeUpdate();
            return fineTicket;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public FineTicket updateFineTicket(FineTicket fineTicket) {
        String query = "UPDATE FineTicket SET create_time = ?, ticket_description = ?, total_fine = ?, reservation_form_id = ? " +
                "WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, fineTicket.getCreateTime());
            ps.setString(2, fineTicket.getTicketDescription());
            ps.setDouble(3, fineTicket.getTotalFine());
            ps.setString(4, fineTicket.getReservationFormId());
            ps.setString(5, fineTicket.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getFineTicketByID(fineTicket.getId());
            } else {
                System.out.println("No FineTicket found with ID: " + fineTicket.getId());
                return null;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteFineTicketByID(String id) {
        if (getFineTicketByID(id) == null) {
            System.out.println("No FineTicket found with ID: " + id);
            return false;
        }

        String query = "UPDATE FineTicket SET is_deleted = true WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("FineTicket has been deleted successfully");
                return true;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public FineTicket mapResultSetToFineTicket(ResultSet rs) throws SQLException {
        FineTicket fineTicket = new FineTicket();
        try {
            fineTicket.setId(rs.getString("id"));
            fineTicket.setCreateTime(rs.getTimestamp("create_time"));
            fineTicket.setTicketDescription(rs.getString("ticket_description"));
            fineTicket.setTotalFine(rs.getDouble("total_fine"));
            fineTicket.setReservationFormId(rs.getString("reservation_form_id"));
            return fineTicket;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to FineTicket: " + e);
        }
    }
}
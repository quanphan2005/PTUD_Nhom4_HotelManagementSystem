package vn.iuh.dao;

import vn.iuh.entity.BienBan;
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

    public BienBan getFineTicketByID(String id) {
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

    public BienBan createFineTicket(BienBan bienBan) {
        String query = "INSERT INTO FineTicket (id, create_time, ticket_description, total_fine, reservation_form_id, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, bienBan.getMaBienBan());
            ps.setTimestamp(2, bienBan.getThoiGianTao());
            ps.setString(3, bienBan.getLiDo());
            ps.setDouble(4, bienBan.getPhiBienBan());
            ps.setString(5, bienBan.getMaChiTietDatPhong());

            ps.executeUpdate();
            return bienBan;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public BienBan updateFineTicket(BienBan bienBan) {
        String query = "UPDATE FineTicket SET create_time = ?, ticket_description = ?, total_fine = ?, reservation_form_id = ? " +
                "WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, bienBan.getThoiGianTao());
            ps.setString(2, bienBan.getLiDo());
            ps.setDouble(3, bienBan.getPhiBienBan());
            ps.setString(4, bienBan.getMaChiTietDatPhong());
            ps.setString(5, bienBan.getMaBienBan());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getFineTicketByID(bienBan.getMaBienBan());
            } else {
                System.out.println("No FineTicket found with ID: " + bienBan.getMaBienBan());
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

    public BienBan mapResultSetToFineTicket(ResultSet rs) throws SQLException {
        BienBan bienBan = new BienBan();
        try {
            bienBan.setMaBienBan(rs.getString("id"));
            bienBan.setThoiGianTao(rs.getTimestamp("create_time"));
            bienBan.setLiDo(rs.getString("ticket_description"));
            bienBan.setPhiBienBan(rs.getDouble("total_fine"));
            bienBan.setMaChiTietDatPhong(rs.getString("reservation_form_id"));
            return bienBan;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to FineTicket: " + e);
        }
    }
}
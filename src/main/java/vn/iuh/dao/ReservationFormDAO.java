package vn.iuh.dao;

import vn.iuh.entity.DonDatPhong;
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

    public DonDatPhong getReservationFormByID(String id) {
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

    public DonDatPhong createReservationForm(DonDatPhong donDatPhong) {
        String query = "INSERT INTO ReservationForm " +
                "(id, reserve_date, note, check_in_date, check_out_date, initial_price, deposit_price, " +
                "is_advanced, customer_id, shift_assignment_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, donDatPhong.getMaDonDatPhong());
            ps.setTimestamp(2, donDatPhong.getReserveDate() != null ? new Timestamp(donDatPhong.getReserveDate().getTime()) : null);
            ps.setString(3, donDatPhong.getMoTa());
            ps.setTimestamp(4, donDatPhong.getTgNhanPhong() != null ? new Timestamp(donDatPhong.getTgNhanPhong().getTime()) : null);
            ps.setTimestamp(5, donDatPhong.getTgTraPhong() != null ? new Timestamp(donDatPhong.getTgTraPhong().getTime()) : null);
            ps.setDouble(6, donDatPhong.getTongTienDuTinh());
            ps.setDouble(7, donDatPhong.getTienDatCoc());
            ps.setBoolean(8, donDatPhong.getIsAdvanced());
            ps.setString(9, donDatPhong.getMaKhachHang());
            ps.setString(10, donDatPhong.getMaPhienDangNhap());

            ps.executeUpdate();
            return donDatPhong;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public DonDatPhong updateReservationForm(DonDatPhong donDatPhong) {
        String query = "UPDATE ReservationForm SET reserve_date = ?, note = ?, check_in_date = ?, check_out_date = ?, " +
                "initial_price = ?, deposit_price = ?, is_advanced = ?, customer_id = ?, shift_assignment_id = ?" +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, donDatPhong.getReserveDate() != null ? new Timestamp(donDatPhong.getReserveDate().getTime()) : null);
            ps.setString(2, donDatPhong.getMoTa());
            ps.setTimestamp(3, donDatPhong.getTgNhanPhong() != null ? new Timestamp(donDatPhong.getTgNhanPhong().getTime()) : null);
            ps.setTimestamp(4, donDatPhong.getTgTraPhong() != null ? new Timestamp(donDatPhong.getTgTraPhong().getTime()) : null);
            ps.setDouble(5, donDatPhong.getTongTienDuTinh());
            ps.setDouble(6, donDatPhong.getTienDatCoc());
            ps.setBoolean(7, donDatPhong.getIsAdvanced());
            ps.setString(8, donDatPhong.getMaKhachHang());
            ps.setString(9, donDatPhong.getMaPhienDangNhap());
            ps.setString(10, donDatPhong.getMaDonDatPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getReservationFormByID(donDatPhong.getMaDonDatPhong());
            } else {
                System.out.println("No reservation form found with ID: " + donDatPhong.getMaDonDatPhong());
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

    private DonDatPhong mapResultSetToReservationForm(ResultSet rs) throws SQLException {
        DonDatPhong donDatPhong = new DonDatPhong();
        try {
            donDatPhong.setMaDonDatPhong(rs.getString("id"));
            donDatPhong.setMoTa(rs.getString("note"));
            donDatPhong.setTgNhanPhong(rs.getTimestamp("check_in_date"));
            donDatPhong.setTgTraPhong(rs.getTimestamp("check_out_date"));
            donDatPhong.setTongTienDuTinh(rs.getDouble("initial_price"));
            donDatPhong.setTienDatCoc(rs.getDouble("deposit_price"));
            donDatPhong.setDaDatTruoc(rs.getBoolean("is_advanced"));
            donDatPhong.setMaKhachHang(rs.getString("customer_id"));
            donDatPhong.setMaPhienDangNhap(rs.getString("shift_assignment_id"));
            return donDatPhong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ReservationForm: " + e);
        }
    }
}

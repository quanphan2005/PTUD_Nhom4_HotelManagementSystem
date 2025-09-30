package vn.iuh.dao;

import vn.iuh.entity.HoaDon;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class InvoiceDAO {
    private final Connection connection;

    public InvoiceDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public InvoiceDAO(Connection connection) {
        this.connection = connection;
    }

    public HoaDon getInvoiceByID(String id) {
        String query = "SELECT * FROM Invoice WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToInvoice(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public HoaDon createInvoice(HoaDon hoaDon) {
        String query = "INSERT INTO Invoice (id, create_date, payment, total_price, tax_price, total_due, " +
                "invoice_type, invoice_status, shift_assignment_id, reservation_form_id, customer_id, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, hoaDon.getMa_hoa_don());
            ps.setTimestamp(2, hoaDon.getThoi_gian_tao() != null ? new Timestamp(hoaDon.getThoi_gian_tao().getTime()) : null);
            ps.setString(3, hoaDon.getPhuong_thuc_thanh_toan());
            ps.setDouble(4, hoaDon.getTong_tien());
            ps.setDouble(5, hoaDon.getTien_thue());
            ps.setDouble(6, hoaDon.getTong_hoa_don());
            ps.setString(7, hoaDon.getKieu_hoa_don());
            ps.setString(8, hoaDon.getTinh_trang_thanh_toan());
            ps.setString(9, hoaDon.getMa_phien_dang_nhap());
            ps.setString(10, hoaDon.getMa_don_dat_phong());
            ps.setString(11, hoaDon.getMa_khach_hang());

            ps.executeUpdate();
            return hoaDon;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public HoaDon updateInvoice(HoaDon hoaDon) {
        String query = "UPDATE Invoice SET create_date = ?, payment = ?, total_price = ?, tax_price = ?, total_due = ?, " +
                "invoice_type = ?, invoice_status = ?, shift_assignment_id = ?, reservation_form_id = ?, customer_id = ? " +
                "WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, hoaDon.getThoi_gian_tao() != null ? new Timestamp(hoaDon.getThoi_gian_tao().getTime()) : null);
            ps.setString(2, hoaDon.getPhuong_thuc_thanh_toan());
            ps.setDouble(3, hoaDon.getTong_tien());
            ps.setDouble(4, hoaDon.getTien_thue());
            ps.setDouble(5, hoaDon.getTong_hoa_don());
            ps.setString(6, hoaDon.getKieu_hoa_don());
            ps.setString(7, hoaDon.getTinh_trang_thanh_toan());
            ps.setString(8, hoaDon.getMa_phien_dang_nhap());
            ps.setString(9, hoaDon.getMa_don_dat_phong());
            ps.setString(10, hoaDon.getMa_khach_hang());
            ps.setString(11, hoaDon.getMa_hoa_don());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getInvoiceByID(hoaDon.getMa_hoa_don());
            } else {
                System.out.println("No invoice found with ID: " + hoaDon.getMa_hoa_don());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteInvoiceByID(String id) {
        if (getInvoiceByID(id) == null) {
            System.out.println("No invoice found with ID: " + id);
            return false;
        }

        String query = "UPDATE Invoice SET is_deleted = true WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Invoice has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private HoaDon mapResultSetToInvoice(ResultSet rs) throws SQLException {
        HoaDon hoaDon = new HoaDon();
        try {
            hoaDon.setMa_hoa_don(rs.getString("id"));
            hoaDon.setThoi_gian_tao(rs.getTimestamp("create_date")); // mapping datetime -> java.util.Date
            hoaDon.setPhuong_thuc_thanh_toan(rs.getString("payment"));
            hoaDon.setTong_tien(rs.getDouble("total_price"));
            hoaDon.setTien_thue(rs.getDouble("tax_price"));
            hoaDon.setTong_hoa_don(rs.getDouble("total_due"));
            hoaDon.setKieu_hoa_don(rs.getString("invoice_type"));
            hoaDon.setTinh_trang_thanh_toan(rs.getString("invoice_status"));
            hoaDon.setMa_phien_dang_nhap(rs.getString("shift_assignment_id"));
            hoaDon.setMa_don_dat_phong(rs.getString("reservation_form_id"));
            hoaDon.setMa_khach_hang(rs.getString("customer_id"));
            return hoaDon;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Invoice: " + e);
        }
    }
}
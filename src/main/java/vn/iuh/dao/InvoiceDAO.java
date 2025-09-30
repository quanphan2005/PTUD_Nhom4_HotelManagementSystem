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
            ps.setString(1, hoaDon.getMaHoaDon());
            ps.setTimestamp(2, hoaDon.getThoiGianTao() != null ? new Timestamp(hoaDon.getThoiGianTao().getTime()) : null);
            ps.setString(3, hoaDon.getPhuongThucThanhToan());
            ps.setDouble(4, hoaDon.getTongTien());
            ps.setDouble(5, hoaDon.getTienThue());
            ps.setDouble(6, hoaDon.getTongHoaDon());
            ps.setString(7, hoaDon.getKieuHoaDon());
            ps.setString(8, hoaDon.getTinhTrangThanhToan());
            ps.setString(9, hoaDon.getMaPhienDangNhap());
            ps.setString(10, hoaDon.getMaDonDatPhong());
            ps.setString(11, hoaDon.getMaKhachHang());

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
            ps.setTimestamp(1, hoaDon.getThoiGianTao() != null ? new Timestamp(hoaDon.getThoiGianTao().getTime()) : null);
            ps.setString(2, hoaDon.getPhuongThucThanhToan());
            ps.setDouble(3, hoaDon.getTongTien());
            ps.setDouble(4, hoaDon.getTienThue());
            ps.setDouble(5, hoaDon.getTongHoaDon());
            ps.setString(6, hoaDon.getKieuHoaDon());
            ps.setString(7, hoaDon.getTinhTrangThanhToan());
            ps.setString(8, hoaDon.getMaPhienDangNhap());
            ps.setString(9, hoaDon.getMaDonDatPhong());
            ps.setString(10, hoaDon.getMaKhachHang());
            ps.setString(11, hoaDon.getMaHoaDon());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getInvoiceByID(hoaDon.getMaHoaDon());
            } else {
                System.out.println("No invoice found with ID: " + hoaDon.getMaHoaDon());
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
            hoaDon.setMaHoaDon(rs.getString("id"));
            hoaDon.setThoiGianTao(rs.getTimestamp("create_date")); // mapping datetime -> java.util.Date
            hoaDon.setPhuongThucThanhToan(rs.getString("payment"));
            hoaDon.setTongTien(rs.getDouble("total_price"));
            hoaDon.setTienThue(rs.getDouble("tax_price"));
            hoaDon.setTongHoaDon(rs.getDouble("total_due"));
            hoaDon.setKieuHoaDon(rs.getString("invoice_type"));
            hoaDon.setTinhTrangThanhToan(rs.getString("invoice_status"));
            hoaDon.setMaPhienDangNhap(rs.getString("shift_assignment_id"));
            hoaDon.setMaDonDatPhong(rs.getString("reservation_form_id"));
            hoaDon.setMaKhachHang(rs.getString("customer_id"));
            return hoaDon;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Invoice: " + e);
        }
    }
}
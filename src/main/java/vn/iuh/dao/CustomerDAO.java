package vn.iuh.dao;

import vn.iuh.entity.KhachHang;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {
    private final Connection connection;

    public CustomerDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public CustomerDAO(Connection connection) {
        this.connection = connection;
    }

    public KhachHang getCustomerByID(String id) {
        String query = "SELECT * FROM Customer WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang findCustomerByCCCD(String cccd) {
        String query = "SELECT * FROM Customer WHERE CCCD = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, cccd);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang findLastCustomer() {
        String query = "SELECT TOP 1 * FROM Customer WHERE is_deleted = 0 ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang createCustomer(KhachHang khachHang) {
        String query = "INSERT INTO Customer (id, customer_name, phone_number, CCCD, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, khachHang.getMa_khach_hang());
            ps.setString(2, khachHang.getTen_khach_hang());
            ps.setString(3, khachHang.getSo_dien_thoai());
            ps.setString(4, khachHang.getCCCD());

            ps.executeUpdate();
            return khachHang;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public KhachHang updateCustomer(KhachHang khachHang) {
        String query = "UPDATE Customer SET customer_name = ?, phone_number = ?, CCCD = ? " +
                "WHERE id = ? AND is_deleted = false";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, khachHang.getTen_khach_hang());
            ps.setString(2, khachHang.getSo_dien_thoai());
            ps.setString(3, khachHang.getCCCD());
            ps.setString(4, khachHang.getMa_khach_hang());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getCustomerByID(khachHang.getMa_khach_hang());
            } else {
                System.out.println("No Customer found with ID: " + khachHang.getMa_khach_hang());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteCustomerByID(String id) {
        if (getCustomerByID(id) == null) {
            System.out.println("No Customer found with ID: " + id);
            return false;
        }

        String query = "UPDATE Customer SET is_deleted = true WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Customer has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private KhachHang mapResultSetToCustomer(ResultSet rs) {
        KhachHang khachHang = new KhachHang();
        try {
            khachHang.setMa_khach_hang(rs.getString("id"));
            khachHang.setTen_khach_hang(rs.getString("customer_name"));
            khachHang.setSo_dien_thoai(rs.getString("phone_number"));
            khachHang.setCCCD(rs.getString("CCCD"));
            return khachHang;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Customer: " + e.getMessage());
        }
    }
}
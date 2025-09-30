package vn.iuh.dao;

import vn.iuh.entity.NhanVien;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDAO {
    private final Connection connection;

    public EmployeeDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public EmployeeDAO(Connection connection) {
        this.connection = connection;
    }

    public NhanVien getEmployeeByID(String id) {
        String query = "SELECT * FROM Employee WHERE id = ? AND is_deleted = false";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public NhanVien createEmployee(NhanVien nhanVien) {
        String query = "INSERT INTO Employee (id, employee_name, CCCD, birth_date, is_deleted) " +
                "VALUES (?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nhanVien.getMa_nhan_vien());
            ps.setString(2, nhanVien.getTen_nhan_vien());
            ps.setString(3, nhanVien.getCCCD());
            ps.setDate(4, new java.sql.Date(nhanVien.getNgay_sinh().getTime()));

            ps.executeUpdate();
            return nhanVien;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public NhanVien updateEmployee(NhanVien nhanVien) {
        String query = "UPDATE Employee SET employee_name = ?, CCCD = ?, birth_date = ? " +
                "WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nhanVien.getTen_nhan_vien());
            ps.setString(2, nhanVien.getCCCD());
            ps.setDate(3, new java.sql.Date(nhanVien.getNgay_sinh().getTime()));
            ps.setString(4, nhanVien.getMa_nhan_vien());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getEmployeeByID(nhanVien.getMa_nhan_vien());
            } else {
                System.out.println("No Employee found with ID: " + nhanVien.getMa_nhan_vien());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteEmployeeByID(String id) {
        if (getEmployeeByID(id) == null) {
            System.out.println("No Employee found with ID: " + id);
            return false;
        }

        String query = "UPDATE Employee SET is_deleted = true WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Employee has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private NhanVien mapResultSetToEmployee(ResultSet rs) {
        NhanVien nhanVien = new NhanVien();
        try {
            nhanVien.setMa_nhan_vien(rs.getString("id"));
            nhanVien.setTen_nhan_vien(rs.getString("employee_name"));
            nhanVien.setCCCD(rs.getString("CCCD"));
            nhanVien.setNgay_sinh(rs.getDate("birth_date"));
            return nhanVien;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Employee: " + e.getMessage());
        }
    }
}
package vn.iuh.dao;

import vn.iuh.entity.Employee;
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

    public Employee getEmployeeByID(String id) {
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

    public Employee createEmployee(Employee employee) {
        String query = "INSERT INTO Employee (id, employee_name, CCCD, birth_date, is_deleted) " +
                "VALUES (?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, employee.getId());
            ps.setString(2, employee.getEmployeeName());
            ps.setString(3, employee.getCCCD());
            ps.setDate(4, new java.sql.Date(employee.getBirthDate().getTime()));

            ps.executeUpdate();
            return employee;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Employee updateEmployee(Employee employee) {
        String query = "UPDATE Employee SET employee_name = ?, CCCD = ?, birth_date = ? " +
                "WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, employee.getEmployeeName());
            ps.setString(2, employee.getCCCD());
            ps.setDate(3, new java.sql.Date(employee.getBirthDate().getTime()));
            ps.setString(4, employee.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getEmployeeByID(employee.getId());
            } else {
                System.out.println("No Employee found with ID: " + employee.getId());
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

    private Employee mapResultSetToEmployee(ResultSet rs) {
        Employee employee = new Employee();
        try {
            employee.setId(rs.getString("id"));
            employee.setEmployeeName(rs.getString("employee_name"));
            employee.setCCCD(rs.getString("CCCD"));
            employee.setBirthDate(rs.getDate("birth_date"));
            return employee;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Employee: " + e.getMessage());
        }
    }
}
package vn.iuh.dao;

import vn.iuh.entity.Customer;
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

    public Customer getCustomerByID(String id) {
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

    public Customer findCustomerByCCCD(String cccd) {
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

    public Customer findLastCustomer() {
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

    public Customer createCustomer(Customer customer) {
        String query = "INSERT INTO Customer (id, customer_name, phone_number, CCCD, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customer.getId());
            ps.setString(2, customer.getCustomerName());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getCCCD());

            ps.executeUpdate();
            return customer;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Customer updateCustomer(Customer customer) {
        String query = "UPDATE Customer SET customer_name = ?, phone_number = ?, CCCD = ? " +
                "WHERE id = ? AND is_deleted = false";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, customer.getCustomerName());
            ps.setString(2, customer.getPhoneNumber());
            ps.setString(3, customer.getCCCD());
            ps.setString(4, customer.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getCustomerByID(customer.getId());
            } else {
                System.out.println("No Customer found with ID: " + customer.getId());
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

    private Customer mapResultSetToCustomer(ResultSet rs) {
        Customer customer = new Customer();
        try {
            customer.setId(rs.getString("id"));
            customer.setCustomerName(rs.getString("customer_name"));
            customer.setPhoneNumber(rs.getString("phone_number"));
            customer.setCCCD(rs.getString("CCCD"));
            return customer;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Customer: " + e.getMessage());
        }
    }
}
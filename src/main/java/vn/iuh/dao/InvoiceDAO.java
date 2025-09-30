package vn.iuh.dao;

import vn.iuh.entity.Invoice;
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

    public Invoice getInvoiceByID(String id) {
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

    public Invoice createInvoice(Invoice invoice) {
        String query = "INSERT INTO Invoice (id, create_date, payment, total_price, tax_price, total_due, " +
                "invoice_type, invoice_status, shift_assignment_id, reservation_form_id, customer_id, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, invoice.getId());
            ps.setTimestamp(2, invoice.getCreatDate() != null ? new Timestamp(invoice.getCreatDate().getTime()) : null);
            ps.setString(3, invoice.getPayment());
            ps.setDouble(4, invoice.getTotalPrice());
            ps.setDouble(5, invoice.getTaxPrice());
            ps.setDouble(6, invoice.getTotalDue());
            ps.setString(7, invoice.getInvoiceType());
            ps.setString(8, invoice.getInvoiceStatus());
            ps.setString(9, invoice.getShiftAssignmentId());
            ps.setString(10, invoice.getReservationFormId());
            ps.setString(11, invoice.getCustomerId());

            ps.executeUpdate();
            return invoice;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Invoice updateInvoice(Invoice invoice) {
        String query = "UPDATE Invoice SET create_date = ?, payment = ?, total_price = ?, tax_price = ?, total_due = ?, " +
                "invoice_type = ?, invoice_status = ?, shift_assignment_id = ?, reservation_form_id = ?, customer_id = ? " +
                "WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, invoice.getCreatDate() != null ? new Timestamp(invoice.getCreatDate().getTime()) : null);
            ps.setString(2, invoice.getPayment());
            ps.setDouble(3, invoice.getTotalPrice());
            ps.setDouble(4, invoice.getTaxPrice());
            ps.setDouble(5, invoice.getTotalDue());
            ps.setString(6, invoice.getInvoiceType());
            ps.setString(7, invoice.getInvoiceStatus());
            ps.setString(8, invoice.getShiftAssignmentId());
            ps.setString(9, invoice.getReservationFormId());
            ps.setString(10, invoice.getCustomerId());
            ps.setString(11, invoice.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getInvoiceByID(invoice.getId());
            } else {
                System.out.println("No invoice found with ID: " + invoice.getId());
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

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        try {
            invoice.setId(rs.getString("id"));
            invoice.setCreatDate(rs.getTimestamp("create_date")); // mapping datetime -> java.util.Date
            invoice.setPayment(rs.getString("payment"));
            invoice.setTotalPrice(rs.getDouble("total_price"));
            invoice.setTaxPrice(rs.getDouble("tax_price"));
            invoice.setTotalDue(rs.getDouble("total_due"));
            invoice.setInvoiceType(rs.getString("invoice_type"));
            invoice.setInvoiceStatus(rs.getString("invoice_status"));
            invoice.setShiftAssignmentId(rs.getString("shift_assignment_id"));
            invoice.setReservationFormId(rs.getString("reservation_form_id"));
            invoice.setCustomerId(rs.getString("customer_id"));
            return invoice;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Invoice: " + e);
        }
    }
}
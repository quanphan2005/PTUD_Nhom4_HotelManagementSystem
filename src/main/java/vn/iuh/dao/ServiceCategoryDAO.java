package vn.iuh.dao;

import vn.iuh.entity.ServiceCategory;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class ServiceCategoryDAO {
    private final Connection connection;

    public ServiceCategoryDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public ServiceCategoryDAO(Connection connection) {
        this.connection = connection;
    }

    public ServiceCategory getServiceCategoryByID(String id) {
        String query = "SELECT * FROM ServiceCategory WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToServiceCategory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public ServiceCategory createServiceCategory(ServiceCategory serviceCategory) {
        String query = "INSERT INTO ServiceCategory " +
                "(id, category_name) VALUES (?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, serviceCategory.getId());
            ps.setString(2, serviceCategory.getCategoryName());

            ps.executeUpdate();
            return serviceCategory;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ServiceCategory updateServiceCategory(ServiceCategory serviceCategory) {
        String query = "UPDATE ServiceCategory SET category_name = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, serviceCategory.getCategoryName());
            ps.setTimestamp(2, serviceCategory.getCreateAt() != null ? new Timestamp(serviceCategory.getCreateAt().getTime()) : null);
            ps.setString(3, serviceCategory.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getServiceCategoryByID(serviceCategory.getId());
            } else {
                System.out.println("No service category found with ID: " + serviceCategory.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteServiceCategoryByID(String id) {
        if (getServiceCategoryByID(id) == null) {
            System.out.println("No service category found with ID: " + id);
            return false;
        }

        String query = "UPDATE ServiceCategory SET is_deleted = 1 WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Service category has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private ServiceCategory mapResultSetToServiceCategory(ResultSet rs) throws SQLException {
        ServiceCategory serviceCategory = new ServiceCategory();
        try {
            serviceCategory.setId(rs.getString("id"));
            serviceCategory.setCategoryName(rs.getString("category_name"));
            serviceCategory.setCreateAt(rs.getTimestamp("create_at"));
            serviceCategory.setDeleted(rs.getBoolean("is_deleted"));
            return serviceCategory;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ServiceCategory: " + e);
        }
    }
}


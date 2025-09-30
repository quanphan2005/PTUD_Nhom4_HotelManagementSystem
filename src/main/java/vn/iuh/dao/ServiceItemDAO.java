package vn.iuh.dao;

import vn.iuh.entity.ServiceItem;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class ServiceItemDAO {
    private final Connection connection;

    public ServiceItemDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public ServiceItemDAO(Connection connection) {
        this.connection = connection;
    }

    public ServiceItem getServiceItemByID(String id) {
        String query = "SELECT * FROM ServiceItem WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToServiceItem(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public ServiceItem createServiceItem(ServiceItem serviceItem) {
        String query = "INSERT INTO ServiceItem " +
                "(id, item_name, service_category_id) " +
                "VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, serviceItem.getId());
            ps.setString(2, serviceItem.getItemName());
            ps.setString(3, serviceItem.getServiceCategoryId());


            ps.executeUpdate();
            return serviceItem;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ServiceItem updateServiceItem(ServiceItem serviceItem) {
        String query = "UPDATE ServiceItem SET item_name = ?, service_category_id = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, serviceItem.getItemName());
            ps.setString(2, serviceItem.getServiceCategoryId());
            ps.setTimestamp(3, serviceItem.getCreateAt() != null ? new Timestamp(serviceItem.getCreateAt().getTime()) : null);
            ps.setString(4, serviceItem.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getServiceItemByID(serviceItem.getId());
            } else {
                System.out.println("No service item found with ID: " + serviceItem.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteServiceItemByID(String id) {
        if (getServiceItemByID(id) == null) {
            System.out.println("No service item found with ID: " + id);
            return false;
        }

        String query = "UPDATE ServiceItem SET is_deleted = 1 WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Service item has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private ServiceItem mapResultSetToServiceItem(ResultSet rs) throws SQLException {
        ServiceItem serviceItem = new ServiceItem();
        try {
            serviceItem.setId(rs.getString("id"));
            serviceItem.setItemName(rs.getString("item_name"));
            serviceItem.setServiceCategoryId(rs.getString("service_category_id"));
            serviceItem.setCreateAt(rs.getTimestamp("create_at"));
            serviceItem.setDeleted(rs.getBoolean("is_deleted"));
            return serviceItem;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ServiceItem: " + e);
        }
    }

}

package vn.iuh.dao;

import vn.iuh.entity.DichVu;
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

    public DichVu getServiceItemByID(String id) {
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

    public DichVu createServiceItem(DichVu dichVu) {
        String query = "INSERT INTO ServiceItem " +
                "(id, item_name, service_category_id) " +
                "VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, dichVu.getMaDichVu());
            ps.setString(2, dichVu.getTenDichVu());
            ps.setString(3, dichVu.getMaLoaiDichVu());


            ps.executeUpdate();
            return dichVu;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public DichVu updateServiceItem(DichVu dichVu) {
        String query = "UPDATE ServiceItem SET item_name = ?, service_category_id = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, dichVu.getTenDichVu());
            ps.setString(2, dichVu.getMaLoaiDichVu());
            ps.setTimestamp(3, dichVu.getThoiGianTao() != null ? new Timestamp(dichVu.getThoiGianTao().getTime()) : null);
            ps.setString(4, dichVu.getMaDichVu());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getServiceItemByID(dichVu.getMaDichVu());
            } else {
                System.out.println("No service item found with ID: " + dichVu.getMaDichVu());
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

    private DichVu mapResultSetToServiceItem(ResultSet rs) throws SQLException {
        DichVu dichVu = new DichVu();
        try {
            dichVu.setMaDichVu(rs.getString("id"));
            dichVu.setTenDichVu(rs.getString("item_name"));
            dichVu.setMaLoaiDichVu(rs.getString("service_category_id"));
            dichVu.setThoiGianTao(rs.getTimestamp("create_at"));
            dichVu.setDeleted(rs.getBoolean("is_deleted"));
            return dichVu;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ServiceItem: " + e);
        }
    }

}

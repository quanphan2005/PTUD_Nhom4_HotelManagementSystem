package vn.iuh.dao;

import vn.iuh.entity.LoaiDichVu;
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

    public LoaiDichVu getServiceCategoryByID(String id) {
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

    public LoaiDichVu createServiceCategory(LoaiDichVu loaiDichVu) {
        String query = "INSERT INTO ServiceCategory " +
                "(id, category_name) VALUES (?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiDichVu.getMaLoaiDichVu());
            ps.setString(2, loaiDichVu.getTenDichVu());

            ps.executeUpdate();
            return loaiDichVu;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public LoaiDichVu updateServiceCategory(LoaiDichVu loaiDichVu) {
        String query = "UPDATE ServiceCategory SET category_name = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiDichVu.getTenDichVu());
            ps.setTimestamp(2, loaiDichVu.getThoiGianTao() != null ? new Timestamp(loaiDichVu.getThoiGianTao().getTime()) : null);
            ps.setString(3, loaiDichVu.getMaLoaiDichVu());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getServiceCategoryByID(loaiDichVu.getMaLoaiDichVu());
            } else {
                System.out.println("No service category found with ID: " + loaiDichVu.getMaLoaiDichVu());
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

    private LoaiDichVu mapResultSetToServiceCategory(ResultSet rs) throws SQLException {
        LoaiDichVu loaiDichVu = new LoaiDichVu();
        try {
            loaiDichVu.setMaLoaiDichVu(rs.getString("id"));
            loaiDichVu.setTenDichVu(rs.getString("category_name"));
            loaiDichVu.setThoiGianTao(rs.getTimestamp("create_at"));
            loaiDichVu.setDeleted(rs.getBoolean("is_deleted"));
            return loaiDichVu;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to ServiceCategory: " + e);
        }
    }
}


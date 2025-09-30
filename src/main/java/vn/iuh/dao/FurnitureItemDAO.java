package vn.iuh.dao;

import vn.iuh.entity.NoiThat;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FurnitureItemDAO {
    private final Connection connection;

    public FurnitureItemDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public FurnitureItemDAO(Connection connection) {
        this.connection = connection;
    }

    public NoiThat createFurnitureItem(NoiThat noiThat) {
        String query = "INSERT INTO FurnitureItem (id, item_name, item_description, is_deleted) VALUES (?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, noiThat.getMaNoiThat());
            ps.setString(2, noiThat.getTenNoiThat());
            ps.setString(3, noiThat.getMoTa());

            ps.executeUpdate();
            return noiThat;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public NoiThat updateFurnitureItem(NoiThat noiThat) {
        String query = "UPDATE FurnitureItem SET item_name = ?, item_description = ? WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, noiThat.getTenNoiThat());
            ps.setString(2, noiThat.getMoTa());
            ps.setString(3, noiThat.getMaNoiThat());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getFurnitureItemByID(noiThat.getMaNoiThat());
            } else {
                System.out.println("No FurnitureItem found with ID (or it is deleted): " + noiThat.getMaNoiThat());
                return null;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteFurnitureItemByID(String id) {
        if (getFurnitureItemByID(id) == null) {
            System.out.println("No FurnitureItem found with ID: " + id);
            return false;
        }

        String query = "UPDATE FurnitureItem SET is_deleted = true WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("FurnitureItem has been soft deleted successfully");
                return true;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public NoiThat getFurnitureItemByID(String id) {
        String query = "SELECT * FROM FurnitureItem WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToFurnitureItem(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public NoiThat mapResultSetToFurnitureItem(ResultSet rs) throws SQLException {
        NoiThat item = new NoiThat();
        try {
            item.setMaNoiThat(rs.getString("id"));
            item.setTenNoiThat(rs.getString("item_name"));
            item.setMoTa(rs.getString("item_description"));
            return item;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to FurnitureItem " + e);
        }
    }

}
package vn.iuh.dao;

import vn.iuh.entity.FurnitureItem;
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

    public FurnitureItem createFurnitureItem(FurnitureItem furnitureItem) {
        String query = "INSERT INTO FurnitureItem (id, item_name, item_description, is_deleted) VALUES (?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, furnitureItem.getId());
            ps.setString(2, furnitureItem.getItemName());
            ps.setString(3, furnitureItem.getItemDescription());

            ps.executeUpdate();
            return furnitureItem;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public FurnitureItem updateFurnitureItem(FurnitureItem furnitureItem) {
        String query = "UPDATE FurnitureItem SET item_name = ?, item_description = ? WHERE id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, furnitureItem.getItemName());
            ps.setString(2, furnitureItem.getItemDescription());
            ps.setString(3, furnitureItem.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getFurnitureItemByID(furnitureItem.getId());
            } else {
                System.out.println("No FurnitureItem found with ID (or it is deleted): " + furnitureItem.getId());
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

    public FurnitureItem getFurnitureItemByID(String id) {
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

    public FurnitureItem mapResultSetToFurnitureItem(ResultSet rs) throws SQLException {
        FurnitureItem item = new FurnitureItem();
        try {
            item.setId(rs.getString("id"));
            item.setItemName(rs.getString("item_name"));
            item.setItemDescription(rs.getString("item_description"));
            return item;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to FurnitureItem " + e);
        }
    }

}
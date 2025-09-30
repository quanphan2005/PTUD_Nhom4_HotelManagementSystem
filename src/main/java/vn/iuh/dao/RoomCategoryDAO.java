package vn.iuh.dao;

import vn.iuh.entity.RoomCategory;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class RoomCategoryDAO {
    private final Connection connection;

    public RoomCategoryDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public RoomCategoryDAO(Connection connection) {
        this.connection = connection;
    }

    public RoomCategory getRoomCategoryByID(String id) {
        String query = "SELECT * FROM RoomCategory WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToRoomCategory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public RoomCategory createRoomCategory(RoomCategory roomCategory) {
        String query = "INSERT INTO RoomCategory " +
                "(id, category_name, number_customer, room_type) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomCategory.getId());
            ps.setString(2, roomCategory.getCategoryName());
            ps.setInt(3, roomCategory.getNumberOfCustomer());
            ps.setString(4, roomCategory.getRoomType());

            ps.executeUpdate();
            return roomCategory;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public RoomCategory updateRoomCategory(RoomCategory roomCategory) {
        String query = "UPDATE RoomCategory SET category_name = ?, number_customer = ?, room_type = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomCategory.getCategoryName());
            ps.setInt(2, roomCategory.getNumberOfCustomer());
            ps.setString(3, roomCategory.getRoomType());
            ps.setTimestamp(4, roomCategory.getCreateAt() != null ? new Timestamp(roomCategory.getCreateAt().getTime()) : null);
            ps.setString(5, roomCategory.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getRoomCategoryByID(roomCategory.getId());
            } else {
                System.out.println("No room category found with ID: " + roomCategory.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteRoomCategoryByID(String id) {
        if (getRoomCategoryByID(id) == null) {
            System.out.println("No room category found with ID: " + id);
            return false;
        }

        String query = "UPDATE RoomCategory SET is_deleted = 1 WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room category has been deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private RoomCategory mapResultSetToRoomCategory(ResultSet rs) throws SQLException {
        RoomCategory roomCategory = new RoomCategory();
        try {
            roomCategory.setId(rs.getString("id"));
            roomCategory.setCategoryName(rs.getString("category_name"));
            roomCategory.setNumberOfCustomer(rs.getInt("number_customer"));
            roomCategory.setRoomType(rs.getString("room_type"));
            roomCategory.setCreateAt(rs.getTimestamp("create_at"));
            roomCategory.setDeleted(rs.getBoolean("is_deleted"));
            return roomCategory;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to RoomCategory: " + e);
        }
    }
}


package vn.iuh.dao;

import vn.iuh.entity.Room;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    private final Connection connection;

    public RoomDAO() {
        connection = DatabaseUtil.getConnect();
    }

    public RoomDAO(Connection connection) {
        this.connection = connection;
    }

    public Room findRoomByID(String roomID) {
        String query = "SELECT * FROM Room WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoom(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    public List<Room> findAll() {
        String query = "SELECT * FROM Room";
        List<Room> rooms = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                rooms.add(mapResultSetToRoom(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return rooms;
    }

    public Room insertRoom(Room room) {
        String query = "INSERT INTO Room (room_name, is_active, create_at, note, room_description, room_category_id)" +
                       " VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, room.getRoomName());
            ps.setBoolean(2, room.isActive());
            ps.setDate(3, new java.sql.Date(room.getCreateDate().getTime()));
            ps.setString(4, room.getNote());
            ps.setString(5, room.getRoomDescription());
            ps.setString(6, room.getRoomCategoryId());

            ps.executeUpdate();
            return findLastRoom();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Room updateRoom(Room room) {
        if (findRoomByID(room.getId()) == null) {
            System.out.println("No room found with ID: " + room.getId());
            return null;
        }

        String query = "UPDATE Room SET room_name = ?, is_active = ?, create_at = ?, note = ?, " +
                       "room_description = ?, room_category_id = ? WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, room.getRoomName());
            ps.setBoolean(2, room.isActive());
            ps.setDate(3, new java.sql.Date(room.getCreateDate().getTime()));
            ps.setString(4, room.getNote());
            ps.setString(5, room.getRoomDescription());
            ps.setString(6, room.getRoomCategoryId());
            ps.setString(7, room.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return findRoomByID(room.getId());
            } else {
                System.out.println("No room found with ID: " + room.getId());
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteRoomByID(String roomID) {
        if (findRoomByID(roomID) == null) {
            System.out.println("No room found with ID: " + roomID);
            return false;
        }

        String query = "DELETE FROM Room WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Room findLastRoom() {
        String query = "SELECT TOP 1 * FROM Room ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoom(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    private Room mapResultSetToRoom(ResultSet rs) {
        Room room = new Room();
        try {
            room.setId(rs.getString("id"));
            room.setRoomName(rs.getString("roomName"));
            room.setActive(rs.getBoolean("isActive"));
            room.setCreateDate(rs.getTimestamp("createDate"));
            room.setNote(rs.getString("note"));
            room.setRoomDescription(rs.getString("roomDescription"));
            room.setRoomCategoryId(rs.getString("roomCategoryId"));

            return room;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to Room entity" + e);
        }
    }
}

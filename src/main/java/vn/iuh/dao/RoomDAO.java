package vn.iuh.dao;

import vn.iuh.entity.Phong;
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

    public Phong findRoomByID(String roomID) {
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

    public List<Phong> findAll() {
        String query = "SELECT * FROM Room";
        List<Phong> phongs = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                phongs.add(mapResultSetToRoom(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return phongs;
    }

    public Phong insertRoom(Phong phong) {
        String query = "INSERT INTO Room (room_name, is_active, create_at, note, room_description, room_category_id)" +
                       " VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, phong.getTenPhong());
            ps.setBoolean(2, phong.isDangHoatDong());
            ps.setDate(3, new java.sql.Date(phong.getCreateDate().getTime()));
            ps.setString(4, phong.getGhiChu());
            ps.setString(5, phong.getMoTaPhong());
            ps.setString(6, phong.getMaLoaiPhong());

            ps.executeUpdate();
            return findLastRoom();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Phong updateRoom(Phong phong) {
        if (findRoomByID(phong.getMaPhong()) == null) {
            System.out.println("No room found with ID: " + phong.getMaPhong());
            return null;
        }

        String query = "UPDATE Room SET room_name = ?, is_active = ?, create_at = ?, note = ?, " +
                       "room_description = ?, room_category_id = ? WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, phong.getTenPhong());
            ps.setBoolean(2, phong.isDangHoatDong());
            ps.setDate(3, new java.sql.Date(phong.getCreateDate().getTime()));
            ps.setString(4, phong.getGhiChu());
            ps.setString(5, phong.getMoTaPhong());
            ps.setString(6, phong.getMaLoaiPhong());
            ps.setString(7, phong.getMaPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return findRoomByID(phong.getMaPhong());
            } else {
                System.out.println("No room found with ID: " + phong.getMaPhong());
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

    public Phong findLastRoom() {
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

    private Phong mapResultSetToRoom(ResultSet rs) {
        Phong phong = new Phong();
        try {
            phong.setMaPhong(rs.getString("id"));
            phong.setTenPhong(rs.getString("roomName"));
            phong.setDangHoatDong(rs.getBoolean("isDangHoatDong"));
            phong.setCreateDate(rs.getTimestamp("createDate"));
            phong.setGhiChu(rs.getString("note"));
            phong.setMoTaPhong(rs.getString("roomDescription"));
            phong.setMaLoaiPhong(rs.getString("roomCategoryId"));

            return phong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to Room entity" + e);
        }
    }
}

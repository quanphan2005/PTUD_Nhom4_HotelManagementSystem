package vn.iuh.dao;

import vn.iuh.entity.LoaiPhong;
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

    public LoaiPhong getRoomCategoryByID(String id) {
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

    public LoaiPhong createRoomCategory(LoaiPhong loaiPhong) {
        String query = "INSERT INTO RoomCategory " +
                "(id, category_name, number_customer, room_type) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiPhong.getMaLoaiPhong());
            ps.setString(2, loaiPhong.getTenLoaiPhong());
            ps.setInt(3, loaiPhong.getSoLuongKhach());
            ps.setString(4, loaiPhong.getPhanLoai());

            ps.executeUpdate();
            return loaiPhong;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public LoaiPhong updateRoomCategory(LoaiPhong loaiPhong) {
        String query = "UPDATE RoomCategory SET category_name = ?, number_customer = ?, room_type = ?, create_at = ? " +
                "WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiPhong.getTenLoaiPhong());
            ps.setInt(2, loaiPhong.getSoLuongKhach());
            ps.setString(3, loaiPhong.getPhanLoai());
            ps.setTimestamp(4, loaiPhong.getThoiGianTao() != null ? new Timestamp(loaiPhong.getThoiGianTao().getTime()) : null);
            ps.setString(5, loaiPhong.getMaLoaiPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getRoomCategoryByID(loaiPhong.getMaLoaiPhong());
            } else {
                System.out.println("No room category found with ID: " + loaiPhong.getMaLoaiPhong());
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

    private LoaiPhong mapResultSetToRoomCategory(ResultSet rs) throws SQLException {
        LoaiPhong loaiPhong = new LoaiPhong();
        try {
            loaiPhong.setMaLoaiPhong(rs.getString("id"));
            loaiPhong.setTenLoaiPhong(rs.getString("category_name"));
            loaiPhong.setSoLuongKhach(rs.getInt("number_customer"));
            loaiPhong.setPhanLoai(rs.getString("room_type"));
            loaiPhong.setThoiGianTao(rs.getTimestamp("create_at"));
            loaiPhong.setDeleted(rs.getBoolean("is_deleted"));
            return loaiPhong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to RoomCategory: " + e);
        }
    }
}


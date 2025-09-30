package vn.iuh.dao;

import vn.iuh.entity.Phong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {
    private final Connection connection;

    public PhongDAO() {
        connection = DatabaseUtil.getConnect();
    }

    public PhongDAO(Connection connection) {
        this.connection = connection;
    }

    public Phong timPhong(String roomID) {
        String query = "SELECT * FROM Phong WHERE ma_phong = ?";

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

    public List<Phong> timTatCaPhong() {
        String query = "SELECT * FROM Phong";
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

    public Phong themPhong(Phong phong) {
        String query = "INSERT INTO Phong (ma_phong, ten_phong, dang_hoat_dong, ghi_chu, mo_ta_phong, ma_loai_phong) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, phong.getMaPhong());
            ps.setString(2, phong.getTenPhong());
            ps.setBoolean(3, phong.isDangHoatDong());
            ps.setString(4, phong.getGhiChu());
            ps.setString(5, phong.getMoTaPhong());
            ps.setString(6, phong.getMaLoaiPhong());

            ps.executeUpdate();
            return timPhongMoiNhat();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Phong capNhatPhong(Phong phong) {
        if (timPhong(phong.getMaPhong()) == null) {
            System.out.println("No room found with ID: " + phong.getMaPhong());
            return null;
        }

        String query = "UPDATE Phong SET ten_phong = ?, dang_hoat_dong = ?, ghi_chu = ?, " +
                       "mo_ta_phong = ?, ma_loai_phong = ? WHERE ma_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, phong.getTenPhong());
            ps.setBoolean(2, phong.isDangHoatDong());
            ps.setString(3, phong.getGhiChu());
            ps.setString(4, phong.getMoTaPhong());
            ps.setString(5, phong.getMaLoaiPhong());
            ps.setString(6, phong.getMaPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0)
                return timPhong(phong.getMaPhong());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean xoaPhong(String roomID) {
        if (timPhong(roomID) == null) {
            System.out.println("No room found with ID: " + roomID);
            return false;
        }

        String query = "DELETE FROM Phong WHERE ma_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Phong timPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM Phong ORDER BY ma_phong DESC";

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
            phong.setMaPhong(rs.getString("ma_phong"));
            phong.setTenPhong(rs.getString("ten_phong"));
            phong.setDangHoatDong(rs.getBoolean("dang_hoat_dong"));
            phong.setGhiChu(rs.getString("ghi_chu"));
            phong.setMoTaPhong(rs.getString("mo_ta_phong"));
            phong.setMaLoaiPhong(rs.getString("ma_loai_phong"));
            phong.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return phong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành Phong" + e.getMessage());
        }
    }
}

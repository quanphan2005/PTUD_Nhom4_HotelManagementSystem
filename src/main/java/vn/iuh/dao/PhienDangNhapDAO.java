package vn.iuh.dao;

import vn.iuh.entity.PhienDangNhap;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class PhienDangNhapDAO {
    private final Connection connection;

    public PhienDangNhapDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public PhienDangNhapDAO(Connection connection) {
        this.connection = connection;
    }

    public PhienDangNhap timPhienDangNhap(String id) {
        String query = "SELECT * FROM PhienDangNhap WHERE ma_phien_dang_nhap = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhPhienDangNhap(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public PhienDangNhap themPhienDangNhap(PhienDangNhap shift) {
        String query = "INSERT INTO PhienDangNhap " +
                "(ma_phien_dang_nhap, so_quay, tg_bat_dau, tg_ket_thuc, ma_tai_khoan) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, shift.getMaPhienDangNhap());
            ps.setInt(2, shift.getSoQuay());
            ps.setTimestamp(3, shift.getTgBatDau() != null ? new Timestamp(shift.getTgBatDau().getTime()) : null);
            ps.setTimestamp(4, shift.getTgKetThuc() != null ? new Timestamp(shift.getTgKetThuc().getTime()) : null);
            ps.setString(5, shift.getMaTaiKhoan());

            ps.executeUpdate();
            return shift;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public PhienDangNhap timPhienDangNhapMoiNhat() {
        String query = "SELECT TOP 1 * FROM PhienDangNhap WHERE da_xoa = 0 ORDER BY ma_phien_dang_nhap DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if(rs.next()) {
                return chuyenKetQuaThanhPhienDangNhap(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private PhienDangNhap chuyenKetQuaThanhPhienDangNhap(ResultSet rs) throws SQLException {
        PhienDangNhap shift = new PhienDangNhap();
        try {
            shift.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            shift.setSoQuay(rs.getInt("so_quay"));
            shift.setTgBatDau(rs.getTimestamp("tg_bat_dau"));
            shift.setTgKetThuc(rs.getTimestamp("tg_ket_thuc"));
            shift.setMaTaiKhoan(rs.getString("ma_tai_khoan"));
            shift.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return shift;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành PhienDangNhap: " + e.getMessage());
        }
    }
}

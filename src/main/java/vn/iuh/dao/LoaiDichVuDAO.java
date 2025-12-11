package vn.iuh.dao;

import vn.iuh.entity.LoaiDichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.List;

public class LoaiDichVuDAO {
    public LoaiDichVu timLoaiDichVu(String id) {
        String query = "SELECT * FROM LoaiDichVu WHERE ma_loai_dich_vu = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
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

    public List<LoaiDichVu> layDanhSachLoaiDichVu() {
        String query = "SELECT * FROM LoaiDichVu WHERE da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            List<LoaiDichVu> loaiDichVuList = new java.util.ArrayList<>();
            while (rs.next()) {
                loaiDichVuList.add(mapResultSetToServiceCategory(rs));
            }
            return loaiDichVuList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public LoaiDichVu capNhatLoaiDichVuDAO(LoaiDichVu loaiDichVu) {
        String query = "UPDATE LoaiDichVu SET ten_loai_dich_vu = ?" +
                " WHERE ma_loai_dich_vu = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiDichVu.getTenDichVu());
            ps.setString(2, loaiDichVu.getMaLoaiDichVu());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timLoaiDichVu(loaiDichVu.getMaLoaiDichVu());
            } else {
                System.out.println("Không tìm thấy dịch vụ với, mã: " + loaiDichVu.getMaLoaiDichVu());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaLoaiDichVu(String id) {
        if (timLoaiDichVu(id) == null) {
            System.out.println("Không tìm thấy dịch vụ với, mã: " + id);
            return false;
        }

        String query = "UPDATE LoaiDichVu SET da_xoa = 1 WHERE ma_loai_dich_vu = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa dịch vụ thành công, mã: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public LoaiDichVu timLoaiDichVuMoiNhat() {
        String query = "SELECT TOP 1 * FROM LoaiDichVu WHERE da_xoa = 0 ORDER BY ma_loai_dich_vu DESC";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

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

    private LoaiDichVu mapResultSetToServiceCategory(ResultSet rs) throws SQLException {
        LoaiDichVu loaiDichVu = new LoaiDichVu();
        try {
            loaiDichVu.setMaLoaiDichVu(rs.getString("ma_loai_dich_vu"));
            loaiDichVu.setTenDichVu(rs.getString("ten_loai_dich_vu"));
            loaiDichVu.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return loaiDichVu;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành LoaiDichVu: " + e.getMessage());
        }
    }
}


package vn.iuh.dao;

import vn.iuh.dto.response.ServiceCategoryResponse;
import vn.iuh.entity.LoaiDichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // Map mã loại dịch vụ thành tên loại dịch vụ để hiển thị lên table
    public Map<String, String> layMapMaThanhTenLoaiDichVu() {
        String sql = "SELECT ma_loai_dich_vu, ten_loai_dich_vu FROM LoaiDichVu WHERE da_xoa = 0 ORDER BY ma_loai_dich_vu ASC";
        Connection connection = DatabaseUtil.getConnect();
        Map<String, String> map = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String ma = rs.getString("ma_loai_dich_vu");
                String ten = rs.getString("ten_loai_dich_vu");
                map.put(ma, ten);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy danh sách loại dịch vụ: " + ex.getMessage(), ex);
        }
        return map;
    }

    public List<ServiceCategoryResponse> layTatCaLoaiDichVuVaSoLuongDichVu() {
        String sql = """
        SELECT l.ma_loai_dich_vu, l.ten_loai_dich_vu, 
               COUNT(dv.ma_dich_vu) AS so_luong
        FROM LoaiDichVu l
        LEFT JOIN DichVu dv 
          ON dv.ma_loai_dich_vu = l.ma_loai_dich_vu 
         AND ISNULL(dv.da_xoa, 0) = 0
        WHERE ISNULL(l.da_xoa, 0) = 0
        GROUP BY l.ma_loai_dich_vu, l.ten_loai_dich_vu
        ORDER BY l.ma_loai_dich_vu ASC
    """;
        Connection connection = DatabaseUtil.getConnect();
        List<ServiceCategoryResponse> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String ma = rs.getString("ma_loai_dich_vu");
                String ten = rs.getString("ten_loai_dich_vu");
                int cnt = rs.getInt("so_luong");
                out.add(new ServiceCategoryResponse(ma, ten, cnt));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy loại dịch vụ kèm số lượng: " + ex.getMessage(), ex);
        }
        return out;
    }

    public boolean existsByTenLoaiDichVu(String tenLoai) {
        if (tenLoai == null) return false;
        String sql = "SELECT TOP 1 1 FROM LoaiDichVu WHERE ten_loai_dich_vu = ? AND ISNULL(da_xoa,0) = 0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenLoai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi kiểm tra tên loại dịch vụ: " + ex.getMessage(), ex);
        }
    }

    public String timMaLoaiDichVuMoiNhatRaw() {
        String sql = "SELECT TOP 1 ma_loai_dich_vu FROM LoaiDichVu WHERE ma_loai_dich_vu IS NOT NULL ORDER BY ma_loai_dich_vu DESC";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("ma_loai_dich_vu");
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy mã LoaiDichVu mới nhất: " + ex.getMessage(), ex);
        }
        return null;
    }

    public boolean insertLoaiDichVu(String maLoai, String tenLoai) {
        String sql = "INSERT INTO LoaiDichVu (ma_loai_dich_vu, ten_loai_dich_vu, thoi_gian_tao, da_xoa) " +
                "VALUES (?, ?, GETDATE(), 0)";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maLoai);
            ps.setString(2, tenLoai);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi chèn LoaiDichVu mới: " + ex.getMessage(), ex);
        }
    }
}


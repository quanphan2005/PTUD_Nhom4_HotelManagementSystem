// file: vn/iuh/dao/NoiThatTrongLoaiPhongDAO.java
package vn.iuh.dao;

import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.entity.NoiThat;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that, ...)
 * - findByLoaiPhong(maLoai)
 * - replaceMappings(maLoai, List<NoiThat>) : cập nhật mapping (soft-delete mapping cũ, insert mapping mới)
 *
 * Lưu ý: id prefix cho mapping là "NP" giống dữ liệu mẫu (NP00000001...)
 */
public class NoiThatTrongLoaiPhongDAO {
    /**
     * Lấy danh sách NoiThat được gán cho 1 LoaiPhong (chỉ lấy mapping da_xoa = 0 và noi_that.da_xoa = 0)
     */
    public List<NoiThat> findByLoaiPhong(String maLoaiPhong) {
        String query = "SELECT nt.ma_noi_that, nt.ten_noi_that, nt.mo_ta " +
                "FROM NoiThatTrongLoaiPhong ntlp " +
                "JOIN NoiThat nt ON ntlp.ma_noi_that = nt.ma_noi_that " +
                "WHERE ntlp.ma_loai_phong = ? AND ntlp.da_xoa = 0 AND nt.da_xoa = 0";
        List<NoiThat> list = new ArrayList<>();
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maLoaiPhong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NoiThat n = new NoiThat();
                n.setMaNoiThat(rs.getString("ma_noi_that"));
                n.setTenNoiThat(rs.getString("ten_noi_that"));
                n.setMoTa(rs.getString("mo_ta"));
                list.add(n);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * Thay thế toàn bộ mapping của 1 LoaiPhong:
     * - soft-delete tất cả mapping hiện tại (set da_xoa = 1)
     * - insert mapping mới (so_luong mặc định = 1)
     *
     * Trả về true nếu commit thành công.
     */
    public boolean replaceMappings(String maLoaiPhong, List<NoiThat> items) {
        if (maLoaiPhong == null) return false;
        try {
            Connection connection = DatabaseUtil.getConnect();
            DatabaseUtil.khoiTaoGiaoTac();

            // Soft-delete existing mappings
            String softDeleteSql = "UPDATE NoiThatTrongLoaiPhong SET da_xoa = 1 WHERE ma_loai_phong = ?";
            try (PreparedStatement psDel = connection.prepareStatement(softDeleteSql)) {
                psDel.setString(1, maLoaiPhong);
                psDel.executeUpdate();
            }

            // find latest existing mapping id to generate next ids
            String latestId = findLatestMappingId();
            // we'll use prefix "NP" and suffix length 8 (data sample uses NP00000001)
            String prefix = "NP";
            int suffixLength = 8;

            String insertSql = "INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psIns = connection.prepareStatement(insertSql)) {
                for (NoiThat n : items) {
                    latestId = EntityUtil.increaseEntityID(latestId, prefix, suffixLength);
                    psIns.setString(1, latestId);
                    psIns.setInt(2, 1); // default so_luong
                    psIns.setString(3, maLoaiPhong);
                    psIns.setString(4, n.getMaNoiThat());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            DatabaseUtil.thucHienGiaoTac();
            return true;
        } catch (SQLException e) {
            DatabaseUtil.hoanTacGiaoTac();
        }
        return false;
    }

    /**
     * Tìm ma_noi_that_trong_loai_phong mới nhất (kể cả da_xoa), trả null nếu chưa có.
     */
    private String findLatestMappingId() {
        String query = "SELECT TOP 1 ma_noi_that_trong_loai_phong FROM NoiThatTrongLoaiPhong ORDER BY ma_noi_that_trong_loai_phong DESC";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ma_noi_that_trong_loai_phong");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean replaceMappingsWithQuantities(String maLoaiPhong, List<NoiThatAssignment> items) {
        if (maLoaiPhong == null) return false;
        try {
            Connection connection = DatabaseUtil.getConnect();
            DatabaseUtil.khoiTaoGiaoTac();

            // Soft-delete existing mappings
            String softDeleteSql = "UPDATE NoiThatTrongLoaiPhong SET da_xoa = 1 WHERE ma_loai_phong = ?";
            try (PreparedStatement psDel = connection.prepareStatement(softDeleteSql)) {
                psDel.setString(1, maLoaiPhong);
                psDel.executeUpdate();
            }

            // find latest existing mapping id to generate next ids
            String latestId = findLatestMappingId();
            String prefix = "NP";
            int suffixLength = 8;

            String insertSql = "INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psIns = connection.prepareStatement(insertSql)) {
                for (NoiThatAssignment it : items) {
                    latestId = EntityUtil.increaseEntityID(latestId, prefix, suffixLength);
                    psIns.setString(1, latestId);
                    psIns.setInt(2, Math.max(1, it.getSoLuong())); // default >=1
                    psIns.setString(3, maLoaiPhong);
                    psIns.setString(4, it.getMaNoiThat());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            DatabaseUtil.thucHienGiaoTac();
            return true;
        } catch (SQLException e) {
            DatabaseUtil.hoanTacGiaoTac();
        }
        return false;
    }

    public int softDeleteByLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null) return 0;
        String sql = "UPDATE NoiThatTrongLoaiPhong SET da_xoa = 1 WHERE ma_loai_phong = ? AND ISNULL(da_xoa,0) = 0";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maLoaiPhong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

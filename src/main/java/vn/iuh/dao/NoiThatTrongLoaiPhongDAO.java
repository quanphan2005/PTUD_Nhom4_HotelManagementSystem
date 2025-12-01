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

public class NoiThatTrongLoaiPhongDAO {
    private final Connection connection;

    public NoiThatTrongLoaiPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public NoiThatTrongLoaiPhongDAO(Connection connection) {
        this.connection = connection;
    }

    //Lấy danh sách nội thất của 1 loại phòng
    public List<NoiThat> findByLoaiPhong(String maLoaiPhong) {
        String query = "SELECT nt.ma_noi_that, nt.ten_noi_that, nt.mo_ta " +
                "FROM NoiThatTrongLoaiPhong ntlp " +
                "JOIN NoiThat nt ON ntlp.ma_noi_that = nt.ma_noi_that " +
                "WHERE ntlp.ma_loai_phong = ? AND ntlp.da_xoa = 0 AND nt.da_xoa = 0";
        List<NoiThat> list = new ArrayList<>();
        try {
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

    //Tìm nội thất trong loại phòng mới nhất để sinh ID
    private String findLatestMappingId() {
        String query = "SELECT TOP 1 ma_noi_that_trong_loai_phong FROM NoiThatTrongLoaiPhong ORDER BY ma_noi_that_trong_loai_phong DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
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
            connection.setAutoCommit(false);

            //Xóa nội thất theo loại phòng
            String softDeleteSql = "UPDATE NoiThatTrongLoaiPhong SET da_xoa = 1 WHERE ma_loai_phong = ?";
            try (PreparedStatement psDel = connection.prepareStatement(softDeleteSql)) {
                psDel.setString(1, maLoaiPhong);
                psDel.executeUpdate();
            }

            // Tìm mã nội thất trong loại phòng mới nhất để sinh ID
            String latestId = findLatestMappingId();
            String prefix = "NP";
            int suffixLength = 8;

            String insertSql = "INSERT INTO NoiThatTrongLoaiPhong (ma_noi_that_trong_loai_phong, so_luong, ma_loai_phong, ma_noi_that) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psIns = connection.prepareStatement(insertSql)) {
                for (NoiThatAssignment it : items) {
                    latestId = EntityUtil.increaseEntityID(latestId, prefix, suffixLength);
                    psIns.setString(1, latestId);
                    psIns.setInt(2, Math.max(1, it.getSoLuong()));
                    psIns.setString(3, maLoaiPhong);
                    psIns.setString(4, it.getMaNoiThat());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
            }
            throw new RuntimeException(e);
        }
    }

    public int softDeleteByLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null) return 0;
        String sql = "UPDATE NoiThatTrongLoaiPhong SET da_xoa = 1 WHERE ma_loai_phong = ? AND ISNULL(da_xoa,0) = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maLoaiPhong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

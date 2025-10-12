package vn.iuh.dao;

import vn.iuh.entity.LichSuDiVao;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LichSuDiVaoDAO {
    private final Connection connection;

    public LichSuDiVaoDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public LichSuDiVaoDAO(Connection connection) {
        this.connection = connection;
    }

    public void themLichSuDiVao(LichSuDiVao lichSuDiVao) {
        String query = "INSERT INTO LichSuDiVao" +
                       " (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong)" +
                       " VALUES (?, ?, ?)";
        try {
            var ps = connection.prepareStatement(query);
            ps.setString(1, lichSuDiVao.getMaLichSuDiVao());
            ps.setBoolean(2, lichSuDiVao.getLaLanDauTien());
            ps.setString(3, lichSuDiVao.getMaChiTietDatPhong());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LichSuDiVao timLichSuDiVaoMoiNhat() {
        String query = "SELECT TOP 1 * FROM LichSuDiVao ORDER BY ma_lich_su_di_vao DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhLichSuDiVao(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private LichSuDiVao chuyenKetQuaThanhLichSuDiVao(ResultSet rs) {
        try {
            String maLichSuDiVao = rs.getString("ma_lich_su_di_vao");
            boolean laLanDauTien = rs.getBoolean("la_lan_dau_tien");
            String maChiTietDatPhong = rs.getString("ma_chi_tiet_dat_phong");
            var thoiGianTao = rs.getTimestamp("thoi_gian_tao");

            return new LichSuDiVao(maLichSuDiVao, laLanDauTien, maChiTietDatPhong, thoiGianTao);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành LichSuDiVao" + e.getMessage());
        }
    }

    public boolean daTonTai(String maChiTietDatPhong) {
        String q = "SELECT COUNT(1) AS cnt FROM LichSuDiVao WHERE ma_chi_tiet_dat_phong = ? AND da_xoa = 0";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, maChiTietDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}

package vn.iuh.dao;

import vn.iuh.entity.LichSuDiVao;
import vn.iuh.entity.LichSuRaNgoai;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LichSuRaNgoaiDAO {
    private final Connection connection;

    public LichSuRaNgoaiDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public LichSuRaNgoaiDAO(Connection connection) {
        this.connection = connection;
    }
    public LichSuRaNgoai themLichSuRaNgoai(LichSuRaNgoai lichSuRaNgoai) {
        String query = "INSERT INTO LichSuRaNgoai" +
                " (ma_lich_su_ra_ngoai, la_lan_cuoi_cung, ma_chi_tiet_dat_phong)" +
                " VALUES (?, ?, ?)";
        try {
            var ps = connection.prepareStatement(query);
            ps.setString(1, lichSuRaNgoai.getMaLichSuRaNgoai());
            ps.setBoolean(2, lichSuRaNgoai.isLaLanCuoiCung());
            ps.setString(3, lichSuRaNgoai.getMaChiTietDatPhong());

            int rowAffected =  ps.executeUpdate();
            return rowAffected > 0 ? lichSuRaNgoai : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int themDanhSachLichSuRaNgoai(List<LichSuRaNgoai> danhSach) {
        String query = "INSERT INTO LichSuRaNgoai (ma_lich_su_ra_ngoai, la_lan_cuoi_cung, ma_chi_tiet_dat_phong) VALUES (?, ?, ?)";
        try (var ps = connection.prepareStatement(query)) {
            for (LichSuRaNgoai lichSu : danhSach) {
                ps.setString(1, lichSu.getMaLichSuRaNgoai());
                ps.setBoolean(2, lichSu.isLaLanCuoiCung());
                ps.setString(3, lichSu.getMaChiTietDatPhong());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            int count = 0;
            for (int r : results) {
                if (r > 0) count++;
            }
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LichSuRaNgoai> timLichSuRaNgoaiBangMaChiTietDatPhong(String maChiTietDatPhong) {
        String query = "SELECT * FROM LichSuRaNgoai WHERE ma_chi_tiet_dat_phong = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            List<LichSuRaNgoai> lichSuRaNgoaiList = new java.util.ArrayList<>();
            var rs = ps.executeQuery();
            while (rs.next())
                lichSuRaNgoaiList.add(chuyenKetQuaThanhLichSuRaNgoai(rs));

            return lichSuRaNgoaiList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LichSuRaNgoai timLichSuRaNgoaiMoiNhat() {
        String query = "SELECT TOP 1 * FROM LichSuRaNgoai ORDER BY ma_lich_su_ra_ngoai DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhLichSuRaNgoai(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LichSuRaNgoai> timLichSuRaNgoaiBangDanhSachMaChiTietDatPhong(List<String> danhSachMaChiTietDatPhong) {
        if (danhSachMaChiTietDatPhong.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM LichSuRaNgoai WHERE ma_chi_tiet_dat_phong IN (");
        for (int i = 0; i < danhSachMaChiTietDatPhong.size(); i++) {
            queryBuilder.append("?");
            if (i < danhSachMaChiTietDatPhong.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(") AND da_xoa = 0");

        try {
            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < danhSachMaChiTietDatPhong.size(); i++) {
                ps.setString(i + 1, danhSachMaChiTietDatPhong.get(i));
            }

            List<LichSuRaNgoai> lichSuRaNgoaiList = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lichSuRaNgoaiList.add(chuyenKetQuaThanhLichSuRaNgoai(rs));
            }

            return lichSuRaNgoaiList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private LichSuRaNgoai chuyenKetQuaThanhLichSuRaNgoai(ResultSet rs) {
        try {
            String maLichSuRaNgoai = rs.getString("ma_lich_su_ra_ngoai");
            boolean laLanCuoiCung = rs.getBoolean("la_lan_cuoi_cung");
            String maChiTietDatPhong = rs.getString("ma_chi_tiet_dat_phong");
            var thoiGianTao = rs.getTimestamp("thoi_gian_tao");

            return new LichSuRaNgoai(maLichSuRaNgoai, laLanCuoiCung, maChiTietDatPhong, thoiGianTao);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành LichSuDiVao" + e.getMessage());
        }
    }
}

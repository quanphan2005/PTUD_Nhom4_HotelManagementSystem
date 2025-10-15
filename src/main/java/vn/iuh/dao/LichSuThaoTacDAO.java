package vn.iuh.dao;

import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichSuThaoTacDAO {
    private final Connection connection;

    public LichSuThaoTacDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public LichSuThaoTacDAO(Connection connection) {
        this.connection = connection;
    }

    public LichSuThaoTac timLichSuThaoTac(String id) {
        String query = "SELECT * FROM LichSuThaoTac WHERE ma_lich_su_thao_tac = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToWorkingHistory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public void themLichSuThaoTac(LichSuThaoTac wh) {
        String query = "INSERT INTO LichSuThaoTac (ma_lich_su_thao_tac, ten_thao_tac, mo_ta, ma_phien_dang_nhap) "
                + "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, wh.getMaLichSuThaoTac());
            ps.setString(2, wh.getTenThaoTac());
            ps.setString(3, wh.getMoTa());
            ps.setString(4, wh.getMaPhienDangNhap());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public LichSuThaoTac timLichSuThaoTacMoiNhat() {
        String query = "SELECT TOP 1 * FROM LichSuThaoTac WHERE da_xoa = 0 ORDER BY ma_lich_su_thao_tac DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToWorkingHistory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    private LichSuThaoTac mapResultSetToWorkingHistory(ResultSet rs) throws SQLException {
        LichSuThaoTac wh = new LichSuThaoTac();
        try {
            wh.setMaLichSuThaoTac(rs.getString("ma_lich_su_thao_tac"));
            wh.setTenThaoTac(rs.getString("ten_thao_tac"));
            wh.setMoTa(rs.getString("mo_ta"));
            wh.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            wh.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return wh;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành LichSuThaoTac: " + e);
        }
    }

public List<LichSuThaoTac> timThaoTacTheoPhienDN(String maPhienDangNhap){
        String query = "SELECT  * FROM LichSuThaoTac WHERE da_xoa = 0 and ma_phien_dang_nhap = ?";
        List<LichSuThaoTac> danhSachThaoTac = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhienDangNhap);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachThaoTac.add(mapResultSetToWorkingHistory(rs));
            }
            return danhSachThaoTac;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }
        return null;
    }
}


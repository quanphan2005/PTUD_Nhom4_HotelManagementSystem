package vn.iuh.dao;

import vn.iuh.entity.ThongBao;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class ThongBaoDAO {
    private final Connection connection;

    public ThongBaoDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public ThongBaoDAO(Connection connection) {
        this.connection = connection;
    }

    public ThongBao timThongBao(String id) {
        String query = "SELECT * FROM ThongBao WHERE ma_thong_bao = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhThongBao(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public ThongBao themThongBao(ThongBao thongBao) {
        String query = "INSERT INTO ThongBao (ma_thong_bao, noi_dung, ma_phien_dang_nhap)" +
                "VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, thongBao.getMaThongBao());
            ps.setString(2, thongBao.getNoiDung());
            ps.setString(3, thongBao.getMaPhienDangNhap());


            ps.executeUpdate();
            return thongBao;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ThongBao timThongBaoMoiNhat() {
        String query = "SELECT TOP 1 * FROM ThongBao WHERE da_xoa = 0 ORDER BY ma_thong_bao DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhThongBao(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public boolean xoaThongBao(String id) {
        if (timThongBao(id) == null) {
            System.out.println("Không tìm thấy thông báo với mã: " + id);
            return false;
        }

        String query = "UPDATE ThongBao SET da_xoa = 1 WHERE ma_thong_bao = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xoa thông báo thành công với mã: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private ThongBao chuyenKetQuaThanhThongBao(ResultSet rs) throws SQLException {
        ThongBao thongBao = new ThongBao();
        try {
            thongBao.setMaThongBao(rs.getString("ma_thong_bao"));
            thongBao.setNoiDung(rs.getString("noi_dung"));
            thongBao.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            thongBao.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return thongBao;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển kết quả thành ThongBao" + e.getMessage());
        }
    }
}


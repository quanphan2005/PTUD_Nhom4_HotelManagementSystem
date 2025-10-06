package vn.iuh.dao;

import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDatPhongDAO {
    private final Connection connection;

    public ChiTietDatPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }
    public void thucHienGiaoTac() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.commit();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi commit transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public void hoanTacGiaoTac() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi rollback transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public ChiTietDatPhongDAO(Connection connection) {
        this.connection = connection;
    }

    public int capNhatKetThucCTDP(List<ChiTietDatPhong> chiTietDatPhongs) {
        if (chiTietDatPhongs == null || chiTietDatPhongs.isEmpty()) return 0;

        StringBuilder query = new StringBuilder("UPDATE ChiTietDatPhong SET kieu_ket_thuc = ? WHERE kieu_ket_thuc is not null ma_chi_tiet_dat_phong IN (");
        for (int i = 0; i < chiTietDatPhongs.size(); i++) {
            query.append("?");
            if (i < chiTietDatPhongs.size() - 1) query.append(",");
        }
        query.append(")");

        try (var ps = connection.prepareStatement(query.toString())) {
            // Set kieu_ket_thuc parameter
            ps.setString(1, "Trả phòng");
            // Set ma_chi_tiet_dat_phong parameters
            for (int i = 0; i < chiTietDatPhongs.size(); i++) {
                ps.setString(i + 2, chiTietDatPhongs.get(i).getMaChiTietDatPhong());
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ChiTietDatPhong findLastestByRoom(String roomID) {
        String query = "SELECT TOP 1 * FROM ChiTietDatPhong WHERE ma_phong = ? and  tg_nhan_phong >= getdate() ORDER BY tg_nhan_phong asc";

        try {
            var ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            var rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToChiTietDatPhong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public ChiTietDatPhong mapResultSetToChiTietDatPhong(ResultSet rs) throws SQLException {
        return new ChiTietDatPhong(
            rs.getString("ma_chi_tiet_dat_phong"),
            rs.getTimestamp("tg_nhan_phong"),
            rs.getTimestamp("tg_tra_phong"),
            rs.getString("kieu_ket_thuc"),
            rs.getString("ma_don_dat_phong"),
            rs.getString("ma_phong"),
            rs.getString("ma_phien_dang_nhap"),
            rs.getTimestamp("thoi_gian_tao")
        );
    }

    public List<ChiTietDatPhong> findByBookingId(String bookingId) {
        String query = "SELECT * FROM ChiTietDatPhong WHERE ma_don_dat_phong = ? and da_xoa = 0 ORDER BY tg_nhan_phong asc";
        List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();
        try {
            var ps = connection.prepareStatement(query);
            ps.setString(1, bookingId);

            var rs = ps.executeQuery();
            while (rs.next()) {
                chiTietDatPhongs.add(mapResultSetToChiTietDatPhong(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return chiTietDatPhongs;
    }
}

package vn.iuh.dao;

import vn.iuh.entity.CongViec;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.List;

public class JobDAO {
    private final Connection connection;

    public JobDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public JobDAO(Connection connection) {
        this.connection = connection;
    }

    public CongViec findLastJob() {
        String query = "SELECT TOP 1 * FROM CongViec ORDER BY ma_cong_viec DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToJob(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public void insertJobs(List<CongViec> congViecs) {
        String query = "INSERT INTO CongViec (ma_cong_viec, ten_trang_thai, tg_bat_dau, tg_ket_thuc, ma_phong) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (CongViec congViec : congViecs) {
                ps.setString(1, congViec.getMaCongViec());
                ps.setString(2, congViec.getTenTrangThai());
                ps.setTimestamp(3, congViec.getTgBatDau());
                ps.setTimestamp(4, congViec.getTgKetThuc());
                ps.setString(5, congViec.getMaPhong());

                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public CongViec mapResultSetToJob(ResultSet rs) throws SQLException, TableEntityMismatch {
        try {
            String maCongViec = rs.getString("ma_cong_viec");
            String tenTrangThai = rs.getString("ten_trang_thai");
            Timestamp tgBatDau = rs.getTimestamp("tg_bat_dau");
            Timestamp tgKetThuc = rs.getTimestamp("tg_ket_thuc");
            String maPhong = rs.getString("ma_phong");
            Timestamp thoiGianTao = rs.getTimestamp("thoi_gian_tao");

            return new CongViec(maCongViec, tenTrangThai, tgBatDau, tgKetThuc, maPhong, thoiGianTao);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển kết quả thành CongViec" + e.getMessage());
        }
    }


}

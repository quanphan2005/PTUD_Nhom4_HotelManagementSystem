package vn.iuh.dao;

import vn.iuh.entity.GiaPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GiaPhongDAO {

    public boolean insertGiaPhong(GiaPhong gp) {
        if (gp.getMaLoaiPhong() == null) return false;
        String sql = "INSERT INTO GiaPhong (ma_gia_phong, ma_loai_phong, gia_ngay_cu, gia_gio_cu, gia_ngay_moi, gia_gio_moi)" +
                           " VALUES (?, ?, ?, ?, ?, ?)";
        try{
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, gp.getMaGiaPhong());
            ps.setString(2, gp.getMaLoaiPhong()) ;
            ps.setDouble(3, gp.getGiaNgayCu());
            ps.setDouble(4, gp.getGiaGioCu());
            ps.setDouble(5, gp.getGioNgayMoi());
            ps.setDouble(6, gp.getGioGioMoi());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Thêm loại phòng lỗi: " + e.getMessage(), e);
        }
    }

    public GiaPhong timGiaPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM GiaPhong ORDER BY ma_gia_phong DESC";

        Connection connection = DatabaseUtil.getConnect();
        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoomPrice(rs) ;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    private GiaPhong mapResultSetToRoomPrice(ResultSet rs) {
        GiaPhong gp = new GiaPhong();
        try {
            gp.setMaGiaPhong(rs.getString("ma_gia_phong"));
            gp.setMaLoaiPhong(rs.getString("ma_loai_phong"));
            gp.setGiaGioCu(rs.getDouble("gia_gio_cu"));
            gp.setGiaNgayCu(rs.getDouble("gia_ngay_cu"));
            gp.setGioGioMoi(rs.getDouble("gia_gio_moi"));
            gp.setGioNgayMoi(rs.getDouble("gia_ngay_moi"));
            return gp;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành Phong" + e.getMessage());
        }
    }

    public List<GiaPhong> findByLoaiPhong(String maLoaiPhong) {
        List<GiaPhong> list = new ArrayList<>();
        String sql = "SELECT ma_gia_phong, ma_loai_phong, gia_gio_cu, gia_ngay_cu, gia_gio_moi, gia_ngay_moi, ma_phien_dang_nhap, thoi_gian_tao " +
                "FROM GiaPhong WHERE ma_loai_phong = ? ORDER BY thoi_gian_tao DESC";
        Connection conn = DatabaseUtil.getConnect();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRoomPriceV2(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy lịch sử giá: " + e.getMessage(), e);
        }
        return list;
    }

    private GiaPhong mapResultSetToRoomPriceV2(ResultSet rs) {
        GiaPhong gp = new GiaPhong();
        try {
            gp.setMaGiaPhong(rs.getString("ma_gia_phong"));
            gp.setMaLoaiPhong(rs.getString("ma_loai_phong"));
            // Các cột giá cũ / mới
            try { gp.setGiaGioCu(rs.getDouble("gia_gio_cu")); } catch (Exception ignore) { gp.setGiaGioCu(0.0); }
            try { gp.setGiaNgayCu(rs.getDouble("gia_ngay_cu")); } catch (Exception ignore) { gp.setGiaNgayCu(0.0); }
            try { gp.setGioGioMoi(rs.getDouble("gia_gio_moi")); } catch (Exception ignore) { gp.setGioGioMoi(0.0); }
            try { gp.setGioNgayMoi(rs.getDouble("gia_ngay_moi")); } catch (Exception ignore) { gp.setGioNgayMoi(0.0); }

            // thông tin ai thay đổi và thời gian
            try { gp.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap")); } catch (Exception ignore) { gp.setMaPhienDangNhap(null); }
            try { gp.setThoiGianTao(rs.getTimestamp("thoi_gian_tao")); } catch (Exception ignore) { gp.setThoiGianTao(null); }

            return gp;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi chuyển ResultSet thành GiaPhong: " + e.getMessage(), e);
        }
    }

    public boolean insertGiaPhongV2(GiaPhong gp) {
        if (gp == null || gp.getMaLoaiPhong() == null) return false;
        String sql = "INSERT INTO GiaPhong (ma_gia_phong, ma_loai_phong, gia_ngay_cu, gia_gio_cu, gia_ngay_moi, gia_gio_moi, ma_phien_dang_nhap) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = DatabaseUtil.getConnect();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, gp.getMaGiaPhong());
                ps.setString(2, gp.getMaLoaiPhong());

                // giá cũ
                ps.setDouble(3, gp.getGiaNgayCu());
                ps.setDouble(4, gp.getGiaGioCu());

                // giá mới
                ps.setDouble(5, gp.getGioNgayMoi());
                ps.setDouble(6, gp.getGioGioMoi());

                // mã phiên đăng nhập (có thể null)
                if (gp.getMaPhienDangNhap() == null) {
                    ps.setNull(7, Types.VARCHAR);
                } else {
                    ps.setString(7, gp.getMaPhienDangNhap());
                }

                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Thêm giá phòng lỗi: " + e.getMessage(), e);
        }
    }

}

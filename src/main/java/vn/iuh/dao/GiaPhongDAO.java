package vn.iuh.dao;

import vn.iuh.entity.GiaPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class GiaPhongDAO {
    private final Connection connection;

    public GiaPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public GiaPhongDAO(Connection connection) {
        this.connection = connection;
    }

    //Thêm giá cho 1 loại phòng mới
    public boolean insertGiaPhong(GiaPhong gp) {
        if (gp.getMaLoaiPhong() == null) return false;
        String sql = "INSERT INTO GiaPhong (ma_gia_phong, ma_loai_phong, gia_ngay_cu, gia_gio_cu, gia_ngay_moi, gia_gio_moi, ma_phien_dang_nhap, thoi_gian_tao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, gp.getMaGiaPhong());
            ps.setString(2, gp.getMaLoaiPhong());
            ps.setObject(3, gp.getGiaNgayCu(), Types.REAL); // nếu null -> setObject cho phép
            ps.setObject(4, gp.getGiaGioCu(), Types.REAL);
            ps.setDouble(5, gp.getGiaNgayMoi());
            ps.setDouble(6, gp.getGiaGioMoi());
            ps.setString(7, gp.getMaPhienDangNhap());
            ps.setTimestamp(8, gp.getThoiGianTao());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Thêm giá phòng lỗi: " + e.getMessage(), e);
        }
    }

    //Tìm giá phòng mới nhất để sinh ID
    public GiaPhong timGiaPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM GiaPhong ORDER BY ma_gia_phong DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToRoomPrice(rs);
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
            // getDouble trả về 0 nếu null; nếu bạn muốn null-handling thì dùng getObject và check.
            Double giaNgayCu = rs.getObject("gia_ngay_cu") != null ? rs.getDouble("gia_ngay_cu") : null;
            Double giaGioCu = rs.getObject("gia_gio_cu") != null ? rs.getDouble("gia_gio_cu") : null;
            gp.setGiaNgayCu(giaNgayCu == null ? 0.0 : giaNgayCu);
            gp.setGiaGioCu(giaGioCu == null ? 0.0 : giaGioCu);
            gp.setGiaNgayMoi(rs.getDouble("gia_ngay_moi"));
            gp.setGiaGioMoi(rs.getDouble("gia_gio_moi"));
            gp.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            gp.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return gp;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành GiaPhong: " + e.getMessage());
        }
    }

    //Xóa giá phòng khi loại phòng bị xóa
    public int softDeleteByLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null) return 0;
        String sql = "UPDATE GiaPhong SET da_xoa = 1 WHERE ma_loai_phong = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maLoaiPhong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi soft-delete GiaPhong: " + e.getMessage(), e);
        }
    }
}

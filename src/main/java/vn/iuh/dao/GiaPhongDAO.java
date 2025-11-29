package vn.iuh.dao;

import vn.iuh.entity.GiaPhong;
import vn.iuh.entity.Phong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

/**
 * DAO đơn giản để thêm giá cho loại phòng vào bảng GiaPhong.
 * Giả sử bảng có các cột: ma_loai_phong, gia_ngay_moi, gia_gio_moi, thoi_gian_tao, da_xoa (da_xoa mặc định 0).
 */
public class GiaPhongDAO {
    private final Connection connection;

    public GiaPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public GiaPhongDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Insert giá mới cho loại phòng.
     * Nếu connection đang trong transaction (autoCommit = false) thì caller sẽ commit/rollback.
     */
    public boolean insertGiaPhong(GiaPhong gp) {
        if (gp.getMaLoaiPhong() == null) return false;
        String sql = "INSERT INTO GiaPhong (ma_gia_phong, ma_loai_phong, gia_ngay_cu, gia_gio_cu, gia_ngay_moi, gia_gio_moi)" +
                           " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
}

package vn.iuh.dao;

import vn.iuh.dto.response.BookingResponseV2;
import vn.iuh.entity.KhachHang;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {
    public KhachHang timKhachHang(String id) {
        String query = "SELECT * FROM KhachHang WHERE ma_khach_hang = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhKhachHang(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang timKhachHangBangCCCD(String cccd) {
        String query = "SELECT * FROM KhachHang WHERE CCCD = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, cccd);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhKhachHang(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang timKhachHangMoiNhat() {
        String query = "SELECT TOP 1 * FROM KhachHang WHERE da_xoa = 0 ORDER BY ma_khach_hang DESC";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhKhachHang(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang themKhachHang(KhachHang khachHang) {
        String query = "INSERT INTO KhachHang (ma_khach_hang, CCCD, ten_khach_hang, so_dien_thoai) " +
                "VALUES (?, ?, ?, ?)";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, khachHang.getMaKhachHang());
            ps.setString(2, khachHang.getCCCD());
            ps.setString(3, khachHang.getTenKhachHang());
            ps.setString(4, khachHang.getSoDienThoai());


            ps.executeUpdate();
            return khachHang;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public KhachHang capNhatKhachHang(KhachHang khachHang) {
        String query = "UPDATE KhachHang SET CCCD = ? ,ten_khach_hang = ?, so_dien_thoai = ?" +
                "WHERE ma_khach_hang = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, khachHang.getCCCD());
            ps.setString(2, khachHang.getTenKhachHang());
            ps.setString(3, khachHang.getSoDienThoai());
            ps.setString(4, khachHang.getMaKhachHang());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timKhachHang(khachHang.getMaKhachHang());
            } else {
                System.out.println("Không tìm thấy khách hàng có mã: " + khachHang.getMaKhachHang());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaKhachHang(String id) {
        if (timKhachHang(id) == null) {
            System.out.println("Không tìm thấy khách hàng có mã: " + id);
            return false;
        }

        String query = "UPDATE KhachHang SET da_xoa = 1 WHERE ma_khach_hang = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa khách hàng thành công!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private KhachHang chuyenKetQuaThanhKhachHang(ResultSet rs) {
        KhachHang khachHang = new KhachHang();
        try {
            khachHang.setMaKhachHang(rs.getString("ma_khach_hang"));
            khachHang.setCCCD(rs.getString("CCCD"));
            khachHang.setTenKhachHang(rs.getString("ten_khach_hang"));
            khachHang.setSoDienThoai(rs.getString("so_dien_thoai"));
            return khachHang;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành KhachHang: " + e.getMessage());
        }
    }

    public List<KhachHang> layTatCaKhachHang() {
        String sql = "SELECT ma_khach_hang, CCCD, ten_khach_hang, so_dien_thoai FROM KhachHang WHERE ISNULL(da_xoa,0) = 0 ORDER BY ma_khach_hang ASC";
        List<KhachHang> ds = new ArrayList<>();
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhachHang kh = chuyenKetQuaThanhKhachHang(rs);
                ds.add(kh);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách khách hàng: " + e.getMessage(), e);
        }
        return ds;
    }

    public List<BookingResponseV2> layDonDatPhongTheoMaKhachHang(String maKhachHang) {
        List<BookingResponseV2> out = new ArrayList<>();
        String sql = """
        SELECT ddp.ma_don_dat_phong,
               ddp.thoi_gian_tao,
               MIN(ctdp.tg_nhan_phong) AS tg_nhan_phong,
               MAX(ctdp.tg_tra_phong) AS tg_tra_phong,
               ddp.loai,
               ddp.tien_dat_coc
        FROM DonDatPhong ddp
        LEFT JOIN ChiTietDatPhong ctdp
          ON ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong AND ISNULL(ctdp.da_xoa,0)=0
        WHERE ddp.ma_khach_hang = ? AND ISNULL(ddp.da_xoa,0)=0
        GROUP BY ddp.ma_don_dat_phong, ddp.thoi_gian_tao, ddp.loai, ddp.tien_dat_coc
        ORDER BY ddp.thoi_gian_tao DESC
        """;
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("ma_don_dat_phong");
                    Timestamp thoiGian = rs.getTimestamp("thoi_gian_tao");
                    Timestamp tgNhan = rs.getTimestamp("tg_nhan_phong");
                    Timestamp tgTra = rs.getTimestamp("tg_tra_phong");
                    String loai = rs.getString("loai");
                    double tien = rs.getDouble("tien_dat_coc");
                    Double tienObj = rs.wasNull() ? null : tien;

                    BookingResponseV2 b = new BookingResponseV2(ma, thoiGian, tgNhan, tgTra, loai, tienObj);
                    out.add(b);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy danh sách đơn đặt của khách " + maKhachHang + ": " + ex.getMessage(), ex);
        }
        return out;
    }

    public boolean existsByTenKhachHang(String tenKhachHang) {
        if (tenKhachHang == null) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE LOWER(ten_khach_hang) = LOWER(?) AND ISNULL(da_xoa,0) = 0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenKhachHang.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng tên khách hàng: " + ex.getMessage(), ex);
        }
    }

    // Lấy mã khách hàng mới nhất (raw) để sinh mã mới
    public String timMaKhachHangMoiNhatRaw() {
        String sql = "SELECT TOP 1 ma_khach_hang FROM KhachHang WHERE ma_khach_hang IS NOT NULL ORDER BY ma_khach_hang DESC";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("ma_khach_hang");
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lấy mã KhachHang mới nhất: " + ex.getMessage(), ex);
        }
        return null;
    }

    // Chèn khách hàng mới (trả về true nếu thành công)
// Lưu ý: thêm thoi_gian_tao, da_xoa = 0
    public boolean insertKhachHang(String maKhachHang, String cccd, String tenKhachHang, String soDienThoai) {
        String sql = "INSERT INTO KhachHang (ma_khach_hang, CCCD, ten_khach_hang, so_dien_thoai, thoi_gian_tao, da_xoa) " +
                "VALUES (?, ?, ?, ?, GETDATE(), 0)";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            if (cccd == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, cccd);
            ps.setString(3, tenKhachHang);
            if (soDienThoai == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, soDienThoai);
            int r = ps.executeUpdate();
            return r > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi chèn KhachHang mới: " + ex.getMessage(), ex);
        }
    }

    // Kiểm tra trùng tên (ngoại trừ 1 id nhất định)
    public boolean existsByTenKhachHangExceptId(String tenKhachHang, String excludeId) {
        if (tenKhachHang == null) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE LOWER(ten_khach_hang) = LOWER(?) AND ma_khach_hang <> ? AND ISNULL(da_xoa,0)=0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tenKhachHang.trim());
            ps.setString(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng tên khách hàng (except): " + ex.getMessage(), ex);
        }
    }

    // Kiểm tra trùng CCCD (ngoại trừ 1 id nhất định)
    public boolean existsByCCCDExceptId(String cccd, String excludeId) {
        if (cccd == null || cccd.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE CCCD = ? AND ma_khach_hang <> ? AND ISNULL(da_xoa,0)=0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cccd.trim());
            ps.setString(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng CCCD (except): " + ex.getMessage(), ex);
        }
    }

    // Kiểm tra trùng điện thoại (ngoại trừ 1 id nhất định)
    public boolean existsByPhoneExceptId(String phone, String excludeId) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM KhachHang WHERE so_dien_thoai = ? AND ma_khach_hang <> ? AND ISNULL(da_xoa,0)=0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            ps.setString(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra trùng số điện thoại (except): " + ex.getMessage(), ex);
        }
    }

    // Kiểm tra xem khách hàng có đơn đặt phòng hiện tại hoặc tương lai hay không
    public boolean hasCurrentOrFutureBookings(String maKhachHang) {
        if (maKhachHang == null || maKhachHang.isBlank()) return false;

        String sql = """
    SELECT TOP 1 1
    FROM DonDatPhong ddp
    WHERE ddp.ma_khach_hang = ?
      AND ISNULL(ddp.da_xoa,0) = 0
      AND (
           -- trường hợp: tg_tra_phong trên form vẫn sau hiện tại (đơn chưa kết thúc)
           ddp.tg_tra_phong >= GETDATE()
        OR
           -- hoặc có ít nhất 1 chi tiết đặt phòng (chi tiết) đang ở tương lai / đang active
           EXISTS (
             SELECT 1
             FROM ChiTietDatPhong c
             WHERE c.ma_don_dat_phong = ddp.ma_don_dat_phong
               AND ISNULL(c.da_xoa,0) = 0
               AND (
                    c.tg_nhan_phong > GETDATE()
                 OR (c.tg_nhan_phong <= GETDATE() AND (c.tg_tra_phong IS NULL OR c.tg_tra_phong >= GETDATE()))
               )
           )
      )
    """;

        Connection connection = DatabaseUtil.getConnect();
        // debug (tùy bạn bật/tắt)
        try {
            String dbName = null;
            try (Statement st = connection.createStatement();
                 ResultSet rdb = st.executeQuery("SELECT DB_NAME() AS db")) {
                if (rdb.next()) dbName = rdb.getString("db");
            } catch (Exception ignore) { /* không bắt buộc */ }

            System.out.println("[DEBUG] hasCurrentOrFutureBookings - DB=" + dbName + " maKh=" + maKhachHang);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, maKhachHang);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = rs.next();
                    System.out.println("[DEBUG] hasCurrentOrFutureBookings -> " + found);
                    return found;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi kiểm tra đơn đặt hiện/tương lai cho khách " + maKhachHang + ": " + ex.getMessage(), ex);
        }
    }

}
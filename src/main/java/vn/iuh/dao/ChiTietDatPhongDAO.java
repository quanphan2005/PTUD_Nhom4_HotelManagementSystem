package vn.iuh.dao;

import vn.iuh.constraint.ActionType;
import vn.iuh.dto.repository.ChangeRoomRecord;
import vn.iuh.dto.repository.ThongTinSuDungPhong;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.Phong;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChiTietDatPhongDAO {
    public ChiTietDatPhong timChiTietDatPhong(String maChiTietDatPhong) {
        String query = "SELECT * FROM ChiTietDatPhong WHERE ma_chi_tiet_dat_phong = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToChiTietDatPhong(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public int capNhatKetThucCTDP(List<String> chiTietDatPhongs, String kieuKetThuc) {
        if (chiTietDatPhongs == null || chiTietDatPhongs.isEmpty()) return 0;

        StringBuilder query = new StringBuilder("UPDATE ChiTietDatPhong SET kieu_ket_thuc = ? WHERE kieu_ket_thuc is null and ma_chi_tiet_dat_phong IN (");
        for (int i = 0; i < chiTietDatPhongs.size(); i++) {
            query.append("?");
            if (i < chiTietDatPhongs.size() - 1) query.append(",");
        }
        query.append(")");

        try {
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(query.toString());
            // Set kieu_ket_thuc parameter
            ps.setString(1, kieuKetThuc);
            // Set ma_chi_tiet_dat_phong parameters
            for (int i = 0; i < chiTietDatPhongs.size(); i++) {
                ps.setString(i + 2, chiTietDatPhongs.get(i));
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ChiTietDatPhong findLastestByRoom(String roomID) {
        String query = "SELECT TOP 1 * FROM ChiTietDatPhong WHERE ma_phong = ? and  tg_nhan_phong >= getdate() ORDER BY tg_nhan_phong asc";

        try {
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
    
    public List<ThongTinSuDungPhong> layThongTinSuDungPhong(String maDonDatPhong) {
        String query = "select ddp.ma_don_dat_phong , ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong, dv.thoi_gian_tao as gio_check_in, \n" +
                "p.ma_phong, p.ten_phong, ctdp.kieu_ket_thuc, ctdp.ghi_chu ,lp.ma_loai_phong  from DonDatPhong ddp\n" +
                "left join ChiTietDatPhong ctdp on ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong\n" +
                "left join LichSuDiVao dv on dv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong and la_lan_dau_tien = 1 \n" +
                "left join Phong p on p.ma_phong = ctdp.ma_phong\n" +
                "left join LoaiPhong lp on lp.ma_loai_phong = p.ma_loai_phong\n" +
                "where ddp.ma_don_dat_phong = ?\n" +
                "order by ctdp.ma_chi_tiet_dat_phong";

        List<ThongTinSuDungPhong> thongTinSuDungPhongList = new ArrayList<>();
        try {
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(query);
            ps.setString(1, maDonDatPhong);

            var rs = ps.executeQuery();
            while (rs.next()) {
                ThongTinSuDungPhong thongTin = new ThongTinSuDungPhong();
                thongTin.setMaPhong(rs.getString("ma_phong"));
                thongTin.setTgNhanPhong(rs.getTimestamp("tg_nhan_phong"));
                thongTin.setTgTraPhong(rs.getTimestamp("tg_tra_phong"));
                thongTin.setGioCheckIn(rs.getTimestamp("gio_check_in"));
                thongTin.setMaChiTietDatPhong(rs.getString("ma_chi_tiet_dat_phong"));
                thongTin.setMaDonDatPhong(rs.getString("ma_don_dat_phong"));
                thongTin.setKieuKetThuc(rs.getString("kieu_ket_thuc"));
                thongTin.setMaLoaiPhong(rs.getString("ma_loai_phong"));
                thongTin.setTenPhong(rs.getString("ten_phong"));
                thongTin.setGhiChu(rs.getString("ghi_chu"));
                thongTinSuDungPhongList.add(thongTin);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return thongTinSuDungPhongList;
    }

    public ThongTinSuDungPhong layThongTinSuDungPhongCuaCTDP(String maChiTietDatPhong) {
        String query = "select ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong, dv.thoi_gian_tao as gio_check_in, \n" +
                "p.ma_phong, p.ten_phong, ctdp.kieu_ket_thuc, ctdp.ghi_chu, lp.ma_loai_phong  from ChiTietDatPhong ctdp" +
                "left join LichSuDiVao dv on dv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong and la_lan_dau_tien = 1 \n" +
                "left join Phong p on p.ma_phong = ctdp.ma_phong\n" +
                "left join LoaiPhong lp on lp.ma_loai_phong = p.ma_loai_phong\n" +
                "where ctdp.ma_chi_tiet_dat_phong = ?";
        try {
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            var rs = ps.executeQuery();
            if (rs.next()) {
                ThongTinSuDungPhong thongTin = new ThongTinSuDungPhong();
                thongTin.setMaPhong(rs.getString("ma_phong"));
                thongTin.setTgNhanPhong(rs.getTimestamp("tg_nhan_phong"));
                thongTin.setTgTraPhong(rs.getTimestamp("tg_tra_phong"));
                thongTin.setGioCheckIn(rs.getTimestamp("gio_check_in"));
                thongTin.setMaChiTietDatPhong(rs.getString("ma_chi_tiet_dat_phong"));
                thongTin.setKieuKetThuc(rs.getString("kieu_ket_thuc"));
                thongTin.setMaLoaiPhong(rs.getString("ma_loai_phong"));
                thongTin.setTenPhong(rs.getString("ten_phong"));
                thongTin.setGhiChu(rs.getString("ghi_chu"));
                return thongTin;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String findFormIDByDetail(String maChiTietDatPhong){
        String sql = "select ma_don_dat_phong from ChiTietDatPhong where ma_chi_tiet_dat_phong = ?";
        try {
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(sql);
            ps.setString(1, maChiTietDatPhong);

            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ma_don_dat_phong");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public ChiTietDatPhong timChiTietDatPhongMoiNhat() {
        String sql = "SELECT TOP 1 * FROM ChiTietDatPhong ORDER BY ma_chi_tiet_dat_phong DESC";
        try {
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(sql);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToChiTietDatPhong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean insert(ChiTietDatPhong ct) {
        String sql = "INSERT INTO ChiTietDatPhong(" +
                "ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, kieu_ket_thuc, " +
                "ma_don_dat_phong, ma_phong, ma_phien_dang_nhap) " +
                "VALUES (?,?,?,?,?,?,?)";
        try{
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(sql);
            ps.setString(1, ct.getMaChiTietDatPhong());
            ps.setTimestamp(2, ct.getTgNhanPhong());
            ps.setTimestamp(3, ct.getTgTraPhong());
            ps.setString(4, ct.getKieuKetThuc());
            ps.setString(5, ct.getMaDonDatPhong());
            ps.setString(6, ct.getMaPhong());
            ps.setString(7, ct.getMaPhienDangNhap());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Cập nhập tg_nhan_phong cho ChiTietDatPhong
    public boolean capNhatTgNhanPhong(String maChiTiet, Timestamp tgNhanPhong, String maPhienDangNhap, Timestamp thoiGianCapNhat) {
        String sql = "UPDATE ChiTietDatPhong SET tg_nhan_phong = ?, ma_phien_dang_nhap = ?, thoi_gian_tao = ? WHERE ma_chi_tiet_dat_phong = ?";
        try{
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, tgNhanPhong);
            ps.setString(2, maPhienDangNhap);
            ps.setTimestamp(3, thoiGianCapNhat);
            ps.setString(4, maChiTiet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi cập nhật tg_nhan_phong cho ChiTietDatPhong " + maChiTiet + ": " + e.getMessage());
            return false;
        }
    }

    // Cập nhật ma_phong cho một ChiTietDatPhong (dùng cho đổi phòng trước checkin)
    public boolean capNhatMaPhongChoChiTiet(String maChiTiet, String maPhongMoi, Timestamp thoiGianCapNhat) {
        String sql = "UPDATE ChiTietDatPhong SET ma_phong = ?, thoi_gian_tao = ? WHERE ma_chi_tiet_dat_phong = ? AND ISNULL(da_xoa,0)=0";
        try{
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maPhongMoi);
            ps.setTimestamp(2, thoiGianCapNhat);
            ps.setString(3, maChiTiet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Kết thúc (end) một ChiTietDatPhong: gán tg_tra_phong và kieu_ket_thuc
    public boolean ketThucChiTietDatPhong(String maChiTiet, Timestamp tgTraPhongMoi, String kieuKetThuc) {
        String sql = "UPDATE ChiTietDatPhong SET tg_tra_phong = ?, kieu_ket_thuc = ? WHERE ma_chi_tiet_dat_phong = ? AND ISNULL(da_xoa,0)=0";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, tgTraPhongMoi);
            ps.setString(2, kieuKetThuc);
            ps.setString(3, maChiTiet);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Tìm chi tiết tiếp theo so với chi tiết hiện tại để tính thời gian tối đa có thể book thêm giờ
    public ChiTietDatPhong timChiTietDatPhongTiepTheoCuaPhong(String maPhong, Timestamp sauThoiGian) {
        if (maPhong == null || sauThoiGian == null) return null;
        String sql = "SELECT TOP 1 * FROM ChiTietDatPhong " +
                "WHERE ma_phong = ? AND tg_nhan_phong > ? AND ISNULL(da_xoa,0)=0 " +
                "ORDER BY tg_nhan_phong ASC";
        try{
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maPhong);
            ps.setTimestamp(2, sauThoiGian);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChiTietDatPhong(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void capNhatCTDPTheoMaDonDatPhong(String maDonDatPhong, String kieuKetThuc){
        String sql = "UPDATE ChiTietDatPhong SET kieu_ket_thuc = ? WHERE ma_don_dat_phong = ? and kieu_ket_thuc is null";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, kieuKetThuc);
            ps.setString(2, maDonDatPhong);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Cập nhật thời gian trả phòng của đơn đặt phòng (dùng cho book thêm giờ)
    public boolean updateDonTraPhong(String maDonDatPhong, Timestamp newTraPhong) {
        if (maDonDatPhong == null || newTraPhong == null) return false;
        String sql = "UPDATE DonDatPhong SET tg_tra_phong = ? WHERE ma_don_dat_phong = ? AND ISNULL(da_xoa,0)=0";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, newTraPhong);
            ps.setString(2, maDonDatPhong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Tìm lọai của đơn đặt phòng
    public String getLoaiDonDatPhong(String maDonDatPhong) {
        if (maDonDatPhong == null) return null;
        String sql = "SELECT loai FROM DonDatPhong WHERE ma_don_dat_phong = ? AND ISNULL(da_xoa,0)=0";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maDonDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("loai");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Cập nhật tg_tra_phong cho tất cả ChiTietDatPhong của đơn
    public int updateAllChiTietTraPhongIfNotEnded(String maDonDatPhong, Timestamp newTraPhong) {
        if (maDonDatPhong == null || newTraPhong == null) return 0;
        String sql = "UPDATE ChiTietDatPhong SET tg_tra_phong = ? " +
                "WHERE ma_don_dat_phong = ? AND ISNULL(kieu_ket_thuc, '') = '' AND ISNULL(da_xoa,0)=0";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, newTraPhong);
            ps.setString(2, maDonDatPhong);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasFutureBookingsForLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) return false;
        String sql = "SELECT TOP 1 ctdp.ma_chi_tiet_dat_phong " +
                "FROM ChiTietDatPhong ctdp " +
                "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                "WHERE p.ma_loai_phong = ? AND ISNULL(ctdp.da_xoa,0) = 0 AND ctdp.tg_nhan_phong > GETDATE()";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasCurrentOrFutureBookingsForLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) return false;
        String sql =
                "SELECT TOP 1 ctdp.ma_chi_tiet_dat_phong " +
                        "FROM ChiTietDatPhong ctdp " +
                        "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                        "WHERE p.ma_loai_phong = ? AND ISNULL(ctdp.da_xoa,0)=0 " +
                        "  AND ( " +
                        "       ctdp.tg_nhan_phong > GETDATE() " +            // future bookings
                        "    OR (ctdp.tg_nhan_phong <= GETDATE() AND (ctdp.tg_tra_phong IS NULL OR ctdp.tg_tra_phong >= GETDATE())) " + // current use
                        "  )";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean capNhatTgNhanPhongChoDonDatPhong(String maDonDatPhong, Timestamp tgNhanPhong, String maPhienDangNhap, Timestamp thoiGianCapNhat) {
        if (maDonDatPhong == null) return false;
        String sql = "UPDATE DonDatPhong SET tg_nhan_phong = ?, ma_phien_dang_nhap = ?, thoi_gian_tao = ? " +
                "WHERE ma_don_dat_phong = ? AND ISNULL(da_xoa, 0) = 0";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, tgNhanPhong);
            ps.setString(2, maPhienDangNhap);
            ps.setTimestamp(3, thoiGianCapNhat);
            ps.setString(4, maDonDatPhong);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ChiTietDatPhong> findNotEndedByBookingId(String bookingId) {
        String query = "SELECT * FROM ChiTietDatPhong " +
                "WHERE ma_don_dat_phong = ? AND ISNULL(kieu_ket_thuc, '') = '' AND ISNULL(da_xoa,0) = 0 " +
                "ORDER BY tg_nhan_phong ASC";
        List<ChiTietDatPhong> result = new ArrayList<>();
        try{
            Connection connection = DatabaseUtil.getConnect();
            var ps = connection.prepareStatement(query);
            ps.setString(1, bookingId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToChiTietDatPhong(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public int markBookingsAsDeletedByCustomer(String maKhachHang) {
        if (maKhachHang == null) return 0;
        String sql = "UPDATE DonDatPhong SET da_xoa = 1 WHERE ma_khach_hang = ? AND ISNULL(da_xoa,0)=0";
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi xóa DonDatPhong cho " + maKhachHang + ": " + ex.getMessage(), ex);
        }
    }

    public int markBookingDetailsAsDeletedByCustomer(String maKhachHang) {
        if (maKhachHang == null) return 0;
        // cập nhật ChiTietDatPhong dựa trên DonDatPhong của khách
        String sql = """
            UPDATE ChiTietDatPhong
            SET da_xoa = 1
            WHERE ma_don_dat_phong IN (
                SELECT ma_don_dat_phong FROM DonDatPhong WHERE ma_khach_hang = ? AND ISNULL(da_xoa,0)=0
            )
        """;
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi xóa ChiTietDatPhong cho khách " + maKhachHang + ": " + ex.getMessage(), ex);
        }
    }

    // Lấy lịch sử đổi phòng thoe đơn đặt phòng
    public List<ChangeRoomRecord> layLichSuDoiPhongTheoDon(String maDonDatPhong) {
        List<ChangeRoomRecord> results = new ArrayList<>();
        PhongDAO phongDAO = new PhongDAO(); // để resolve tên -> ma_phong nếu cần
        try (Connection conn = DatabaseUtil.getConnect()) {
            // 1) Lấy từ LichSuThaoTac
            String sql = "SELECT ma_lich_su_thao_tac, ten_thao_tac, mo_ta, thoi_gian_tao FROM LichSuThaoTac " +
                    "WHERE ten_thao_tac IN (?, ?) AND mo_ta LIKE ? ORDER BY thoi_gian_tao ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ActionType.CHANGE_ROOM_BEFORE_CHECKIN.getActionName());
                ps.setString(2, ActionType.CHANGE_ROOM_AFTER_CHECKIN.getActionName());
                ps.setString(3, "Đổi phòng cho đơn " + maDonDatPhong + ":%");
                try (ResultSet rs = ps.executeQuery()) {
                    // regex mới: bắt 2 phần có thể chứa khoảng trắng (lazy)
                    Pattern p = Pattern.compile("Đổi phòng cho đơn\\s+" + Pattern.quote(maDonDatPhong) + "\\s*:\\s*(.+?)\\s*->\\s*(.+?)($|\\s)", Pattern.CASE_INSENSITIVE);
                    while (rs.next()) {
                        String moTa = rs.getString("mo_ta");
                        Timestamp t = rs.getTimestamp("thoi_gian_tao");
                        String tenThaoTac = rs.getString("ten_thao_tac");
                        if (moTa == null) continue;
                        Matcher m = p.matcher(moTa.trim());
                        if (m.find()) {
                            String rawOld = m.group(1).trim();
                            String rawNew = m.group(2).trim();

                            // Try to resolve to ma_phong if raw is a display name
                            String resolvedOld = resolveToRoomId(phongDAO, rawOld);
                            String resolvedNew = resolveToRoomId(phongDAO, rawNew);

                            // If resolution failed, keep raw (so we still show something)
                            String oldRoom = resolvedOld != null ? resolvedOld : rawOld;
                            String newRoom = resolvedNew != null ? resolvedNew : rawNew;

                            results.add(new ChangeRoomRecord(null, null, oldRoom, newRoom, t, tenThaoTac));
                        }
                    }
                }
            }

            // 2) Fallback: dò cặp ChiTietDatPhong (sử dụng phương thức findByBookingId đã có trong DAO)
            List<ChiTietDatPhong> details = this.findByBookingId(maDonDatPhong);
            if (details != null && details.size() > 1) {
                Collections.sort(details, Comparator.comparing(ChiTietDatPhong::getTgNhanPhong, Comparator.nullsFirst(Comparator.naturalOrder())));
                for (int i = 0; i < details.size() - 1; ++i) {
                    ChiTietDatPhong prev = details.get(i);
                    ChiTietDatPhong next = details.get(i + 1);
                    if (prev.getTgTraPhong() == null || next.getTgNhanPhong() == null) continue;
                    long diff = Math.abs(prev.getTgTraPhong().getTime() - next.getTgNhanPhong().getTime());
                    // nếu tg_tra == tg_nhan (hoặc lệch <= 2s) và phòng khác -> coi là đổi phòng
                    if (diff <= 2000L && prev.getMaPhong() != null && next.getMaPhong() != null
                            && !prev.getMaPhong().equalsIgnoreCase(next.getMaPhong())) {

                        // tránh trùng với record từ LichSuThaoTac
                        boolean exists = false;
                        for (ChangeRoomRecord r : results) {
                            if (r.getOldRoom() != null && r.getNewRoom() != null &&
                                    r.getOldRoom().equalsIgnoreCase(prev.getMaPhong()) &&
                                    r.getNewRoom().equalsIgnoreCase(next.getMaPhong()) &&
                                    Math.abs(r.getTime().getTime() - prev.getTgTraPhong().getTime()) <= 2000L) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            results.add(new ChangeRoomRecord(prev.getMaChiTietDatPhong(), next.getMaChiTietDatPhong(),
                                    prev.getMaPhong(), next.getMaPhong(),
                                    prev.getTgTraPhong(), ActionType.CHANGE_ROOM_AFTER_CHECKIN.getActionName()));
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            // log nếu cần
        }

        // sắp xếp theo thời gian (tăng dần)
        results.sort(Comparator.comparing(ChangeRoomRecord::getTime, Comparator.nullsFirst(Comparator.naturalOrder())));
        return results;
    }

    private String resolveToRoomId(PhongDAO phongDAO, String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            // nếu raw chính xác là mã phòng
            Phong p = phongDAO.timPhong(raw);
            if (p != null && p.getMaPhong() != null) return p.getMaPhong();

            // thử tìm theo tên (có thể raw = "Phòng T007" hoặc "T007")
            Phong byName = phongDAO.timPhongTheoTen(raw);
            if (byName != null && byName.getMaPhong() != null) return byName.getMaPhong();

            // try trimming common prefix "Phòng " (VN)
            String trimmed = raw.replaceAll("(?i)ph[oòóỏõọ]ng\\s+", "").trim();
            if (!trimmed.equals(raw)) {
                Phong p2 = phongDAO.timPhongTheoTen(trimmed);
                if (p2 != null && p2.getMaPhong() != null) return p2.getMaPhong();
            }
        } catch (Throwable ignore) { }
        return null;
    }

    // Hàm tìm chi tiết đặt phòng dùng riêng cho chức năng đổi phòng
    public List<ChiTietDatPhong> findByBookingIdV2(String bookingId) {
        String query = "SELECT * FROM ChiTietDatPhong " +
                "WHERE ma_don_dat_phong = ? AND ISNULL(kieu_ket_thuc, '') = '' AND ISNULL(da_xoa,0) = 0 " +
                "ORDER BY tg_nhan_phong ASC";
        List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();
        Connection connection = DatabaseUtil.getConnect();
        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chiTietDatPhongs.add(mapResultSetToChiTietDatPhong(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return chiTietDatPhongs;
    }

}

package vn.iuh.dao;

import vn.iuh.dto.repository.ThongTinSuDungPhong;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}

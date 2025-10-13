package vn.iuh.dao;

import vn.iuh.constraint.RoomEndType;
import vn.iuh.dto.repository.ThongTinSuDungPhong;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.DonDatPhong;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietDatPhongDAO {
    private final Connection connection;

    public ChiTietDatPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public ChiTietDatPhongDAO(Connection connection) {
        this.connection = connection;
    }

    public int capNhatKetThucCTDP(List<String> chiTietDatPhongs) {
        if (chiTietDatPhongs == null || chiTietDatPhongs.isEmpty()) return 0;

        StringBuilder query = new StringBuilder("UPDATE ChiTietDatPhong SET kieu_ket_thuc = ? WHERE ma_chi_tiet_dat_phong IN (");
        for (int i = 0; i < chiTietDatPhongs.size(); i++) {
            query.append("?");
            if (i < chiTietDatPhongs.size() - 1) query.append(",");
        }
        query.append(")");

        try (var ps = connection.prepareStatement(query.toString())) {
            // Set kieu_ket_thuc parameter
            ps.setString(1, RoomEndType.TRA_PHONG.getStatus());
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

    public List<ThongTinSuDungPhong> layThongTinSuDungPhong(String maDonDatPhong) {
        String query = "select ddp.ma_don_dat_phong , ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong, dv.thoi_gian_tao as gio_check_in, \n" +
                "p.ma_phong, ctdp.kieu_ket_thuc, lp.ma_loai_phong  from DonDatPhong ddp\n" +
                "left join ChiTietDatPhong ctdp on ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong\n" +
                "left join LichSuDiVao dv on dv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong\n" +
                "left join Phong p on p.ma_phong = ctdp.ma_phong\n" +
                "left join LoaiPhong lp on lp.ma_loai_phong = p.ma_loai_phong\n" +
                "where dv.la_lan_dau_tien is not null and ddp.ma_don_dat_phong = ?";

        List<ThongTinSuDungPhong> thongTinSuDungPhongList = new ArrayList<>();
        try {
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
                thongTinSuDungPhongList.add(thongTin);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return thongTinSuDungPhongList;
    }

    public String findFormIDByDetail(String maChiTietDatPhong){
        String sql = "select ma_don_dat_phong from ChiTietDatPhong where ma_chi_tiet_dat_phong = ?";
        try {
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
        String sql = "SELECT TOP 1 * FROM ChiTietDatPhong WHERE da_xoa = 0 ORDER BY ma_chi_tiet_dat_phong DESC";
        try (var ps = connection.prepareStatement(sql)) {
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
        try (var ps = connection.prepareStatement(sql)) {
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

}

package vn.iuh.dao;

import vn.iuh.dto.repository.ThongTinDatPhong;
import vn.iuh.dto.repository.ThongTinPhong;
import vn.iuh.entity.LichSuDiVao;
import vn.iuh.entity.DonDatPhong;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatPhongDAO {
    private final Connection connection;

    public DatPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public void khoiTaoGiaoTac() {
        DatabaseUtil.enableTransaction(connection);
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

    public boolean themDonDatPhong(DonDatPhong donDatPhongEntity) {
        String query = "INSERT INTO DonDatPhong" +
                       " (ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc" +
                       ", da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, donDatPhongEntity.getMaDonDatPhong());
            ps.setString(2, donDatPhongEntity.getMoTa());
            ps.setTimestamp(3, donDatPhongEntity.getTgNhanPhong());
            ps.setTimestamp(4, donDatPhongEntity.getTgTraPhong());
            ps.setDouble(5, donDatPhongEntity.getTongTienDuTinh());
            ps.setDouble(6, donDatPhongEntity.getTienDatCoc());
            ps.setBoolean(7, donDatPhongEntity.getDaDatTruoc());
            ps.setString(8, donDatPhongEntity.getMaKhachHang());
            ps.setString(9, donDatPhongEntity.getMaPhienDangNhap());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi thêm đơn đặt phòng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean themChiTietDatPhong(DonDatPhong donDatPhongEntity,
                                       List<ChiTietDatPhong> chiTietDatPhongs) {
        String query = "INSERT INTO ChiTietDatPhong" +
                       " (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, kieu_ket_thuc, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (ChiTietDatPhong chiTietDatPhong : chiTietDatPhongs) {
                ps.setString(1, chiTietDatPhong.getMaChiTietDatPhong());
                ps.setTimestamp(2, chiTietDatPhong.getTgNhanPhong());
                ps.setTimestamp(3, chiTietDatPhong.getTgTraPhong());
                ps.setString(4, chiTietDatPhong.getKieuKetThuc());
                ps.setString(5, donDatPhongEntity.getMaDonDatPhong());
                ps.setString(6, chiTietDatPhong.getMaPhong());
                ps.setString(7, donDatPhongEntity.getMaPhienDangNhap());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == chiTietDatPhongs.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean themLichSuDiVao(List<LichSuDiVao> lichSuDiVaos) {
        String query = "INSERT INTO LichSuDiVao" +
                       " (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong)" +
                       " VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (LichSuDiVao historyCheckIn : lichSuDiVaos) {
                ps.setString(1, historyCheckIn.getMaLichSuDiVao());
                ps.setBoolean(2, historyCheckIn.getLaLanDauTien());
                ps.setString(3, historyCheckIn.getMaChiTietDatPhong());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == lichSuDiVaos.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ThongTinPhong> timTatCaThongTinPhong() {
        String query = "SELECT p.ma_phong, p.ten_phong, p.dang_hoat_dong, cv.ten_trang_thai, " +
                        "lp.phan_loai, lp.so_luong_khach, gp.gia_ngay_moi, gp.gia_gio_moi " +
                        "FROM Phong p " +
                        "LEFT JOIN LoaiPhong lp ON p.ma_loai_phong = lp.ma_loai_phong " +
                        "OUTER APPLY ( " +
                        "    SELECT TOP 1 gp.gia_ngay_moi, gp.gia_gio_moi " +
                        "    FROM GiaPhong gp " +
                        "    WHERE gp.ma_loai_phong = lp.ma_loai_phong " +
                        "    ORDER BY gp.thoi_gian_tao DESC " +
                        ") AS gp " +
                        "OUTER APPLY ( " +
                        "    SELECT TOP 1 cv.ten_trang_thai " +
                        "    FROM CongViec cv " +
                        "    WHERE cv.ma_phong = p.ma_phong AND (GETDATE() BETWEEN cv.tg_bat_dau AND cv.tg_ket_thuc) and da_xoa = 0" +
                        ") AS cv";
        List<ThongTinPhong> thongTinPhongs = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                thongTinPhongs.add(chuyenKetQuaThanhThongTinPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return thongTinPhongs;
    }


    public List<ThongTinDatPhong> timTatCaThongTinDatPhong(List<String> phongKhongKhaDungs) {
        if (phongKhongKhaDungs.isEmpty())
            return new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT p.ma_phong, kh.ten_khach_hang, ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " WHERE p.ma_phong IN (");

        for (int i = 0; i < phongKhongKhaDungs.size(); i++) {
            query.append("?");
            if (i < phongKhongKhaDungs.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        List<ThongTinDatPhong> thongTinDatPhongs = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            for (int i = 0; i < phongKhongKhaDungs.size(); i++) {
                ps.setString(i + 1, phongKhongKhaDungs.get(i));
            }

            var rs = ps.executeQuery();

            while (rs.next())
                thongTinDatPhongs.add(chuyenKetQuaThanhThongTinDatPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return thongTinDatPhongs;
    }



    public List<ThongTinDatPhong> timThongTinDatPhongTrongKhoang(Timestamp tgNhanPhong, Timestamp tgTraPhong, List<String> danhSachMaPhong) {
        if (danhSachMaPhong.isEmpty())
            return new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT p.ma_phong, kh.ten_khach_hang, ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " WHERE p.ma_phong IN (");

        for (int i = 0; i < danhSachMaPhong.size(); i++) {
            query.append("?");
            if (i < danhSachMaPhong.size() - 1) {
                query.append(", ");
            }
        }
        query.append(") AND NOT (ctdp.tg_tra_phong <= ? OR ctdp.tg_nhan_phong >= ?)");

        List<ThongTinDatPhong> thongTinDatPhongs = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            int index = 1;
            for (String maPhong : danhSachMaPhong) {
                ps.setString(index++, maPhong);
            }

            Timestamp tgNhanPhongDuKien = new Timestamp(tgNhanPhong.getTime() - 3 * 60 * 60 * 1000);
            Timestamp tgTraPhongDuKien = new Timestamp(tgTraPhong.getTime() + 3 * 60 * 60 * 1000);

            ps.setTimestamp(index++, tgNhanPhongDuKien);
            ps.setTimestamp(index, tgTraPhongDuKien);

            var rs = ps.executeQuery();

            while (rs.next())
                thongTinDatPhongs.add(chuyenKetQuaThanhThongTinDatPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return thongTinDatPhongs;
    }

    public DonDatPhong timDonDatPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM DonDatPhong WHERE da_xoa = 0 ORDER BY ma_don_dat_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhDonDatPhong(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public ChiTietDatPhong timChiTietDatPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM ChiTietDatPhong ORDER BY ma_chi_tiet_dat_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhChiTietDatPhong(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    private ThongTinPhong chuyenKetQuaThanhThongTinPhong(ResultSet rs) {
        try {
            return new ThongTinPhong(
                    rs.getString("ma_phong"),
                    rs.getString("ten_phong"),
                    rs.getBoolean("dang_hoat_dong"),
                    rs.getString("ten_trang_thai"),
                    rs.getString("phan_loai"),
                    rs.getString("so_luong_khach"),
                    rs.getDouble("gia_ngay_moi"),
                    rs.getDouble("gia_gio_moi")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành ThongTinPhong" + e.getMessage());
        }
    }

    private ThongTinDatPhong chuyenKetQuaThanhThongTinDatPhong(ResultSet rs) throws SQLException {
        try {
            return new ThongTinDatPhong(
                    rs.getString("ma_phong"),
                    rs.getString("ten_khach_hang"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành ThongTinDatPhong" + e.getMessage());
        }
    }

    private DonDatPhong chuyenKetQuaThanhDonDatPhong(ResultSet rs) {
        try {
            return new DonDatPhong(
                    rs.getString("ma_don_dat_phong"),
                    rs.getString("mo_ta"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong"),
                    rs.getDouble("tong_tien_du_tinh"),
                    rs.getDouble("tien_dat_coc"),
                    rs.getBoolean("da_dat_truoc"),
                    rs.getString("ma_khach_hang"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getString("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành DonDatPhong" + e.getMessage());
        }
    }

    private ChiTietDatPhong chuyenKetQuaThanhChiTietDatPhong(ResultSet rs) {
        try {
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
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành ChiTietDatPhong: " + e.getMessage());
        }
    }

    public ThongTinDatPhong timDonDatPhongChoCheckInCuaPhong(String maPhong, Timestamp tgBatDau, Timestamp tgKetThuc) {
        String query = "SELECT p.ma_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong, kh.ten_khach_hang, ctdp.ma_chi_tiet_dat_phong " +
                        "FROM Phong p " +
                        "LEFT JOIN ChiTietDatPhong ctdp ON ctdp.ma_phong = p.ma_phong " +
                        "LEFT JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong " +
                        "LEFT JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang " +
                        "WHERE  p.ma_phong = ? and ctdp.tg_nhan_phong BETWEEN dateadd(second, -10 , ?) AND ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhong);
            ps.setTimestamp(2, tgBatDau);
            ps.setTimestamp(3, tgKetThuc);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhThongTinDatPhong(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public DonDatPhong getDonDatPhongById(String maDonDatPhong) {
        String query = "SELECT * FROM DonDatPhong WHERE ma_don_dat_phong = ? AND da_xoa = 0";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDonDatPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhDonDatPhong(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }
}
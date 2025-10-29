package vn.iuh.dao;

import vn.iuh.constraint.InvoiceType;
import vn.iuh.constraint.RoomEndType;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.constraint.WorkTimeCost;
import vn.iuh.dto.repository.*;
import vn.iuh.dto.response.ReservationResponse;
import vn.iuh.dto.response.RoomUsageServiceResponse;
import vn.iuh.entity.KhachHang;
import vn.iuh.entity.LichSuDiVao;
import vn.iuh.entity.DonDatPhong;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatPhongDAO {
    private final Connection connection;

    public DatPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public Connection getConnection() {
        return this.connection;
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
                       ", da_dat_truoc, loai, ma_khach_hang, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, donDatPhongEntity.getMaDonDatPhong());
            ps.setString(2, donDatPhongEntity.getMoTa());
            ps.setTimestamp(3, donDatPhongEntity.getTgNhanPhong());
            ps.setTimestamp(4, donDatPhongEntity.getTgTraPhong());
            ps.setDouble(5, donDatPhongEntity.getTongTienDuTinh());
            ps.setDouble(6, donDatPhongEntity.getTienDatCoc());
            ps.setBoolean(7, donDatPhongEntity.isDaDatTruoc());
            ps.setString(8, donDatPhongEntity.getLoai());
            ps.setString(9, donDatPhongEntity.getMaKhachHang());
            ps.setString(10, donDatPhongEntity.getMaPhienDangNhap());

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

    public DonDatPhong timDonDatPhong(String maDonDatPhong) {
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
                       "    WHERE cv.ma_phong = p.ma_phong AND (GETDATE() >= cv.tg_bat_dau) and da_xoa = 0" +
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


    public List<PhieuDatPhong> timTatCaPhieuDatPhong() {
        String query =
                "SELECT DISTINCT p.ma_phong, p.ten_phong, kh.ten_khach_hang, ddp.ma_don_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN LichSuDiVao lsdv ON lsdv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " JOIN CongViec cv ON cv.ma_phong = p.ma_phong " +
                " WHERE ctdp.tg_nhan_phong >= DATEADD(HOUR, ?, GETDATE())" + // Get all booking from now - X hours
                " AND cv.ten_trang_thai = ?" +
                " AND ctdp.da_xoa = 0" +
                " AND ddp.da_xoa = 0" +
                " AND cv.da_xoa = 0" +
                " ORDER BY ctdp.tg_nhan_phong";

        List<PhieuDatPhong> danhSachPhieuDatPhong = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, -WorkTimeCost.CHECKIN_LATE_MAX.getMinutes());
            ps.setString(2, RoomStatus.ROOM_BOOKED_STATUS.getStatus());

            var rs = ps.executeQuery();

            while (rs.next())
                danhSachPhieuDatPhong.add(chuyenKetQuaThanhPhieuDatPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachPhieuDatPhong;
    }

    public CustomerInfo timThongTinKhachHangBangMaDonDatPhong(String maDonDatPhong) {
        String query = "SELECT kh.ma_khach_hang, kh.CCCD, kh.ten_khach_hang, kh.so_dien_thoai" +
                       " FROM DonDatPhong ddp" +
                       " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                       " WHERE ddp.ma_don_dat_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDonDatPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return new CustomerInfo(
                        rs.getString("ma_khach_hang"),
                        rs.getString("CCCD"),
                        rs.getString("ten_khach_hang"),
                        rs.getString("so_dien_thoai")
                );
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }


    public CustomerInfo timThongTinKhachHangBangMaChiTietDatPhong(String maChiTietDatPhong) {
        String query = "SELECT kh.ma_khach_hang, kh.CCCD, kh.ten_khach_hang, kh.so_dien_thoai" +
                       " FROM ChiTietDatPhong ctdp" +
                       " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                       " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                       " WHERE ctdp.ma_chi_tiet_dat_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return new CustomerInfo(
                        rs.getString("ma_khach_hang"),
                        rs.getString("CCCD"),
                        rs.getString("ten_khach_hang"),
                        rs.getString("so_dien_thoai")
                );
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public List<PhieuDatPhong> timThongTinDatPhongBangMaPhong(String id) {
        String query =
                "SELECT DISTINCT p.ma_phong, p.ten_phong, kh.ten_khach_hang, ddp.ma_don_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " JOIN CongViec cv ON cv.ma_phong = p.ma_phong " +
                " WHERE ctdp.tg_nhan_phong > GETDATE()" +
                " AND cv.tg_bat_dau = ctdp.tg_nhan_phong" +
                " AND cv.ten_trang_thai = ?" +
                " AND ddp.da_xoa = 0" +
                " AND cv.da_xoa = 0" +
                " AND p.ma_phong = ?";

        List<PhieuDatPhong> danhSachPhieuDatPhong = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, RoomStatus.ROOM_BOOKED_STATUS.getStatus());
            ps.setString(2, id);

            var rs = ps.executeQuery();

            while (rs.next())
                danhSachPhieuDatPhong.add(chuyenKetQuaThanhPhieuDatPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachPhieuDatPhong;
    }

    public List<ThongTinDatPhong> timTatCaThongTinDatPhongTheoDanhSachMaPhong(List<String> phongKhongKhaDungs) {
        if (phongKhongKhaDungs.isEmpty())
            return new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT p.ma_phong, kh.ten_khach_hang, ddp.ma_don_dat_phong, ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " JOIN CongViec cv ON cv.ma_phong = p.ma_phong " +
                " AND ctdp.tg_nhan_phong <= GETDATE()" +
                " AND (GETDATE() <= DATEADD(MINUTE, ?, ctdp.tg_tra_phong) OR cv.ten_trang_thai = ?)" +
                " AND ddp.da_xoa = 0" +
                " WHERE p.ma_phong IN (");

        for (int i = 0; i < phongKhongKhaDungs.size(); i++) {
            query.append("?");
            if (i < phongKhongKhaDungs.size() - 1) {
                query.append(", ");
            }
        }
        query.append(") ORDER by ctdp.tg_nhan_phong");

        List<ThongTinDatPhong> thongTinDatPhongs = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            int i = 1;
            ps.setInt(i, WorkTimeCost.CHECKOUT_LATE_MIN.getMinutes());
            i++;
            ps.setString(i, RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
            i++;

            for (String maPhong : phongKhongKhaDungs) {
                ps.setString(i, maPhong);
                i++;
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

    public List<ThongTinPhong> timTatCaPhongTrongKhoangThoiGian(Timestamp timeIn, Timestamp timeOut) {
        String query =
                "SELECT p.ma_phong, p.ten_phong, p.dang_hoat_dong, cv.ten_trang_thai, lp.phan_loai, lp.so_luong_khach, gp.gia_ngay_moi, gp.gia_gio_moi" +
                " FROM Phong p" +
                " JOIN LoaiPhong lp ON p.ma_loai_phong = lp.ma_loai_phong" +
                " JOIN GiaPhong gp ON gp.ma_loai_phong = lp.ma_loai_phong" +
                " LEFT JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " LEFT JOIN CongViec cv ON cv.ma_phong = p.ma_phong AND (GETDATE() >= cv.tg_bat_dau) and cv.da_xoa = 0" +
                " WHERE ctdp.da_xoa = 0" +
                " AND ctdp.kieu_ket_thuc is null" +
                " AND NOT (ctdp.tg_nhan_phong <= ? AND ? <= ctdp.tg_tra_phong ) " +
                " AND NOT (ctdp.tg_nhan_phong <= ? AND ? <= ctdp.tg_tra_phong ) " +
                " ORDER BY p.ma_phong";

        List<ThongTinPhong> danhSachThongTinPhongTrong = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, timeIn);
            ps.setTimestamp(2, timeIn);
            ps.setTimestamp(3, timeOut);
            ps.setTimestamp(4, timeOut);

            var rs = ps.executeQuery();

            while (rs.next())
                danhSachThongTinPhongTrong.add(chuyenKetQuaThanhThongTinPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachThongTinPhongTrong;
    }

    /*
        1. Room that currently checkout late
        2. Room has booked but not yet check-in in range timeIn - timeOut
     */
    public List<String> timTatCaPhongKhongKhaDungTrongKhoang(Timestamp timeIn, Timestamp timeOut) {
        // Convert this query below to query (String)
//        select ma_phong from CongViec cv
//        WHERE cv.da_xoa = 0
//        AND (
//            (cv.ten_trang_thai = N'CHECKOUT TRỄ' AND DATEADD(HOUR, 6, cv.tg_ket_thuc) < GETDATE())
//            OR (
//                    cv.ten_trang_thai = N'CHỜ CHECKIN'
//            AND (cv.tg_bat_dau <= DATEADD(DAY, 3, GETDATE()) AND DATEADD(DAY, 3, GETDATE()) <= DATEADD(HOUR, 3, cv.tg_ket_thuc))
//            OR (cv.tg_bat_dau <= DATEADD(DAY, 4, GETDATE()) AND DATEADD(DAY, 4, GETDATE()) <= DATEADD(HOUR, 3, cv.tg_ket_thuc))
//            )
//        )
        String query =
                "select ma_phong from CongViec cv" +
                    " WHERE cv.da_xoa = 0" +
                        " AND (" +
                            " (cv.ten_trang_thai = ? AND DATEADD(MINUTE, ?, cv.tg_ket_thuc) >= ?)" +
                            " OR (" +
                                " (cv.tg_bat_dau <= ? AND ? <= DATEADD(MINUTE , ?, cv.tg_ket_thuc))" +
                                " OR (cv.tg_bat_dau <= ? AND ? <= DATEADD(MINUTE , ?, cv.tg_ket_thuc))" +
                            " )" +
                        " )";

        List<String> danhSachThongMaPhongKhongKhaDung = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus());
            ps.setInt(2, WorkTimeCost.CHECKOUT_LATE_MAX.getMinutes());
            ps.setTimestamp(3, timeIn);
            ps.setTimestamp(4, timeIn);
            ps.setTimestamp(5, timeIn);
            ps.setInt(6, WorkTimeCost.CLEANING_TIME.getMinutes()
                      + WorkTimeCost.CHECKOUT_LATE_MIN.getMinutes());
            ps.setTimestamp(7, timeOut);
            ps.setTimestamp(8, timeOut);
            ps.setInt(9, WorkTimeCost.CLEANING_TIME.getMinutes()
                       + WorkTimeCost.CHECKOUT_LATE_MIN.getMinutes());

            var rs = ps.executeQuery();

            while (rs.next())
                danhSachThongMaPhongKhongKhaDung.add(rs.getString("ma_phong"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachThongMaPhongKhongKhaDung;
    }

    public List<ThongTinDatPhong> timThongTinDatPhongTrongKhoang(Timestamp tgNhanPhong, Timestamp tgTraPhong,
                                                                 List<String> danhSachMaPhong) {
        if (danhSachMaPhong.isEmpty())
            return new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT p.ma_phong, kh.ten_khach_hang, ddp.ma_don_dat_phong, ctdp.ma_chi_tiet_dat_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " WHERE ctdp.da_xoa = 0" +
                " AND ctdp.kieu_ket_thuc is null" +
                " AND p.ma_phong IN (");

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

    public List<ChiTietDatPhong> timChiTietDatPhongBangMaDatPhong(String maDatPhong) {
        String query = "SELECT * FROM ChiTietDatPhong WHERE ma_don_dat_phong = ?";
        List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDatPhong);

            var rs = ps.executeQuery();
            while (rs.next())
                chiTietDatPhongs.add(chuyenKetQuaThanhChiTietDatPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return chiTietDatPhongs;
    }

    public ChiTietDatPhong timChiTietDatPhongBangMaDatPhong(String maDatPhong, String maPhong) {
        String query = "SELECT * FROM ChiTietDatPhong WHERE ma_don_dat_phong = ? AND ma_phong = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDatPhong);
            ps.setString(2, maPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhChiTietDatPhong(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    public void huyDonDatPhong(String maDatPhong) {
        String query = "UPDATE DonDatPhong SET da_xoa = 1 WHERE ma_don_dat_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDatPhong);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Lỗi xóa đơn đặt phòng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void huyDanhSachChiTietDatPhong(List<String> ids) {
        if (ids.isEmpty())
            return;

        StringBuilder query =
                new StringBuilder("UPDATE ChiTietDatPhong SET da_xoa = 1 WHERE ma_chi_tiet_dat_phong IN (");
        for (int i = 0; i < ids.size(); i++) {
            query.append("?");
            if (i < ids.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            for (int i = 0; i < ids.size(); i++) {
                ps.setString(i + 1, ids.get(i));
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Lỗi xóa chi tiết đặt phòng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void huyChiTietDatPhong(String maChiTietDatPhong) {
        String query = "UPDATE ChiTietDatPhong SET da_xoa = 1 WHERE ma_chi_tiet_dat_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Lỗi xóa chi tiết đặt phòng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public DonDatPhong timDonDatPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM DonDatPhong ORDER BY ma_don_dat_phong DESC";

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

    public List<ChiTietDatPhong> timTatCaChiTietDatPhongBangMaDatPhong(String maDatPhong) {
        String query = "SELECT * FROM ChiTietDatPhong WHERE ma_don_dat_phong = ? AND da_xoa = 0";
        List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDatPhong);

            var rs = ps.executeQuery();
            while (rs.next())
                chiTietDatPhongs.add(chuyenKetQuaThanhChiTietDatPhong(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return chiTietDatPhongs;
    }

    public ThongTinDatPhong timDonDatPhongChoCheckInCuaPhong(String maPhong, Timestamp tgBatDau, Timestamp tgKetThuc) {
        String query =
                "SELECT p.ma_phong, ctdp.tg_nhan_phong, ctdp.tg_tra_phong, kh.ten_khach_hang, ddp.ma_don_dat_phong, ctdp.ma_chi_tiet_dat_phong " +
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

    public DonDatPhong getDonDatPhongById(String maDonDatPhong, boolean includeDeleted) {
        String query = "SELECT * FROM DonDatPhong WHERE ma_don_dat_phong = ?";
        if (!includeDeleted) {
            query += " AND da_xoa = 0";
        }
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

    public List<DonDatPhong> getAllCurrentReservation() {
        String query =
                "SELECT DISTINCT ddp.* " +
                "FROM DonDatPhong ddp " +
                "LEFT JOIN HoaDon hd " +
                "ON ddp.ma_don_dat_phong = hd.ma_don_dat_phong " +
                "LEFT JOIN ChiTietDatPhong ctdp ON ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong " +
                "LEFT JOIN LichSuRaNgoai lsrn ON ctdp.ma_chi_tiet_dat_phong = lsrn.ma_chi_tiet_dat_phong " +
                "WHERE ddp.da_xoa = 0 " +
                "AND (la_lan_cuoi_cung is null OR la_lan_cuoi_cung = 0)" +
                "AND (hd.kieu_hoa_don is null OR hd.kieu_hoa_don != ?) " +
                "AND ctdp.kieu_ket_thuc is null"
                ;

        List<DonDatPhong> reservationIds = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, InvoiceType.PAYMENT_INVOICE.getStatus());
            var rs = ps.executeQuery();
            while (rs.next()) {
                reservationIds.add(chuyenKetQuaThanhDonDatPhong(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching current reservation IDs: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return reservationIds;
    }

    public List<KhachHang> getCustomerInfoByReservationIds(List<String> currentReservationIds) {
        if (currentReservationIds.isEmpty())
            return new ArrayList<>();

        StringBuilder query =
                new StringBuilder("SELECT DISTINCT kh.* " +
                                  "FROM KhachHang kh " +
                                  "JOIN DonDatPhong ddp ON kh.ma_khach_hang = ddp.ma_khach_hang " +
                                  "WHERE ddp.ma_don_dat_phong IN (");
        for (int i = 0; i < currentReservationIds.size(); i++) {
            query.append("?");
            if (i < currentReservationIds.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        List<KhachHang> customers = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());
            for (int i = 0; i < currentReservationIds.size(); i++) {
                ps.setString(i + 1, currentReservationIds.get(i));
            }
            var rs = ps.executeQuery();

            while (rs.next()) {
                customers.add(chuyenKetQuaThanhKhachHang(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching customer info by reservation IDs: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return customers;
    }

    private KhachHang chuyenKetQuaThanhKhachHang(ResultSet rs) {
        try {
            return new KhachHang(
                    rs.getString("ma_khach_hang"),
                    rs.getString("CCCD"),
                    rs.getString("ten_khach_hang"),
                    rs.getString("so_dien_thoai"),
                    rs.getTimestamp("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Table entity mismatch when converting to KhachHang: " + e.getMessage());
        }
    }

    public List<ReservationStatusRepository> getAllCurrentReservationsWithStatus(List<String> reservationIds) {
        if(reservationIds == null || reservationIds.isEmpty()){
            return Collections.emptyList();
        }
        StringBuilder query =
                new StringBuilder("SELECT DISTINCT ctdp.ma_don_dat_phong, lsdv.la_lan_dau_tien, ctdp.tg_nhan_phong, ctdp.tg_tra_phong " +
                                  "FROM ChiTietDatPhong ctdp " +
                                  "LEFT JOIN LichSuDiVao lsdv ON ctdp.ma_chi_tiet_dat_phong = lsdv.ma_chi_tiet_dat_phong " +
                                  "WHERE ctdp.kieu_ket_thuc is null " +
                                  "AND ctdp.ma_don_dat_phong IN (");
        for (int i = 0; i < reservationIds.size(); i++) {
            query.append("?");
            if (i < reservationIds.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        List<ReservationStatusRepository> reservations = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());
            for (int i = 0; i < reservationIds.size(); i++) {
                ps.setString(i + 1, reservationIds.get(i));
            }
            var rs = ps.executeQuery();

            while (rs.next()) {
                reservations.add(new ReservationStatusRepository(
                        rs.getString("ma_don_dat_phong"),
                        rs.getBoolean("la_lan_dau_tien"),
                        rs.getTimestamp("tg_nhan_phong"),
                        rs.getTimestamp("tg_tra_phong")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching reservations with status: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return reservations;
    }

    public List<ReservationResponse> getAllPassReservationsWithStatusInRange(Timestamp startDate, Timestamp endDate) {
        String query =
                "SELECT DISTINCT kh.CCCD, kh.ten_khach_hang, ddp.ma_don_dat_phong, " +
                "ddp.loai, ddp.tg_nhan_phong, ddp.tg_tra_phong, " +
                "null as ten_trang_thai, ddp.da_xoa " +
                "FROM DonDatPhong ddp " +
                "JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang " +
                "JOIN ChiTietDatPhong ctdp ON ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong " +
                "WHERE (ddp.da_xoa = ? OR ctdp.kieu_ket_thuc = ? OR ctdp.kieu_ket_thuc = ?)" +
                "AND ((ctdp.tg_nhan_phong BETWEEN ? AND ?) " +
                "OR (ctdp.tg_tra_phong BETWEEN ? AND ?)) " +
                "ORDER BY ddp.tg_nhan_phong ASC, ddp.ma_don_dat_phong ASC";

        List<vn.iuh.dto.response.ReservationResponse> reservations = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setBoolean(1, true);
            ps.setString(2, RoomEndType.TRA_PHONG.getStatus());
            ps.setString(3, RoomEndType.KHONG_NHAN_PHONG.getStatus());
            ps.setTimestamp(4, startDate);
            ps.setTimestamp(5, endDate);
            ps.setTimestamp(6, startDate);
            ps.setTimestamp(7, endDate);
            var rs = ps.executeQuery();

            while (rs.next()) {
                reservations.add(chuyenKetQuaThanhReservationResponse(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching reservations with status: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return reservations;
    }

    public List<ReservationDetailRepository> getReservationDetailByReservationId(String maDonDatPhong) {
        List<ReservationDetailRepository> reservationDetails = new ArrayList<>();
        String query = "SELECT DISTINCT ctdp.ma_chi_tiet_dat_phong, ctdp.ma_phong, p.ten_phong, lsdv.la_lan_dau_tien, " +
                       "ctdp.kieu_ket_thuc, ctdp.tg_nhan_phong, ctdp.tg_tra_phong, ctdp.da_xoa " +
                       "FROM ChiTietDatPhong ctdp " +
                       "LEFT JOIN LichSuDiVao lsdv ON ctdp.ma_chi_tiet_dat_phong = lsdv.ma_chi_tiet_dat_phong " +
                       "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                       "AND (la_lan_dau_tien = 1 OR (la_lan_dau_tien is null AND lsdv.thoi_gian_tao is null)) " +
                       "WHERE ctdp.ma_don_dat_phong = ? "
                ;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, maDonDatPhong);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reservationDetails.add(new ReservationDetailRepository(
                        rs.getString("ma_chi_tiet_dat_phong"),
                        rs.getString("ma_phong"),
                        rs.getString("ten_phong"),
                        rs.getString("kieu_ket_thuc"),
                        rs.getBoolean("la_lan_dau_tien"),
                        rs.getTimestamp("tg_nhan_phong"),
                        rs.getTimestamp("tg_tra_phong"),
                        rs.getBoolean("da_xoa")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy chi tiết phòng: " + e.getMessage());
        }

        return reservationDetails;
    }

    private ReservationResponse chuyenKetQuaThanhReservationResponse(ResultSet rs) {
        try {
            return new ReservationResponse(
                    rs.getString("CCCD"),
                    rs.getString("ten_khach_hang"),
                    rs.getString("ma_don_dat_phong"),
                    rs.getString("loai"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong"),
                    rs.getString("ten_trang_thai"),
                    rs.getBoolean("da_xoa")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành ReservationResponse: " + e.getMessage());
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
                    rs.getString("ma_don_dat_phong"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành ThongTinDatPhong" + e.getMessage());
        }
    }

    private PhieuDatPhong chuyenKetQuaThanhPhieuDatPhong(ResultSet rs) {
        try {
            return new PhieuDatPhong(
                    rs.getString("ma_phong"),
                    rs.getString("ten_phong"),
                    rs.getString("ten_khach_hang"),
                    rs.getString("ma_don_dat_phong"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành PhieuDatPhong" + e.getMessage());
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
                    rs.getString("loai"),
                    rs.getString("ma_khach_hang"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getString("thoi_gian_tao"),
                    rs.getBoolean("da_xoa")
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

    public ChiTietDatPhong timChiTietDatPhongBangMaChiTietDatPhong(String maChiTietDatPhong) {
        String query = "SELECT * FROM ChiTietDatPhong WHERE ma_chi_tiet_dat_phong = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhChiTietDatPhong(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }
}


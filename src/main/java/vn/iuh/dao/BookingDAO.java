package vn.iuh.dao;

import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.event.create.RoomFilter;
import vn.iuh.dto.repository.BookingInfo;
import vn.iuh.dto.repository.RoomInfo;
import vn.iuh.entity.LichSuDiVao;
import vn.iuh.entity.DonDatPhong;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    private final Connection connection;

    public BookingDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public void beginTransaction() {
        DatabaseUtil.enableTransaction(connection);
    }

    public void commitTransaction() {
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

    public void rollbackTransaction() {
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

    public boolean insertReservationForm(DonDatPhong donDatPhongEntity) {
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

    public boolean insertRoomReservationDetail(DonDatPhong donDatPhongEntity,
                                               List<ChiTietDatPhong> chiTietDatPhongs) {
        String query = "INSERT INTO ChiTietDatPhong" +
                       " (ma_chi_tiet_dat_phong, tg_nhan_phong, tg_tra_phong, kieu_ket_thuc, ma_don_dat_phong, ma_phong, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (ChiTietDatPhong chiTietDatPhong : chiTietDatPhongs) {
                System.out.println(chiTietDatPhong.getMaPhong());

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

    public boolean insertRoomUsageService(DonDatPhong donDatPhongEntity,
                                          List<PhongDungDichVu> phongDungDichVus) {
        String query = "INSERT INTO PhongDungDichVu" +
                       " (ma_phong_dung_dich_vu, tong_tien, so_luong, thoi_gian_dung, gia_thoi_diem_do, ma_chi_tiet_dat_phong, ma_dich_vu, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (PhongDungDichVu phongDungDichVu : phongDungDichVus) {
                ps.setString(1, phongDungDichVu.getMaPhongDungDichVu());
                ps.setDouble(2, phongDungDichVu.getTongTien());
                ps.setInt(3, phongDungDichVu.getSoLuong());
                ps.setTimestamp(4, phongDungDichVu.getThoiGianDung());
                ps.setDouble(5, phongDungDichVu.getGiaThoiDiemDo());
                ps.setString(6, phongDungDichVu.getMaChiTietDatPhong());
                ps.setString(7, phongDungDichVu.getMaDichVu());
                ps.setString(8, donDatPhongEntity.getMaPhienDangNhap());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == phongDungDichVus.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean insertHistoryCheckIn(DonDatPhong donDatPhong, List<LichSuDiVao> historyCheckIns) {
        String query = "INSERT INTO LichSuDiVao" +
                       " (ma_lich_su_di_vao, la_lan_dau_tien, ma_chi_tiet_dat_phong)" +
                       " VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (LichSuDiVao historyCheckIn : historyCheckIns) {
                ps.setString(1, historyCheckIn.getMaLichSuDiVao());
                ps.setBoolean(2, historyCheckIn.getLaLanDauTien());
                ps.setString(3, historyCheckIn.getMaChiTietDatPhong());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == historyCheckIns.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RoomInfo> findAllRoomInfo() {
        String query = "SELECT p.ma_phong, p.ten_phong, p.dang_hoat_dong, cv.ten_trang_thai, lp.phan_loai, lp.so_luong_khach" +
                       ", gp.gia_ngay_moi, gp.gia_gio_moi" +
                       " FROM Phong p" +
                       " LEFT JOIN CongViec cv ON cv.ma_phong = p.ma_phong" +
                       " JOIN LoaiPhong lp ON lp.ma_loai_phong = p.ma_phong" +
                       " JOIN GiaPhong gp ON gp.ma_loai_phong = lp.ma_loai_phong" +
                       " WHERE p.da_xoa = 0" +
                       " ORDER BY gp.thoi_gian_tao DESC, p.ma_phong ASC";
        List<RoomInfo> rooms = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                rooms.add(mapResultSetToRoomInfo(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return rooms;
    }

    public List<BookingInfo> findAllBookingInfo(List<String> nonAvailableRoomIds) {
        if (nonAvailableRoomIds.isEmpty())
            return new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT p.ma_phong, kh.ten_khach_hang, ctdp.tg_nhan_phong, ctdp.tg_tra_phong" +
                " FROM Phong p" +
                " JOIN ChiTietDatPhong ctdp ON p.ma_phong = ctdp.ma_phong" +
                " JOIN DonDatPhong ddp ON ddp.ma_don_dat_phong = ctdp.ma_don_dat_phong" +
                " JOIN KhachHang kh ON kh.ma_khach_hang = ddp.ma_khach_hang" +
                " WHERE p.ma_phong IN (");

        for (int i = 0; i < nonAvailableRoomIds.size(); i++) {
            query.append("?");
            if (i < nonAvailableRoomIds.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        List<BookingInfo> bookings = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            for (int i = 0; i < nonAvailableRoomIds.size(); i++) {
                ps.setString(i + 1, nonAvailableRoomIds.get(i));
            }

            var rs = ps.executeQuery();

            while (rs.next())
                bookings.add(mapResultSetToBookingInfo(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return bookings;
    }

    public DonDatPhong findLastReservationForm() {
        String query = "SELECT TOP 1 * FROM DonDatPhong ORDER BY ma_don_dat_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToReservationForm(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public ChiTietDatPhong findLastRoomReservationDetail() {
        String query = "SELECT TOP 1 * FROM ChiTietDatPhong ORDER BY ma_chi_tiet_dat_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoomReservationDetail(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public LichSuDiVao findLastHistoryCheckIn() {
        String query = "SELECT TOP 1 * FROM LichSuDiVao ORDER BY ma_lich_su_di_vao DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToHistoryCheckIn(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PhongDungDichVu findLastRoomUsageService() {
        String query = "SELECT TOP 1 * FROM PhongDungDichVu ORDER BY ma_phong_dung_dich_vu DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoomUsageService(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    private RoomInfo mapResultSetToRoomInfo(ResultSet rs) {
        try {
            return new RoomInfo(
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
            throw new TableEntityMismatch("Can`t map ResultSet to RoomInfo" + e.getMessage());
        }
    }

    private BookingInfo mapResultSetToBookingInfo(ResultSet rs) throws SQLException {
        BookingInfo bookingInfo = new BookingInfo(
                rs.getString("id"),
                rs.getString("customer_name"),
                rs.getTimestamp("time_in"),
                rs.getTimestamp("time_out")
        );

        System.out.println(bookingInfo.getTimeIn());
        return bookingInfo;
    }

    private DonDatPhong mapResultSetToReservationForm(ResultSet rs) {
        try {
            return new DonDatPhong(
                    rs.getString("ma_don_dat_phong"),
                    rs.getString("mo_ta"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong"),
                    rs.getBoolean("da_dat_truoc"),
                    rs.getDouble("tong_tien_du_tinh"),
                    rs.getDouble("tien_dat_coc"),
                    rs.getString("ma_khach_hang"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getString("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to ReservationForm" + e.getMessage());
        }
    }

    private ChiTietDatPhong mapResultSetToRoomReservationDetail(ResultSet rs) {
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
            throw new TableEntityMismatch("Can`t map ResultSet to RoomReservationDetail" + e.getMessage());
        }
    }

    private LichSuDiVao mapResultSetToHistoryCheckIn(ResultSet rs) {
        try {
            return new LichSuDiVao(
                    rs.getString("ma_lich_su_di_vao"),
                    rs.getBoolean("la_lan_dau_tien"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getTimestamp("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to HistoryCheckIn" + e.getMessage());
        }
    }

    private PhongDungDichVu mapResultSetToRoomUsageService(ResultSet rs) {
        try {
            return new PhongDungDichVu(
                    rs.getString("ma_phong_dung_dich_vu"),
                    rs.getDouble("tong_tien"),
                    rs.getInt("so_luong"),
                    rs.getTimestamp("thoi_gian_dung"),
                    rs.getDouble("gia_thoi_diem_do"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getString("ma_dich_vu"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getTimestamp("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to RoomUsageService" + e.getMessage());
        }
    }
}
package vn.iuh.dao;

import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.entity.Phong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PhongDAO {
    private final Connection connection;

    public PhongDAO() {
        connection = DatabaseUtil.getConnect();
    }

    public PhongDAO(Connection connection) {
        this.connection = connection;
    }

    public Phong timPhong(String roomID) {
        String query = "SELECT * FROM Phong WHERE ma_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoom(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    public Phong timPhongTheoTen(String tenPhong) {
        String query = "SELECT * FROM Phong WHERE ten_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, tenPhong);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoom(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    public List<Phong> timTatCaPhong() {
        String query = "SELECT * FROM Phong";
        List<Phong> phongs = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                phongs.add(mapResultSetToRoom(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return phongs;
    }

    public List<RoomFurnitureItem> timTatCaNoiThatTrongPhong(String roomID) {
        String query = "SELECT nt.ma_noi_that, nt.ten_noi_that, nttlp.so_luong " +
                       "FROM Phong p" +
                       " JOIN LoaiPhong lp ON p.ma_loai_phong = lp.ma_loai_phong" +
                       " JOIN NoiThatTrongLoaiPhong nttlp ON lp.ma_loai_phong = nttlp.ma_loai_phong" +
                       " JOIN NoiThat nt ON nttlp.ma_noi_that = nt.ma_noi_that" +
                       " WHERE p.ma_phong = ?";

        List<RoomFurnitureItem> furnitureItems = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            var rs = ps.executeQuery();

            while (rs.next()) {
                furnitureItems.add(chuyenKetQuaThanhNoiThatTrongPhong(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return furnitureItems;
    }

    public Phong themPhong(Phong phong) {
        String query = "INSERT INTO Phong (ma_phong, ten_phong, dang_hoat_dong, ghi_chu, mo_ta_phong, ma_loai_phong) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, phong.getMaPhong());
            ps.setString(2, phong.getTenPhong());
            ps.setBoolean(3, phong.isDangHoatDong());
            ps.setString(4, phong.getGhiChu());
            ps.setString(5, phong.getMoTaPhong());
            ps.setString(6, phong.getMaLoaiPhong());

            ps.executeUpdate();
            return timPhongMoiNhat();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Phong capNhatPhong(Phong phong) {
        if (timPhong(phong.getMaPhong()) == null) {
            System.out.println("No room found with ID: " + phong.getMaPhong());
            return null;
        }

        String query = "UPDATE Phong SET ten_phong = ?, dang_hoat_dong = ?, ghi_chu = ?, " +
                       "mo_ta_phong = ?, ma_loai_phong = ? WHERE ma_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, phong.getTenPhong());
            ps.setBoolean(2, phong.isDangHoatDong());
            ps.setString(3, phong.getGhiChu());
            ps.setString(4, phong.getMoTaPhong());
            ps.setString(5, phong.getMaLoaiPhong());
            ps.setString(6, phong.getMaPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0)
                return timPhong(phong.getMaPhong());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean xoaPhong(String roomID) {
        if (timPhong(roomID) == null) {
            System.out.println("No room found with ID: " + roomID);
            return false;
        }

        String query = "DELETE FROM Phong WHERE ma_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, roomID);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Phong timPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM Phong ORDER BY ma_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoom(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    private Phong mapResultSetToRoom(ResultSet rs) {
        Phong phong = new Phong();
        try {
            phong.setMaPhong(rs.getString("ma_phong"));
            phong.setTenPhong(rs.getString("ten_phong"));
            phong.setDangHoatDong(rs.getBoolean("dang_hoat_dong"));
            phong.setGhiChu(rs.getString("ghi_chu"));
            phong.setMoTaPhong(rs.getString("mo_ta_phong"));
            phong.setMaLoaiPhong(rs.getString("ma_loai_phong"));
            phong.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return phong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành Phong" + e.getMessage());
        }
    }

    private RoomFurnitureItem chuyenKetQuaThanhNoiThatTrongPhong(ResultSet rs) {
        RoomFurnitureItem item = new RoomFurnitureItem();
        try {
            item.setMaNoiThat(rs.getString("ma_noi_that"));
            item.setName(rs.getString("ten_noi_that"));
            item.setQuantity(rs.getInt("so_luong"));
            return item;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành RoomFurnitureItem" + e.getMessage());
        }
    }

    public List<Phong> timPhongUngVien(String currentRoomId, int neededPersons, Timestamp fromTime, Timestamp toTime) {
        List<Phong> results = new LinkedList<>();

        if (currentRoomId == null || fromTime == null) return results; // bảo vệ đầu vào

        try {
            // 1) Lấy ma_loai_phong và ten_loai_phong của phòng hiện tại
            String qGetLoai = "SELECT p.ma_loai_phong, lp.ten_loai_phong " +
                    "FROM Phong p JOIN LoaiPhong lp ON p.ma_loai_phong = lp.ma_loai_phong " +
                    "WHERE p.ma_phong = ?";

            String maLoaiPhong = null;
            String tenLoaiPhong = null;
            try (PreparedStatement ps = connection.prepareStatement(qGetLoai)) {
                ps.setString(1, currentRoomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maLoaiPhong = rs.getString("ma_loai_phong");
                        tenLoaiPhong = rs.getString("ten_loai_phong");
                    } else {
                        return results; // không tìm thấy phòng hiện tại
                    }
                }
            }

            // Nếu toTime != null mà < fromTime thì hoán đổi cho an toàn
            if (toTime != null && toTime.before(fromTime)) {
                Timestamp tmp = fromTime;
                fromTime = toTime;
                toTime = tmp;
            }

            // 2) Lấy danh sách phòng cơ bản phù hợp (THEO TÊN LOẠI PHÒNG)

            //Tìm tất cả các phòng khác với phòng hiện tại nhưng cùng loại phòng
            String baseQuery = "SELECT p.* FROM Phong p " +
                    "JOIN LoaiPhong lp ON p.ma_loai_phong = lp.ma_loai_phong " +
                    "WHERE p.ma_phong <> ? " +
                    "AND lp.ten_loai_phong = ? " +
                    "AND lp.so_luong_khach >= ? " +
                    "AND p.dang_hoat_dong = 1 " +
                    "AND ISNULL(p.da_xoa,0) = 0";

            try (PreparedStatement psBase = connection.prepareStatement(baseQuery)) {
                psBase.setString(1, currentRoomId);
                psBase.setString(2, tenLoaiPhong);
                psBase.setInt(3, neededPersons);

                try (ResultSet rs = psBase.executeQuery()) {
                    // Prepare statements để kiểm tra các phòng còn trống thời gian phù hợp với phòng cũ
                    final String bookingOverlapWhenToNotNull =
                            "SELECT 1 FROM ChiTietDatPhong cdp " +
                                    "WHERE cdp.ma_phong = ? AND ISNULL(cdp.da_xoa,0)=0 " +
                                    "  AND cdp.tg_nhan_phong IS NOT NULL AND cdp.tg_tra_phong IS NOT NULL " +
                                    "  AND cdp.tg_nhan_phong < cdp.tg_tra_phong " +
                                    "  AND NOT (cdp.tg_tra_phong <= ? OR cdp.tg_nhan_phong >= ?)";

                    final String bookingOverlapWhenToNull =
                            "SELECT 1 FROM ChiTietDatPhong cdp " +
                                    "WHERE cdp.ma_phong = ? AND ISNULL(cdp.da_xoa,0)=0 " +
                                    "  AND cdp.tg_nhan_phong IS NOT NULL AND cdp.tg_tra_phong IS NOT NULL " +
                                    "  AND cdp.tg_nhan_phong < cdp.tg_tra_phong " +
                                    "  AND cdp.tg_tra_phong > ?";

                    final String workOverlapWhenToNotNull =
                            "SELECT 1 FROM CongViec cv " +
                                    "WHERE cv.ma_phong = ? AND ISNULL(cv.da_xoa,0)=0 " +
                                    "  AND cv.tg_bat_dau IS NOT NULL AND cv.tg_ket_thuc IS NOT NULL " +
                                    "  AND cv.tg_bat_dau < cv.tg_ket_thuc " +
                                    "  AND NOT (cv.tg_ket_thuc <= ? OR cv.tg_bat_dau >= ?)";

                    final String workOverlapWhenToNull =
                            "SELECT 1 FROM CongViec cv " +
                                    "WHERE cv.ma_phong = ? AND ISNULL(cv.da_xoa,0)=0 " +
                                    "  AND cv.tg_bat_dau IS NOT NULL AND cv.tg_ket_thuc IS NOT NULL " +
                                    "  AND cv.tg_bat_dau < cv.tg_ket_thuc " +
                                    "  AND cv.tg_ket_thuc > ?";

                    try (PreparedStatement psBookingOverlapNotNull = connection.prepareStatement(bookingOverlapWhenToNotNull);
                         PreparedStatement psBookingOverlapNull = connection.prepareStatement(bookingOverlapWhenToNull);
                         PreparedStatement psWorkOverlapNotNull = connection.prepareStatement(workOverlapWhenToNotNull);
                         PreparedStatement psWorkOverlapNull = connection.prepareStatement(workOverlapWhenToNull)) {

                        while (rs.next()) {
                            Phong p = mapResultSetToRoom(rs);
                            String roomId = p.getMaPhong();

                            boolean blockedByBooking = false;
                            boolean blockedByWork = false;

                            if (toTime != null) {
                                psBookingOverlapNotNull.setString(1, roomId);
                                psBookingOverlapNotNull.setTimestamp(2, fromTime);
                                psBookingOverlapNotNull.setTimestamp(3, toTime);
                                try (ResultSet r2 = psBookingOverlapNotNull.executeQuery()) {
                                    blockedByBooking = r2.next();
                                }

                                psWorkOverlapNotNull.setString(1, roomId);
                                psWorkOverlapNotNull.setTimestamp(2, fromTime);
                                psWorkOverlapNotNull.setTimestamp(3, toTime);
                                try (ResultSet r3 = psWorkOverlapNotNull.executeQuery()) {
                                    blockedByWork = r3.next();
                                }
                            } else {
                                // Trường hợp thời gian checkout == null
                                psBookingOverlapNull.setString(1, roomId);
                                psBookingOverlapNull.setTimestamp(2, fromTime);
                                try (ResultSet r2 = psBookingOverlapNull.executeQuery()) {
                                    blockedByBooking = r2.next();
                                }

                                psWorkOverlapNull.setString(1, roomId);
                                psWorkOverlapNull.setTimestamp(2, fromTime);
                                try (ResultSet r3 = psWorkOverlapNull.executeQuery()) {
                                    blockedByWork = r3.next();
                                }
                            }

                            //Nếu phòng không bận (trống vào khoảng thời gian cần đổi) thì thêm phòng vào danh sách
                            if (!blockedByBooking && !blockedByWork) {
                                results.add(p);
                            } else {
                                String reason = blockedByBooking && blockedByWork ? "booking+work" :
                                        blockedByBooking ? "booking" : "work";
                                System.out.println("timPhongUngVien: Loai phong " + p.getMaPhong() + " vì: " + reason);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    // Lấy giá mới nhất của một loại phòng
    public double[] getLatestPriceForLoaiPhong(String maLoaiPhong) {
        double[] price = new double[]{0.0, 0.0};
        String query = "SELECT TOP 1 gia_ngay_moi, gia_gio_moi FROM GiaPhong WHERE ma_loai_phong = ? AND ISNULL(da_xoa,0)=0 ORDER BY thoi_gian_tao DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double gNgay = rs.getDouble("gia_ngay_moi");
                    if (rs.wasNull()) gNgay = 0.0;
                    double gGio = rs.getDouble("gia_gio_moi");
                    if (rs.wasNull()) gGio = 0.0;
                    price[0] = gNgay;
                    price[1] = gGio;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return price;
    }
}

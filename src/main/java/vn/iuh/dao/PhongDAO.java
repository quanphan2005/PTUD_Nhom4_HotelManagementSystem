package vn.iuh.dao;

import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.dto.repository.RoomWithCategory;
import vn.iuh.dto.repository.ThongTinPhong;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.Phong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PhongDAO {
    public Phong timPhong(String roomID) {
        String query = "SELECT * FROM Phong WHERE ma_phong = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
        String query = "SELECT * FROM Phong where da_xoa = 0";
        List<Phong> phongs = new ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
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

    public List<RoomWithCategory> timTatCaPhongVoiLoaiBangDanhSachMaPhong(List<String> danhSachMaPhong) {
        if (danhSachMaPhong.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder queryStringBuilder = new StringBuilder(
                "SELECT p.ma_phong, p.ten_phong, p.dang_hoat_dong, lp.ma_loai_phong, lp.ten_loai_phong, " +
                "lp.so_luong_khach, gp.gia_ngay_moi, gp.gia_gio_moi " +
                "FROM Phong p " +
                "JOIN LoaiPhong lp ON p.ma_loai_phong = lp.ma_loai_phong " +
                "JOIN GiaPhong gp ON lp.ma_loai_phong = gp.ma_loai_phong " +
                "WHERE p.da_xoa = 0 " +
                "AND lp.da_xoa = 0 " +
                "AND gp.da_xoa = 0 " +
                "AND p.ma_phong IN ("
        );

        for (int i = 0; i < danhSachMaPhong.size(); i++) {
            queryStringBuilder.append("?");
            if (i < danhSachMaPhong.size() - 1) {
                queryStringBuilder.append(", ");
            }
        }
        queryStringBuilder.append(")");
        String query = queryStringBuilder.toString();

        List<RoomWithCategory> results = new ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < danhSachMaPhong.size(); i++) {
                ps.setString(i + 1, danhSachMaPhong.get(i));
            }

            var rs = ps.executeQuery();
            while (rs.next()) {
                RoomWithCategory roomWithCategory = chuyenKetQuaThanhRoomWithCategory(rs);
                results.add(roomWithCategory);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return results;
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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

    private RoomWithCategory chuyenKetQuaThanhRoomWithCategory(ResultSet rs) {
        try {
            return new RoomWithCategory(
                    rs.getString("ma_phong"),
                    rs.getString("ten_phong"),
                    rs.getBoolean("dang_hoat_dong"),
                    rs.getString("ma_loai_phong"),
                    rs.getString("ten_loai_phong"),
                    rs.getInt("so_luong_khach"),
                    rs.getDouble("gia_ngay_moi"),
                    rs.getDouble("gia_gio_moi")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành RoomWithCategory" + e.getMessage());
        }
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

        if (currentRoomId == null || fromTime == null || toTime == null) return results;

        try {
            // 1) Lấy ma_loai_phong của phòng hiện tại
            String qGetLoai = "SELECT p.ma_loai_phong " +
                    "FROM Phong p " +
                    "WHERE p.ma_phong = ?";

            String maLoaiPhong = null;
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(qGetLoai);
            ps.setString(1, currentRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maLoaiPhong = rs.getString("ma_loai_phong");
                } else {
                    return results;
                }
            }

            // Nếu toTime < fromTime thì hoán đổi cho an toàn
            if (toTime.before(fromTime)) {
                Timestamp tmp = fromTime;
                fromTime = toTime;
                toTime = tmp;
            }

            // 2) Lấy danh sách phòng khác cùng ma_loai_phong, đang hoạt động, đủ chỗ
            String baseQuery = "SELECT p.* FROM Phong p " +
                    "WHERE p.ma_phong <> ? " +
                    "AND p.ma_loai_phong = ? " +
                    "AND ISNULL(p.dang_hoat_dong, 0) = 1 " +
                    "AND ISNULL(p.da_xoa,0) = 0";

            // 3) Kiểm tra xem có bị trùng chi tiết đặt phòng hay không
            // nếu trùng thời gian trên chi tiết đặt phòng ==> Không thể đổi phòng
            final String bookingOverlap =
                    "SELECT 1 FROM ChiTietDatPhong cdp " +
                            "WHERE cdp.ma_phong = ? AND ISNULL(cdp.da_xoa,0)=0 " +
                            "  AND cdp.kieu_ket_thuc IS NULL " +
                            "  AND cdp.tg_nhan_phong IS NOT NULL AND cdp.tg_tra_phong IS NOT NULL " +
                            "  AND cdp.tg_nhan_phong < cdp.tg_tra_phong " +
                            "  AND NOT (cdp.tg_tra_phong <= ? OR cdp.tg_nhan_phong >= ?)";

            final String workOverlap =
                    "SELECT 1 FROM CongViec cv " +
                            "WHERE cv.ma_phong = ? AND ISNULL(cv.da_xoa,0)=0 " +
                            "  AND cv.tg_bat_dau IS NOT NULL AND cv.tg_ket_thuc IS NOT NULL " +
                            "  AND cv.tg_bat_dau < cv.tg_ket_thuc " +
                            "  AND NOT (cv.tg_ket_thuc <= ? OR cv.tg_bat_dau >= ?)";

            try (PreparedStatement psBase = connection.prepareStatement(baseQuery);
                 PreparedStatement psBookingOverlap = connection.prepareStatement(bookingOverlap);
                 PreparedStatement psWorkOverlap = connection.prepareStatement(workOverlap)) {

                psBase.setString(1, currentRoomId);
                psBase.setString(2, maLoaiPhong);

                try (ResultSet rs = psBase.executeQuery()) {
                    while (rs.next()) {
                        Phong p = mapResultSetToRoom(rs);
                        String roomId = p.getMaPhong();

                        boolean blockedByBooking = false;
                        boolean blockedByWork = false;

                        // Kiểm tra chi tiết đặt phòng
                        psBookingOverlap.setString(1, roomId);
                        psBookingOverlap.setTimestamp(2, fromTime);
                        psBookingOverlap.setTimestamp(3, toTime);
                        try (ResultSet r2 = psBookingOverlap.executeQuery()) {
                            blockedByBooking = r2.next();
                        }

                        // kiểm tra công việc
                        psWorkOverlap.setString(1, roomId);
                        psWorkOverlap.setTimestamp(2, fromTime);
                        psWorkOverlap.setTimestamp(3, toTime);
                        try (ResultSet r3 = psWorkOverlap.executeQuery()) {
                            blockedByWork = r3.next();
                        }

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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return results;
    }


    // Lấy giá mới nhất của một loại phòng
    public double[] getLatestPriceForLoaiPhong(String maLoaiPhong) {
        double[] price = new double[]{0.0, 0.0};
        String query = "SELECT TOP 1 gia_ngay_moi, gia_gio_moi FROM GiaPhong WHERE ma_loai_phong = ? AND ISNULL(da_xoa,0)=0 ORDER BY thoi_gian_tao DESC";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

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


    // Tìm danh sách nội thất theo loại phòng
    public List<RoomFurnitureItem> timNoiThatTheoLoaiPhong(String maLoaiPhong) {
        String query = "SELECT nttlp.ma_noi_that, nt.ten_noi_that, nttlp.so_luong " +
                "FROM NoiThatTrongLoaiPhong nttlp " +
                "JOIN NoiThat nt ON nttlp.ma_noi_that = nt.ma_noi_that " +
                "WHERE nttlp.ma_loai_phong = ? AND ISNULL(nttlp.da_xoa,0) = 0";

        List<RoomFurnitureItem> furnitureItems = new ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    furnitureItems.add(chuyenKetQuaThanhNoiThatTrongPhong(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return furnitureItems;
    }

    public boolean xoaPhongQuanLyPhongPanel(String roomID) {
        if (timPhong(roomID) == null) {
            System.out.println("No room found with ID: " + roomID);
            return false;
        }

        String query = "UPDATE Phong SET da_xoa = 1 WHERE ma_phong = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, roomID);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Phong> timTatCaPhongChoQuanLyPhong() {
        String query = "SELECT * FROM Phong WHERE da_xoa=0";
        List<Phong> phongs = new ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
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

    public CongViec layCongViecHienTaiCuaPhong(String maPhong) {
        String query = "SELECT TOP 1 * FROM CongViec " +
                "WHERE ma_phong = ? AND ISNULL(da_xoa,0) = 0 " +
                "  AND ( (tg_bat_dau IS NOT NULL AND tg_ket_thuc IS NOT NULL AND ? BETWEEN tg_bat_dau AND tg_ket_thuc) " +
                "        OR (tg_bat_dau IS NOT NULL AND tg_ket_thuc IS NULL AND tg_bat_dau <= ?) ) " +
                "ORDER BY tg_bat_dau DESC";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setString(1, maPhong);
            ps.setTimestamp(2, now);
            ps.setTimestamp(3, now);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CongViec cv = new CongViec();
                    cv.setMaCongViec(rs.getString("ma_cong_viec"));
                    cv.setTenTrangThai(rs.getString("ten_trang_thai"));
                    cv.setTgBatDau(rs.getTimestamp("tg_bat_dau"));
                    cv.setTgKetThuc(rs.getTimestamp("tg_ket_thuc"));
                    cv.setMaPhong(rs.getString("ma_phong"));
                    cv.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));

                    return cv;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

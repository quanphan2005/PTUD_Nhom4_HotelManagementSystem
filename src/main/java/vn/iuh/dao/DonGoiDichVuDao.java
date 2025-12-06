package vn.iuh.dao;

import vn.iuh.dto.repository.RoomUsageServiceInfo;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DonGoiDichVuDao {
    private final Connection connection;

    public DonGoiDichVuDao() {
        this.connection = DatabaseUtil.getConnect();
    }

    public DonGoiDichVuDao(Connection connection) {
        this.connection = connection;
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

    public void themPhongDungDichVu(List<PhongDungDichVu> danhSachPhongDungDichVu) {
        String query = "INSERT INTO PhongDungDichVu" +
                       " (ma_phong_dung_dich_vu, so_luong, gia_thoi_diem_do, ma_chi_tiet_dat_phong, ma_dich_vu, ma_phien_dang_nhap, tong_tien)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (PhongDungDichVu phongDungDichVu : danhSachPhongDungDichVu) {
                ps.setString(1, phongDungDichVu.getMaPhongDungDichVu());
                ps.setInt(2, phongDungDichVu.getSoLuong());
                ps.setDouble(3, phongDungDichVu.getGiaThoiDiemDo());
                ps.setString(4, phongDungDichVu.getMaChiTietDatPhong());
                ps.setString(5, phongDungDichVu.getMaDichVu());
                ps.setString(6, phongDungDichVu.getMaPhienDangNhap());
                ps.setBigDecimal(7, phongDungDichVu.getTongTien());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ThongTinDichVu> timTatCaThongTinDichVu() {
        String query = "SELECT dv.ma_dich_vu, dv.ten_dich_vu, dv.ton_kho, dv.co_the_tang, gdv.gia_moi, ldv.ten_loai_dich_vu" +
                       " FROM DichVu dv" +
                       " JOIN LoaiDichVu ldv ON dv.ma_loai_dich_vu = ldv.ma_loai_dich_vu " +
                       " LEFT JOIN GiaDichVu gdv ON dv.ma_dich_vu = gdv.ma_dich_vu" +
                       " WHERE dv.da_xoa = 0";

        List<ThongTinDichVu> danhSachThongTinDichVu = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                danhSachThongTinDichVu.add(chuyenKetQuaThanhThongTinDichVu(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachThongTinDichVu;
    }

    public PhongDungDichVu timPhongDungDichVuMoiNhat() {
        String query = "SELECT TOP 1 * FROM PhongDungDichVu ORDER BY ma_phong_dung_dich_vu DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhPhongDungDichVu(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public List<PhongDungDichVu> timDonGoiDichVuBangMaDatPhong(String maDatPhong) {
        String query = "SELECT pddv.* " +
                       " FROM ChiTietDatPhong ctdp " +
                       " JOIN PhongDungDichVu pddv ON ctdp.ma_chi_tiet_dat_phong = pddv.ma_chi_tiet_dat_phong " +
                       " WHERE ctdp.ma_don_dat_phong = ?";

        List<PhongDungDichVu> roomUsageServiceInfos = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDatPhong);
            var rs = ps.executeQuery();
            while (rs.next())
                roomUsageServiceInfos.add(chuyenKetQuaThanhPhongDungDichVu(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return roomUsageServiceInfos;
    }

    public List<RoomUsageServiceInfo> timTatCaDonGoiDichVuBangMaDatPhong(String maDatPhong) {
        String query = "SELECT pddv.*, dv.ten_dich_vu, p.ten_phong " +
                       "FROM ChiTietDatPhong ctdp" +
                       " JOIN PhongDungDichVu pddv ON ctdp.ma_chi_tiet_dat_phong = pddv.ma_chi_tiet_dat_phong " +
                       " JOIN DichVu dv ON pddv.ma_dich_vu = dv.ma_dich_vu " +
                       " JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                       " WHERE ctdp.ma_don_dat_phong = ?";

        List<RoomUsageServiceInfo> roomUsageServiceInfos = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDatPhong);
            var rs = ps.executeQuery();
            while (rs.next())
                roomUsageServiceInfos.add(chuyenKetQuaThanhRoomUsageServiceInfo(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return roomUsageServiceInfos;
    }

    public void capNhatSoLuongTonKhoDichVu(String maDichVu, int soLuong) {
        String query = "UPDATE DichVu SET ton_kho = ton_kho + ? WHERE ma_dich_vu = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, soLuong);
            ps.setString(2, maDichVu);

            int rowsAffected = ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PhongDungDichVu> timDonGoiDichVuBangDonDatPhong(String maDonDatPhong){
        List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();

        String query = "SELECT pddv.*, dv.ten_dich_vu, p.ten_phong FROM PhongDungDichVu pddv " +
                        "JOIN DichVu dv ON pddv.ma_dich_vu = dv.ma_dich_vu " +
                        "JOIN ChiTietDatPhong ctdp ON pddv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong "+
                        "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                        "WHERE ctdp.ma_don_dat_phong = ? " +
                        "ORDER BY pddv.so_luong desc";
        try{
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,maDonDatPhong);
            var rs = ps.executeQuery();
            while(rs.next()){
                PhongDungDichVu pddv = mapResultSet(rs);
                pddv.setTenDichVu(rs.getString("ten_dich_vu"));
                pddv.setTenPhong(rs.getString("ten_phong"));
                pddv.setTongTien(rs.getBigDecimal("tong_tien"));
                danhSachPhongDungDichVu.add(pddv);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return danhSachPhongDungDichVu;
    }

//    public List<PhongDungDichVu> timDonGoiDichVuBangChiTietDatPhong(String maChiTietDatPhong) {
//        String query = "SELECT * FROM PhongDungDichVu WHERE ma_chi_tiet_dat_phong = ? ORDER BY ma_phong_dung_dich_vu DESC";
//
//        List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();
//        try {
//            PreparedStatement ps = connection.prepareStatement(query);
//            ps.setString(1, maChiTietDatPhong);
//            var rs = ps.executeQuery();
//            while (rs.next())
//                danhSachPhongDungDichVu.add(chuyenKetQuaThanhPhongDungDichVu(rs));
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (TableEntityMismatch mismatchException) {
//            System.out.println(mismatchException.getMessage());
//        }
//
//        return danhSachPhongDungDichVu;
//    }

    public List<RoomUsageServiceInfo> timTatCaDonGoiDichVuBangChiTietDatPhong(String maChiTietDatPhong) {

        String query =
                "SELECT pddv.*, dv.ten_dich_vu, p.ten_phong " +
                "FROM PhongDungDichVu pddv " +
                "JOIN DichVu dv ON pddv.ma_dich_vu = dv.ma_dich_vu " +
                "JOIN ChiTietDatPhong ctdp ON pddv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong " +
                "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                "WHERE pddv.ma_chi_tiet_dat_phong = ?";

        List<RoomUsageServiceInfo> roomUsageServiceInfos = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maChiTietDatPhong);

            var rs = ps.executeQuery();
            while (rs.next())
                roomUsageServiceInfos.add(chuyenKetQuaThanhRoomUsageServiceInfo(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return roomUsageServiceInfos;
    }

    public List<RoomUsageServiceInfo> timDonGoiDichVuBangDanhSachChiTietDatPhong(ArrayList<String> danhSachMaChiTietDatPhong) {
        if (danhSachMaChiTietDatPhong.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT pddv.*, dv.ten_dich_vu, p.ten_phong " +
                "FROM PhongDungDichVu pddv " +
                "JOIN DichVu dv ON pddv.ma_dich_vu = dv.ma_dich_vu " +
                "JOIN ChiTietDatPhong ctdp ON pddv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong " +
                "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                "WHERE pddv.ma_chi_tiet_dat_phong IN ("
        );
        for (int i = 0; i < danhSachMaChiTietDatPhong.size(); i++) {
            queryBuilder.append("?");
            if (i < danhSachMaChiTietDatPhong.size() - 1)
                queryBuilder.append(", ");
        }
        queryBuilder.append(")");
        String query = queryBuilder.toString();

        List<RoomUsageServiceInfo> danhSachPhongDungDichVu = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (int i = 0; i < danhSachMaChiTietDatPhong.size(); i++) {
                ps.setString(i + 1, danhSachMaChiTietDatPhong.get(i));
            }

            var rs = ps.executeQuery();
            while (rs.next())
                danhSachPhongDungDichVu.add(chuyenKetQuaThanhRoomUsageServiceInfo(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachPhongDungDichVu;
    }

    private RoomUsageServiceInfo chuyenKetQuaThanhRoomUsageServiceInfo(ResultSet rs) {
        try {
            return new RoomUsageServiceInfo(
                    rs.getString("ma_phong_dung_dich_vu"),
                    rs.getInt("so_luong"),
                    rs.getDouble("gia_thoi_diem_do"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getString("ten_phong"),
                    rs.getString("ma_dich_vu"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getTimestamp("thoi_gian_tao"),
                    rs.getString("ten_dich_vu"),
                    rs.getBigDecimal("tong_tien"));
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành RoomUsageServiceInfo: " + e.getMessage());
        }
    }

    public void huyDanhSachPhongDungDichVu(List<String> ids) {
        StringBuilder string = new StringBuilder(
                "UPDATE PhongDungDichVu SET da_xoa = 1 WHERE ma_phong_dung_dich_vu IN ("
        );

        for (int i = 0; i < ids.size(); i++) {
            string.append("?");
            if (i < ids.size() - 1)
                string.append(", ");
        }
        string.append(")");

        String query = string.toString();
        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (int i = 0; i < ids.size(); i++)
                ps.setString(i + 1, ids.get(i));

            ps.executeBatch();

        } catch (SQLException e) {
            System.out.println("Lỗi xóa danh sách phòng dùng dịch vụ: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private ThongTinDichVu chuyenKetQuaThanhThongTinDichVu(ResultSet rs) throws SQLException, TableEntityMismatch {
        try {
            String maDichVu = rs.getString("ma_dich_vu");
            String tenDichVu = rs.getString("ten_dich_vu");
            String tenLoaiDichVu = rs.getString("ten_loai_dich_vu");
            double giaMoi = rs.getDouble("gia_moi");
            int tonKho = rs.getInt("ton_kho");

            boolean coTheTang = rs.getBoolean("co_the_tang");

            return new ThongTinDichVu(maDichVu, tenDichVu, tonKho, coTheTang, giaMoi, tenLoaiDichVu);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi khi chuyển kết quả sang ThongTinDichVu: " + e.getMessage());
        }
    }

    private PhongDungDichVu mapResultSet(ResultSet rs){
        PhongDungDichVu phongDungDichVu = new PhongDungDichVu();

        try{
            phongDungDichVu.setMaPhongDungDichVu(rs.getString("ma_phong_dung_dich_vu"));
            phongDungDichVu.setSoLuong(rs.getInt("so_luong"));
            phongDungDichVu.setGiaThoiDiemDo(rs.getDouble("gia_thoi_diem_do"));
            phongDungDichVu.setMaChiTietDatPhong(rs.getString("ma_chi_tiet_dat_phong"));
            phongDungDichVu.setMaDichVu(rs.getString("ma_dich_vu"));
            phongDungDichVu.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            phongDungDichVu.setTongTien(BigDecimal.valueOf(rs.getDouble("tong_tien")));
            return phongDungDichVu;
        }catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành PhongDungDichVu" + e.getMessage());
        }
    }

    private PhongDungDichVu chuyenKetQuaThanhPhongDungDichVu(ResultSet rs) {
        try {
            return new PhongDungDichVu(
                    rs.getString("ma_phong_dung_dich_vu"),
                    rs.getInt("so_luong"),
                    rs.getDouble("gia_thoi_diem_do"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getString("ma_dich_vu"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getTimestamp("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành PhongDungDichVu: " + e.getMessage());
        }
    }

    public List<PhongDungDichVu> timDonGoiDichVuBangChiTietDatPhong(List<String> maChiTietDatPhongList) {
        if (maChiTietDatPhongList == null || maChiTietDatPhongList.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders = String.join(",", Collections.nCopies(maChiTietDatPhongList.size(), "?"));
        String query = "SELECT pddv.*, dv.ten_dich_vu, p.ten_phong FROM PhongDungDichVu pddv " +
                "JOIN DichVu dv ON pddv.ma_dich_vu = dv.ma_dich_vu " +
                "JOIN ChiTietDatPhong ctdp ON pddv.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong "+
                "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                "WHERE ctdp.ma_chi_tiet_dat_phong IN (" + placeholders + ")";

        List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < maChiTietDatPhongList.size(); i++) {
                ps.setString(i + 1, maChiTietDatPhongList.get(i));
            }

            var rs = ps.executeQuery();
            while (rs.next()) {
                PhongDungDichVu pddv = chuyenKetQuaThanhPhongDungDichVu(rs);
                pddv.setTenDichVu(rs.getString("ten_dich_vu"));
                pddv.setTenPhong(rs.getString("ten_phong"));
                pddv.setTongTien(rs.getBigDecimal("tong_tien"));
                danhSachPhongDungDichVu.add(pddv);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return danhSachPhongDungDichVu;
    }
}

package vn.iuh.dao;

import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.DonDatPhong;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoiDichVuDao {
    private final Connection connection;

    public GoiDichVuDao() {
        this.connection = DatabaseUtil.getConnect();
    }

    public GoiDichVuDao(Connection connection) {
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
                       " (ma_phong_dung_dich_vu, so_luong, gia_thoi_diem_do, duoc_tang, ma_chi_tiet_dat_phong, ma_dich_vu, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (PhongDungDichVu phongDungDichVu : danhSachPhongDungDichVu) {
                ps.setString(1, phongDungDichVu.getMaPhongDungDichVu());
                ps.setInt(2, phongDungDichVu.getSoLuong());
                ps.setDouble(3, phongDungDichVu.getGiaThoiDiemDo());
                ps.setBoolean(4, phongDungDichVu.getDuocTang());
                ps.setString(5, phongDungDichVu.getMaChiTietDatPhong());
                ps.setString(6, phongDungDichVu.getMaDichVu());
                ps.setString(7, phongDungDichVu.getMaPhienDangNhap());

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
                       " JOIN LoaiDichVu ldv ON dv.ma_loai_dich_vu = ldv.ma_loai_dich_vu" +
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

    public void capNhatSoLuongTonKhoDichVu(String maDichVu, int soLuong) {
        String query = "UPDATE DichVu SET ton_kho = ton_kho - ? WHERE ma_dich_vu = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, soLuong);
            ps.setString(2, maDichVu);

            int rowsAffected = ps.executeUpdate();

        } catch (SQLException e) {
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

    private PhongDungDichVu chuyenKetQuaThanhPhongDungDichVu(ResultSet rs) {
        try {
            return new PhongDungDichVu(
                    rs.getString("ma_phong_dung_dich_vu"),
                    rs.getInt("so_luong"),
                    rs.getDouble("gia_thoi_diem_do"),
                    rs.getBoolean("duoc_tang"),
                    rs.getString("ma_chi_tiet_dat_phong"),
                    rs.getString("ma_dich_vu"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getTimestamp("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành PhongDungDichVu: " + e.getMessage());
        }
    }
}

package vn.iuh.dao;

import vn.iuh.entity.HoaDon;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class HoaDonDAO {
    private final Connection connection;

    public HoaDonDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public HoaDonDAO(Connection connection) {
        this.connection = connection;
    }

    public HoaDon timHoaDon(String id) {
        String query = "SELECT * FROM HoaDon WHERE ma_hoa_don = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhHoaDon(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public HoaDon createInvoice(HoaDon hoaDon) {
        String query = "INSERT INTO HoaDon (ma_hoa_don, phuong_thuc_thanh_toan, tong_tien, tien_thue, tong_hoa_don, " +
                "kieu_hoa_don, tinh_trang_thanh_toan, ma_phien_dang_nhap, ma_don_dat_phong, ma_khach_hang) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, hoaDon.getMaHoaDon());
            ps.setString(2, hoaDon.getPhuongThucThanhToan());
            ps.setDouble(3, hoaDon.getTongTien());
            ps.setDouble(4, hoaDon.getTienThue());
            ps.setDouble(5, hoaDon.getTongHoaDon());
            ps.setString(6, hoaDon.getKieuHoaDon());
            ps.setString(7, hoaDon.getTinhTrangThanhToan());
            ps.setString(8, hoaDon.getMaPhienDangNhap());
            ps.setString(9, hoaDon.getMaDonDatPhong());
            ps.setString(10, hoaDon.getMaKhachHang());

            ps.executeUpdate();
            return hoaDon;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public HoaDon timHoaDonMoiNhat() {
        String query = "SELECT TOP 1 * FROM HoaDon WHERE da_xoa = 0 ORDER BY ma_hoa_don DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhHoaDon(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    private HoaDon chuyenKetQuaThanhHoaDon(ResultSet rs) throws SQLException {
        HoaDon hoaDon = new HoaDon();
        try {
            hoaDon.setMaHoaDon(rs.getString("ma_hoa_don"));
            hoaDon.setPhuongThucThanhToan(rs.getString("phuong_thuc_thanh_toan"));
            hoaDon.setTongTien(rs.getDouble("tong_tien"));
            hoaDon.setTienThue(rs.getDouble("tien_thue"));
            hoaDon.setTongHoaDon(rs.getDouble("tong_hoa_don"));
            hoaDon.setKieuHoaDon(rs.getString("kieu_hoa_don"));
            hoaDon.setTinhTrangThanhToan(rs.getString("tinh_trang_thanh_toan"));
            hoaDon.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            hoaDon.setMaDonDatPhong(rs.getString("ma_don_dat_phong"));
            hoaDon.setMaKhachHang(rs.getString("ma_khach_hang"));
            hoaDon.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));

            return hoaDon;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Invoice: " + e);
        }
    }
}
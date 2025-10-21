package vn.iuh.dao;

import vn.iuh.constraint.InvoiceType;
import vn.iuh.dto.event.update.InvoiceStatusUpdate;
import vn.iuh.entity.HoaDon;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {
    private final Connection connection;

    public HoaDonDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public HoaDonDAO(Connection connection) {
        this.connection = connection;
    }

    public HoaDon timHoaDon(String id) {
        String query = "SELECT * FROM HoaDon WHERE ma_hoa_don = ?";

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
        String sql = "Insert into HoaDon (ma_hoa_don, phuong_thuc_thanh_toan, kieu_hoa_don, tinh_trang_thanh_toan, ma_phien_dang_nhap, ma_don_dat_phong, ma_khach_hang, tong_tien, tien_thue, tong_hoa_don) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hoaDon.getMaHoaDon());
            ps.setString(2, hoaDon.getPhuongThucThanhToan());
            ps.setString(3, hoaDon.getKieuHoaDon());
            ps.setString(4, hoaDon.getTinhTrangThanhToan());
            ps.setString(5, hoaDon.getMaPhienDangNhap());
            ps.setString(6, hoaDon.getMaDonDatPhong());
            ps.setString(7, hoaDon.getMaKhachHang());
            ps.setBigDecimal(8, hoaDon.getTongTien());
            ps.setBigDecimal(9, hoaDon.getTienThue());
            ps.setBigDecimal(10, hoaDon.getTongHoaDon());

            int rs = ps.executeUpdate();
            if (rs > 0) {
                return hoaDon;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean updateTinhTrangThanhToan(HoaDon hoaDon){
        String sql = "Update HoaDon set phuong_thuc_thanh_toan = ? , tinh_trang_thanh_toan = ? where ma_hoa_don = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, hoaDon.getPhuongThucThanhToan());
            ps.setString(2, hoaDon.getTinhTrangThanhToan());
            ps.setString(3, hoaDon.getMaHoaDon());
            int rs = ps.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }
        return false;
    }

    public HoaDon timHoaDonMoiNhat() {
        String query = "SELECT TOP 1 * FROM HoaDon ORDER BY ma_hoa_don DESC";

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

    public HoaDon timHoaTheoMaDonDatPhong(String maDonDatPhong, String kieuHoaDon){
        String query = "SELECT TOP 1 * FROM HoaDon where ma_don_dat_phong = ? and kieu_hoa_don = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maDonDatPhong);
            ps.setString(2, kieuHoaDon);
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
            hoaDon.setKieuHoaDon(rs.getString("kieu_hoa_don"));
            hoaDon.setTinhTrangThanhToan(rs.getString("tinh_trang_thanh_toan"));
            hoaDon.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            hoaDon.setMaDonDatPhong(rs.getString("ma_don_dat_phong"));
            hoaDon.setMaKhachHang(rs.getString("ma_khach_hang"));
            hoaDon.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            hoaDon.setTongTien(rs.getBigDecimal("tong_tien"));
            hoaDon.setTongTien(rs.getBigDecimal("tien_thue"));
            hoaDon.setTongTien(rs.getBigDecimal("tong_hoa_don"));

            return hoaDon;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Invoice: " + e);
        }
    }

    public HoaDon findInvoiceForReservation(String reservationId){
        String query = "Select * from HoaDon where ma_don_dat_phong = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, reservationId);
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

    public List<HoaDon> layDanhSachHoaDonTrongKhoang(Timestamp tgBatDau, Timestamp tgKetThuc, String maNhanVien){
        String sql = "select * from HoaDon where (thoi_gian_tao between ? and ?) and  (? IS NULL OR ma_nhan_vien = ?)" +
                "order by ma_hoa_don ";

        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setTimestamp(1,tgBatDau);
            ps.setTimestamp(2,tgKetThuc);
            ps.setString(3, maNhanVien);
            ps.setString(4, maNhanVien);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachHoaDon.add(chuyenKetQuaThanhHoaDon(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }
        return danhSachHoaDon;
    }
}
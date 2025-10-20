package vn.iuh.dao;

import vn.iuh.dto.event.update.InvoicePricingUpdate;
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

    public List<HoaDon> layTatCaHoaDon(){
        List<HoaDon> list = new ArrayList<>();
        String query = "SELECT hd.ma_hoa_don, hd.kieu_hoa_don, hd.ma_don_dat_phong, hd.ma_khach_hang FROM HoaDon hd";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                HoaDon hoaDon= new HoaDon();
                hoaDon.setMaHoaDon(rs.getString("ma_hoa_don"));
                hoaDon.setKieuHoaDon(rs.getString("kieu_hoa_don"));
                hoaDon.setMaDonDatPhong(rs.getString("ma_don_dat_phong"));
                hoaDon.setMaKhachHang(rs.getString("ma_khach_hang"));
                list.add(hoaDon);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

//    public List<HoaDon> timTheoNgay(Date tuNgay, Date denNgay){
//        List<HoaDon> dsHoaDon = new ArrayList<>();
//        String query = "SELECT * FROM HoaDon " +
//                "WHERE CAST(thoi_gian_tao AS DATE) BETWEEN ? AND ?";
//
//        try (PreparedStatement ps = connection.prepareStatement(query)) {
//
//            // SỬA 2: Dùng setDate thay vì setTimestamp để khớp với CAST
//            ps.setDate(1, new java.sql.Date(tuNgay.getTime()));
//            ps.setDate(2, new java.sql.Date(denNgay.getTime()));
//
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    HoaDon hd = mapResultSet(rs);
//                    dsHoaDon.add(hd);
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return dsHoaDon;
//    }

    public boolean boSungGiaTien(InvoicePricingUpdate pricing){
        String sql = "Update HoaDon set tong_tien = ?, tien_thue = ? , tong_hoa_don = ? where ma_hoa_don = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setBigDecimal(1, pricing.getTongTien());
            ps.setBigDecimal(2, pricing.getTienThue());
            ps.setBigDecimal(3, pricing.getTongHoaDon());
            ps.setString(4, pricing.getMaHoaDon());

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
        String sql = "select * from HoaDon where getdate() between ? and ? order by ma_hoa_don and  (? IS NULL OR ma_nhan_vien = ?)";

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
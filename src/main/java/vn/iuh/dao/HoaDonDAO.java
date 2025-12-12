package vn.iuh.dao;

import vn.iuh.constraint.InvoiceType;
import vn.iuh.dto.repository.CustomerPayments;
import vn.iuh.entity.HoaDon;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {
    public HoaDon timHoaDon(String id) {
        String query = "SELECT * FROM HoaDon WHERE ma_hoa_don = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            hoaDon.setTienThue(rs.getBigDecimal("tien_thue"));
            hoaDon.setTongHoaDon(rs.getBigDecimal("tong_hoa_don"));

            return hoaDon;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Invoice: " + e);
        }
    }

    public HoaDon findInvoiceForReservation(String reservationId, String invoiceType){
        String query = "Select top 1 * from HoaDon where ma_don_dat_phong = ? and kieu_hoa_don = ?";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, reservationId);
            ps.setString(2, invoiceType);
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

    public List<HoaDon> layDanhSachHoaDon(){
        List<HoaDon> list = new ArrayList<>();
        String query = "select * from HoaDon";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            var rs = ps.executeQuery();
            while (rs.next()) {

                HoaDon hoaDon = chuyenKetQuaThanhHoaDon(rs);
                list.add(hoaDon);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy tất cả hóa đơn: " + e.getMessage(), e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return list;
    }



    public List<HoaDon> danhSachHoaDonTrongKhoang(Timestamp from, Timestamp to) {
        String query = "SELECT hd.ma_hoa_don, hd.kieu_hoa_don, hd.ma_don_dat_phong, hd.ma_khach_hang " +
                "FROM HoaDon hd WHERE hd.thoi_gian_tao BETWEEN ? AND ?";

        List<HoaDon> danhSachHoaDon = new ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, from);
            ps.setTimestamp(2, to);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setMaHoaDon(rs.getString("ma_hoa_don"));
                    hd.setKieuHoaDon(rs.getString("kieu_hoa_don"));
                    hd.setMaDonDatPhong(rs.getString("ma_don_dat_phong"));
                    hd.setMaKhachHang(rs.getString("ma_khach_hang"));

                    danhSachHoaDon.add(hd);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return danhSachHoaDon;
    }

    public CustomerPayments timThongTinThanhToanCuaKhachHangBangMaChiTietDatPhong(String maChiTietDatPhong) {
        String query =
                "SELECT DISTINCT hd.kieu_hoa_don," +
                " hd.tong_tien as tong_tien_dat_coc," +
                " (SELECT SUM(pddv.tong_tien) FROM PhongDungDichVu pddv where pddv.ma_chi_tiet_dat_phong = ?) AS tong_tien_dich_vu" +
                " FROM ChiTietDatPhong ctdp" +
                " JOIN DonDatPhong ddp ON ctdp.ma_don_dat_phong = ddp.ma_don_dat_phong" +
                " LEFT JOIN HoaDon hd ON ddp.ma_don_dat_phong = hd.ma_don_dat_phong" +
                " WHERE ctdp.ma_chi_tiet_dat_phong = ?"
                ;

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, maChiTietDatPhong);
            ps.setString(2, maChiTietDatPhong);

            var rs = ps.executeQuery();
            if (rs.next()) {
                String kieuHoaDon = rs.getString("kieu_hoa_don");
                BigDecimal tongTienDatCoc = rs.getBigDecimal("tong_tien_dat_coc");
                BigDecimal tongTienDichVu = rs.getBigDecimal("tong_tien_dich_vu");

                if (kieuHoaDon == null || !kieuHoaDon.equalsIgnoreCase(InvoiceType.DEPOSIT_INVOICE.getStatus())) {
                    tongTienDatCoc = BigDecimal.ZERO;
                }

                return new CustomerPayments(
                        tongTienDatCoc,
                        tongTienDichVu
                );
            }
            else
                return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public List<HoaDon> layDanhSachHoaDonTrongKhoang(Timestamp tgBatDau, Timestamp tgKetThuc, String maNhanVien){
        String sql = "select hd.*, nv.ten_nhan_vien from HoaDon hd \n" +
                    "left join PhienDangNhap pdn on pdn.ma_phien_dang_nhap = hd.ma_phien_dang_nhap\n" +
                    "left join TaiKhoan tk on pdn.ma_tai_khoan = tk.ma_tai_khoan\n" +
                    "left join NhanVien nv on nv.ma_nhan_vien = tk.ma_nhan_vien\n" +
                    "where (hd.thoi_gian_tao between ? and ?) and  (? IS NULL OR nv.ma_nhan_vien =?)\n" +
                    "order by ma_hoa_don";

        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        try {
            Connection connection = DatabaseUtil.getConnect();
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
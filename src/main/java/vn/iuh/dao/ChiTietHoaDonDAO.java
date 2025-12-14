package vn.iuh.dao;

import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChiTietHoaDonDAO {
    public boolean insert(ChiTietHoaDon chiTietHoaDon) {
        String sql = "INSERT INTO ChiTietHoaDon (ma_chi_tiet_hoa_don, thoi_gian_su_dung, ma_hoa_don, ma_chi_tiet_dat_phong, ma_phong, don_gia_phong) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, chiTietHoaDon.getMaChiTietHoaDon());
            ps.setDouble(2, chiTietHoaDon.getThoiGianSuDung());
            ps.setString(3, chiTietHoaDon.getMaHoaDon());
            ps.setString(4, chiTietHoaDon.getMaChiTietDatPhong());
            ps.setString(5, chiTietHoaDon.getMaPhong());
            ps.setBigDecimal(6, chiTietHoaDon.getDonGiaPhongHienTai());
            return ps.executeUpdate() > 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    public boolean themDanhSachChiTietHoaDon(List<ChiTietHoaDon> danhSachChiTietHoaDon){
        String sql = "INSERT INTO ChiTietHoaDon (ma_chi_tiet_hoa_don, thoi_gian_su_dung, ma_hoa_don, ma_chi_tiet_dat_phong, ma_phong, don_gia_phong, tong_tien) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {

            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            // Tắt auto-commit để thực hiện batch an toàn
            connection.setAutoCommit(false);

            for (ChiTietHoaDon cthd : danhSachChiTietHoaDon) {
                ps.setString(1, cthd.getMaChiTietHoaDon());
                ps.setDouble(2, cthd.getThoiGianSuDung());
                ps.setString(3, cthd.getMaHoaDon());
                ps.setString(4, cthd.getMaChiTietDatPhong());
                ps.setString(5, cthd.getMaPhong());
                ps.setBigDecimal(6, cthd.getDonGiaPhongHienTai());
                ps.setBigDecimal(7, cthd.getTongTien());

                ps.addBatch(); // thêm vào batch
            }

            int[] results = ps.executeBatch(); // thực thi batch
            connection.commit(); // xác nhận giao dịch

            // kiểm tra nếu có ít nhất 1 bản ghi được thêm
            return Arrays.stream(results).sum() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ChiTietHoaDon mapResultSet(ResultSet rs) throws SQLException {
        ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
        chiTietHoaDon.setMaChiTietHoaDon(rs.getString("ma_chi_tiet_hoa_don"));
        chiTietHoaDon.setThoiGianSuDung(rs.getDouble("thoi_gian_su_dung"));
        chiTietHoaDon.setMaHoaDon(rs.getString("ma_hoa_don"));
        chiTietHoaDon.setMaChiTietDatPhong(rs.getString("ma_chi_tiet_dat_phong"));
        chiTietHoaDon.setMaPhong(rs.getString("ma_phong"));
        chiTietHoaDon.setDonGiaPhongHienTai(rs.getBigDecimal("don_gia_phong"));
        chiTietHoaDon.setTongTien(rs.getBigDecimal("tong_tien"));
        return chiTietHoaDon;
    }

    public ChiTietHoaDon getById(String maChiTietHoaDon) {
        String sql = "SELECT * FROM ChiTietHoaDon WHERE ma_chi_tiet_hoa_don = ?";
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, maChiTietHoaDon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ChiTietHoaDon layChiTietHoaDonMoiNhat() {
        String sql = "SELECT TOP 1 *" +
                        "FROM ChiTietHoaDon " +
                        "Where da_xoa = 0 " +
                        "ORDER BY ma_chi_tiet_hoa_don DESC";
        try{
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<ChiTietHoaDon> layChiTietHoaDonBangMaHoaDon(String maHoaDon){
        String query = "select cthd.*, p.ten_phong from ChiTietHoaDon cthd join Phong p on cthd.ma_phong = p.ma_phong where cthd.ma_hoa_don = ?";

        List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maHoaDon);

            var rs = ps.executeQuery();
            while(rs.next()){
                ChiTietHoaDon cthd = mapResultSet(rs);
                cthd.setTenPhong(rs.getString("ten_phong"));
                danhSachChiTietHoaDon.add(cthd);
            }
        }catch(SQLException e) {
            throw new RuntimeException(e);
        }catch (TableEntityMismatch e){
            System.out.print(e.getMessage());
        }
        return danhSachChiTietHoaDon;
    }

    public List<ChiTietHoaDon> getInvoiceDetaiByInvoiceId(String maHoaDon){
        String query = "select * from ChiTietHoaDon where ma_hoa_don = ?";
        List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();
        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1,maHoaDon);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachChiTietHoaDon.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return danhSachChiTietHoaDon;
    }

}

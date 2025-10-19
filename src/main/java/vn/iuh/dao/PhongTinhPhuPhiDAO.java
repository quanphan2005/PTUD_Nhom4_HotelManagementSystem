package vn.iuh.dao;

import vn.iuh.entity.PhongTinhPhuPhi;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhongTinhPhuPhiDAO {
    private final Connection connection;

    public PhongTinhPhuPhiDAO() {
        connection = DatabaseUtil.getConnect();
    }

    public PhongTinhPhuPhiDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insert(PhongTinhPhuPhi ptpp){
        String query = "insert into PhongTinhPhuPhi(ma_phong_tinh_phu_phi, ma_chi_tiet_dat_phong, ma_phu_phi, don_gia_phu_phi) values (?, ? ,?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, ptpp.getMaPhongTinhPhuPhi());
            ps.setString(2, ptpp.getMaChiTietDatPhong());
            ps.setString(3, ptpp.getMaPhuPhi());
            ps.setDouble(4, ptpp.getDonGiaPhuPhi().doubleValue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean themDanhSachPhuPhiChoCacPhong(List<PhongTinhPhuPhi> danhSachPhongTinhPhuPhi){
        String query = "INSERT INTO PhongTinhPhuPhi (ma_phong_tinh_phu_phi, ma_chi_tiet_dat_phong, ma_phu_phi, don_gia_phu_phi, tong_tien) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (PhongTinhPhuPhi congViec : danhSachPhongTinhPhuPhi) {
                ps.setString(1, congViec.getMaPhongTinhPhuPhi());
                ps.setString(2, congViec.getMaChiTietDatPhong());
                ps.setString(3, congViec.getMaPhuPhi());
                ps.setBigDecimal(4, congViec.getDonGiaPhuPhi());
                ps.setBigDecimal(5, congViec.getTongTien());
                ps.addBatch();
            }
            return ps.executeBatch().length > 1;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm danh sách phòng tính phụ phí");
        }
    }

    public PhongTinhPhuPhi getLatest(){
        String query = "SELECT TOP 1 * " +
                        "FROM PhongTinhPhuPhi " +
                        "WHERE da_xoa = 0 " +
                        "ORDER BY ma_phong_tinh_phu_phi DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSet(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return null;
    }

    public List<PhongTinhPhuPhi> getPhuPhiTheoMaHoaDon(String maHoaDon){
        List<PhongTinhPhuPhi> danhSachPhuPhi = new ArrayList<>();

        String query  = "SELECT ptpp.*, pp.ten_phu_phi, p.ten_phong FROM PhongTinhPhuPhi ptpp " +
                "JOIN PhuPhi pp ON ptpp.ma_phu_phi = pp.ma_phu_phi " +
                "JOIN ChiTietDatPhong ctdp ON ptpp.ma_chi_tiet_dat_phong = ctdp.ma_chi_tiet_dat_phong " +
                "JOIN ChiTietHoaDon cthd ON ctdp.ma_chi_tiet_dat_phong = cthd.ma_chi_tiet_dat_phong " +
                "JOIN Phong p ON ctdp.ma_phong = p.ma_phong " +
                "WHERE cthd.ma_hoa_don = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, maHoaDon);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhongTinhPhuPhi phuPhi = mapResultSet(rs);
                    phuPhi.setTenPhuPhi(rs.getString("ten_phu_phi"));
                    phuPhi.setTenPhong(rs.getString("ten_phong"));
                    danhSachPhuPhi.add(phuPhi);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch e) {
            System.out.println(e.getMessage());
        }
        return danhSachPhuPhi;
    }

    private PhongTinhPhuPhi mapResultSet(ResultSet rs){
        PhongTinhPhuPhi phong = new PhongTinhPhuPhi();
        try {
            phong.setMaPhongTinhPhuPhi(rs.getString("ma_phong_tinh_phu_phi"));
            phong.setMaChiTietDatPhong(rs.getString("ma_chi_tiet_dat_phong"));
            phong.setMaPhuPhi(rs.getString("ma_phu_phi"));
            phong.setDonGiaPhuPhi(BigDecimal.valueOf(rs.getDouble("don_gia_phu_phi")));
            return phong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành Phong" + e.getMessage());
        }
    }

    public boolean daTonTai(String maChiTiet, String maPhuPhi) {
        String q = "SELECT COUNT(1) AS cnt FROM PhongTinhPhuPhi WHERE ma_chi_tiet_dat_phong = ? AND ma_phu_phi = ? AND da_xoa = 0";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, maChiTiet);
            ps.setString(2, maPhuPhi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}

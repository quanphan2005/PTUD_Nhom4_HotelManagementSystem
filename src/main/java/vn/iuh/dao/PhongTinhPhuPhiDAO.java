package vn.iuh.dao;

import vn.iuh.entity.PhongTinhPhuPhi;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String query = "INSERT INTO PhongTinhPhuPhi (ma_phong_tinh_phu_phi, ma_chi_tiet_dat_phong, ma_phu_phi, don_gia_phu_phi) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (PhongTinhPhuPhi congViec : danhSachPhongTinhPhuPhi) {
                ps.setString(1, congViec.getMaPhongTinhPhuPhi());
                ps.setString(2, congViec.getMaChiTietDatPhong());
                ps.setString(3, congViec.getMaPhuPhi());
                ps.setBigDecimal(4, congViec.getDonGiaPhuPhi());
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

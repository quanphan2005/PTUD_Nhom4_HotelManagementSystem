package vn.iuh.dao;

import vn.iuh.entity.BienBan;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BienBaoDAO {
    public BienBan timBienBan(String id) {
        String query = "SELECT * FROM BienBan WHERE ma_bien_ban = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            var rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhBienBan(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public BienBan themBienBan(BienBan bienBan) {
        String query = "INSERT INTO BienBan (ma_bien_ban, li_do, phi_bien_ban, ma_chi_tiet_dat_phong, ma_phien_dang_nhap) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, bienBan.getMaBienBan());
            ps.setString(2, bienBan.getLiDo());
            ps.setDouble(3, bienBan.getPhiBienBan());
            ps.setString(4, bienBan.getMaChiTietDatPhong());
            ps.setString(5, bienBan.getMaPhienDangNhap());

            ps.executeUpdate();
            return bienBan;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }



    public BienBan chuyenKetQuaThanhBienBan(ResultSet rs) throws SQLException {
        BienBan bienBan = new BienBan();
        try {
            bienBan.setMaBienBan(rs.getString("ma_bien_ban"));
            bienBan.setLiDo(rs.getString("li_do"));
            bienBan.setPhiBienBan(rs.getDouble("phi_bien_ban"));
            bienBan.setMaChiTietDatPhong(rs.getString("ma_chi_tiet_dat_phong"));
            bienBan.setMaPhienDangNhap(rs.getString("ma_phien_dang_nhap"));
            return bienBan;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành biên bản: " + e.getMessage());
        }
    }
}
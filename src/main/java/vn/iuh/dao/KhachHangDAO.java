package vn.iuh.dao;

import vn.iuh.entity.KhachHang;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KhachHangDAO {
    public KhachHang timKhachHang(String id) {
        String query = "SELECT * FROM KhachHang WHERE ma_khach_hang = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhKhachHang(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang timKhachHangBangCCCD(String cccd) {
        String query = "SELECT * FROM KhachHang WHERE CCCD = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, cccd);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhKhachHang(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang timKhachHangMoiNhat() {
        String query = "SELECT TOP 1 * FROM KhachHang WHERE da_xoa = 0 ORDER BY ma_khach_hang DESC";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhKhachHang(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public KhachHang themKhachHang(KhachHang khachHang) {
        String query = "INSERT INTO KhachHang (ma_khach_hang, CCCD, ten_khach_hang, so_dien_thoai) " +
                "VALUES (?, ?, ?, ?)";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, khachHang.getMaKhachHang());
            ps.setString(2, khachHang.getCCCD());
            ps.setString(3, khachHang.getTenKhachHang());
            ps.setString(4, khachHang.getSoDienThoai());


            ps.executeUpdate();
            return khachHang;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public KhachHang capNhatKhachHang(KhachHang khachHang) {
        String query = "UPDATE KhachHang SET CCCD = ? ,ten_khach_hang = ?, so_dien_thoai = ?" +
                "WHERE ma_khach_hang = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, khachHang.getCCCD());
            ps.setString(2, khachHang.getTenKhachHang());
            ps.setString(3, khachHang.getSoDienThoai());
            ps.setString(4, khachHang.getMaKhachHang());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timKhachHang(khachHang.getMaKhachHang());
            } else {
                System.out.println("Không tìm thấy khách hàng có mã: " + khachHang.getMaKhachHang());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaKhachHang(String id) {
        if (timKhachHang(id) == null) {
            System.out.println("Không tìm thấy khách hàng có mã: " + id);
            return false;
        }

        String query = "UPDATE KhachHang SET da_xoa = 1 WHERE ma_khach_hang = ?";

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa khách hàng thành công!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private KhachHang chuyenKetQuaThanhKhachHang(ResultSet rs) {
        KhachHang khachHang = new KhachHang();
        try {
            khachHang.setMaKhachHang(rs.getString("ma_khach_hang"));
            khachHang.setCCCD(rs.getString("CCCD"));
            khachHang.setTenKhachHang(rs.getString("ten_khach_hang"));
            khachHang.setSoDienThoai(rs.getString("so_dien_thoai"));
            return khachHang;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành KhachHang: " + e.getMessage());
        }
    }
}
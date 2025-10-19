package vn.iuh.dao;

import vn.iuh.entity.TaiKhoan;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {
    private final Connection connection;

    public TaiKhoanDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public TaiKhoanDAO(Connection connection) {
        this.connection = connection;
    }

    public TaiKhoan timTaiKhoanMoiNhat() {
        String query = "SELECT TOP 1 * FROM TaiKhoan WHERE da_xoa = 0 ORDER BY ma_tai_khoan DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if(rs.next()) {
                return chuyenKetQuaThanhTaiKhoan(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public TaiKhoan themTaiKhoan(TaiKhoan taiKhoan) {
        String query  = "INSERT INTO TaiKhoan (ma_tai_khoan, ten_dang_nhap, mat_khau, ma_chuc_vu, ma_nhan_vien)" +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, taiKhoan.getMaTaiKhoan());
            ps.setString(2, taiKhoan.getTenDangNhap());
            ps.setString(3, taiKhoan.getMatKhau());
            ps.setString(4, taiKhoan.getMaChucVu());
            ps.setString(5, taiKhoan.getMaNhanVien());

            ps.executeUpdate();
            return taiKhoan;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public TaiKhoan capNhatTaiKhoan(TaiKhoan taiKhoan) {
        String query = "UPDATE TaiKhoan SET ten_dang_nhap = ?, mat_khau = ?, ma_chuc_vu = ?, " +
                "ma_nhan_vien = ? WHERE ma_tai_khoan = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, taiKhoan.getTenDangNhap());
            ps.setString(2, taiKhoan.getMatKhau());
            ps.setString(3, taiKhoan.getMaChucVu());
            ps.setString(4, taiKhoan.getMaNhanVien());
            ps.setString(5, taiKhoan.getMaTaiKhoan());

            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                return timTaiKhoan(taiKhoan.getMaTaiKhoan());
            } else {
                System.out.println("Không tim thấy tài khoản có mã tài khoản: " + taiKhoan.getMaTaiKhoan());
                return null;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaTaiKhoan(String id) {
        if(timTaiKhoan(id) == null) {
            System.out.println("Không tìm thấy tài khoản có mã: " + id);
            return false;
        }

        String query = "UPDATE TaiKhoan SET da_xoa = 1 WHERE ma_tai_khoan = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                System.out.println("Tài khoản đã được xóa thành công!");
                return true;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public TaiKhoan timTaiKhoan(String accountID) {
        String query = "SELECT * FROM TaiKhoan WHERE ma_tai_khoan = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, accountID);

            var rs = ps.executeQuery();
            if(rs.next())
                return chuyenKetQuaThanhTaiKhoan(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public TaiKhoan timTaiKhoanBangUserName(String userName) {
        String query = "SELECT * FROM TaiKhoan WHERE ten_dang_nhap = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, userName);

            var rs = ps.executeQuery();
            if(rs.next())
                return chuyenKetQuaThanhTaiKhoan(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public List<TaiKhoan> getAllTaiKhoan() {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT ma_tai_khoan, ten_dang_nhap, ma_chuc_vu, " +
                "ma_nhan_vien FROM TaiKhoan";

        try {
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TaiKhoan tk = new TaiKhoan();
                tk.setMaTaiKhoan(rs.getString("ma_tai_khoan"));
                tk.setTenDangNhap(rs.getString("ten_dang_nhap"));
                tk.setMaChucVu(rs.getString("ma_chuc_vu"));
                tk.setMaNhanVien(rs.getString("ma_nhan_vien"));
                list.add(tk);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }



    public TaiKhoan chuyenKetQuaThanhTaiKhoan(ResultSet rs) throws SQLException {
        TaiKhoan taiKhoan = new TaiKhoan();
        try {
            taiKhoan.setMaTaiKhoan(rs.getString("ma_tai_khoan"));
            taiKhoan.setTenDangNhap(rs.getString("ten_dang_nhap"));
            taiKhoan.setMatKhau(rs.getString("mat_khau"));
            taiKhoan.setMaChucVu(rs.getString("ma_chuc_vu"));
            taiKhoan.setMaNhanVien(rs.getString("ma_nhan_vien"));
            taiKhoan.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return taiKhoan;
        } catch(SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành TaiKhoan: " + e);
        }
    }
}

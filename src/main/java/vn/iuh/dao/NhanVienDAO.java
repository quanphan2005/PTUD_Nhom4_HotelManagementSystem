package vn.iuh.dao;

import vn.iuh.dto.repository.ThongTinNhanVien;
import vn.iuh.entity.NhanVien;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    private final Connection connection;

    public NhanVienDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public NhanVienDAO(Connection connection) {
        this.connection = connection;
    }

    public NhanVien timNhanVien(String id) {
        String query = "SELECT * FROM NhanVien WHERE ma_nhan_vien = ? AND da_xoa = 0";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhNhanVien(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public List<NhanVien> timNhanVienBangTen(String tenNhanVien){
        String query = "select * from NhanVien where ten_nhan_vien = ? and da_xoa = 0";
        List<NhanVien> nhanVienList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, tenNhanVien);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nhanVienList.add(chuyenKetQuaThanhNhanVien(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return nhanVienList;
    }

    public NhanVien timNhanVienBangSDT(String sdt){
        String query = "select * from NhanVien where so_dien_thoai = ? and da_xoa = 0";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, sdt);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhNhanVien(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public List<NhanVien> dsNhanVienChuaCoTaiKhoan() {
        String query = "select * from NhanVien nv left join TaiKhoan tk on nv.ma_nhan_vien = tk.ma_nhan_vien where tk.ma_tai_khoan is null";
        List<NhanVien> dsNhanVienChuaCoTaiKhoan = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dsNhanVienChuaCoTaiKhoan.add(chuyenKetQuaThanhNhanVien(rs));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return dsNhanVienChuaCoTaiKhoan;
    }

    public List<NhanVien> layDanhSachNhanVien(){
        String query = "SELECT * FROM NhanVien WHERE da_xoa = 0";
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachNhanVien.add(chuyenKetQuaThanhNhanVien(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return danhSachNhanVien;
    }

    public NhanVien themNhanVien(NhanVien nhanVien) {
        String query = "INSERT INTO NhanVien (ma_nhan_vien, ten_nhan_vien, CCCD, ngay_sinh, so_dien_thoai) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nhanVien.getMaNhanVien());
            ps.setString(2, nhanVien.getTenNhanVien());
            ps.setString(3, nhanVien.getCCCD());
            ps.setDate(4, new java.sql.Date(nhanVien.getNgaySinh().getTime()));
            ps.setString(5, nhanVien.getSoDienThoai());

            ps.executeUpdate();
            return nhanVien;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public NhanVien capNhatNhanVien(NhanVien nhanVien) {
        String query = "UPDATE NhanVien SET ten_nhan_vien = ?, CCCD = ?, ngay_sinh = ?, so_dien_thoai = ?" +
                "WHERE ma_nhan_vien = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nhanVien.getTenNhanVien());
            ps.setString(2, nhanVien.getCCCD());
            ps.setDate(3, new java.sql.Date(nhanVien.getNgaySinh().getTime()));
            ps.setString(4, nhanVien.getSoDienThoai());
            ps.setString(5, nhanVien.getMaNhanVien());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timNhanVien(nhanVien.getMaNhanVien());
            } else {
                System.out.println("Không tìm thấy nhân viên có mã: " + nhanVien.getMaNhanVien());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }



    public boolean xoaNhanVien(String id) {
        if (timNhanVien(id) == null) {
            System.out.println("Không tìm thấy nhân viên có mã: " + id);
            return false;
        }

        String query = "UPDATE NhanVien SET da_xoa = 1 WHERE ma_nhan_vien = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa nhân viên thành công!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public NhanVien timNhanVienBangCCCD(String cccd) {
        String query = "SELECT * FROM NhanVien WHERE CCCD = ? AND da_xoa = 0";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, cccd);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhNhanVien(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public NhanVien timNhanVienMoiNhat() {
        String query = "SELECT TOP 1 * FROM NhanVien WHERE da_xoa = 0 ORDER BY ma_nhan_vien DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhNhanVien(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    private NhanVien chuyenKetQuaThanhNhanVien(ResultSet rs) {
        NhanVien nhanVien = new NhanVien();
        try {
            nhanVien.setMaNhanVien(rs.getString("ma_nhan_vien"));
            nhanVien.setTenNhanVien(rs.getString("ten_nhan_vien"));
            nhanVien.setCCCD(rs.getString("CCCD"));
            nhanVien.setNgaySinh(rs.getTimestamp("ngay_sinh"));
            nhanVien.setSoDienThoai(rs.getString("so_dien_thoai"));
            return nhanVien;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành nhân viên: " + e.getMessage());
        }
    }

    public ThongTinNhanVien layThongTinNV(String maNhanVien) {
        String query = "select nv.ten_nhan_vien, nv.CCCD, nv.ngay_sinh, nv.so_dien_thoai, tk.ma_chuc_vu from NhanVien nv join TaiKhoan tk"
                + " on nv.ma_nhan_vien = tk.ma_nhan_vien where tk.ma_nhan_vien = ?" ;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, maNhanVien);
            var rs = ps.executeQuery();

            if(rs.next()) {
                ThongTinNhanVien ttnv = new ThongTinNhanVien();

                ttnv.setTenNhanVien(rs.getString("ten_nhan_vien"));
                ttnv.setCCCD(rs.getString("CCCD"));
                ttnv.setSoDienThoai(rs.getString("so_dien_thoai"));
                ttnv.setNgaySinh(rs.getTimestamp("ngay_sinh"));
                ttnv.setChucVu(rs.getString("ma_chuc_vu"));

                return ttnv;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public NhanVien layNVTheoMaPhienDangNhap(String maPhienDangNhap){
        String sql = "select nv.* from PhienDangNhap pdn \n" +
                "left join TaiKhoan tk on pdn.ma_tai_khoan = tk.ma_tai_khoan\n" +
                "left join NhanVien nv on nv.ma_nhan_vien = tk.ma_nhan_vien\n" +
                "where pdn.ma_phien_dang_nhap = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, maPhienDangNhap);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhNhanVien(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }
        return null;
    }
}
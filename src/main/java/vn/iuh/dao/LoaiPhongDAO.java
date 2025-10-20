package vn.iuh.dao;

import com.github.lgooddatepicker.zinternaltools.Pair;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaiPhongDAO {
    private final Connection connection;

    public LoaiPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public LoaiPhongDAO(Connection connection) {
        this.connection = connection;
    }

    public LoaiPhong getRoomCategoryByID(String id) {
        String query = "SELECT * FROM LoaiPhong WHERE ma_loai_phong = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhLoaiPhong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public List<LoaiPhong> layTatCaLoaiPhong() {
        String query = "SELECT * FROM LoaiPhong WHERE da_xoa = 0";
        List<LoaiPhong> danhSachLoaiPhong = new java.util.ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachLoaiPhong.add(chuyenKetQuaThanhLoaiPhong(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return danhSachLoaiPhong;
    }

    public LoaiPhong themLoaiPhong(LoaiPhong loaiPhong) {
        String query = "INSERT INTO LoaiPhong (ma_loai_phong, ten_loai_phong, so_luong_khach, phan_loai) " +
                "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiPhong.getMaLoaiPhong());
            ps.setString(2, loaiPhong.getTenLoaiPhong());
            ps.setInt(3, loaiPhong.getSoLuongKhach());
            ps.setString(4, loaiPhong.getPhanLoai());

            ps.executeUpdate();
            return loaiPhong;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public LoaiPhong capNhatLoaiPhong(LoaiPhong loaiPhong) {
        if (getRoomCategoryByID(loaiPhong.getMaLoaiPhong()) == null) {
            System.out.println("No room category found with ID: " + loaiPhong.getMaLoaiPhong());
            return null;
        }

        String query = "UPDATE LoaiPhong SET ten_loai_phong = ?, so_luong_khach = ?, phan_loai = ? " +
                " WHERE ma_loai_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiPhong.getTenLoaiPhong());
            ps.setInt(2, loaiPhong.getSoLuongKhach());
            ps.setString(3, loaiPhong.getPhanLoai());
            ps.setString(4, loaiPhong.getMaLoaiPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return getRoomCategoryByID(loaiPhong.getMaLoaiPhong());
            } else {
                System.out.println("No room category found with ID: " + loaiPhong.getMaLoaiPhong());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaLoaiPhong(String id) {
        if (getRoomCategoryByID(id) == null) {
            System.out.println("Không tìm thấy loại phòng, mã: " + id);
            return false;
        }

        String query = "UPDATE LoaiPhong SET da_xoa = 1 WHERE ma_loai_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa loại phòng thành công, mã: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public LoaiPhong timLoaiPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM LoaiPhong WHERE da_xoa = 0 ORDER BY ma_loai_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhLoaiPhong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    private LoaiPhong chuyenKetQuaThanhLoaiPhong(ResultSet rs) throws SQLException {
        LoaiPhong loaiPhong = new LoaiPhong();
        try {
            loaiPhong.setMaLoaiPhong(rs.getString("ma_loai_phong"));
            loaiPhong.setTenLoaiPhong(rs.getString("ten_loai_phong"));
            loaiPhong.setSoLuongKhach(rs.getInt("so_luong_khach"));
            loaiPhong.setPhanLoai(rs.getString("phan_loai"));
            loaiPhong.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return loaiPhong;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành LoaiPhong" + e.getMessage());
        }
    }

    public Map<String, Double> layGiaLoaiPhongTheoId(String loaiPhongId) {
        String query = "select gia_gio_moi as gia_gio, gia_ngay_moi as gia_ngay from GiaPhong gp\n" +
                       "where\tgp.ma_loai_phong = ?\n" +
                       "order by thoi_gian_tao desc\n";
        Map<String, Double> listPrice = new HashMap<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, loaiPhongId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                listPrice.put("gia_ngay", rs.getDouble("gia_ngay"));
                listPrice.put("gia_gio", rs.getDouble("gia_gio"));
            }

            return listPrice;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }
        return null;
    }

    public Map<String, BigDecimal> layGiaLoaiPhongTheoMaPhong(String maPhong) {
        String query = "select top 1  gp.gia_ngay_moi as gia_ngay , gp.gia_gio_moi as gia_gio from Phong p\n" +
                        "left join LoaiPhong lp on lp.ma_loai_phong = p.ma_loai_phong\n" +
                        "left join GiaPhong gp on lp.ma_loai_phong = gp.ma_loai_phong\n" +
                        "where gp.da_xoa = 0 and ma_phong = ? \n" +
                        "order by gp.thoi_gian_tao desc";
        Map<String, BigDecimal> listPrice = new HashMap<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                listPrice.put("gia_ngay", rs.getBigDecimal("gia_ngay"));
                listPrice.put("gia_gio", rs.getBigDecimal("gia_gio"));
            }

            return listPrice;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }
        return null;
    }

}


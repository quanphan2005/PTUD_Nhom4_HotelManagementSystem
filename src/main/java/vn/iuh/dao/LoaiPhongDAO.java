package vn.iuh.dao;

import com.github.lgooddatepicker.zinternaltools.Pair;
import vn.iuh.constraint.PriceType;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.gui.panel.statistic.RoomCategoryStatistic;
import vn.iuh.gui.panel.statistic.RoomStatistic;
import vn.iuh.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaiPhongDAO {
    public LoaiPhong getRoomCategoryByID(String id) {
        String query = "SELECT * FROM LoaiPhong WHERE ma_loai_phong = ? AND da_xoa = 0";

        try {
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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

    public List<RoomStatistic> layThongKeTheoLoaiPhong(Timestamp startTime, Timestamp endTime) {
        String query = "select lp.ma_loai_phong, lp.ten_loai_phong, p.ma_phong, p.ten_phong,count(ctdp.ma_chi_tiet_dat_phong) as so_luot_dat,sum(cthd.thoi_gian_su_dung) as thoi_gian_dat ,sum(cthd.tong_tien) as doanh_thu from LoaiPhong lp\n" +
                "left join Phong p on p.ma_loai_phong = lp.ma_loai_phong\n" +
                "left join ChiTietDatPhong ctdp on ctdp.ma_phong  = p.ma_phong\n" +
                "left join ChiTietHoaDon cthd on ctdp.ma_chi_tiet_dat_phong = cthd.ma_chi_tiet_dat_phong\n" +
                "where cthd.thoi_gian_tao between ? and ? " +
                "group by lp.ma_loai_phong, lp.ten_loai_phong, p.ma_phong, p.ten_phong\n" +
                "order by thoi_gian_dat desc, doanh_thu desc, so_luot_dat desc";
        List<RoomStatistic> danhSachPhong = new java.util.ArrayList<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setTimestamp(1, startTime);
            ps.setTimestamp(2, endTime);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                danhSachPhong.add(new RoomStatistic(
                        rs.getString("ma_loai_phong"),
                        rs.getString("ten_loai_phong"),
                        rs.getString(("ma_phong")),
                        rs.getString("ten_phong"),
                        rs.getInt("so_luot_dat"),
                        rs.getDouble("thoi_gian_dat"),
                        rs.getBigDecimal("doanh_thu")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return danhSachPhong;
    }

    public LoaiPhong themLoaiPhong(LoaiPhong loaiPhong) {
        String query = "INSERT INTO LoaiPhong (ma_loai_phong, ten_loai_phong, so_luong_khach, phan_loai) " +
                "VALUES (?, ?, ?, ?)";

        try {
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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
            Connection connection = DatabaseUtil.getConnect();
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

    public Map<PriceType, BigDecimal> layGiaLoaiPhongTheoMaPhong(String maPhong) {
        String query = "select top 1  gp.gia_ngay_moi as gia_ngay , gp.gia_gio_moi as gia_gio from Phong p\n" +
                        "left join LoaiPhong lp on lp.ma_loai_phong = p.ma_loai_phong\n" +
                        "left join GiaPhong gp on lp.ma_loai_phong = gp.ma_loai_phong\n" +
                        "where gp.da_xoa = 0 and ma_phong = ? \n" +
                        "order by gp.thoi_gian_tao desc";
        Map<PriceType, BigDecimal> listPrice = new HashMap<>();

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                listPrice.put(PriceType.GIA_NGAY, rs.getBigDecimal("gia_ngay"));
                listPrice.put(PriceType.GIA_GIO, rs.getBigDecimal("gia_gio"));
            }

            return listPrice;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }
        return null;
    }

    public LoaiPhong timLoaiPhongMoiNhatBaoGomDaXoa() {
        String query = "SELECT TOP 1 * FROM LoaiPhong ORDER BY ma_loai_phong DESC";
        try {
            Connection connection = DatabaseUtil.getConnect();
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

    /**
     * Chèn LoaiPhong mới trực tiếp (không dùng themLoaiPhong).
     * Không commit/rollback ở đây — caller có thể quản lý transaction nếu cần.
     */
    public LoaiPhong insertLoaiPhong(LoaiPhong loaiPhong) {
        if (loaiPhong == null) throw new IllegalArgumentException("loaiPhong null");

        String sql = "INSERT INTO LoaiPhong (ma_loai_phong, ten_loai_phong, so_luong_khach, phan_loai, thoi_gian_tao, da_xoa) " +
                "VALUES (?, ?, ?, ?, ?, 0)";

        Timestamp now = loaiPhong.getThoiGianTao() != null ? loaiPhong.getThoiGianTao() : new Timestamp(System.currentTimeMillis());

        try {
            Connection connection = DatabaseUtil.getConnect();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, loaiPhong.getMaLoaiPhong());
            ps.setString(2, loaiPhong.getTenLoaiPhong());
            ps.setInt(3, loaiPhong.getSoLuongKhach());
            ps.setString(4, loaiPhong.getPhanLoai());
            ps.setTimestamp(5, now);

            int rows = ps.executeUpdate();
            if (rows == 1) {
                loaiPhong.setThoiGianTao(now);
                return loaiPhong;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("LoaiPhongDAO.insertLoaiPhong lỗi: " + e.getMessage(), e);
        }
    }

}


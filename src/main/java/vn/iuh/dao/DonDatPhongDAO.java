package vn.iuh.dao;

import vn.iuh.entity.DonDatPhong;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class DonDatPhongDAO {
    private final Connection connection;

    public DonDatPhongDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public DonDatPhongDAO(Connection connection) {
        this.connection = connection;
    }

    public DonDatPhong timDonDatPhong(String id) {
        String query = "SELECT * FROM DonDatPhong WHERE ma_don_dat_phong = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhDonDatPhong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public boolean themDonDatPhong(DonDatPhong donDatPhongEntity) {
        String query = "INSERT INTO DonDatPhong" +
                       " (ma_don_dat_phong, mo_ta, tg_nhan_phong, tg_tra_phong, tong_tien_du_tinh, tien_dat_coc" +
                       ", da_dat_truoc, ma_khach_hang, ma_phien_dang_nhap)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, donDatPhongEntity.getMaDonDatPhong());
            ps.setString(2, donDatPhongEntity.getMoTa());
            ps.setTimestamp(3, donDatPhongEntity.getTgNhanPhong());
            ps.setTimestamp(4, donDatPhongEntity.getTgTraPhong());
            ps.setDouble(5, donDatPhongEntity.getTongTienDuTinh());
            ps.setDouble(6, donDatPhongEntity.getTienDatCoc());
            ps.setBoolean(7, donDatPhongEntity.getDaDatTruoc());
            ps.setString(8, donDatPhongEntity.getMaKhachHang());
            ps.setString(9, donDatPhongEntity.getMaPhienDangNhap());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi thêm đơn đặt phòng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public DonDatPhong capNhatDonDatPhong(DonDatPhong donDatPhong) {
        if (timDonDatPhong(donDatPhong.getMaDonDatPhong()) == null) {
            System.out.println("Không tìm thấy đơn đặt phòng, mã: " + donDatPhong.getMaDonDatPhong());
            return null;
        }

        String query = "UPDATE DonDatPhong SET mo_ta = ?, tg_nhan_phong = ?, tg_tra_phong = ?, tong_tien_du_tinh = ?," +
                       " tien_dat_coc = ?, da_dat_truoc = ?, ma_khach_hang = ?, ma_phien_dang_nhap = ? " +
                       "WHERE ma_don_dat_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, donDatPhong.getMoTa());
            ps.setTimestamp(2, donDatPhong.getTgNhanPhong());
            ps.setTimestamp(3, donDatPhong.getTgTraPhong());
            ps.setDouble(4, donDatPhong.getTongTienDuTinh());
            ps.setDouble(5, donDatPhong.getTienDatCoc());
            ps.setBoolean(6, donDatPhong.getDaDatTruoc());
            ps.setString(7, donDatPhong.getMaKhachHang());
            ps.setString(8, donDatPhong.getMaPhienDangNhap());
            ps.setString(9, donDatPhong.getMaDonDatPhong());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cập nhật đơn đặt phòng thành công, mã: " + donDatPhong.getMaDonDatPhong());
                return timDonDatPhong(donDatPhong.getMaDonDatPhong());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaDonDatPhong(String id) {
        if (timDonDatPhong(id) == null) {
            System.out.println("Không tìm thấy đơn đặt phòng, mã: " + id);
            return false;
        }

        String query = "UPDATE DonDatPhong SET da_xoa = 1 WHERE ma_don_dat_phong = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa đơn đặt phòng thành công, mã: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public DonDatPhong timDonDatPhongMoiNhat() {
        String query = "SELECT TOP 1 * FROM DonDatPhong WHERE da_xoa = 0 ORDER BY ma_don_dat_phong DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhDonDatPhong(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    private DonDatPhong chuyenKetQuaThanhDonDatPhong(ResultSet rs) {
        try {
            return new DonDatPhong(
                    rs.getString("ma_don_dat_phong"),
                    rs.getString("mo_ta"),
                    rs.getTimestamp("tg_nhan_phong"),
                    rs.getTimestamp("tg_tra_phong"),
                    rs.getDouble("tong_tien_du_tinh"),
                    rs.getDouble("tien_dat_coc"),
                    rs.getBoolean("da_dat_truoc"),
                    rs.getString("ma_khach_hang"),
                    rs.getString("ma_phien_dang_nhap"),
                    rs.getString("thoi_gian_tao")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển ResultSet thành DonDatPhong" + e.getMessage());
        }
    }
}

package vn.iuh.dao;

import vn.iuh.entity.DichVu;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;
import java.util.List;

public class DichVuDAO {
    private final Connection connection;

    public DichVuDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public DichVuDAO(Connection connection) {
        this.connection = connection;
    }

    public DichVu timDichVu(String id) {
        String query = "SELECT * FROM DichVu WHERE ma_dich_vu = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhDichVu(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        System.out.println("Không tìm thấy dịch vụ, mã: " + id);
        return null;
    }

    public List<DichVu> timTatCaDichVu() {
        String query = "SELECT * FROM DichVu WHERE da_xoa = 0";
        List<DichVu> dichVus = new java.util.ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dichVus.add(chuyenKetQuaThanhDichVu(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return dichVus;
    }

    public DichVu createServiceItem(DichVu dichVu) {
        String query = "INSERT INTO DichVu " +
                "(ma_dich_vu, ten_dich_vu, ton_kho, co_the_tang, ma_loai_dich_vu) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, dichVu.getMaDichVu());
            ps.setString(2, dichVu.getTenDichVu());
            ps.setInt(3, dichVu.getTonKho());
            ps.setBoolean(4, dichVu.getCoTheTang());
            ps.setString(5, dichVu.getMaLoaiDichVu());

            ps.executeUpdate();
            return dichVu;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public DichVu updateServiceItem(DichVu dichVu) {
        if (timDichVu(dichVu.getMaDichVu()) == null) {
            System.out.println("Không tìm thấy dịch vụ, mã: " + dichVu.getMaDichVu());
            return null;
        }

        String query = "UPDATE DichVu SET ten_dich_vu = ?, ton_kho = ?, co_the_tang = ?, ma_loai_dich_vu = ?" +
                "WHERE ma_dich_vu = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, dichVu.getTenDichVu());
            ps.setInt(2, dichVu.getTonKho());
            ps.setBoolean(3, dichVu.getCoTheTang());
            ps.setString(4, dichVu.getMaLoaiDichVu());
            ps.setString(5, dichVu.getMaDichVu());


            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timDichVu(dichVu.getMaDichVu());
            } else {
                System.out.println("No service item found with ID: " + dichVu.getMaDichVu());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteServiceItemByID(String id) {
        if (timDichVu(id) == null) {
            System.out.println("Không tìm thấy dịch vụ, mã: " + id);
            return false;
        }

        String query = "UPDATE DichVu SET da_xoa = 1 WHERE ma_dich_vu = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xóa dịch vụ thành công!");
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public DichVu timDichVuMoiNhat() {
        String query = "SELECT TOP 1 * FROM DichVu ORDER BY ma_dich_vu DESC WHERE da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhDichVu(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public List<DichVu> timDanhSachDichVuBangDanhSachMa(List<String> serviceIds) {
        List<DichVu> dichVus = new java.util.ArrayList<>();
        if (serviceIds == null || serviceIds.isEmpty()) {
            return dichVus;
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM DichVu WHERE da_xoa = 0 AND ma_dich_vu IN (");
        for (int i = 0; i < serviceIds.size(); i++) {
            queryBuilder.append("?");
            if (i < serviceIds.size() - 1) {
                queryBuilder.append(", ");
            }
        }
        queryBuilder.append(")");

        try {
            PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < serviceIds.size(); i++) {
                ps.setString(i + 1, serviceIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dichVus.add(chuyenKetQuaThanhDichVu(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return dichVus;
    }

    private DichVu chuyenKetQuaThanhDichVu(ResultSet rs) throws SQLException {
        DichVu dichVu = new DichVu();
        try {
            dichVu.setMaDichVu(rs.getString("ma_dich_vu"));
            dichVu.setTenDichVu(rs.getString("ten_dich_vu"));
            dichVu.setTonKho(rs.getInt("ton_kho"));
            dichVu.setCoTheTang(rs.getBoolean("co_the_tang"));
            dichVu.setMaLoaiDichVu(rs.getString("ma_loai_dich_vu"));
            dichVu.setThoiGianTao(rs.getTimestamp("thoi_gian_tao"));
            return dichVu;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Không thể chuyển kết quả thành DichVu: " + e.getMessage());
        }
    }
}

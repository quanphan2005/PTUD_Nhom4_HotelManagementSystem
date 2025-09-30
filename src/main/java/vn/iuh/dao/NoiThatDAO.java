package vn.iuh.dao;

import vn.iuh.entity.NoiThat;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NoiThatDAO {
    private final Connection connection;

    public NoiThatDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public NoiThatDAO(Connection connection) {
        this.connection = connection;
    }

    public NoiThat themNoiThat(NoiThat noiThat) {
        String query = "INSERT INTO NoiThat (ma_noi_that, ten_noi_that, mo_ta) VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, noiThat.getMaNoiThat());
            ps.setString(2, noiThat.getTenNoiThat());
            ps.setString(3, noiThat.getMoTa());

            ps.executeUpdate();
            return noiThat;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public NoiThat capNhatNoiThat(NoiThat noiThat) {
        String query = "UPDATE NoiThat SET ten_noi_that = ?, mo_ta = ? WHERE ma_noi_that = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, noiThat.getTenNoiThat());
            ps.setString(2, noiThat.getMoTa());
            ps.setString(3, noiThat.getMaNoiThat());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return timNoiThat(noiThat.getMaNoiThat());
            } else {
                System.out.println("Không tìm thấy nội thất với mã: " + noiThat.getMaNoiThat());
                return null;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean xoaNoiThat(String id) {
        if (timNoiThat(id) == null) {
            System.out.println("No FurnitureItem found with ID: " + id);
            return false;
        }

        String query = "UPDATE NoiThat SET da_xoa = 1 WHERE ma_noi_that = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Xoa nội thất thành công với mã: " + id);
                return true;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public NoiThat timNoiThat(String id) {
        String query = "SELECT * FROM NoiThat WHERE ma_noi_that = ? AND da_xoa = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            var rs = ps.executeQuery();
            if (rs.next())
                return chuyenKetQuaThanhNoiThat(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public NoiThat timNoiThatMoiNhat() {
        String query = "SELECT TOP 1 * FROM NoiThat WHERE da_xoa = 0 ORDER BY ma_noi_that DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return chuyenKetQuaThanhNoiThat(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public NoiThat chuyenKetQuaThanhNoiThat(ResultSet rs) throws SQLException {
        NoiThat item = new NoiThat();
        try {
            item.setMaNoiThat(rs.getString("ma_noi_that"));
            item.setTenNoiThat(rs.getString("ten_noi_that"));
            item.setMoTa(rs.getString("mo_ta"));
            return item;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Lỗi chuyển kết quả thành NoiThat" + e.getMessage());
        }
    }

}
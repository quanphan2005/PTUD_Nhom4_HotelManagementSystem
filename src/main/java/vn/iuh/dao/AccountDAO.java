package vn.iuh.dao;

import vn.iuh.entity.TaiKhoan;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {
    private final Connection connection;

    public AccountDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public AccountDAO(Connection connection) {
        this.connection = connection;
    }

    public TaiKhoan findLastAccount() {
        String query = "select TOP 1 * from account where is_deleted = false order by id desc";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if(rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public TaiKhoan createAccount(TaiKhoan taiKhoan) {
        String query  = "INSERT INTO Account (id, userName, user_password, user_role, employee_id, is_deleted)" +
                "VALUES (?, ?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, taiKhoan.getMa_tai_khoan());
            ps.setString(2, taiKhoan.getTen_dang_nhap());
            ps.setString(3, taiKhoan.getMat_khau());
            ps.setString(4, taiKhoan.getMa_chuc_vu());
            ps.setString(5, taiKhoan.getMa_nhan_vien());

            ps.executeUpdate();
            return taiKhoan;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public TaiKhoan updateAccount(TaiKhoan taiKhoan) {
        String query = "update Account set userName = ?, user_password = ?, user_role = ?, " +
                "employee_id = ? where id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, taiKhoan.getTen_dang_nhap());
            ps.setString(2, taiKhoan.getMat_khau());
            ps.setString(3, taiKhoan.getMa_chuc_vu());
            ps.setString(4, taiKhoan.getMa_nhan_vien());
            ps.setString(5, taiKhoan.getMa_tai_khoan());

            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                return getAccountByID(taiKhoan.getMa_tai_khoan());
            } else {
                System.out.println("No account found with ID: " + taiKhoan.getMa_tai_khoan());
                return null;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public boolean deleteAccountByID(String id) {
        if(getAccountByID(id) == null) {
            System.out.println("No account found with ID: " + id);
            return false;
        }

        String query = "UPDATE Account SET is_deleted = true WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                System.out.println("Account has been deleted successfully");
                return true;
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public TaiKhoan getAccountByID(String accountID) {
        String query = "SELECT * FROM Account WHERE ID = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, accountID);

            var rs = ps.executeQuery();
            if(rs.next())
                return mapResultSetToAccount(rs);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public TaiKhoan mapResultSetToAccount(ResultSet rs) throws SQLException {
        TaiKhoan taiKhoan = new TaiKhoan();
        try {
            taiKhoan.setMa_tai_khoan(rs.getString("id"));
            taiKhoan.setTen_dang_nhap(rs.getString("userName"));
            taiKhoan.setMat_khau(rs.getString("user_password"));
            taiKhoan.setMa_chuc_vu(rs.getString("user_role"));
            taiKhoan.setMa_nhan_vien(rs.getString("employee_id"));
            return taiKhoan;
        } catch(SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Account" + e);
        }
    }
}

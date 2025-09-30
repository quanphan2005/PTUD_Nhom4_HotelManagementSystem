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

    public TaiKhoan updateAccount(TaiKhoan taiKhoan) {
        String query = "update Account set userName = ?, user_password = ?, user_role = ?, " +
                "employee_id = ? where id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, taiKhoan.getTenDangNhap());
            ps.setString(2, taiKhoan.getMatKhau());
            ps.setString(3, taiKhoan.getMaChucVu());
            ps.setString(4, taiKhoan.getMaNhanVien());
            ps.setString(5, taiKhoan.getMaTaiKhoan());

            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                return getAccountByID(taiKhoan.getMaTaiKhoan());
            } else {
                System.out.println("No account found with ID: " + taiKhoan.getMaTaiKhoan());
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
            taiKhoan.setMaTaiKhoan(rs.getString("id"));
            taiKhoan.setTenDangNhap(rs.getString("userName"));
            taiKhoan.setMatKhau(rs.getString("user_password"));
            taiKhoan.setMaChucVu(rs.getString("user_role"));
            taiKhoan.setMaNhanVien(rs.getString("employee_id"));
            return taiKhoan;
        } catch(SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Account" + e);
        }
    }
}

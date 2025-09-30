package vn.iuh.dao;

import vn.iuh.entity.Account;
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

    public Account findLastAccount() {
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

    public Account createAccount(Account account) {
        String query  = "INSERT INTO Account (id, userName, user_password, user_role, employee_id, is_deleted)" +
                "VALUES (?, ?, ?, ?, ?, false)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, account.getId());
            ps.setString(2, account.getUserName());
            ps.setString(3, account.getUserPassword());
            ps.setString(4, account.getUserRole());
            ps.setString(5, account.getEmployeeId());

            ps.executeUpdate();
            return account;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Account updateAccount(Account account) {
        String query = "update Account set userName = ?, user_password = ?, user_role = ?, " +
                "employee_id = ? where id = ? AND is_deleted = false";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, account.getUserName());
            ps.setString(2, account.getUserPassword());
            ps.setString(3, account.getUserRole());
            ps.setString(4, account.getEmployeeId());
            ps.setString(5, account.getId());

            int rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0) {
                return getAccountByID(account.getId());
            } else {
                System.out.println("No account found with ID: " + account.getId());
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

    public Account getAccountByID(String accountID) {
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

    public Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        try {
            account.setId(rs.getString("id"));
            account.setUserName(rs.getString("userName"));
            account.setUserPassword(rs.getString("user_password"));
            account.setUserRole(rs.getString("user_role"));
            account.setEmployeeId(rs.getString("employee_id"));
            return account;
        } catch(SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to Account" + e);
        }
    }
}

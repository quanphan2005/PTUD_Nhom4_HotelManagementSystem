package vn.iuh.dao;

import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.*;

public class WorkingHistoryDAO {
    private final Connection connection;

    public WorkingHistoryDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public WorkingHistoryDAO(Connection connection) {
        this.connection = connection;
    }

    public LichSuThaoTac getWorkingHistoryByID(String id) {
        String query = "SELECT * FROM WorkingHistory WHERE id = ? AND is_deleted = 0";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToWorkingHistory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch te) {
            System.out.println(te.getMessage());
        }

        return null;
    }

    public void insertWorkingHistory(LichSuThaoTac wh) {
        String query = "INSERT INTO WorkingHistory (id, task_name, create_time, action_description, shift_assignment_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, wh.getMaLichSuThaoTac());
            ps.setString(2, wh.getTenThaoTac());
            ps.setTimestamp(3, wh.getThoiGianTao() != null ? new Timestamp(wh.getThoiGianTao().getTime()) : null);
            ps.setString(4, wh.getMoTa());
            ps.setString(5, wh.getMaPhienDangNhap());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public LichSuThaoTac findLastWorkingHistory() {
        String query = "SELECT TOP 1 * FROM WorkingHistory ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToWorkingHistory(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    private LichSuThaoTac mapResultSetToWorkingHistory(ResultSet rs) throws SQLException {
        LichSuThaoTac wh = new LichSuThaoTac();
        try {
            wh.setMaLichSuThaoTac(rs.getString("id"));
            wh.setTenThaoTac(rs.getString("task_name"));
            wh.setThoiGianTao(rs.getTimestamp("create_time"));
            wh.setMoTa(rs.getString("action_description"));
            wh.setMaPhienDangNhap(rs.getString("shift_assignment_id"));
            return wh;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to WorkingHistory: " + e);
        }
    }
}


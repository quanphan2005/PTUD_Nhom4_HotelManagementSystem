package vn.iuh.dao;

import vn.iuh.entity.WorkingHistory;
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

    public WorkingHistory getWorkingHistoryByID(String id) {
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

    public void insertWorkingHistory(WorkingHistory wh) {
        String query = "INSERT INTO WorkingHistory (id, task_name, create_time, action_description, shift_assignment_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, wh.getId());
            ps.setString(2, wh.getTaskName());
            ps.setTimestamp(3, wh.getCreateTime() != null ? new Timestamp(wh.getCreateTime().getTime()) : null);
            ps.setString(4, wh.getActionDescription());
            ps.setString(5, wh.getShiftAssignmentId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public WorkingHistory findLastWorkingHistory() {
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

    private WorkingHistory mapResultSetToWorkingHistory(ResultSet rs) throws SQLException {
        WorkingHistory wh = new WorkingHistory();
        try {
            wh.setId(rs.getString("id"));
            wh.setTaskName(rs.getString("task_name"));
            wh.setCreateTime(rs.getTimestamp("create_time"));
            wh.setActionDescription(rs.getString("action_description"));
            wh.setShiftAssignmentId(rs.getString("shift_assignment_id"));
            return wh;
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can't map ResultSet to WorkingHistory: " + e);
        }
    }
}


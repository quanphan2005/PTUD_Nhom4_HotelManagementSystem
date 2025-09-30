package vn.iuh.dao;

import vn.iuh.entity.Job;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JobDAO {
    private final Connection connection;

    public JobDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public JobDAO(Connection connection) {
        this.connection = connection;
    }

    public Job findLastJob() {
        String query = "SELECT TOP 1 * FROM Job ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToJob(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch et) {
            System.out.println(et.getMessage());
        }

        return null;
    }

    public void insertJobs(List<Job> jobs) {
        String query = "INSERT INTO Job (id, start_time, end_time, status_name, room_id) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            for (Job job : jobs) {
                ps.setString(1, job.getId());
                ps.setTimestamp(2, job.getStartTime());
                ps.setTimestamp(3, job.getEndTime());
                ps.setString(4, job.getStatusName());
                ps.setString(5, job.getRoomdId());

                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Job mapResultSetToJob(ResultSet rs) throws SQLException, TableEntityMismatch {
        try {
            String id = rs.getString("id");
            String statusName = rs.getString("status_name");
            java.sql.Timestamp startTime = rs.getTimestamp("start_time");
            java.sql.Timestamp endTime = rs.getTimestamp("end_time");
            String roomId = rs.getString("room_id");

            return new Job(id, startTime, endTime, statusName, roomId);
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can not map ResultSet to Job entity" + e.getMessage());
        }
    }


}

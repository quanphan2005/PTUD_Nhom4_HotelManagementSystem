package vn.iuh.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;

public class RoomStatusHandler implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Connection connection = DatabaseUtil.getNewConnect();

        try {
            GridRoomPanel gridRoomPanel = (GridRoomPanel) context.getMergedJobDataMap().get("gridRoomPanel");
//            updateRoomStatusCheckInToUsage(gridRoomPanel, connection);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    private void updateRoomStatusCheckInToUsage(GridRoomPanel gridRoomPanel, Connection connection) {
//        String query = "SELECT r.id FROM Room r" +
//                       " JOIN RoomReservationDetail rrd ON r.id = rrd.room_id" +
//                       " WHERE room_status = N'ĐANG KIỂM TRA'" +
//                       " AND rrd.end_type IS NULL" +
//                       " AND DATEDIFF(MINUTE , rrd.time_in, GETDATE()) > 30;";
//
//        List<String> roomIds = new ArrayList<>();
//        String newStatus = RoomStatus.ROOM_USING_STATUS.getStatus();
//        try {
//            PreparedStatement ps = connection.prepareStatement(query);
//
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                roomIds.add(rs.getString("id"));
//            }
//
//            if (roomIds.isEmpty()) return;
//
//            String updateQuery = "UPDATE Room SET room_status = ? WHERE id IN (?)";
//            ps = connction.prepareStatement(updateQuery);
//            ps.setString(1, newStatus);
//            ps.setString(2, String.join(",", roomIds));
//
//            int rowsAffected = ps.executeUpdate();
//            System.out.println("Updated " + rowsAffected + " rooms from 'ĐANG KIỂM TRA' to 'ĐANG SỬ DỤNG'.");
//
//            gridRoomPanel.updateRoomItemStatus(roomIds, newStatus);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}

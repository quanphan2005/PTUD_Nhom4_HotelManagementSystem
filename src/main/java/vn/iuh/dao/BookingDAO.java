package vn.iuh.dao;

import vn.iuh.constraint.RoomStatus;
import vn.iuh.dto.event.create.RoomFilter;
import vn.iuh.dto.repository.BookingInfo;
import vn.iuh.dto.repository.RoomInfo;
import vn.iuh.entity.HistoryCheckIn;
import vn.iuh.entity.ReservationForm;
import vn.iuh.entity.RoomReservationDetail;
import vn.iuh.entity.RoomUsageService;
import vn.iuh.exception.TableEntityMismatch;
import vn.iuh.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    private final Connection connection;

    public BookingDAO() {
        this.connection = DatabaseUtil.getConnect();
    }

    public void beginTransaction() {
        DatabaseUtil.enableTransaction(connection);
    }

    public void commitTransaction() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.commit();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi commit transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public void rollbackTransaction() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi rollback transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public List<RoomInfo> findAllEmptyRooms() {
        String query = "SELECT r.id, r.room_name, r.is_active, rc.room_type, rc.number_customer" +
                       ", rlp.updated_daily_price, rlp.updated_hourly_price" +
                       " FROM Room r" +
                       " JOIN RoomCategory rc ON rc.id = r.room_category_id" +
                       " JOIN RoomListPrice rlp ON rlp.room_category_id = rc.id" +
                       " WHERE r.is_active = ? AND r.is_deleted = 0" +
                       " ORDER BY rlp.create_at DESC, r.id ASC";
        List<RoomInfo> rooms = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, RoomStatus.ROOM_AVAILABLE_STATUS.getStatus());
            var rs = ps.executeQuery();
            while (rs.next())
                rooms.add(mapResultSetToRoomInfo(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return rooms;
    }

    public List<RoomInfo> findRoomsByFilter(RoomFilter roomFilter) {
        String query = "SELECT r.id, r.room_name, r.is_active, rc.room_type, rc.number_customer" +
                       ", rlp.updated_daily_price, rlp.updated_hourly_price" +
                       " FROM Room r" +
                       " JOIN RoomCategory rc ON rc.id = r.room_category_id" +
                       " JOIN RoomListPrice rlp ON rlp.room_category_id = rc.id" +
                       " WHERE r.is_active = ? AND r.is_deleted = 0" +
                       " ORDER BY rlp.create_at DESC, r.id ASC";
        List<RoomInfo> rooms = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, RoomStatus.ROOM_AVAILABLE_STATUS.getStatus());
            var rs = ps.executeQuery();
            while (rs.next())
                rooms.add(mapResultSetToRoomInfo(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return rooms;
    }

    public boolean insertReservationForm(ReservationForm reservationFormEntity) {
        String query = "INSERT INTO ReservationForm" +
                       " (id, reserve_date, note, check_in_date, check_out_date, initial_price" +
                       ", deposit_price, is_advanced, customer_id, shift_assignment_id)" +
                       " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, reservationFormEntity.getId());
            ps.setTimestamp(2, reservationFormEntity.getReserveDate());
            ps.setString(3, reservationFormEntity.getNote());
            ps.setTimestamp(4, reservationFormEntity.getCheckInDate());
            ps.setTimestamp(5, reservationFormEntity.getCheckOutDate());
            ps.setDouble(6, reservationFormEntity.getInitialPrice());
            ps.setDouble(7, reservationFormEntity.getDepositPrice());
            ps.setBoolean(8, reservationFormEntity.getIsAdvanced());
            ps.setString(9, reservationFormEntity.getCustomerId());
            ps.setString(10, reservationFormEntity.getShiftAssignmentId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi insert ReservationForm: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean insertRoomReservationDetail(ReservationForm reservationFormEntity,
                                               List<RoomReservationDetail> roomReservationDetails) {
        String query = "INSERT INTO RoomReservationDetail" +
                       " (id, time_in, time_out, reservation_form_id, room_id, shift_assignment_id)" +
                       " VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (RoomReservationDetail roomReservationDetail : roomReservationDetails) {
                System.out.println(roomReservationDetail.getRoomId());

                ps.setString(1, roomReservationDetail.getId());
                ps.setTimestamp(2, roomReservationDetail.getTimeIn());
                ps.setTimestamp(3, roomReservationDetail.getTimeOut());
                ps.setString(4, reservationFormEntity.getId());
                ps.setString(5, roomReservationDetail.getRoomId());
                ps.setString(6, reservationFormEntity.getShiftAssignmentId());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == roomReservationDetails.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean insertRoomUsageService(ReservationForm reservationFormEntity,
                                          List<RoomUsageService> roomUsageServices) {
        String query = "INSERT INTO RoomUsageService" +
                       " (id, quantity, total_price, order_time, service_item_id, room_detail_id)" +
                       " VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (RoomUsageService roomUsageService : roomUsageServices) {
                ps.setString(1, roomUsageService.getId());
                ps.setInt(2, roomUsageService.getQuantity());
                ps.setDouble(3, roomUsageService.getTotalPrice());
                ps.setDate(4, roomUsageService.getOrderTime());
                ps.setString(5, roomUsageService.getServiceItemId());
                ps.setString(6, reservationFormEntity.getId());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == roomUsageServices.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean insertHistoryCheckIn(ReservationForm reservationForm, List<HistoryCheckIn> historyCheckIns) {
        String query = "INSERT INTO HistoryCheckIn" +
                       " (id, check_in_time, is_first, room_reservation_detail_id)" +
                       " VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            for (HistoryCheckIn historyCheckIn : historyCheckIns) {
                ps.setString(1, historyCheckIn.getId());
                ps.setTimestamp(2, historyCheckIn.getCheckInTime());
                ps.setBoolean(3, historyCheckIn.getIsFirst());
                ps.setString(4, historyCheckIn.getRoomReservationDetailId());

                ps.addBatch();
            }

            int[] rowsAffected = ps.executeBatch();
            return rowsAffected.length == historyCheckIns.size();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RoomReservationDetail> findRoomReservationDetailByReservationFormID(String reservationFormId) {
        String query = "SELECT * FROM RoomReservationDetail WHERE reservation_form_id = ?";
        List<RoomReservationDetail> roomReservationDetails = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, reservationFormId);

            var rs = ps.executeQuery();
            if (rs.next())
                roomReservationDetails.add(mapResultSetToRoomReservationDetail(rs));
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }

        return roomReservationDetails;
    }

    public List<RoomInfo> findAllRoomInfo() {
        String query = "SELECT r.id, r.room_name, r.is_active, j.status_name, rc.room_type, rc.number_customer" +
                       ", rlp.updated_daily_price, rlp.updated_hourly_price" +
                       " FROM Room r" +
                       " LEFT JOIN Job j ON j.room_id = r.id" +
                       " JOIN RoomCategory rc ON rc.id = r.room_category_id" +
                       " JOIN RoomListPrice rlp ON rlp.room_category_id = rc.id" +
                       " WHERE r.is_deleted = 0" +
                       " ORDER BY rlp.create_at DESC, r.id ASC";
        List<RoomInfo> rooms = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            while (rs.next())
                rooms.add(mapResultSetToRoomInfo(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return rooms;
    }

    public List<BookingInfo> findAllBookingInfo(List<String> nonAvailableRoomIds) {
        if (nonAvailableRoomIds.isEmpty())
            return new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT r.id, c.customer_name, rrd.time_in, rrd.time_out" +
                " FROM Room r" +
                " JOIN RoomReservationDetail rrd ON r.id = rrd.room_id" +
                " JOIN ReservationForm rf ON rf.id = rrd.reservation_form_id" +
                " JOIN Customer c ON c.id = rf.customer_id" +
                " WHERE r.id IN (");

        for (int i = 0; i < nonAvailableRoomIds.size(); i++) {
            query.append("?");
            if (i < nonAvailableRoomIds.size() - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        List<BookingInfo> bookings = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(query.toString());

            for (int i = 0; i < nonAvailableRoomIds.size(); i++) {
                ps.setString(i + 1, nonAvailableRoomIds.get(i));
            }

            var rs = ps.executeQuery();

            while (rs.next())
                bookings.add(mapResultSetToBookingInfo(rs));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
        }

        return bookings;
    }

    public ReservationForm findLastReservationForm() {
        String query = "SELECT TOP 1 * FROM ReservationForm ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToReservationForm(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public RoomReservationDetail findLastRoomReservationDetail() {
        String query = "SELECT TOP 1 * FROM RoomReservationDetail ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoomReservationDetail(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public HistoryCheckIn findLastHistoryCheckIn() {
        String query = "SELECT TOP 1 * FROM HistoryCheckIn ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToHistoryCheckIn(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RoomUsageService findLastRoomUsageService() {
        String query = "SELECT TOP 1 * FROM RoomUsageService ORDER BY id DESC";

        try {
            PreparedStatement ps = connection.prepareStatement(query);

            var rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToRoomUsageService(rs);
            else
                return null;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (TableEntityMismatch mismatchException) {
            System.out.println(mismatchException.getMessage());
            return null;
        }
    }

    public boolean updateRoomStatus(String roomId, String newStatus) {
        String query = "UPDATE Room SET is_active = ? WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, newStatus);
            ps.setString(2, roomId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private RoomInfo mapResultSetToRoomInfo(ResultSet rs) {
        try {
            return new RoomInfo(
                    rs.getString("id"),
                    rs.getString("room_name"),
                    rs.getBoolean("is_active"),
                    rs.getString("status_name"),
                    rs.getString("room_type"),
                    rs.getString("number_customer"),
                    rs.getDouble("updated_daily_price"),
                    rs.getDouble("updated_hourly_price")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to RoomInfo" + e.getMessage());
        }
    }

    private BookingInfo mapResultSetToBookingInfo(ResultSet rs) throws SQLException {
        BookingInfo bookingInfo = new BookingInfo(
                rs.getString("id"),
                rs.getString("customer_name"),
                rs.getTimestamp("time_in"),
                rs.getTimestamp("time_out")
        );

        System.out.println(bookingInfo.getTimeIn());
        return bookingInfo;
    }

    private ReservationForm mapResultSetToReservationForm(ResultSet rs) {
        try {
            return new ReservationForm(
                    rs.getString("id"),
                    rs.getTimestamp("reserve_date"),
                    rs.getString("note"),
                    rs.getTimestamp("check_in_date"),
                    rs.getTimestamp("check_out_date"),
                    rs.getDouble("initial_price"),
                    rs.getDouble("deposit_price"),
                    rs.getBoolean("is_advanced"),
                    rs.getString("customer_id"),
                    rs.getString("shift_assignment_id")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to ReservationForm" + e.getMessage());
        }
    }

    private RoomReservationDetail mapResultSetToRoomReservationDetail(ResultSet rs) {
        try {
            return new RoomReservationDetail(
                    rs.getString("id"),
                    rs.getTimestamp("time_out"),
                    rs.getTimestamp("time_in"),
                    rs.getString("end_type"),
                    rs.getString("reservation_form_id"),
                    rs.getString("room_id"),
                    rs.getString("shift_assignment_id")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to RoomReservationDetail" + e.getMessage());
        }
    }

    private HistoryCheckIn mapResultSetToHistoryCheckIn(ResultSet rs) {
        try {
            return new HistoryCheckIn(
                    rs.getString("id"),
                    rs.getTimestamp("check_in_time"),
                    rs.getBoolean("is_first"),
                    rs.getString("room_reservation_detail_id")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to HistoryCheckIn" + e.getMessage());
        }
    }

    private RoomUsageService mapResultSetToRoomUsageService(ResultSet rs) {
        try {
            return new RoomUsageService(
                    rs.getString("id"),
                    rs.getDouble("total_price"),
                    rs.getInt("quantity"),
                    rs.getDate("order_time"),
                    rs.getString("reservation_form_id"),
                    rs.getString("service_item_id"),
                    rs.getString("shift_assignment_id")
            );
        } catch (SQLException e) {
            throw new TableEntityMismatch("Can`t map ResultSet to RoomUsageService" + e.getMessage());
        }
    }
}
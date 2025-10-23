package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ReservationDetailRepository {
    private String ReservationDetailId;
    private String roomId;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;

    public ReservationDetailRepository() {
    }

    public ReservationDetailRepository(String reservationDetailId, String roomId, String roomName,
                                       Timestamp timeIn, Timestamp timeOut) {
        ReservationDetailId = reservationDetailId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public String getReservationDetailId() {
        return ReservationDetailId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Timestamp getTimeIn() {
        return timeIn;
    }

    public Timestamp getTimeOut() {
        return timeOut;
    }
}

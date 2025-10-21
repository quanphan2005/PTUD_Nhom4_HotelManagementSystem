package vn.iuh.dto.response;

import java.sql.Time;
import java.sql.Timestamp;

public class ReservationDetailResponse {
    private String ReservationDetailId;
    private String roomId;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;

    public ReservationDetailResponse() {
    }

    public ReservationDetailResponse(String reservationDetailId, String roomId, String roomName,
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

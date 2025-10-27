package vn.iuh.dto.response;

import java.sql.Time;
import java.sql.Timestamp;

public class ReservationDetailResponse {
    private String ReservationDetailId;
    private String roomId;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private String status;

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

    public ReservationDetailResponse(String reservationDetailId, String roomId, String roomName, Timestamp timeIn,
                                     Timestamp timeOut, String status) {
        ReservationDetailId = reservationDetailId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
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

    public String getStatus() {
        return status;
    }
}

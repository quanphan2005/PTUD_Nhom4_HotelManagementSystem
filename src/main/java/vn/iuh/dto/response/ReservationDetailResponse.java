package vn.iuh.dto.response;

import java.sql.Timestamp;

public class ReservationDetailResponse {
    private String reservationDetailId;
    private String reservationId;
    private String roomId;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private String status;

    public ReservationDetailResponse() {
    }

    public ReservationDetailResponse(String reservationDetailId, String reservationId, String roomId, String roomName, Timestamp timeIn,
                                     Timestamp timeOut, String status) {
        this.reservationDetailId = reservationDetailId;
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }

    public String getReservationDetailId() {
        return reservationDetailId;
    }

    public String getReservationId() {
        return reservationId;
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

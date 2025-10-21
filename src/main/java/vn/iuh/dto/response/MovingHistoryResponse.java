package vn.iuh.dto.response;

import java.sql.Timestamp;

public class MovingHistoryResponse {
    private String reservationDetailId;
    private String roomId;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private String note;

    public MovingHistoryResponse() {
    }

    public MovingHistoryResponse(String reservationDetailId, String roomId, String roomName, Timestamp timeIn,
                                 Timestamp timeOut, String note) {
        this.reservationDetailId = reservationDetailId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.note = note;
    }

    public String getReservationDetailId() {
        return reservationDetailId;
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

    public String getNote() {
        return note;
    }
}

package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ReservationDetailRepository {
    private String ReservationDetailId;
    private String roomId;
    private String roomName;
    private String endType;
    private Boolean isCheckin;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private Boolean isDeleted;

    public ReservationDetailRepository(String reservationDetailId, String roomId, String roomName, String endType,
                                       Boolean isCheckin, Timestamp timeIn, Timestamp timeOut, Boolean isDeleted) {
        ReservationDetailId = reservationDetailId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.endType = endType;
        this.isCheckin = isCheckin;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.isDeleted = isDeleted;
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

    public Boolean isCheckin() {
        return isCheckin;
    }

    public String getEndType() {
        return endType;
    }

    public Timestamp getTimeIn() {
        return timeIn;
    }

    public Timestamp getTimeOut() {
        return timeOut;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }
}

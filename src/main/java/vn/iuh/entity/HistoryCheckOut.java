package vn.iuh.entity;

import java.sql.Date;

public class HistoryCheckOut {
    private String id;
    private Date checkOutTime;
    private boolean isFinal;
    private String roomReservationDetailId;

    public HistoryCheckOut() {
    }

    public HistoryCheckOut(String id, Date checkOutTime, boolean isFinal, String roomReservationDetailId) {
        this.id = id;
        this.checkOutTime = checkOutTime;
        this.isFinal = isFinal;
        this.roomReservationDetailId = roomReservationDetailId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public String getRoomReservationDetailId() {
        return roomReservationDetailId;
    }

    public void setRoomReservationDetailId(String roomReservationDetailId) {
        this.roomReservationDetailId = roomReservationDetailId;
    }
}

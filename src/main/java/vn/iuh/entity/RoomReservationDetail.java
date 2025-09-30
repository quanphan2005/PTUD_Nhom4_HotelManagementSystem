package vn.iuh.entity;

import java.sql.Timestamp;

public class RoomReservationDetail {
    private String id;
    private Timestamp timeOut;
    private Timestamp timeIn;
    private String endType;
    private String reservationFormId;
    private String roomId;
    private String shiftAssignmentId;

    public RoomReservationDetail() {
    }

    public RoomReservationDetail(String id, Timestamp timeOut, Timestamp timeIn, String endType, String reservationFormId,
                                 String roomId, String shiftAssignmentId) {
        this.id = id;
        this.timeOut = timeOut;
        this.timeIn = timeIn;
        this.endType = endType;
        this.reservationFormId = reservationFormId;
        this.roomId = roomId;
        this.shiftAssignmentId = shiftAssignmentId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Timestamp timeOut) {
        this.timeOut = timeOut;
    }

    public Timestamp getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(Timestamp timeIn) {
        this.timeIn = timeIn;
    }

    public String getEndType() {
        return endType;
    }

    public void setEndType(String endType) {
        this.endType = endType;
    }

    public String getReservationFormId() {
        return reservationFormId;
    }

    public void setReservationFormId(String reservationFormId) {
        this.reservationFormId = reservationFormId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public void setShiftAssignmentId(String shiftAssignmentId) {
        this.shiftAssignmentId = shiftAssignmentId;
    }
}

package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class BookingInfo {
    private final String roomId;
    private final String customerName;
    private final Timestamp timeIn;
    private final Timestamp timeOut;

    public BookingInfo(String roomId, String customerName, Timestamp timeIn, Timestamp timeOut) {
        this.roomId = roomId;
        this.customerName = customerName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    @Override
    public String toString() {
        return "BookingInfo{" +
               "roomId='" + roomId + '\'' +
               ", customerName='" + customerName + '\'' +
               ", timeIn=" + timeIn +
               ", timeOut=" + timeOut +
               '}';
    }

    public String getRoomId() {
        return roomId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Timestamp getTimeIn() {
        return timeIn;
    }

    public Timestamp getTimeOut() {
        return timeOut;
    }
}

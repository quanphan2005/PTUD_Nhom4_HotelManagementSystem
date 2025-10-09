package vn.iuh.dto.response;

import java.sql.Timestamp;
import java.util.Objects;

public class BookingResponse {
    private String roomId;
    private String roomName;
    private boolean isActive;
    private String roomStatus;
    private String roomType;
    private String numberOfCustomers;
    private double dailyPrice;
    private double hourlyPrice;
    private String customerName;
    private String maChiTietDatPhong;
    private Timestamp timeIn;
    private Timestamp timeOut;

    public BookingResponse(String roomId, String roomName, boolean isActive, String roomStatus, String roomType, String numberOfCustomers,
                           double dailyPrice, double hourlyPrice) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.isActive = isActive;
        this.roomStatus = roomStatus;
        this.roomType = roomType;
        this.numberOfCustomers = numberOfCustomers;
        this.dailyPrice = dailyPrice;
        this.hourlyPrice = hourlyPrice;
    }

    public BookingResponse(String roomId, String roomName, boolean isActive, String roomStatus, String roomType,
                           String numberOfCustomers, double dailyPrice, double hourlyPrice, String customerName,
                           String maChiTietDatPhong, Timestamp timeIn, Timestamp timeOut) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.isActive = isActive;
        this.roomStatus = roomStatus;
        this.roomType = roomType;
        this.numberOfCustomers = numberOfCustomers;
        this.dailyPrice = dailyPrice;
        this.hourlyPrice = hourlyPrice;
        this.customerName = customerName;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public BookingResponse(BookingResponse bookingResponse) {
        this.roomId = bookingResponse.roomId;
        this.roomName = bookingResponse.roomName;
        this.isActive = bookingResponse.isActive;
        this.roomStatus = bookingResponse.roomStatus;
        this.roomType = bookingResponse.roomType;
        this.numberOfCustomers = bookingResponse.numberOfCustomers;
        this.dailyPrice = bookingResponse.dailyPrice;
        this.hourlyPrice = bookingResponse.hourlyPrice;
        this.customerName = bookingResponse.customerName;
        this.maChiTietDatPhong = bookingResponse.maChiTietDatPhong;
        this.timeIn = bookingResponse.timeIn;
        this.timeOut = bookingResponse.timeOut;
    }

    @Override
    public String toString() {
        return "BookingResponse{" +
               "roomId='" + roomId + '\'' +
               ", roomName='" + roomName + '\'' +
               ", roomStatus='" + roomStatus + '\'' +
               ", roomType='" + roomType + '\'' +
               ", numberOfCustomers='" + numberOfCustomers + '\'' +
               ", dailyPrice=" + dailyPrice +
               ", hourlyPrice=" + hourlyPrice +
               ", customerName='" + customerName + '\'' +
               ", timeIn=" + timeIn +
               ", timeOut=" + timeOut +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof String) {
            return roomId.equals(o);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, roomName, roomStatus, roomType, numberOfCustomers, dailyPrice, hourlyPrice,
                            customerName, timeIn, timeOut);
    }

    public void updateBookingInfo(String customerName, String maChiTietDatPhong, Timestamp timeIn, Timestamp timeOut) {
        this.customerName = customerName;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getNumberOfCustomers() {
        return numberOfCustomers;
    }

    public double getDailyPrice() {
        return dailyPrice;
    }

    public double getHourlyPrice() {
        return hourlyPrice;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public Timestamp getTimeIn() {
        return timeIn;
    }

    public Timestamp getTimeOut() {
        return timeOut;
    }
}

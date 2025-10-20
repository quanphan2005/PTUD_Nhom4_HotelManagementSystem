package vn.iuh.dto.response;

import java.sql.Timestamp;

public class ReservationResponse {
    private String CCCD;
    private String customerName;
    private String maDonDatPhong;
    private String roomId;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private String status;

    public ReservationResponse() {
    }

    public ReservationResponse(String CCCD, String customerName, String maDonDatPhong, String roomId, String roomName,
                               Timestamp timeIn, Timestamp timeOut, String status) {
        this.CCCD = CCCD;
        this.customerName = customerName;
        this.maDonDatPhong = maDonDatPhong;
        this.roomId = roomId;
        this.roomName = roomName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
    }

    public String getCCCD() {
        return CCCD;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
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

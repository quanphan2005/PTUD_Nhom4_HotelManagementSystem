package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class DonDatPhong {
    private String customerName;
    private String maDonDatPhong;
    private String roomName;
    private Timestamp timeIn;
    private Timestamp timeOut;

    public DonDatPhong(String customerName, String maDonDatPhong, String roomName, Timestamp timeIn,
                       Timestamp timeOut) {
        this.customerName = customerName;
        this.maDonDatPhong = maDonDatPhong;
        this.roomName = roomName;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
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

package vn.iuh.dto.response;

import java.sql.Timestamp;

public class ReservationResponse {
    private String CCCD;
    private String customerName;
    private String maKhachHang;
    private String maDonDatPhong;
    private String type;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private String status;
    private boolean isDeleted;

    public ReservationResponse() {
    }

    public ReservationResponse(String CCCD, String customerName, String maDonDatPhong, String type,
                               Timestamp timeIn, Timestamp timeOut, String status, boolean isDeleted) {
        this.CCCD = CCCD;
        this.customerName = customerName;
        this.maDonDatPhong = maDonDatPhong;
        this.type = type;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.status = status;
        this.isDeleted = isDeleted;
    }

    public String getCCCD() {
        return CCCD;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public String getType() {
        return type;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public void setMaDonDatPhong(String maDonDatPhong) {
        this.maDonDatPhong = maDonDatPhong;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimeIn(Timestamp timeIn) {
        this.timeIn = timeIn;
    }

    public void setTimeOut(Timestamp timeOut) {
        this.timeOut = timeOut;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}

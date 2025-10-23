package vn.iuh.dto.response;

import java.sql.Timestamp;

public class ReservationResponse {
    private String CCCD;
    private String customerName;
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
}

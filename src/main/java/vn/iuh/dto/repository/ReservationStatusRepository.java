package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ReservationStatusRepository {
    private String maDonDatPhong;
    private Boolean checkin;
    private Timestamp checkinDate;
    private Timestamp checkoutTime;

    public ReservationStatusRepository() {
    }

    public ReservationStatusRepository(String maDonDatPhong, Boolean isCheckin, Timestamp checkinDate,
                                       Timestamp checkoutTime) {
        this.maDonDatPhong = maDonDatPhong;
        this.checkin = isCheckin;
        this.checkinDate = checkinDate;
        this.checkoutTime = checkoutTime;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public Boolean isCheckin() {
        return checkin;
    }

    public Timestamp getCheckinDate() {
        return checkinDate;
    }

    public Timestamp getCheckoutTime() {
        return checkoutTime;
    }
}

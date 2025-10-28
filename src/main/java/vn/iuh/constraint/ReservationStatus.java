package vn.iuh.constraint;

public enum ReservationStatus {
    CHECKED_IN("CHỜ NHẬN PHÒNG"),
    NO_CHECKIN("KHÔNG NHẬN PHÒNG"),
    CHECKING("ĐANG NHẬN PHÒNG"),
    USING("ĐANG SỬ DỤNG"),
    TRANSFERRED("ĐÃ CHUYỂN PHÒNG"),
    CHECKOUT_LATE("CHECKOUT TRỄ"),
    COMPLETED("ĐÃ HOÀN THÀNH"),
    CANCELLED("ĐÃ HỦY")
    ;

    public final String status;

    public String getStatus() {
        return status;
    }

    public static ReservationStatus fromStatus(String reservationStatus) {
        for (ReservationStatus status : ReservationStatus.values()) {
            if (status.status.equalsIgnoreCase(reservationStatus)) {
                return status;
            }
        }
        return null;
    }

    ReservationStatus(String status) {
        this.status = status;
    }
}

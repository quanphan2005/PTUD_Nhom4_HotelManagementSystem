package vn.iuh.constraint;

public enum ReservationStatus {
    USING("ĐANG SỬ DỤNG"),
    CHECKED_IN("CHỜ NHẬN PHÒNG"),
    COMPLETED("ĐÃ HOÀN THÀNH"),
    CANCELLED("ĐÃ HỦY")
    ;

    public final String status;

    public String getStatus() {
        return status;
    }

    ReservationStatus(String status) {
        this.status = status;
    }
}

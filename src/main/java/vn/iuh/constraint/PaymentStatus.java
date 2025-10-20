package vn.iuh.constraint;

public enum PaymentStatus {
    PAID("ĐÃ THANH TOÁN"),
    UNPAID("CHƯA THANH TOÁN"),
    PARTIALLY_PAID("ĐÃ THANH TOÁN MỘT PHẦN"),
    REFUNDED("ĐÃ HOÀN TIỀN"),
    ;

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

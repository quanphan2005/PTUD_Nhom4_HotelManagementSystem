package vn.iuh.constraint;

public enum PaymentMethod {
    CASH("TIỀN MẶT"),
    BANK_TRANSFER("CHUYỂN KHOẢN"),
    ;

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}

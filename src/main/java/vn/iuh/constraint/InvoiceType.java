package vn.iuh.constraint;

public enum InvoiceType {
    THANH_TOAN("THANH TOÁN"),
    ;

    private  String status;
    InvoiceType(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

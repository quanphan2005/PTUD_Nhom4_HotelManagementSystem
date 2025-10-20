package vn.iuh.constraint;

public enum InvoiceType {
    PAYMENT_INVOICE("THANH TOÁN"),
    DEPOSIT_INVOICE("ĐẶT CỌC"),
    REFUND_INVOICE("HOÀN TIỀN")
    ;

    private  String status;
    InvoiceType(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

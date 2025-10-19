package vn.iuh.constraint;

public enum EntityIDSymbol {
    ROOM_PREFIX("PH", 8),
    RESERVATION_FORM_PREFIX("DP",8),
    ROOM_RESERVATION_DETAIL_PREFIX("CP",8),
    HISTORY_CHECKIN_PREFIX("LV",8),
    ROOM_USAGE_SERVICE_PREFIX("PV",8),
    ACCOUNT_PREFIX("TK", 8),
    AdditionalFee_PREFIX("PP", 8),
    CUSTOMER_PREFIX("KH", 8),
    JOB_PREFIX("CV", 8),
    WORKING_HISTORY_PREFIX("LT", 8),
    HISTORY_CHECKOUT_PREFIX("LN", 8),
    INVOICE_DETAIL_PREFIX("CD", 8),
    INVOICE_PREFIX("HD", 8),
    ROOM_FEE("PP", 8),
    LOGIN_SESSION("PN", 8),
    NOTIFICATION_PREFIX("TB", 8);
//................... Other here


    public String prefix;
    public int numberLength;

    EntityIDSymbol(String prefix, int numberLength) {
        this.prefix = prefix;
        this.numberLength = numberLength;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getLength() {
        return numberLength;
    }
}

package vn.iuh.constraint;

public enum EntityIDSymbol {
    ROOM_PREFIX("RO", 8),
    RESERVATION_FORM_PREFIX("RF",8),
    ROOM_RESERVATION_DETAIL_PREFIX("RD",8),
    HISTORY_CHECKIN_PREFIX("HI",8),
    ROOM_USAGE_SERVICE_PREFIX("RS",8),
    ACCOUNT_PREFIX("AC", 8),
    AdditionalFee_PREFIX("AF", 8),
    CUSTOMER_PREFIX("CU", 8),
    JOB_PREFIX("JO", 8),
    WORKING_HISTORY_PREFIX("WH", 8);
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

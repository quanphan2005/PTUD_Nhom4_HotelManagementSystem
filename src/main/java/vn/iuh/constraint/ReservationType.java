package vn.iuh.constraint;

public enum ReservationType {
    SINGLE("ĐẶT ĐƠN"),
    MULTI("ĐẶT NHIỀU")
    ;

    public String type;

    ReservationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}

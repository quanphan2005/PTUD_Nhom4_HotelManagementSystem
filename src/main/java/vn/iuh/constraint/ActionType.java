package vn.iuh.constraint;

public enum ActionType {
    BOOKING("ĐẶT PHÒNG"),
    PRE_BOOKING("ĐẶT TRƯỚC"),
    CHECKIN("NHẬN PHÒNG"),
    CHECKOUT("TRẢ PHÒNG"),
    CANCEL("HỦY PHÒNG"),
    EXTEND("GIA HẠN THỜI GIAN LƯU TRÚ");

    public String actionName;

    ActionType(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }
}

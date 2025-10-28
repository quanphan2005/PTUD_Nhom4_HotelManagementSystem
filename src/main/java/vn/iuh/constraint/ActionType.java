package vn.iuh.constraint;

public enum ActionType {
    BOOKING("ĐẶT PHÒNG"),
    PRE_BOOKING("ĐẶT TRƯỚC"),
    CHECKIN("NHẬN PHÒNG"),
    CHECKOUT("TRẢ PHÒNG"),
    CANCEL("HỦY PHÒNG"),
    EXTEND("GIA HẠN THỜI GIAN LƯU TRÚ"),
    CANCEL_RESERVATION("HỦY ĐẶT PHÒNG"),
    CHANGE_ROOM_BEFORE_CHECKIN("ĐỔI PHÒNG TRƯỚC KHI CHECKIN"),
    CHANGE_ROOM_AFTER_CHECKIN("ĐỔI PHÒNG SAU KHI CHECKIN"),
    EDIT_ROOM("SỬA THÔNG TIN PHÒNG"),
    CREATE_ROOM("THÊM PHÒNG"),
    DELETE_ROOM("XÓA PHÒNG"),
    CREATE_ROOM_CATEGORY("THÊM LOẠI PHÒNG"),
    DELETE_ROOM_CATEGORY("SỬA LOẠI PHÒNG"),
    EDIT_ROOM_CATEGORY("SỬA LOẠI PHÒNG");


    public String actionName;

    ActionType(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }
}

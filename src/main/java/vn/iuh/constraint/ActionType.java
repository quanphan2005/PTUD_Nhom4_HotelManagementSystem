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
    DELETE_ROOM_CATEGORY("XÓA LOẠI PHÒNG"),
    EDIT_ROOM_CATEGORY("SỬA LOẠI PHÒNG"),
    CREATE_SERVICE("THÊM DỊCH VỤ"),
    UPDATE_SERVICE("SỬA THÔNG TIN DỊCH VỤ"),
    DELETE_SERVICE("XÓA DỊCH VỤ"),
    CREATE_SERVICE_CATEGORY("THÊM LOẠI DỊCH VỤ"),
    UPDATE_SERVICE_CATEGORY("SỬA THÔNG TIN LOẠI DỊCH VỤ"),
    DELETE_SERVICE_CATEGORY("XÓA LOẠI DỊCH VỤ"),
    CREATE_CUSTOMER("THÊM KHÁCH HÀNG"),
    UPDATE_CUSTOMER("SỬA THÔNG TIN KHÁCH HÀNG"),
    DELETE_CUSTOMER("XÓA KHÁCH HÀNG");



    public String actionName;

    ActionType(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }
}

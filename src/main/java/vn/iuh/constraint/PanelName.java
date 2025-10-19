package vn.iuh.constraint;

public enum PanelName {
    BOOKING("Đặt phòng"),
    ROOM_USING("Thông tin đặt phòng"),
    MULTI_BOOKING("Đặt nhiều phòng"),
    RESERVATION_MANAGEMENT("Quản lý đặt phòng"),
    RESERVATION_FORM_MANAGEMENT("Quản lý đơn đặt phòng trước"),
    RESERVATION_FORM_SEARCH("Tìm kiếm phiếu đặt phòng"),
    CHECKIN("Nhận phòng"),
    CHECKOUT("Trả phòng"),
    SERVICE_ORDER("Gọi dịch vụ"),
    STATISTIC("Thống kê"),
    ACCOUNT("Tài khoản"),
    ;


    public final String name;

    PanelName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

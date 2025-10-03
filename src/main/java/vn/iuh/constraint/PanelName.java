package vn.iuh.constraint;

public enum PanelName {
    BOOKING("Đặt phòng"),
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

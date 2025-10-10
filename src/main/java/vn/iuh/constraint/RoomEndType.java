package vn.iuh.constraint;

public enum RoomEndType {
    DOI_PHONG("ĐỔI PHÒNG"),
    KHONG_NHAN_PHONG("KHÔNG NHẬN PHÒNG"),
    TRA_PHONG_LOI("TRẢ PHÒNG DO LỖI"),
    TRA_PHONG("TRẢ PHÒNG"),
    HUY_PHONG("HỦY PHÒNG"),
    ;

    public String status;
    RoomEndType(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

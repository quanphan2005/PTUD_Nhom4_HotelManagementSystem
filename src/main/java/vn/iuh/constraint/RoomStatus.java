package vn.iuh.constraint;

public enum RoomStatus {
    ROOM_AVAILABLE_STATUS("CÒN TRỐNG"),
    ROOM_BOOKED_STATUS("CHỜ CHECKIN"),
    ROOM_CHECKING_STATUS("ĐANG KIỂM TRA"),
    ROOM_USING_STATUS("ĐANG SỬ DỤNG"),
    ROOM_CHECKOUT_LATE_STATUS("TRẢ PHÒNG MUỘN"),
    ROOM_CLEANING_STATUS("ĐANG DỌN DẸP"),
    ROOM_MAINTENANCE_STATUS("ĐANG BẢO TRÌ"),;

    public String status;
    RoomStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static RoomStatus fromString(String text) {
        for (RoomStatus b : RoomStatus.values()) {
            if (b.status.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}

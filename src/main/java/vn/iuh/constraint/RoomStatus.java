package vn.iuh.constraint;

public enum RoomStatus {

    // Available for booking
    ROOM_EMPTY_STATUS("CÒN TRỐNG"),

    // Busy room
    ROOM_BOOKED_STATUS("CHỜ CHECKIN"),
    ROOM_CHECKING_STATUS("KIỂM TRA"),
    ROOM_USING_STATUS("SỬ DỤNG"),
    ROOM_CHECKOUT_LATE_STATUS("CHECKOUT TRỄ"),


    ROOM_CLEANING_STATUS("DỌN DẸP"),
    ROOM_MAINTENANCE_STATUS("BẢO TRÌ"),
    ;

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

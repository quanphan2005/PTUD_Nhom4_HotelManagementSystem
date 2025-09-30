package vn.iuh.dto.event.create;

public class RoomCreationEvent {
    private final String roomName;
    private final boolean isActive;
    private final String note;
    private final String roomDescription;
    private final String roomCategoryId;

    public RoomCreationEvent(String roomName, boolean isActive, String note, String roomDescription,
                             String roomCategoryId) {
        this.roomName = roomName;
        this.isActive = isActive;
        this.note = note;
        this.roomDescription = roomDescription;
        this.roomCategoryId = roomCategoryId;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public String getNote() {
        return note;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public String getRoomCategoryId() {
        return roomCategoryId;
    }


}

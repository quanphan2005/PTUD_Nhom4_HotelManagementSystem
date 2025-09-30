package vn.iuh.dto.event.update;

public class RoomModificationEvent {
    private final String id;
    private final String roomName;
    private final String note;
    private final String roomDescription;
    private final String roomCategoryId;

    public RoomModificationEvent(String id, String roomName, String note, String roomDescription,
                                 String roomCategoryId) {
        this.id = id;
        this.roomName = roomName;
        this.note = note;
        this.roomDescription = roomDescription;
        this.roomCategoryId = roomCategoryId;
    }

    public String getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
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

package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ChangeRoomRecord {
    private final String oldDetailId;   // có thể null
    private final String newDetailId;   // có thể null
    private final String oldRoom;
    private final String newRoom;
    private final Timestamp time;       // thời điểm đổi
    private final String type;          // trước/sau checkin

    public ChangeRoomRecord(String oldDetailId, String newDetailId, String oldRoom, String newRoom, Timestamp time, String type) {
        this.oldDetailId = oldDetailId;
        this.newDetailId = newDetailId;
        this.oldRoom = oldRoom;
        this.newRoom = newRoom;
        this.time = time;
        this.type = type;
    }

    // getters
    public String getOldDetailId() { return oldDetailId; }
    public String getNewDetailId() { return newDetailId; }
    public String getOldRoom() { return oldRoom; }
    public String getNewRoom() { return newRoom; }
    public Timestamp getTime() { return time; }
    public String getType() { return type; }
}

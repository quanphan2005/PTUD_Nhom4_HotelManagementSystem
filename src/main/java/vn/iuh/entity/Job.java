package vn.iuh.entity;

import java.sql.Timestamp;

public class Job {
    private String id;
    private Timestamp startTime;
    private Timestamp endTime;
    private String statusName;
    private String roomdId;

    public Job(String id, Timestamp startTime, Timestamp endTime, String statusName, String roomdId) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.statusName = statusName;
        this.roomdId = roomdId;
    }

    public Job() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getRoomdId() {
        return roomdId;
    }

    public void setRoomdId(String roomdId) {
        this.roomdId = roomdId;
    }
}

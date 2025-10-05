package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class RoomJob {
    private String jobId;
    private String roomId;
    private String statusName;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean isDeleted;

    public RoomJob(String jobId, String roomId, String statusName, Timestamp startTime, Timestamp endTime, boolean isDeleted) {
        this.jobId = jobId;
        this.roomId = roomId;
        this.statusName = statusName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDeleted = isDeleted;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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
}

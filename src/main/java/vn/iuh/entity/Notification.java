package vn.iuh.entity;

import java.sql.Timestamp;

public class Notification {
    private String id;
    private Timestamp createTime;
    private String notiMessage;
    private String shiftAssignmentId;
    private Timestamp createAt;
    private boolean isDeleted;

    public Notification() {
    }

    public Notification(String id, Timestamp createTime, String notiMessage, String shiftAssignmentId, Timestamp createAt, boolean isDeleted) {
        this.id = id;
        this.createTime = createTime;
        this.notiMessage = notiMessage;
        this.shiftAssignmentId = shiftAssignmentId;
        this.createAt = createAt;
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getNotiMessage() {
        return notiMessage;
    }

    public void setNotiMessage(String notiMessage) {
        this.notiMessage = notiMessage;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public void setShiftAssignmentId(String shiftAssignmentId) {
        this.shiftAssignmentId = shiftAssignmentId;
    }

    public Timestamp getCreateAt() { return createAt; }

    public void setCreateAt(Timestamp createAt) { this.createAt = createAt; }

    public boolean getIsDeleted() { return isDeleted; }

    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
}

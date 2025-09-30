package vn.iuh.entity;

import java.sql.Timestamp;

public class ShiftAssignment {
    private String id;
    private int counterNumber;
    private Timestamp startTime;
    private Timestamp endTime;
    private String accountId;
    private Timestamp createAt;
    private boolean isDeleted;

    public ShiftAssignment() {
    }

    public ShiftAssignment(String id, int counterNumber, Timestamp startTime, Timestamp endTime,
                           String accountId, Timestamp createAt, boolean isDeleted) {
        this.id = id;
        this.counterNumber = counterNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.accountId = accountId;
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

    public int getCounterNumber() {
        return counterNumber;
    }

    public void setCounterNumber(int counterNumber) {
        this.counterNumber = counterNumber;
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Timestamp getCreateAt() { return createAt; }

    public void setCreateAt(Timestamp createAt) { this.createAt = createAt; }

    public boolean getIsDeleted() { return isDeleted; }

    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
}

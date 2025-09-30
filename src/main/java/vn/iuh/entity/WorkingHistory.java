package vn.iuh.entity;

import java.sql.Timestamp;

public class WorkingHistory {
    private String id;
    private String taskName;
    private Timestamp createTime;
    private String actionDescription;
    private String shiftAssignmentId;

    public WorkingHistory() {
    }

    public WorkingHistory(String id, String taskName, Timestamp createTime, String actionDescription,
                          String shiftAssignmentId) {
        this.id = id;
        this.taskName = taskName;
        this.createTime = createTime;
        this.actionDescription = actionDescription;
        this.shiftAssignmentId = shiftAssignmentId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public void setShiftAssignmentId(String shiftAssignmentId) {
        this.shiftAssignmentId = shiftAssignmentId;
    }
}

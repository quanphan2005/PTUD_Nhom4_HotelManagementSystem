package vn.iuh.entity;

import java.sql.Time;

public class RoomListPrice {
    private String id;
    private Time createAt;
    private double previousDailyPrice;
    private double previousHourlyPrice;
    private double updatedDailyPrice;
    private double updatedHourlyPrice;
    private String roomCategoryId;
    private String shiftAssignmentId;

    public RoomListPrice() {
    }

    public RoomListPrice(String id, Time createAt, double previousDailyPrice, double previousHourlyPrice,
                         double updatedDailyPrice, double updatedHourlyPrice, String roomCategoryId,
                         String shiftAssignmentId) {
        this.id = id;
        this.createAt = createAt;
        this.previousDailyPrice = previousDailyPrice;
        this.previousHourlyPrice = previousHourlyPrice;
        this.updatedDailyPrice = updatedDailyPrice;
        this.updatedHourlyPrice = updatedHourlyPrice;
        this.roomCategoryId = roomCategoryId;
        this.shiftAssignmentId = shiftAssignmentId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Time getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Time createAt) {
        this.createAt = createAt;
    }

    public double getPreviousDailyPrice() {
        return previousDailyPrice;
    }

    public void setPreviousDailyPrice(double previousDailyPrice) {
        this.previousDailyPrice = previousDailyPrice;
    }

    public double getPreviousHourlyPrice() {
        return previousHourlyPrice;
    }

    public void setPreviousHourlyPrice(double previousHourlyPrice) {
        this.previousHourlyPrice = previousHourlyPrice;
    }

    public double getUpdatedDailyPrice() {
        return updatedDailyPrice;
    }

    public void setUpdatedDailyPrice(double updatedDailyPrice) {
        this.updatedDailyPrice = updatedDailyPrice;
    }

    public double getUpdatedHourlyPrice() {
        return updatedHourlyPrice;
    }

    public void setUpdatedHourlyPrice(double updatedHourlyPrice) {
        this.updatedHourlyPrice = updatedHourlyPrice;
    }

    public String getRoomCategoryId() {
        return roomCategoryId;
    }

    public void setRoomCategoryId(String roomCategoryId) {
        this.roomCategoryId = roomCategoryId;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public void setShiftAssignmentId(String shiftAssignmentId) {
        this.shiftAssignmentId = shiftAssignmentId;
    }
}
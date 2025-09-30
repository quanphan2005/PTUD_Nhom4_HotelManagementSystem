package vn.iuh.entity;

import java.sql.Timestamp;

public class RoomCategory {
    private String id;
    private String CategoryName;
    private int numberOfCustomer;
    private String roomType;
    private Timestamp createAt;
    private boolean isDeleted;

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    public RoomCategory() {
    }

    public RoomCategory(String id, String CategoryName, int numberOfCustomer, String roomType, Timestamp createAt,  boolean isDeleted) {
        this.id = id;
        this.CategoryName = CategoryName;
        this.numberOfCustomer = numberOfCustomer;
        this.roomType = roomType;
        this.createAt = createAt;
        this.isDeleted = isDeleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        this.CategoryName = categoryName;
    }

    public int getNumberOfCustomer() {
        return numberOfCustomer;
    }

    public void setNumberOfCustomer(int numberOfCustomer) {
        this.numberOfCustomer = numberOfCustomer;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }
}
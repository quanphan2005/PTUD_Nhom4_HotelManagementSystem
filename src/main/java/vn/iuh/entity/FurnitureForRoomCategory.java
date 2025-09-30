package vn.iuh.entity;

public class FurnitureForRoomCategory {
    private String id;
    private int number;
    private String roomCategoryId;
    private String furnitureItemId;

    public FurnitureForRoomCategory() {
    }

    public FurnitureForRoomCategory(String id, int number, String roomCategoryId, String furnitureItemId) {
        this.id = id;
        this.number = number;
        this.roomCategoryId = roomCategoryId;
        this.furnitureItemId = furnitureItemId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getRoomCategoryId() {
        return roomCategoryId;
    }

    public void setRoomCategoryId(String roomCategoryId) {
        this.roomCategoryId = roomCategoryId;
    }

    public String getFurnitureItemId() {
        return furnitureItemId;
    }

    public void setFurnitureItemId(String furnitureItemId) {
        this.furnitureItemId = furnitureItemId;
    }
}
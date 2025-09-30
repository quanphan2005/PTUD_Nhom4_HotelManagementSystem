package vn.iuh.entity;

public class FurnitureItem {
    private String id;
    private String itemName;
    private String itemDescription;

    public FurnitureItem() {
    }

    public FurnitureItem(String id, String itemName, String itemDescription) {
        this.id = id;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
}
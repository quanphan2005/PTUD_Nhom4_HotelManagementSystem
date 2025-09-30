package vn.iuh.entity;

import java.sql.Timestamp;

public class ServiceItem {
    private String id;
    private String itemName;
    private String serviceCategoryId;
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

    public ServiceItem() {
    }

    public ServiceItem(String id, String itemName, String serviceCategoryId, Timestamp createAt, boolean isDeleted) {
        this.id = id;
        this.itemName = itemName;
        this.serviceCategoryId = serviceCategoryId;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(String serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }
}

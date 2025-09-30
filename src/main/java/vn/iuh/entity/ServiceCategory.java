package vn.iuh.entity;

import java.sql.Timestamp;

public class ServiceCategory {
    private String id;
    private String categoryName;
    private Timestamp createAt;
    private boolean isDeleted;


    public ServiceCategory() {
    }

    public ServiceCategory(String id, String categoryName, Timestamp createAt, boolean isDeleted) {
        this.id = id;
        this.categoryName = categoryName;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

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
}

package vn.iuh.entity;

import java.sql.Date;

public class AdditionalFee {
    private String id;
    private String feeName;
    private Date createAt;

    public AdditionalFee() {
    }

    public AdditionalFee(String id, String feeName, Date createAt) {
        this.id = id;
        this.feeName = feeName;
        this.createAt = createAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeeName() {
        return feeName;
    }

    public void setFeeName(String feeName) {
        this.feeName = feeName;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}

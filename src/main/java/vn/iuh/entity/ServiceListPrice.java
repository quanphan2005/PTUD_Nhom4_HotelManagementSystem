package vn.iuh.entity;

import java.sql.Date;

public class ServiceListPrice {
    private String id;
    private double previousPrice;
    private double updatedPrice;
    private Date createAt;
    private String shiftAssignmentId;
    private String serviceItemId;

    public ServiceListPrice() {}

    public ServiceListPrice(String id, double previousPrice, double updatedPrice, Date createAt, String shiftAssignmentId, String serviceItemId) {
        this.id = id;
        this.previousPrice = previousPrice;
        this.updatedPrice = updatedPrice;
        this.createAt = createAt;
        this.shiftAssignmentId = shiftAssignmentId;
        this.serviceItemId = serviceItemId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getPreviousPrice() { return previousPrice; }
    public void setPreviousPrice(double previousPrice) { this.previousPrice = previousPrice; }
    public double getUpdatedPrice() { return updatedPrice; }
    public void setUpdatedPrice(double updatedPrice) { this.updatedPrice = updatedPrice; }
    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }
    public String getShiftAssignmentId() { return shiftAssignmentId; }
    public void setShiftAssignmentId(String shiftAssignmentId) { this.shiftAssignmentId = shiftAssignmentId; }
    public String getServiceItemId() { return serviceItemId; }
    public void setServiceItemId(String serviceItemId) { this.serviceItemId = serviceItemId; }
}

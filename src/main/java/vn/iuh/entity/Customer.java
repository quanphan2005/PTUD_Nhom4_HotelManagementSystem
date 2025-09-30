package vn.iuh.entity;

public class Customer {
    private String id;
    private String customerName;
    private String phoneNumber;
    private String CCCD;

    public Customer() {
    }

    public Customer(String id, String customerName, String phoneNumber, String CCCD) {
        this.id = id;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.CCCD = CCCD;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }
}

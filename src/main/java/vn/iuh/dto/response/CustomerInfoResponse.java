package vn.iuh.dto.response;

public class CustomerInfoResponse {
    private final String customerId;
    private final String CCCD;
    private final String customerName;
    private final String customerPhone;

    public CustomerInfoResponse(String customerId, String CCCD, String customerName, String customerPhone) {
        this.customerId = customerId;
        this.CCCD = CCCD;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCCCD() {
        return CCCD;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    @Override
    public String toString() {
        return "CustomerInfo{" +
               "customerId='" + customerId + '\'' +
               ", CCCD='" + CCCD + '\'' +
               ", customerName='" + customerName + '\'' +
               ", customerPhone='" + customerPhone + '\'' +
               '}';
    }
}

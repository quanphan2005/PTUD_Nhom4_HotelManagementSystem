package vn.iuh.dto.response;

public class CustomerInfoWithPayments {
    private final String customerId;
    private final String CCCD;
    private final String customerName;
    private final String customerPhone;
    private final double totalServiceCost;
    private final double totalDepositPayment;

    public CustomerInfoWithPayments(String customerId, String CCCD, String customerName,
                                    String customerPhone, double totalServiceCost,
                                    double totalDepositPayment) {
        this.customerId = customerId;
        this.CCCD = CCCD;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalServiceCost = totalServiceCost;
        this.totalDepositPayment = totalDepositPayment;
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

    public double getTotalServiceCost() {
        return totalServiceCost;
    }

    public double getTotalDepositPayment() {
        return totalDepositPayment;
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

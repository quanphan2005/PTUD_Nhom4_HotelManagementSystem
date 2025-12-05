package vn.iuh.dto.response;

public class RoomUsageServiceResponse {
    private String roomUsageServiceId;
    private String roomName;
    private String serviceName;
    private int quantity;
    private double price;
    private double totalPrice;

    public RoomUsageServiceResponse() {
    }

    public RoomUsageServiceResponse(String roomUsageServiceId, String roomName, String serviceName, int quantity,
                                    double price) {
        this.roomUsageServiceId = roomUsageServiceId;
        this.roomName = roomName;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.price = price;
    }

    public RoomUsageServiceResponse(String roomUsageServiceId, String roomName, String serviceName, int quantity,
                                    double price, double totalPrice) {
        this.roomUsageServiceId = roomUsageServiceId;
        this.roomName = roomName;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }

    public String getRoomUsageServiceId() {
        return roomUsageServiceId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}

package vn.iuh.dto.response;

public class RoomUsageServiceResponse {
    private String RoomUsageServiceId;
    private String roomName;
    private String serviceName;
    private int quantity;
    private boolean isGifted;

    public RoomUsageServiceResponse() {
    }

    public RoomUsageServiceResponse(String roomUsageServiceId, String roomName, String serviceName,
                                    int quantity, boolean isGifted) {
        RoomUsageServiceId = roomUsageServiceId;
        this.roomName = roomName;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.isGifted = isGifted;
    }

    public String getRoomUsageServiceId() {
        return RoomUsageServiceId;
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

    public boolean isGifted() {
        return isGifted;
    }
}

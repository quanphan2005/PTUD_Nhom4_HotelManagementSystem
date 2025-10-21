package vn.iuh.dto.response;

public class RoomUsageServiceResponse {
    private String RoomUsageServiceId;
    private String roomId;
    private String roomName;
    private String serviceName;
    private int quantity;
    private boolean isGifted;

    public RoomUsageServiceResponse() {
    }

    public RoomUsageServiceResponse(String roomUsageServiceId, String roomId, String roomName, String serviceName,
                                    int quantity, boolean isGifted) {
        RoomUsageServiceId = roomUsageServiceId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.isGifted = isGifted;
    }

    public String getRoomUsageServiceId() {
        return RoomUsageServiceId;
    }

    public String getRoomId() {
        return roomId;
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

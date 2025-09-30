package vn.iuh.dto.repository;

public class RoomInfo {
    private final String id;
    private final String roomName;
    private boolean isActive;
    private String roomStatus;
    private final String roomType;
    private final String numberOfCustomers;
    private final double dailyPrice;
    private final double hourlyPrice;

    public RoomInfo(String id, String roomName, boolean isActive, String roomStatus, String roomType, String numberOfCustomers,
                    double dailyPrice, double hourlyPrice) {
        this.id = id;
        this.roomName = roomName;
        this.isActive = isActive;
        this.roomStatus = roomStatus;
        this.roomType = roomType;
        this.numberOfCustomers = numberOfCustomers;
        this.dailyPrice = dailyPrice;
        this.hourlyPrice = hourlyPrice;
    }

    public String getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getNumberOfCustomers() {
        return numberOfCustomers;
    }

    public double getDailyPrice() {
        return dailyPrice;
    }

    public double getHourlyPrice() {
        return hourlyPrice;
    }
}

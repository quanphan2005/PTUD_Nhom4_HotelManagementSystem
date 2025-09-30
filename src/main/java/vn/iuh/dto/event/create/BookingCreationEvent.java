package vn.iuh.dto.event.create;

import java.sql.Timestamp;
import java.util.List;

public class BookingCreationEvent {
    private String customerName;
    private String phoneNumber;
    private String CCCD;
    private Timestamp reserveDate;
    private String note;
    private Timestamp checkInDate;
    private Timestamp checkOutDate;
    private double initialPrice;
    private double depositPrice;
    private boolean isAdvanced;
    private List<String> roomIds;
    private List<String> serviceIds;
    private String shiftAssignmentId;
    private Timestamp createAt;
    public BookingCreationEvent(String customerName, String phoneNumber, String CCCD, Timestamp reserveDate, String note,
                                Timestamp checkInDate, Timestamp checkOutDate, double initialPrice, double depositPrice,
                                boolean isAdvanced, List<String> roomIds, List<String> serviceIds,
                                String shiftAssignmentId, Timestamp createAt) {
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.CCCD = CCCD;
        this.reserveDate = reserveDate;
        this.note = note;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.initialPrice = initialPrice;
        this.depositPrice = depositPrice;
        this.isAdvanced = isAdvanced;
        this.roomIds = roomIds;
        this.serviceIds = serviceIds;
        this.shiftAssignmentId = shiftAssignmentId;
        this.createAt = createAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCCCD() {
        return CCCD;
    }

    public Timestamp getReserveDate() {
        return reserveDate;
    }

    public String getNote() {
        return note;
    }

    public Timestamp getCheckInDate() {
        return checkInDate;
    }

    public Timestamp getCheckOutDate() {
        return checkOutDate;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public double getDepositPrice() {
        return depositPrice;
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }

    public List<String> getRoomIds() {
        return roomIds;
    }

    public List<String> getServiceIds() {
        return serviceIds;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public Timestamp getCreateAt() { return createAt; }
}

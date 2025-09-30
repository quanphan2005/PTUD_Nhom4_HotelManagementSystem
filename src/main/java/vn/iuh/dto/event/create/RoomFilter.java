package vn.iuh.dto.event.create;

import java.sql.Timestamp;

public class RoomFilter {
    private Integer numberOfCustomer;
    private String roomType;
    private Timestamp timeIn;
    private Timestamp timeOut;
    private boolean isEmpty;
}

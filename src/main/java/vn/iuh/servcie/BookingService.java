package vn.iuh.servcie;

import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.RoomFilter;
import vn.iuh.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    boolean createBooking(BookingCreationEvent bookingCreationEvent);
    List<BookingResponse> getAllEmptyRooms();
    List<BookingResponse> getRoomsByFilter(RoomFilter roomFilter);
    List<BookingResponse> getAllBookingInfo();
}

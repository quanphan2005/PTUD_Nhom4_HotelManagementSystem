package vn.iuh.service;

import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    boolean createBooking(BookingCreationEvent bookingCreationEvent);
    List<BookingResponse> getAllBookingInfo();
}

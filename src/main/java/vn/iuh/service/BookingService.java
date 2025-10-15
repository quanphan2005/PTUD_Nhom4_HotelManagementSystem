package vn.iuh.service;

import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.CustomerInfoResponse;
import vn.iuh.dto.response.ReservationFormResponse;

import java.util.List;

public interface BookingService {
    boolean createBooking(BookingCreationEvent bookingCreationEvent);
    List<ReservationFormResponse> getAllReservationForms();
    List<ReservationFormResponse> getReseravtionFormByRoomId(String id);
    List<BookingResponse> getAllBookingInfo();
    CustomerInfoResponse getCustomerInfoByBookingId(String maChiTietDatPhong);
    boolean cancelReservation(String maDatPhong);
    boolean cancelRoomReservation(String maDatPhong, String maPhong);
}

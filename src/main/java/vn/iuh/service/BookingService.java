package vn.iuh.service;

import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.CustomerInfoResponse;
import vn.iuh.dto.response.EventResponse;
import vn.iuh.dto.response.PreReservationResponse;
import vn.iuh.dto.response.ReservationResponse;
import vn.iuh.dto.response.ReservationInfoDetailResponse;

import java.sql.Timestamp;
import java.util.List;

public interface BookingService {
    EventResponse createBooking(BookingCreationEvent bookingCreationEvent);
    List<PreReservationResponse> getAllReservationForms();
    List<PreReservationResponse> getReseravtionFormByRoomId(String id);
    List<BookingResponse> getAllBookingInfo();
    List<BookingResponse> getAllEmptyRoomInRange(Timestamp timeIn, Timestamp timeOut);
    List<String> getAllNonEmptyRoomInRange(Timestamp timeIn, Timestamp timeOut);
    CustomerInfoResponse getCustomerInfoByBookingId(String maChiTietDatPhong);
    boolean cancelReservation(String maDatPhong);
    boolean cancelRoomReservation(String maDatPhong, String maPhong);
    List<ReservationResponse> getAllReservationsWithStatus();
    List<ReservationResponse> getAllReservationsWithStatusInRange(Timestamp startDate, Timestamp endDate);
    ReservationInfoDetailResponse getReservationDetailInfo(String maDonDatPhong);
}

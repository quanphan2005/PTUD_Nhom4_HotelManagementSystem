package vn.iuh.service;

import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.response.*;

import java.sql.Timestamp;
import java.util.List;

public interface BookingService {
    EventResponse<DepositInvoiceResponse> createBooking(BookingCreationEvent bookingCreationEvent);
    List<PreReservationResponse> getAllReservationForms();
    List<PreReservationResponse> getReseravtionFormByRoomId(String id);
    List<BookingResponse> getAllBookingInfo();
    List<BookingResponse> getAllEmptyRoomInRange(Timestamp timeIn, Timestamp timeOut);
    List<String> getAllNonEmptyRoomInRange(Timestamp timeIn, Timestamp timeOut);
    CustomerInfoResponse getCustomerInfoByBookingId(String maChiTietDatPhong);
    boolean cancelReservation(String maDatPhong);
    boolean cancelRoomReservation(String maDatPhong, String maPhong);
    List<ReservationResponse> getAllCurrentReservationsWithStatus();
    List<ReservationResponse> getAllPastReservationsWithStatusInRange(Timestamp startDate, Timestamp endDate);
    ReservationInfoDetailResponse getReservationDetailInfo(String maDonDatPhong);
}

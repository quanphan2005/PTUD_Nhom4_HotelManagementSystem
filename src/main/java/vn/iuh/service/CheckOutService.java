package vn.iuh.service;
import vn.iuh.dto.response.InvoiceResponse;

public interface CheckOutService {
    InvoiceResponse checkOutReservation(String reservationId);
    InvoiceResponse checkOutByReservationDetail(String reservationDetail);
    boolean createHoaDonForAutoCheckout(String reservationDetail);
}

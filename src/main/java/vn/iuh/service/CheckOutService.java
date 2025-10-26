package vn.iuh.service;
import vn.iuh.dto.response.InvoiceResponse;

import java.util.List;

public interface CheckOutService {
    InvoiceResponse checkOutReservation(String reservationId);
    InvoiceResponse checkOutByReservationDetail(String reservationDetail);
    List<String> createHoaDonForAutoCheckout(String reservationDetail);
}

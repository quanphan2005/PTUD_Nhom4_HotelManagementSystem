package vn.iuh.service;
import vn.iuh.dto.repository.WarningReservation;
import vn.iuh.dto.response.InvoiceResponse;
import vn.iuh.entity.DonDatPhong;

import java.util.List;

public interface CheckOutService {
    InvoiceResponse checkOutReservation(String reservationId);
    InvoiceResponse checkOutByReservationDetail(String reservationDetail);
    List<String> createHoaDonForAutoCheckout(String reservationDetail);
    DonDatPhong checkReservation(String reservationDetailId);
    void handleSimpleAutoCheckOut(WarningReservation wr);
}

package vn.iuh.service;

public interface CheckOutService {
    boolean checkOutReservation(String reservationId);
    boolean checkOutByReservationDetail(String reservationDetail);
}

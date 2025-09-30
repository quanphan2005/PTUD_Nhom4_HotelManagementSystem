package vn.iuh.servcie;

import vn.iuh.entity.ReservationForm;

public interface ReservationFormService {
    ReservationForm getReservationFormByID(String id);
    ReservationForm createReservationForm(ReservationForm reservationForm);
    ReservationForm updateReservationForm(ReservationForm reservationForm);
    boolean deleteReservationFormByID(String id);
}

package vn.iuh.service;

import vn.iuh.entity.DonDatPhong;

public interface ReservationFormService {
    DonDatPhong getReservationFormByID(String id);
    DonDatPhong createReservationForm(DonDatPhong donDatPhong);
    DonDatPhong updateReservationForm(DonDatPhong donDatPhong);
    boolean deleteReservationFormByID(String id);
}

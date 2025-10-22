package vn.iuh.service;

import vn.iuh.dto.repository.BookThemGioInfo;

public interface BookThemGioService {
    BookThemGioInfo layThongTinChoBookThemGio(String maChiTietDatPhong, String maPhong);
    boolean bookThemGio(String maChiTietDatPhong, String maPhong, long millisToAdd);
}

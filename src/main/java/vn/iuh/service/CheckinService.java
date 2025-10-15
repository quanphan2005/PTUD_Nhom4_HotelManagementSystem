package vn.iuh.service;

public interface CheckinService {
    boolean checkin(String maDonDatPhong, String tenPhong);
    String getLastError();
}

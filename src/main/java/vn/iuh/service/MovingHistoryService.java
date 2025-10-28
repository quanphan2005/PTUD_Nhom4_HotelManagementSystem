package vn.iuh.service;

import vn.iuh.dto.response.EventResponse;

public interface MovingHistoryService {
    boolean createEnteringHistory(String maChiTietDatPhong);
    boolean createLeavingHistory(String maChiTietDatPhong);
}

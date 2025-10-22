package vn.iuh.service;

import vn.iuh.dto.response.BookingResponse;

import java.sql.Timestamp;
import java.util.List;

public interface DoiPhongService {
    boolean changeRoom(String reservationId, String oldRoomId, String newRoomId, boolean applyFee);
    String getLastError();
    List<BookingResponse> timPhongPhuHopChoDoiPhong(String currentRoomId, int neededPersons, Timestamp fromTime, Timestamp toTime);
    String layMaDonDatPhong(String maChiTietDatPhong);
    int timSoNguoiCan(String roomId);
    String timTenLoaiPhong(String roomId);

}

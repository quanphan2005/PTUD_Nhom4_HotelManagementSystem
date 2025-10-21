package vn.iuh.service;

import vn.iuh.dto.response.BookingResponse;

import java.sql.Timestamp;
import java.util.List;

public interface DoiPhongService {
    public boolean changeRoom(String maDonDatPhong, String maPhongMoi, String maPhongCu);
    String getLastError();
    List<BookingResponse> timPhongPhuHopChoDoiPhong(String currentRoomId, int neededPersons, Timestamp fromTime, Timestamp toTime);
}

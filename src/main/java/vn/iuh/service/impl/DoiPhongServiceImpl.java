package vn.iuh.service.impl;

import vn.iuh.dao.LoaiPhongDAO;
import vn.iuh.dao.PhongDAO;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.Phong;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.service.DoiPhongService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DoiPhongServiceImpl implements DoiPhongService {

    private final PhongDAO phongDAO;
    private final LoaiPhongDAO loaiPhongDAO;

    public DoiPhongServiceImpl() {
        this.phongDAO = new PhongDAO();
        this.loaiPhongDAO = new LoaiPhongDAO();
    }

    //Hàm để tìm các phòng phù hợp cho đổi phòng
    public List<BookingResponse> timPhongPhuHopChoDoiPhong(String currentRoomId, int neededPersons, Timestamp fromTime, Timestamp toTime) {
        List<BookingResponse> results = new ArrayList<>();

        // 1) Lấy ra list Phong phù hợp tìm được từ DAO
        List<Phong> candidatePhongs = phongDAO.timPhongUngVien(currentRoomId, neededPersons, fromTime, toTime);

        if (candidatePhongs == null || candidatePhongs.isEmpty()) {
            return results;
        }

        // 2) Map từng Phong -> BookingResponse
        for (Phong p : candidatePhongs) {
            double[] price = phongDAO.getLatestPriceForLoaiPhong(p.getMaLoaiPhong());
            double giaNgay = price != null && price.length > 0 ? price[0] : 0.0;
            double giaGio = price != null && price.length > 1 ? price[1] : 0.0;

            // Lấy tên loại phòng
            String tenLoai = null;
            try {
                LoaiPhong lp = loaiPhongDAO.getRoomCategoryByID(p.getMaLoaiPhong());
                if (lp != null) tenLoai = lp.getTenLoaiPhong();
            } catch (Exception ignored) {
                // nếu lỗi thì fallback về mã loại
            }

            BookingResponse br = new BookingResponse(
                    p.getMaPhong(),
                    p.getTenPhong(),
                    p.isDangHoatDong(),
                    RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
                    tenLoai != null ? tenLoai : p.getMaLoaiPhong(),
                    String.valueOf(neededPersons),
                    giaNgay,
                    giaGio                                           //
            );

            results.add(br);
        }

        return results;
    }

    // Xử lí đổi phòng
    public boolean changeRoom(String reservationId, String oldRoomId, String newRoomId) {
        throw new UnsupportedOperationException("changeRoom chưa được implement. Vui lòng implement trong BookingService/DAO với transaction.");
    }

    public String getLastError() {
        return "Error";
    }
}

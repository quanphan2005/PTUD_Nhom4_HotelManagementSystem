package vn.iuh.service;

import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.entity.LoaiPhong;

import java.math.BigDecimal;
import java.util.List;

public interface LoaiPhongService {
    LoaiPhong getRoomCategoryByID(String id);
    List<RoomCategoryResponse> getAllRoomCategories();
    LoaiPhong createRoomCategory(LoaiPhong loaiPhong);
    LoaiPhong updateRoomCategory(LoaiPhong loaiPhong);
    boolean deleteRoomCategoryByID(String id);
    BigDecimal layGiaTheoLoaiPhong(String maLoaiPhong, boolean isGiaNgay);
}

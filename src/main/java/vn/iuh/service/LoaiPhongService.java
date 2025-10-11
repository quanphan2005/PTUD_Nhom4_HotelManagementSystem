package vn.iuh.service;

import vn.iuh.entity.LoaiPhong;

import java.math.BigDecimal;

public interface LoaiPhongService {
    LoaiPhong getRoomCategoryByID(String id);
    LoaiPhong createRoomCategory(LoaiPhong loaiPhong);
    LoaiPhong updateRoomCategory(LoaiPhong loaiPhong);
    boolean deleteRoomCategoryByID(String id);
    BigDecimal layGiaTheoLoaiPhong(String maLoaiPhong, boolean isGiaNgay);
}

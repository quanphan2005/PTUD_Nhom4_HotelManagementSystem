package vn.iuh.service;

import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.gui.panel.statistic.FillterRoomStatistic;
import vn.iuh.gui.panel.statistic.RoomStatistic;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface LoaiPhongService {
    LoaiPhong getRoomCategoryByID(String id);
    List<RoomCategoryResponse> getAllRoomCategories();
    LoaiPhong createRoomCategory(LoaiPhong loaiPhong);
    LoaiPhong updateRoomCategory(LoaiPhong loaiPhong);
    boolean deleteRoomCategoryByID(String id);
    BigDecimal layGiaTheoLoaiPhong(String maLoaiPhong, boolean isGiaNgay);
    List<RoomStatistic> getListRoomCategoryByFilter(FillterRoomStatistic filter);
}

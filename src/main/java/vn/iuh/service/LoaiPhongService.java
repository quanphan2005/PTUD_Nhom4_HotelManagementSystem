package vn.iuh.service;

import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.dto.response.RoomCategoryPriceHistory;
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

    LoaiPhong findLatestIncludingDeleted();
    LoaiPhong updateRoomCategoryV2(LoaiPhong loaiPhong);
    boolean deleteRoomCategoryByIDV2(String id);
    LoaiPhong getRoomCategoryByIDV2(String id);

    LoaiPhong createRoomCategoryV2(LoaiPhong loaiPhong, double giaNgay, double giaGio, List<NoiThatAssignment> itemsWithQty);
    Map<String, Double> getLatestPriceMap(String maLoaiPhong);
    List<RoomCategoryPriceHistory> getPriceHistoryWithActor(String maLoaiPhong);
    boolean updateRoomCategoryWithAudit(LoaiPhong loaiPhong,
                                List<NoiThatAssignment> itemsWithQty,
                                Double giaGioVal,
                                Double giaNgayVal);
    boolean updateRoomCategoryWithAudit(LoaiPhong loaiPhong, List<NoiThatAssignment> itemsWithQty);
    List<LoaiPhong> layTatCaLoaiPhongHienCo();

}

package vn.iuh.servcie;

import vn.iuh.entity.LoaiPhong;

public interface RoomCategoryService {
    LoaiPhong getRoomCategoryByID(String id);
    LoaiPhong createRoomCategory(LoaiPhong loaiPhong);
    LoaiPhong updateRoomCategory(LoaiPhong loaiPhong);
    boolean deleteRoomCategoryByID(String id);
}

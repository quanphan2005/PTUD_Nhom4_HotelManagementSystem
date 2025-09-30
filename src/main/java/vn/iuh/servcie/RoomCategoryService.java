package vn.iuh.servcie;

import vn.iuh.entity.RoomCategory;

public interface RoomCategoryService {
    RoomCategory getRoomCategoryByID(String id);
    RoomCategory createRoomCategory(RoomCategory roomCategory);
    RoomCategory updateRoomCategory(RoomCategory roomCategory);
    boolean deleteRoomCategoryByID(String id);
}

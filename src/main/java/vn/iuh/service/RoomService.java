package vn.iuh.service;

import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.Phong;

import java.util.List;

public interface RoomService {
    Phong getRoomByID(String roomID);
    List<Phong> getAll();
    List<RoomFurnitureItem> getAllFurnitureInRoom(String roomID);
    Phong createRoom(RoomCreationEvent room);
    Phong updateRoom(RoomModificationEvent room);
    boolean deleteRoomByID(String roomID);
    boolean completeCleaning(String roomId);

    List<Phong> getRoomsByPeopleCount(int people);
    List<Phong> getRoomsByStatus(String status);
    List<Phong> getRoomsByPhanLoai(String phanLoai);
    LoaiPhong getRoomCategoryByID(String id);
    double[] getLatestPriceForLoaiPhong(String maLoaiPhong);
    CongViec getCurrentJobForRoom(String maPhong);

    List<LoaiPhong> getAllRoomCategories();

    List<RoomFurnitureItem> getFurnitureForLoaiPhong(String maLoaiPhong);
    List<Phong> getAllQuanLyPhongPanel();
}

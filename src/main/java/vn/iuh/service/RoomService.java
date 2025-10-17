package vn.iuh.service;

import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.dto.repository.RoomFurnitureItem;
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
}

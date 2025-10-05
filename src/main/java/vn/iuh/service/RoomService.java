package vn.iuh.service;

import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.entity.Phong;

import java.util.List;

public interface RoomService {
    Phong getRoomByID(String roomID);
    List<Phong> getAll();
    Phong createRoom(RoomCreationEvent room);
    Phong updateRoom(RoomModificationEvent room);
    boolean deleteRoomByID(String roomID);
}

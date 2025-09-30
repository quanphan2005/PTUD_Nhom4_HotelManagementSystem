package vn.iuh.servcie;

import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.entity.Room;

import java.util.List;

public interface RoomService {
    Room getRoomByID(String roomID);
    List<Room> getAll();
    Room createRoom(RoomCreationEvent room);
    Room updateRoom(RoomModificationEvent room);
    boolean deleteRoomByID(String roomID);
}

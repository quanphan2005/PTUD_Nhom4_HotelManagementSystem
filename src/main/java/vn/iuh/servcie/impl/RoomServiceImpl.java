package vn.iuh.servcie.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.RoomDAO;
import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.entity.Room;
import vn.iuh.servcie.RoomService;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class RoomServiceImpl implements RoomService {
    private final RoomDAO roomDAO;

    public RoomServiceImpl() {
        roomDAO = new RoomDAO();
    }

    public RoomServiceImpl(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    @Override
    public Room getRoomByID(String roomID) {
        Room room = roomDAO.findRoomByID(roomID);
        if (room == null) {
            System.out.println("Room with ID " + roomID + " not found.");
            return null;
        } else {
            return room;
        }
    }

    @Override
    public List<Room> getAll() {
        List<Room> rooms = roomDAO.findAll();
        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return null;
        } else {
            return rooms;
        }
    }

    @Override
    public Room createRoom(RoomCreationEvent room) {
        Room lastedRoom = roomDAO.findLastRoom();
        String newID = EntityUtil.increaseEntityID(
                lastedRoom.getId(),
                EntityIDSymbol.ROOM_PREFIX.getPrefix(),
                EntityIDSymbol.ROOM_PREFIX.getLength());

        Room newRoom = new Room(
                newID,
                room.getRoomName(),
                room.getIsActive(),
                new Timestamp(new Date().getTime()),
                room.getNote(),
                room.getRoomDescription(),
                room.getRoomCategoryId()
        );

        return roomDAO.insertRoom(newRoom);
    }

    @Override
    public Room updateRoom(RoomModificationEvent room) {
        Room existingRoom = roomDAO.findRoomByID(room.getId());
        if (existingRoom == null) {
            System.out.println("Room with ID " + room.getId() + " not found.");
            return null;
        }

        existingRoom.setRoomName(room.getRoomName());
        existingRoom.setActive(existingRoom.isActive());
        existingRoom.setNote(room.getNote());
        existingRoom.setRoomDescription(room.getRoomDescription());
        existingRoom.setRoomCategoryId(room.getRoomCategoryId());

        return roomDAO.updateRoom(existingRoom);
    }

    @Override
    public boolean deleteRoomByID(String roomID) {
        return roomDAO.deleteRoomByID(roomID);
    }
}
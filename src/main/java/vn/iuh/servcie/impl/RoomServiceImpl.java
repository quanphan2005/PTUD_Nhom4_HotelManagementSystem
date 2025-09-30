package vn.iuh.servcie.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.RoomDAO;
import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.entity.Phong;
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
    public Phong getRoomByID(String roomID) {
        Phong phong = roomDAO.findRoomByID(roomID);
        if (phong == null) {
            System.out.println("Room with ID " + roomID + " not found.");
            return null;
        } else {
            return phong;
        }
    }

    @Override
    public List<Phong> getAll() {
        List<Phong> phongs = roomDAO.findAll();
        if (phongs.isEmpty()) {
            System.out.println("No rooms found.");
            return null;
        } else {
            return phongs;
        }
    }

    @Override
    public Phong createRoom(RoomCreationEvent room) {
        Phong lastedPhong = roomDAO.findLastRoom();
        String newID = EntityUtil.increaseEntityID(
                lastedPhong.getMaPhong(),
                EntityIDSymbol.ROOM_PREFIX.getPrefix(),
                EntityIDSymbol.ROOM_PREFIX.getLength());

        Phong newPhong = new Phong(
                newID,
                room.getRoomName(),
                room.getIsActive(),
                new Timestamp(new Date().getTime()),
                room.getNote(),
                room.getRoomDescription(),
                room.getRoomCategoryId()
        );

        return roomDAO.insertRoom(newPhong);
    }

    @Override
    public Phong updateRoom(RoomModificationEvent room) {
        Phong existingPhong = roomDAO.findRoomByID(room.getId());
        if (existingPhong == null) {
            System.out.println("Room with ID " + room.getId() + " not found.");
            return null;
        }

        existingPhong.setTenPhong(room.getRoomName());
        existingPhong.setDangHoatDong(existingPhong.isDangHoatDong());
        existingPhong.setGhiChu(room.getNote());
        existingPhong.setMoTaPhong(room.getRoomDescription());
        existingPhong.setMaLoaiPhong(room.getRoomCategoryId());

        return roomDAO.updateRoom(existingPhong);
    }

    @Override
    public boolean deleteRoomByID(String roomID) {
        return roomDAO.deleteRoomByID(roomID);
    }
}
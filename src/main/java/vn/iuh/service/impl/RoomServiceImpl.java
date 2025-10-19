package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.PhongDAO;
import vn.iuh.dto.event.create.RoomCreationEvent;
import vn.iuh.dto.event.update.RoomModificationEvent;
import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.Phong;
import vn.iuh.service.RoomService;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class RoomServiceImpl implements RoomService {
    private final PhongDAO phongDAO;
    private final CongViecDAO congViecDAO;

    public RoomServiceImpl() {
        phongDAO = new PhongDAO();
        congViecDAO = new CongViecDAO();
    }

    public RoomServiceImpl(PhongDAO phongDAO, CongViecDAO congViecDAO) {
        this.phongDAO = phongDAO;
        this.congViecDAO = congViecDAO;
    }

    @Override
    public Phong getRoomByID(String roomID) {
        Phong phong = phongDAO.timPhong(roomID);
        if (phong == null) {
            System.out.println("Room with ID " + roomID + " not found.");
            return null;
        } else {
            return phong;
        }
    }

    @Override
    public List<Phong> getAll() {
        List<Phong> phongs = phongDAO.timTatCaPhong();
        if (phongs.isEmpty()) {
            System.out.println("No rooms found.");
            return null;
        } else {
            return phongs;
        }
    }

    @Override
    public List<RoomFurnitureItem> getAllFurnitureInRoom(String roomID) {
        return phongDAO.timTatCaNoiThatTrongPhong(roomID);
    }

    @Override
    public Phong createRoom(RoomCreationEvent room) {
        Phong lastedPhong = phongDAO.timPhongMoiNhat();
        String newID = EntityUtil.increaseEntityID(
                lastedPhong.getMaPhong(),
                EntityIDSymbol.ROOM_PREFIX.getPrefix(),
                EntityIDSymbol.ROOM_PREFIX.getLength());

        Phong newPhong = new Phong(
                newID,
                room.getRoomName(),
                true,
                room.getNote(),
                room.getRoomDescription(),
                room.getRoomCategoryId(),
                new Timestamp(new Date().getTime())
        );

        return phongDAO.themPhong(newPhong);
    }

    @Override
    public Phong updateRoom(RoomModificationEvent room) {
        Phong existingPhong = phongDAO.timPhong(room.getId());
        if (existingPhong == null) {
            System.out.println("Room with ID " + room.getId() + " not found.");
            return null;
        }

        existingPhong.setTenPhong(room.getRoomName());
        existingPhong.setDangHoatDong(existingPhong.isDangHoatDong());
        existingPhong.setGhiChu(room.getNote());
        existingPhong.setMoTaPhong(room.getRoomDescription());
        existingPhong.setMaLoaiPhong(room.getRoomCategoryId());

        return phongDAO.capNhatPhong(existingPhong);
    }

    @Override
    public boolean deleteRoomByID(String roomID) {
        return phongDAO.xoaPhong(roomID);
    }

    public boolean completeCleaning(String roomID) {
        CongViec congViec = congViecDAO.layCongViecHienTaiCuaPhong(roomID);
        System.out.println("Hoàn tất dọn dẹp phòng: " + roomID);
        return congViecDAO.removeJob(congViec.getMaCongViec());
    }
}
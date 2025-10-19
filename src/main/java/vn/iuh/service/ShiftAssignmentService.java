package vn.iuh.service;

import vn.iuh.entity.PhienDangNhap;

import java.sql.Timestamp;

public interface ShiftAssignmentService {
    PhienDangNhap getShiftAssignmentByID(String id);
    PhienDangNhap createShiftAssignment(PhienDangNhap phienDangNhap);
    PhienDangNhap updateShiftAssignment(PhienDangNhap phienDangNhap);
    boolean deleteShiftAssignmentByID(String id);
    void updateFinishingTime(String id, Timestamp tgketthuc);
}
package vn.iuh.service.impl;

import vn.iuh.dao.PhienDangNhapDAO;
import vn.iuh.entity.PhienDangNhap;
import vn.iuh.service.ShiftAssignmentService;

import java.sql.Timestamp;

public class PhienDangNhapServiceImpl implements ShiftAssignmentService {
    private final PhienDangNhapDAO phienDangNhapDAO;

    public PhienDangNhapServiceImpl(){
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }
    @Override
    public PhienDangNhap getShiftAssignmentByID(String id) {
        return phienDangNhapDAO.timPhienDangNhap(id);
    }


    @Override
    public PhienDangNhap createShiftAssignment(PhienDangNhap phienDangNhap) {
        return null;
    }

    @Override
    public PhienDangNhap updateShiftAssignment(PhienDangNhap phienDangNhap) {
        return null;
    }

    @Override
    public boolean deleteShiftAssignmentByID(String id) {
        return false;
    }

    @Override
    public void updateFinishingTime(String id, Timestamp tgketthuc) {
        phienDangNhapDAO.capNhatThoiGianKetThuc(id, tgketthuc);
    }
}

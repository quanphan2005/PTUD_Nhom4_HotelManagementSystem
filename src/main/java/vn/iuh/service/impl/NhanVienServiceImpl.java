package vn.iuh.service.impl;

import vn.iuh.dao.NhanVienDAO;
import vn.iuh.entity.NhanVien;
import vn.iuh.service.EmployeeService;

import java.util.List;

public class NhanVienServiceImpl implements EmployeeService {
    private final NhanVienDAO nhanVienDAO;

    public NhanVienServiceImpl(){ this.nhanVienDAO = new NhanVienDAO();}


    @Override
    public NhanVien getEmployeeByID(String id) {
        return null;
    }

    @Override
    public NhanVien createEmployee(NhanVien nhanVien) {
        return null;
    }

    @Override
    public NhanVien updateEmployee(NhanVien nhanVien) {
        return null;
    }

    @Override
    public boolean deleteEmployeeByID(String id) {
        return false;
    }

}

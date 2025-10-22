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
        return nhanVienDAO.timNhanVien(id);
    }

    @Override
    public List<NhanVien> getEmployeeByName(String name) {
        return nhanVienDAO.timNhanVienBangTen(name);
    }

    @Override
    public NhanVien getEmployeeByCCCD(String cccd) {
        return nhanVienDAO.timNhanVienBangCCCD(cccd);
    }

    @Override
    public NhanVien getEmployeeBySDT(String sdt) {
        return nhanVienDAO.timNhanVienBangSDT(sdt);
    }

    @Override
    public NhanVien createEmployee(NhanVien nhanVien) {
        return nhanVienDAO.themNhanVien(nhanVien);
    }

    @Override
    public NhanVien updateEmployee(NhanVien nhanVien) {
        return nhanVienDAO.capNhatNhanVien(nhanVien);
    }

    @Override
    public boolean deleteEmployeeByID(String id) {
        return nhanVienDAO.xoaNhanVien(id);
    }

    @Override
    public List<NhanVien> getAllEmployee() {
        return nhanVienDAO.layDanhSachNhanVien();
    }


}

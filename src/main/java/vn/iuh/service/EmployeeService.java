package vn.iuh.service;

import vn.iuh.entity.NhanVien;

import java.util.List;

public interface EmployeeService {
    NhanVien getEmployeeByID(String id);
    List<NhanVien> getEmployeeByName(String name);
    NhanVien getEmployeeByCCCD(String cccd);
    NhanVien getEmployeeBySDT(String sdt);
    NhanVien createEmployee(NhanVien nhanVien);
    NhanVien updateEmployee(NhanVien nhanVien);
    boolean deleteEmployeeByID(String id);
    List<NhanVien> getAllEmployee();
}

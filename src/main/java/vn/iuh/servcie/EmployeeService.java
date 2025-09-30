package vn.iuh.servcie;

import vn.iuh.entity.NhanVien;

public interface EmployeeService {
    NhanVien getEmployeeByID(String id);
    NhanVien createEmployee(NhanVien nhanVien);
    NhanVien updateEmployee(NhanVien nhanVien);
    boolean deleteEmployeeByID(String id);
}

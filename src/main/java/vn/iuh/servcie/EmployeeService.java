package vn.iuh.servcie;

import vn.iuh.entity.Employee;

public interface EmployeeService {
    Employee getEmployeeByID(String id);
    Employee createEmployee(Employee employee);
    Employee updateEmployee(Employee employee);
    boolean deleteEmployeeByID(String id);
}

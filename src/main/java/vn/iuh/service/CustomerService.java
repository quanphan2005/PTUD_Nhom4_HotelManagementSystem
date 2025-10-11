package vn.iuh.service;

import vn.iuh.entity.KhachHang;

public interface CustomerService {
    KhachHang getCustomerByID(String id);
    KhachHang getCustomerByCCCD(String cccd);
    KhachHang createCustomer(KhachHang khachHang);
    KhachHang updateCustomer(KhachHang khachHang);
    boolean deleteCustomerByID(String id);
}

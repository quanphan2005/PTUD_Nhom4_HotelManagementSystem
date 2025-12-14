package vn.iuh.service;

import vn.iuh.dto.response.BookingResponseV2;
import vn.iuh.entity.KhachHang;

import java.util.List;

public interface CustomerService {
    KhachHang getCustomerByID(String id);
    KhachHang getCustomerByCCCD(String cccd);
    KhachHang createCustomer(KhachHang khachHang);
    KhachHang updateCustomer(KhachHang khachHang);
    boolean deleteCustomerByID(String id);

    List<KhachHang> layTatCaKhachHang();
    List<BookingResponseV2> layDonDatPhongCuaKhach(String maKhachHang);
    KhachHang createCustomerV2(KhachHang khachHang);
    KhachHang updateCustomerV2(KhachHang khachHang);
    boolean deleteCustomerByIDV2(String id);

}

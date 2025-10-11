package vn.iuh.service.impl;

import vn.iuh.dao.KhachHangDAO;
import vn.iuh.entity.KhachHang;
import vn.iuh.service.CustomerService;

public class CustomerServiceImpl implements CustomerService {
    private final KhachHangDAO khachHangDAO;

    public CustomerServiceImpl() {
        this.khachHangDAO = new KhachHangDAO();
    }

    public CustomerServiceImpl(KhachHangDAO khachHangDAO) {
        this.khachHangDAO = khachHangDAO;
    }

    @Override
    public KhachHang getCustomerByID(String id) {
        return null;
    }

    @Override
    public KhachHang getCustomerByCCCD(String cccd) {
        return khachHangDAO.timKhachHangBangCCCD(cccd);
    }

    @Override
    public KhachHang createCustomer(KhachHang khachHang) {
        return null;
    }

    @Override
    public KhachHang updateCustomer(KhachHang khachHang) {
        return null;
    }

    @Override
    public boolean deleteCustomerByID(String id) {
        return false;
    }
}

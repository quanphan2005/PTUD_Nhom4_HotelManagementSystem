package vn.iuh.service;

import vn.iuh.entity.TaiKhoan;

public interface AccountService {
    TaiKhoan getAccountByID(String id);
    TaiKhoan createAccount(TaiKhoan taiKhoan);
    TaiKhoan updateAccount(TaiKhoan taiKhoan);
    boolean deleteAccountByID(String id);
}

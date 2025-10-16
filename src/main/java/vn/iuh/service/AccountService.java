package vn.iuh.service;

import vn.iuh.dto.event.create.LoginEvent;
import vn.iuh.dto.response.AccountResponse;
import vn.iuh.entity.TaiKhoan;

import javax.swing.table.DefaultTableModel;

public interface AccountService {
    TaiKhoan getAccountByID(String id);
    TaiKhoan createAccount(TaiKhoan taiKhoan);
    TaiKhoan updateAccount(TaiKhoan taiKhoan);
    boolean deleteAccountByID(String id);
    AccountResponse getAllAccount();
    void loadTableTaiKhoan(DefaultTableModel model);
    void loadTableNhanVienChuaCoTaiKhoan(DefaultTableModel model);
    boolean handleLogin(LoginEvent loginEvent);
}

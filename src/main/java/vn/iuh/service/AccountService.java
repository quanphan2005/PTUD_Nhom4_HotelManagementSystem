package vn.iuh.service;

import vn.iuh.dto.event.create.LoginEvent;
import vn.iuh.entity.TaiKhoan;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public interface AccountService {
    TaiKhoan getAccountByID(String id);
    TaiKhoan createAccount(TaiKhoan taiKhoan);
    TaiKhoan updateAccount(TaiKhoan taiKhoan);
    boolean deleteAccountByID(String id);
    List<TaiKhoan> getAllAccount();
    boolean handleLogin(LoginEvent loginEvent);
}

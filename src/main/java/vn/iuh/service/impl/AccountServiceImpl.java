package vn.iuh.service.impl;

import vn.iuh.config.SecurityConfig;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.dto.event.create.LoginEvent;
import vn.iuh.dto.response.AccountResponse;
import vn.iuh.entity.NhanVien;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.service.AccountService;

import javax.swing.table.DefaultTableModel;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class AccountServiceImpl implements AccountService {
    private final TaiKhoanDAO taiKhoanDAO;
    public AccountServiceImpl() {this.taiKhoanDAO = new TaiKhoanDAO(); }


    @Override
    public TaiKhoan getAccountByID(String id) {
        return null;
    }

    @Override
    public TaiKhoan createAccount(TaiKhoan taiKhoan) {
        return null;
    }

    @Override
    public TaiKhoan updateAccount(TaiKhoan taiKhoan) {
        return null;
    }

    @Override
    public boolean deleteAccountByID(String id) {
        return false;
    }

    @Override
    public List<TaiKhoan> getAllAccount() {
        List<TaiKhoan> tk = taiKhoanDAO.getAllTaiKhoan();
        return tk;
    }

    // handle login
    @Override
    public boolean handleLogin(LoginEvent loginEvent){
        TaiKhoan taiKhoan = taiKhoanDAO.timTaiKhoanBangUserName(loginEvent.getUserName());
        if(taiKhoan != null){
            String hassedPassFromDb =taiKhoan.getMatKhau();
            String inputPassword = loginEvent.getPassWord();

            if(SecurityConfig.checkPassword(inputPassword, hassedPassFromDb)){
                System.out.println("Đăng nhập thành công");
                return true;
            }else{
                System.out.println("Sai mật khẩu");
                return false;
            }
        }else{
            System.out.print("Tài khoản không tồn tại");
            return false;
        }
    }

}

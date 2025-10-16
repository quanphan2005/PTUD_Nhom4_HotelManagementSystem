package vn.iuh.service.impl;

import vn.iuh.config.SecurityConfig;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.dto.event.create.LoginEvent;
import vn.iuh.dto.response.AccountResponse;
import vn.iuh.entity.NhanVien;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.service.AccountService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    public AccountResponse getAllAccount() {
        List<TaiKhoan> tk = taiKhoanDAO.getAllTaiKhoan();
        if(tk.isEmpty()){
            return new AccountResponse(false, null);
        }
        return new AccountResponse(true, tk);
    }


    public void loadTableTaiKhoan(DefaultTableModel model) {
        model.setRowCount(0);
        String[] columns = {"Mã tài khoản", "Tên tài khoản", "Chức vụ", "Mã nhân viên"};
        model.setColumnIdentifiers(columns);

        for (TaiKhoan tk : taiKhoanDAO.getAllTaiKhoan()) {
            model.addRow(new Object[]{
                    tk.getMaTaiKhoan(),
                    tk.getTenDangNhap(),
                    tk.getMaChucVu().trim().equals("CV001") ? "Lễ tân" : "Quản lý",
                    tk.getMaNhanVien()
            });
        }
    }

    public void loadTableNhanVienChuaCoTaiKhoan(DefaultTableModel model) {
        model.setRowCount(0);
        String[] columns = {"Mã", "Tên nhân viên", "CCCD", "Số điện thoại"};
        model.setColumnIdentifiers(columns);

        for (NhanVien nv : List.of(new NhanVienDAO().timNhanVienMoiNhat())) {
            model.addRow(new Object[]{
                    nv.getMaNhanVien(),
                    nv.getTenNhanVien(),
                    nv.getCCCD(),
                    nv.getSoDienThoai()
            });
        }
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

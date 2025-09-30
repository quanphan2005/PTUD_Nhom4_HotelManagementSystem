package vn.iuh.entity;

import java.sql.Timestamp;

public class TaiKhoan {
    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private String maChucVu;
    private String ma_nhan_vien;
    private Timestamp thoi_gian_tao;

    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String tenDangNhap, String matKhau, String ma_chuc_vu, String ma_nhan_vien, Timestamp thoi_gian_tao) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.ma_chuc_vu = ma_chuc_vu;
        this.ma_nhan_vien = ma_nhan_vien;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getMa_chuc_vu() {
        return ma_chuc_vu;
    }

    public void setMa_chuc_vu(String role) {
        this.ma_chuc_vu = role;
    }

    public String getMa_nhan_vien() {
        return ma_nhan_vien;
    }

    public void setMa_nhan_vien(String ma_nhan_vien) {
        this.ma_nhan_vien = ma_nhan_vien;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

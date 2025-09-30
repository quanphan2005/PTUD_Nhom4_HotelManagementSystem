package vn.iuh.entity;

import java.sql.Timestamp;

public class TaiKhoan {
    private String ma_tai_khoan;
    private String ten_dang_nhap;
    private String mat_khau;
    private String ma_chuc_vu;
    private String ma_nhan_vien;
    private Timestamp thoi_gian_tao;

    public TaiKhoan() {
    }

    public TaiKhoan(String ma_tai_khoan, String ten_dang_nhap, String mat_khau, String ma_chuc_vu, String ma_nhan_vien, Timestamp thoi_gian_tao) {
        this.ma_tai_khoan = ma_tai_khoan;
        this.ten_dang_nhap = ten_dang_nhap;
        this.mat_khau = mat_khau;
        this.ma_chuc_vu = ma_chuc_vu;
        this.ma_nhan_vien = ma_nhan_vien;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_tai_khoan() {
        return ma_tai_khoan;
    }

    public void setMa_tai_khoan(String ma_tai_khoan) {
        this.ma_tai_khoan = ma_tai_khoan;
    }

    public String getTen_dang_nhap() {
        return ten_dang_nhap;
    }

    public void setTen_dang_nhap(String ten_dang_nhap) {
        this.ten_dang_nhap = ten_dang_nhap;
    }

    public String getMat_khau() {
        return mat_khau;
    }

    public void setMat_khau(String mat_khau) {
        this.mat_khau = mat_khau;
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

package vn.iuh.entity;

import java.sql.Timestamp;

public class PhienDangNhap {
    private String maPhienDangNhap;
    private int soQuay;
    private Timestamp tgBatDau;
    private Timestamp tgKetThuc;
    private String maTaiKhoan;
    private Timestamp thoiGianTao;

    public PhienDangNhap() {
    }

    public PhienDangNhap(String maPhienDangNhap, int soQuay, Timestamp tgBatDau, Timestamp tgKetThuc, String maTaiKhoan,
                         Timestamp thoiGianTao) {
        this.maPhienDangNhap = maPhienDangNhap;
        this.soQuay = soQuay;
        this.tgBatDau = tgBatDau;
        this.tgKetThuc = tgKetThuc;
        this.maTaiKhoan = maTaiKhoan;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public int getSoQuay() {
        return soQuay;
    }

    public void setSoQuay(int soQuay) {
        this.soQuay = soQuay;
    }

    public Timestamp getTgBatDau() {
        return tgBatDau;
    }

    public void setTgBatDau(Timestamp tgBatDau) {
        this.tgBatDau = tgBatDau;
    }

    public Timestamp getTgKetThuc() {
        return tgKetThuc;
    }

    public void setTgKetThuc(Timestamp tgKetThuc) {
        this.tgKetThuc = tgKetThuc;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

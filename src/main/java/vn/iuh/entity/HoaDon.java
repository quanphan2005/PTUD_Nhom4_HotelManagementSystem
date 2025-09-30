package vn.iuh.entity;

import java.sql.Timestamp;

public class HoaDon {
    private String ma_hoa_don;
    private String phuong_thuc_thanh_toan;
    private double tong_tien;
    private double tien_thue;
    private double tong_hoa_don;
    private String kieu_hoa_don;
    private String tinh_trang_thanh_toan;
    private String ma_phien_dang_nhap;
    private String ma_don_dat_phong;
    private String ma_khach_hang;
    private Timestamp thoi_gian_tao;

    public HoaDon() {
    }

    public HoaDon(String ma_hoa_don, String phuong_thuc_thanh_toan, double tong_tien, double tien_thue, double tong_hoa_don, String kieu_hoa_don, String tinh_trang_thanh_toan, String ma_phien_dang_nhap, String ma_don_dat_phong, String ma_khach_hang, Timestamp thoi_gian_tao) {
        this.ma_hoa_don = ma_hoa_don;
        this.phuong_thuc_thanh_toan = phuong_thuc_thanh_toan;
        this.tong_tien = tong_tien;
        this.tien_thue = tien_thue;
        this.tong_hoa_don = tong_hoa_don;
        this.kieu_hoa_don = kieu_hoa_don;
        this.tinh_trang_thanh_toan = tinh_trang_thanh_toan;
        this.ma_phien_dang_nhap = ma_phien_dang_nhap;
        this.ma_don_dat_phong = ma_don_dat_phong;
        this.ma_khach_hang = ma_khach_hang;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_hoa_don() {
        return ma_hoa_don;
    }

    public void setMa_hoa_don(String ma_hoa_don) {
        this.ma_hoa_don = ma_hoa_don;
    }

    public String getPhuong_thuc_thanh_toan() {
        return phuong_thuc_thanh_toan;
    }

    public void setPhuong_thuc_thanh_toan(String phuong_thuc_thanh_toan) {
        this.phuong_thuc_thanh_toan = phuong_thuc_thanh_toan;
    }

    public double getTong_tien() {
        return tong_tien;
    }

    public void setTong_tien(double tong_tien) {
        this.tong_tien = tong_tien;
    }

    public double getTien_thue() {
        return tien_thue;
    }

    public void setTien_thue(double tien_thue) {
        this.tien_thue = tien_thue;
    }

    public double getTong_hoa_don() {
        return tong_hoa_don;
    }

    public void setTong_hoa_don(double tong_hoa_don) {
        this.tong_hoa_don = tong_hoa_don;
    }

    public String getKieu_hoa_don() {
        return kieu_hoa_don;
    }

    public void setKieu_hoa_don(String kieu_hoa_don) {
        this.kieu_hoa_don = kieu_hoa_don;
    }

    public String getTinh_trang_thanh_toan() {
        return tinh_trang_thanh_toan;
    }

    public void setTinh_trang_thanh_toan(String tinh_trang_thanh_toan) {
        this.tinh_trang_thanh_toan = tinh_trang_thanh_toan;
    }

    public String getMa_phien_dang_nhap() {
        return ma_phien_dang_nhap;
    }

    public void setMa_phien_dang_nhap(String ma_phien_dang_nhap) {
        this.ma_phien_dang_nhap = ma_phien_dang_nhap;
    }

    public String getMa_don_dat_phong() {
        return ma_don_dat_phong;
    }

    public void setMa_don_dat_phong(String ma_don_dat_phong) {
        this.ma_don_dat_phong = ma_don_dat_phong;
    }

    public String getMa_khach_hang() {
        return ma_khach_hang;
    }

    public void setMa_khach_hang(String ma_khach_hang) {
        this.ma_khach_hang = ma_khach_hang;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

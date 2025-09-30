package vn.iuh.entity;

import java.sql.Timestamp;

public class KhachHang {
    private String ma_khach_hang;
    private String CCCD;
    private String ten_khach_hang;
    private String so_dien_thoai;
    private Timestamp thoi_gian_tao;



    public KhachHang() {
    }

    public KhachHang(String ma_khach_hang, String CCCD, String ten_khach_hang, String so_dien_thoai, Timestamp thoi_gian_tao) {
        this.ma_khach_hang = ma_khach_hang;
        this.CCCD = CCCD;
        this.ten_khach_hang = ten_khach_hang;
        this.so_dien_thoai = so_dien_thoai;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_khach_hang() {
        return ma_khach_hang;
    }

    public void setMa_khach_hang(String ma_khach_hang) {
        this.ma_khach_hang = ma_khach_hang;
    }

    public String getTen_khach_hang() {
        return ten_khach_hang;
    }

    public void setTen_khach_hang(String ten_khach_hang) {
        this.ten_khach_hang = ten_khach_hang;
    }

    public String getSo_dien_thoai() {
        return so_dien_thoai;
    }

    public void setSo_dien_thoai(String so_dien_thoai) {
        this.so_dien_thoai = so_dien_thoai;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

package vn.iuh.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class NhanVien {
    private String ma_nhan_vien;
    private String ten_nhan_vien;
    private String CCCD;
    private Date ngay_sinh;
    private String so_dien_thoai;
    private Timestamp thoi_gian_tao;

    public NhanVien() {
    }

    public NhanVien(String ma_nhan_vien, String ten_nhan_vien, String CCCD, Date ngay_sinh, String so_dien_thoai, Timestamp thoi_gian_tao) {
        this.ma_nhan_vien = ma_nhan_vien;
        this.ten_nhan_vien = ten_nhan_vien;
        this.CCCD = CCCD;
        this.ngay_sinh = ngay_sinh;
        this.so_dien_thoai = so_dien_thoai;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    public String getMa_nhan_vien() {
        return ma_nhan_vien;
    }

    public void setMa_nhan_vien(String ma_nhan_vien) {
        this.ma_nhan_vien = ma_nhan_vien;
    }

    public String getTen_nhan_vien() {
        return ten_nhan_vien;
    }

    public void setTen_nhan_vien(String ten_nhan_vien) {
        this.ten_nhan_vien = ten_nhan_vien;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public Date getNgay_sinh() {
        return ngay_sinh;
    }

    public void setNgay_sinh(Date ngay_sinh) {
        this.ngay_sinh = ngay_sinh;
    }

    public String getSo_dien_thoai() {
        return so_dien_thoai;
    }

    public void setSo_dien_thoai(String so_dien_thoai) {
        this.so_dien_thoai = so_dien_thoai;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}
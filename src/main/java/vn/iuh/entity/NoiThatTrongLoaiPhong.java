package vn.iuh.entity;

import java.sql.Timestamp;

public class NoiThatTrongLoaiPhong {
    private String ma_noi_that_trong_loai_phong;
    private int so_luong;
    private String ma_loai_phong;
    private String ma_noi_that;
    private Timestamp thoi_gian_tao;

    public NoiThatTrongLoaiPhong() {
    }

    public NoiThatTrongLoaiPhong(String ma_noi_that_trong_loai_phong, int so_luong, String ma_loai_phong, String ma_noi_that, Timestamp thoi_gian_tao) {
        this.ma_noi_that_trong_loai_phong = ma_noi_that_trong_loai_phong;
        this.so_luong = so_luong;
        this.ma_loai_phong = ma_loai_phong;
        this.ma_noi_that = ma_noi_that;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_noi_that_trong_loai_phong() {
        return ma_noi_that_trong_loai_phong;
    }

    public void setMa_noi_that_trong_loai_phong(String ma_noi_that_trong_loai_phong) {
        this.ma_noi_that_trong_loai_phong = ma_noi_that_trong_loai_phong;
    }

    public int getSo_luong() {
        return so_luong;
    }

    public void setSo_luong(int so_luong) {
        this.so_luong = so_luong;
    }

    public String getMa_loai_phong() {
        return ma_loai_phong;
    }

    public void setMa_loai_phong(String ma_loai_phong) {
        this.ma_loai_phong = ma_loai_phong;
    }

    public String getMa_noi_that() {
        return ma_noi_that;
    }

    public void setMa_noi_that(String ma_noi_that) {
        this.ma_noi_that = ma_noi_that;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}
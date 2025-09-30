package vn.iuh.entity;

import java.sql.Timestamp;

public class Phong {
    private String maPhong;
    private String tenPhong;
    private boolean dangHoatDong;
    private String ghiChu;
    private String moTaPhong;
    private String maLoaiPhong;
    private Timestamp thoiGianTao;

    public Phong() {
    }

    public Phong(String maPhong, String tenPhong, boolean dangHoatDong, String ghiChu, String moTaPhong,
                 String maLoaiPhong,
                 Timestamp thoiGianTao) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.dangHoatDong = dangHoatDong;
        this.ghiChu = ghiChu;
        this.moTaPhong = moTaPhong;
        this.maLoaiPhong = maLoaiPhong;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean dangHoatDong) {
        this.dangHoatDong = dangHoatDong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getMoTaPhong() {
        return moTaPhong;
    }

    public void setMoTaPhong(String moTaPhong) {
        this.moTaPhong = moTaPhong;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
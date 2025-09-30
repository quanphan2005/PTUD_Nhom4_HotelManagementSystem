package vn.iuh.entity;

import java.sql.Timestamp;

public class BienBan {
    private String maBienBan;
    private String liDo;
    private Double phiBienBan;
    private String maChiTietDatPhong;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    public BienBan() {
    }

    public BienBan(String maBienBan, String liDo, Double phiBienBan, String maChiTietDatPhong, String maPhienDangNhap, Timestamp thoiGianTao) {
        this.maBienBan = maBienBan;
        this.liDo = liDo;
        this.phiBienBan = phiBienBan;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaBienBan() {
        return maBienBan;
    }

    public void setMaBienBan(String maBienBan) {
        this.maBienBan = maBienBan;
    }

    public String getLiDo() {
        return liDo;
    }

    public void setLiDo(String liDo) {
        this.liDo = liDo;
    }

    public Double getPhiBienBan() {
        return phiBienBan;
    }

    public void setPhiBienBan(Double phiBienBan) {
        this.phiBienBan = phiBienBan;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

package vn.iuh.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class GiaDichVu {
    private String maGiaDichVu;
    private double giaCu;
    private double giaMoi;
    private Timestamp thoiGianTao;
    private String maPhienDangNhap;
    private String maDichVu;

    public GiaDichVu() {}

    public GiaDichVu(String maGiaDichVu, double giaCu, double giaMoi, Timestamp thoiGianTao, String maPhienDangNhap,
                     String maDichVu) {
        this.maGiaDichVu = maGiaDichVu;
        this.giaCu = giaCu;
        this.giaMoi = giaMoi;
        this.thoiGianTao = thoiGianTao;
        this.maPhienDangNhap = maPhienDangNhap;
        this.maDichVu = maDichVu;
    }

    public String getMaGiaDichVu() {
        return maGiaDichVu;
    }

    public void setMaGiaDichVu(String maGiaDichVu) {
        this.maGiaDichVu = maGiaDichVu;
    }

    public double getGiaCu() {
        return giaCu;
    }

    public void setGiaCu(double giaCu) {
        this.giaCu = giaCu;
    }

    public double getGiaMoi() {
        return giaMoi;
    }

    public void setGiaMoi(double giaMoi) {
        this.giaMoi = giaMoi;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }
}

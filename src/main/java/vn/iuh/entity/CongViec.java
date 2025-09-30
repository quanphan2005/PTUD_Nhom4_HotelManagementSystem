package vn.iuh.entity;

import java.sql.Timestamp;

public class CongViec {
    private String maCongViec;
    private String tenTrangThai;
    private Timestamp tgBatDau;
    private Timestamp tgKetThuc;
    private String maPhong;
    private Timestamp thoiGianTao;

    public CongViec(String maCongViec, String tenTrangThai, Timestamp tgBatDau, Timestamp tgKetThuc, String maPhong,
                    Timestamp thoiGianTao) {
        this.maCongViec = maCongViec;
        this.tenTrangThai = tenTrangThai;
        this.tgBatDau = tgBatDau;
        this.tgKetThuc = tgKetThuc;
        this.maPhong = maPhong;
        this.thoiGianTao = thoiGianTao;
    }

    public CongViec() {
    }

    public String getMaCongViec() {
        return maCongViec;
    }

    public void setMaCongViec(String maCongViec) {
        this.maCongViec = maCongViec;
    }

    public String getTenTrangThai() {
        return tenTrangThai;
    }

    public void setTenTrangThai(String tenTrangThai) {
        this.tenTrangThai = tenTrangThai;
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

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

package vn.iuh.entity;

import java.sql.Timestamp;

public class LichSuThaoTac {
    private String maLichSuThaoTac;
    private String tenThaoTac;
    private String moTa;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    public LichSuThaoTac() {
    }

    public LichSuThaoTac(String maLichSuThaoTac, String tenThaoTac, String moTa, String maPhienDangNhap,
                         Timestamp thoiGianTao) {
        this.maLichSuThaoTac = maLichSuThaoTac;
        this.tenThaoTac = tenThaoTac;
        this.moTa = moTa;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaLichSuThaoTac() {
        return maLichSuThaoTac;
    }

    public void setMaLichSuThaoTac(String maLichSuThaoTac) {
        this.maLichSuThaoTac = maLichSuThaoTac;
    }

    public String getTenThaoTac() {
        return tenThaoTac;
    }

    public void setTenThaoTac(String tenThaoTac) {
        this.tenThaoTac = tenThaoTac;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
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
}

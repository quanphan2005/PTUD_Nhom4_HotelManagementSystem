package vn.iuh.entity;

import java.sql.Timestamp;

public class LichSuRaNgoai {
    private String maLichSuRaNgoai;
    private boolean laLanCuoiCung;
    private String maChiTietDatPhong;
    private Timestamp thoiGianTao;

    public LichSuRaNgoai() {
    }

    public LichSuRaNgoai(String maLichSuRaNgoai, boolean laLanCuoiCung, String maChiTietDatPhong, Timestamp thoiGianTao) {
        this.maLichSuRaNgoai = maLichSuRaNgoai;
        this.laLanCuoiCung = laLanCuoiCung;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaLichSuRaNgoai() {
        return maLichSuRaNgoai;
    }

    public void setMaLichSuRaNgoai(String maLichSuRaNgoai) {
        this.maLichSuRaNgoai = maLichSuRaNgoai;
    }

    public boolean isLaLanCuoiCung() {
        return laLanCuoiCung;
    }

    public void setLaLanCuoiCung(boolean laLanCuoiCung) {
        this.laLanCuoiCung = laLanCuoiCung;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

package vn.iuh.entity;

import java.sql.Timestamp;

public class LichSuDiVao {
    private String maLichSuDiVao;
    private boolean laLanDauTien;
    private String maChiTietDatPhong;
    private Timestamp thoiGianTao;

    public LichSuDiVao() {
    }

    public LichSuDiVao(String maLichSuDiVao, boolean laLanDauTien, String maChiTietDatPhong, Timestamp thoiGianTao) {
        this.maLichSuDiVao = maLichSuDiVao;
        this.laLanDauTien = laLanDauTien;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaLichSuDiVao() {
        return maLichSuDiVao;
    }

    public void setMaLichSuDiVao(String maLichSuDiVao) {
        this.maLichSuDiVao = maLichSuDiVao;
    }

    public boolean getLaLanDauTien() {
        return laLanDauTien;
    }

    public void setLaLanDauTien(boolean laLanDauTien) {
        this.laLanDauTien = laLanDauTien;
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

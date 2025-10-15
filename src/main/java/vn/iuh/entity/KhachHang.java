package vn.iuh.entity;

import java.sql.Timestamp;

public class KhachHang {
    private String maKhachHang;
    private String CCCD;
    private String tenKhachHang;
    private String soDienThoai;
    private Timestamp thoiGianTao;

    public KhachHang() {
    }

    public KhachHang(String maKhachHang, String CCCD, String tenKhachHang, String soDienThoai, Timestamp thoiGianTao) {
        this.maKhachHang = maKhachHang;
        this.CCCD = CCCD;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.thoiGianTao = thoiGianTao;
    }

    @Override
    public String toString() {
        return "KhachHang{" +
               "maKhachHang='" + maKhachHang + '\'' +
               ", CCCD='" + CCCD + '\'' +
               ", tenKhachHang='" + tenKhachHang + '\'' +
               ", soDienThoai='" + soDienThoai + '\'' +
               ", thoiGianTao=" + thoiGianTao +
               '}';
    }

    // Getters and Setters
    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

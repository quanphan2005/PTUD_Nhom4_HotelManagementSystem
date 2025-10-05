package vn.iuh.entity;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private String maChiTietHoaDon;
    private String maHoaDon;
    private String maPhong;
    private String maChiTietDatPhong;
    private BigDecimal donGiaPhongHienTai;
    private double thoiGianSuDung;

    public String getMaChiTietHoaDon() {
        return maChiTietHoaDon;
    }

    public void setMaChiTietHoaDon(String maChiTietHoaDon) {
        this.maChiTietHoaDon = maChiTietHoaDon;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public BigDecimal getDonGiaPhongHienTai() {
        return donGiaPhongHienTai;
    }

    public void setDonGiaPhongHienTai(BigDecimal donGiaPhongHienTai) {
        this.donGiaPhongHienTai = donGiaPhongHienTai;
    }

    public double getThoiGianSuDung() {
        return thoiGianSuDung;
    }

    public void setThoiGianSuDung(double thoiGianSuDung) {
        this.thoiGianSuDung = thoiGianSuDung;
    }

    public BigDecimal tinhThanhTien() {
        return donGiaPhongHienTai.multiply(BigDecimal.valueOf(thoiGianSuDung));
    }
}

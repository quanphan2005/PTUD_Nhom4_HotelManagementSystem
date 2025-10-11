package vn.iuh.entity;

import java.math.BigDecimal;

public class PhongTinhPhuPhi {
    private String maPhongTinhPhuPhi;
    private String maChiTietDatPhong;
    private String maPhuPhi;
    private BigDecimal donGiaPhuPhi;

    public PhongTinhPhuPhi() {
    }

    public PhongTinhPhuPhi(String maPhongTinhPhuPhi, String maChiTietDatPhong, String maPhuPhi, BigDecimal donGiaPhuPhi) {
        this.maPhongTinhPhuPhi = maPhongTinhPhuPhi;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maPhuPhi = maPhuPhi;
        this.donGiaPhuPhi = donGiaPhuPhi;
    }

    public String getMaPhongTinhPhuPhi() {
        return maPhongTinhPhuPhi;
    }

    public void setMaPhongTinhPhuPhi(String maPhongTinhPhuPhi) {
        this.maPhongTinhPhuPhi = maPhongTinhPhuPhi;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getMaPhuPhi() {
        return maPhuPhi;
    }

    public void setMaPhuPhi(String maPhuPhi) {
        this.maPhuPhi = maPhuPhi;
    }

    public BigDecimal getDonGiaPhuPhi() {
        return donGiaPhuPhi;
    }

    public void setDonGiaPhuPhi(BigDecimal donGiaPhuPhi) {
        this.donGiaPhuPhi = donGiaPhuPhi;
    }
}

package vn.iuh.entity;

import vn.iuh.exception.BusinessException;

import java.math.BigDecimal;

public class PhongTinhPhuPhi {
    private String maPhongTinhPhuPhi;
    private String maChiTietDatPhong;
    private String maPhuPhi;
    private BigDecimal donGiaPhuPhi;
    private String tenPhuPhi;
    private String tenPhong;
    private BigDecimal tongTien;

    public BigDecimal getTongTien() {
        if(tongTien != null){
            return tongTien;
        }
        else {
            this.tongTien = this.donGiaPhuPhi;
            return this.tongTien;
        }
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public PhongTinhPhuPhi() {
    }

    public PhongTinhPhuPhi(String maPhongTinhPhuPhi, String maChiTietDatPhong, String maPhuPhi, BigDecimal donGiaPhuPhi) {
        this.maPhongTinhPhuPhi = maPhongTinhPhuPhi;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maPhuPhi = maPhuPhi;
        this.donGiaPhuPhi = donGiaPhuPhi;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public String getTenPhuPhi() {
        return tenPhuPhi;
    }

    public void setTenPhuPhi(String tenPhuPhi) {
        this.tenPhuPhi = tenPhuPhi;
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

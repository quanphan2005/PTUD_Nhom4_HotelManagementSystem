package vn.iuh.entity;

import java.sql.Timestamp;

public class GiaPhuPhi {
    private String ma_gia_phu_phi;
    private double gia_truoc_do;
    private double gia_hien_tai;
    private boolean la_phan_tram;
    private String ma_phien_dang_nhap;
    private String ma_phu_phi;
    private Timestamp thoi_gian_tao;

    public GiaPhuPhi() {
    }

    public GiaPhuPhi(String ma_gia_phu_phi, double gia_truoc_do, double gia_hien_tai, boolean la_phan_tram, String ma_phien_dang_nhap, String ma_phu_phi, Timestamp thoi_gian_tao) {
        this.ma_gia_phu_phi = ma_gia_phu_phi;
        this.gia_truoc_do = gia_truoc_do;
        this.gia_hien_tai = gia_hien_tai;
        this.la_phan_tram = la_phan_tram;
        this.ma_phien_dang_nhap = ma_phien_dang_nhap;
        this.ma_phu_phi = ma_phu_phi;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_gia_phu_phi() {
        return ma_gia_phu_phi;
    }

    public void setMa_gia_phu_phi(String ma_gia_phu_phi) {
        this.ma_gia_phu_phi = ma_gia_phu_phi;
    }

    public double getGia_truoc_do() {
        return gia_truoc_do;
    }

    public void setGia_truoc_do(double gia_truoc_do) {
        this.gia_truoc_do = gia_truoc_do;
    }

    public double getGia_hien_tai() {
        return gia_hien_tai;
    }

    public void setGia_hien_tai(double gia_hien_tai) {
        this.gia_hien_tai = gia_hien_tai;
    }

    public boolean getLa_phan_tram() {
        return la_phan_tram;
    }

    public void setLa_phan_tram(boolean la_phan_tram) {
        this.la_phan_tram = la_phan_tram;
    }

    public String getMa_phien_dang_nhap() {
        return ma_phien_dang_nhap;
    }

    public void setMa_phien_dang_nhap(String ma_phien_dang_nhap) {
        this.ma_phien_dang_nhap = ma_phien_dang_nhap;
    }

    public String getMa_phu_phi() {
        return ma_phu_phi;
    }

    public void setMa_phu_phi(String ma_phu_phi) {
        this.ma_phu_phi = ma_phu_phi;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

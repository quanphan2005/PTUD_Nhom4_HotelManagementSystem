package vn.iuh.entity;

import java.sql.Timestamp;

public class BienBan {
    private String ma_bien_ban;
    private String li_do;
    private Double phi_bien_ban;
    private String ma_chi_tiet_dat_phong;
    private String ma_phien_dang_nhap;
    private Timestamp thoi_gian_tao;

    public BienBan() {
    }

    public BienBan(String ma_bien_ban, String li_do, Double phi_bien_ban, String ma_chi_tiet_dat_phong, String ma_phien_dang_nhap, Timestamp thoi_gian_tao) {
        this.ma_bien_ban = ma_bien_ban;
        this.li_do = li_do;
        this.phi_bien_ban = phi_bien_ban;
        this.ma_chi_tiet_dat_phong = ma_chi_tiet_dat_phong;
        this.ma_phien_dang_nhap = ma_phien_dang_nhap;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_bien_ban() {
        return ma_bien_ban;
    }

    public void setMa_bien_ban(String ma_bien_ban) {
        this.ma_bien_ban = ma_bien_ban;
    }

    public String getLi_do() {
        return li_do;
    }

    public void setLi_do(String li_do) {
        this.li_do = li_do;
    }

    public Double getPhi_bien_ban() {
        return phi_bien_ban;
    }

    public void setPhi_bien_ban(Double phi_bien_ban) {
        this.phi_bien_ban = phi_bien_ban;
    }

    public String getMa_chi_tiet_dat_phong() {
        return ma_chi_tiet_dat_phong;
    }

    public void setMa_chi_tiet_dat_phong(String ma_chi_tiet_dat_phong) {
        this.ma_chi_tiet_dat_phong = ma_chi_tiet_dat_phong;
    }

    public String getMa_phien_dang_nhap() {
        return ma_phien_dang_nhap;
    }

    public void setMa_phien_dang_nhap(String ma_phien_dang_nhap) {
        this.ma_phien_dang_nhap = ma_phien_dang_nhap;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

package vn.iuh.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class LichSuRaNgoai {
    private String ma_lich_su_ra_ngoai;
    private boolean la_lan_cuoi_cung;
    private String ma_chi_tiet_dat_phong;
    private Timestamp thoi_gian_tao;

    public LichSuRaNgoai() {
    }

    public LichSuRaNgoai(String ma_lich_su_ra_ngoai, boolean la_lan_cuoi_cung, String ma_chi_tiet_dat_phong, Timestamp thoi_gian_tao) {
        this.ma_lich_su_ra_ngoai = ma_lich_su_ra_ngoai;
        this.la_lan_cuoi_cung = la_lan_cuoi_cung;
        this.ma_chi_tiet_dat_phong = ma_chi_tiet_dat_phong;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_lich_su_ra_ngoai() {
        return ma_lich_su_ra_ngoai;
    }

    public void setMa_lich_su_ra_ngoai(String ma_lich_su_ra_ngoai) {
        this.ma_lich_su_ra_ngoai = ma_lich_su_ra_ngoai;
    }

    public boolean isLa_lan_cuoi_cung() {
        return la_lan_cuoi_cung;
    }

    public void setLa_lan_cuoi_cung(boolean la_lan_cuoi_cung) {
        this.la_lan_cuoi_cung = la_lan_cuoi_cung;
    }

    public String getMa_chi_tiet_dat_phong() {
        return ma_chi_tiet_dat_phong;
    }

    public void setMa_chi_tiet_dat_phong(String ma_chi_tiet_dat_phong) {
        this.ma_chi_tiet_dat_phong = ma_chi_tiet_dat_phong;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

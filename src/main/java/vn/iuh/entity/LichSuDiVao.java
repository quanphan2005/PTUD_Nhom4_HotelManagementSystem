package vn.iuh.entity;

import java.sql.Timestamp;

public class LichSuDiVao {
    private String ma_lich_su_di_vao;
    private boolean la_lan_dau_tien;
    private String ma_chi_tiet_dat_phong;
    private Timestamp thoi_gian_tao;

    public LichSuDiVao() {
    }

    public LichSuDiVao(String ma_lich_su_di_vao, boolean la_lan_dau_tien, String ma_chi_tiet_dat_phong, Timestamp thoi_gian_tao) {
        this.ma_lich_su_di_vao = ma_lich_su_di_vao;
        this.la_lan_dau_tien = la_lan_dau_tien;
        this.ma_chi_tiet_dat_phong = ma_chi_tiet_dat_phong;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_lich_su_di_vao() {
        return ma_lich_su_di_vao;
    }

    public void setMa_lich_su_di_vao(String ma_lich_su_di_vao) {
        this.ma_lich_su_di_vao = ma_lich_su_di_vao;
    }

    public boolean isLa_lan_dau_tien() {
        return la_lan_dau_tien;
    }

    public void setLa_lan_dau_tien(boolean la_lan_dau_tien) {
        this.la_lan_dau_tien = la_lan_dau_tien;
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

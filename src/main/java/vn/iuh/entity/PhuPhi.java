package vn.iuh.entity;

import java.sql.Date;

public class PhuPhi {
    private String ma_phu_phi;
    private String ten_phu_phi;
    private Date thoi_gian_tao;

    public PhuPhi() {
    }

    public PhuPhi(String ma_phu_phi, String ten_phu_phi, Date thoi_gian_tao) {
        this.ma_phu_phi = ma_phu_phi;
        this.ten_phu_phi = ten_phu_phi;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_phu_phi() {
        return ma_phu_phi;
    }

    public void setMa_phu_phi(String ma_phu_phi) {
        this.ma_phu_phi = ma_phu_phi;
    }

    public String getTen_phu_phi() {
        return ten_phu_phi;
    }

    public void setTen_phu_phi(String ten_phu_phi) {
        this.ten_phu_phi = ten_phu_phi;
    }

    public Date getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Date thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}

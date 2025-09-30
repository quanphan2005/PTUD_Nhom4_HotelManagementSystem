package vn.iuh.entity;

import java.sql.Timestamp;

public class NoiThat {
    private String ma_noi_that;
    private String ten_noi_that;
    private String mo_ta;
    private Timestamp thoi_gian_tao;

    public NoiThat() {
    }

    public NoiThat(String ma_noi_that, String ten_noi_that, String mo_ta, Timestamp thoi_gian_tao) {
        this.ma_noi_that = ma_noi_that;
        this.ten_noi_that = ten_noi_that;
        this.mo_ta = mo_ta;
        this.thoi_gian_tao = thoi_gian_tao;
    }

    // Getters and Setters
    public String getMa_noi_that() {
        return ma_noi_that;
    }

    public void setMa_noi_that(String ma_noi_that) {
        this.ma_noi_that = ma_noi_that;
    }

    public String getTen_noi_that() {
        return ten_noi_that;
    }

    public void setTen_noi_that(String ten_noi_that) {
        this.ten_noi_that = ten_noi_that;
    }

    public String getMo_ta() {
        return mo_ta;
    }

    public void setMo_ta(String mo_ta) {
        this.mo_ta = mo_ta;
    }

    public Timestamp getThoi_gian_tao() {
        return thoi_gian_tao;
    }

    public void setThoi_gian_tao(Timestamp thoi_gian_tao) {
        this.thoi_gian_tao = thoi_gian_tao;
    }
}
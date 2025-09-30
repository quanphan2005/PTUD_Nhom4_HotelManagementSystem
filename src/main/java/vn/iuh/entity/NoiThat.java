package vn.iuh.entity;

import java.sql.Timestamp;

public class NoiThat {
    private String maNoiThat;
    private String tenNoiThat;
    private String moTa;
    private Timestamp thoiGianTao;

    public NoiThat() {
    }

    public NoiThat(String maNoiThat, String tenNoiThat, String moTa, Timestamp thoiGianTao) {
        this.maNoiThat = maNoiThat;
        this.tenNoiThat = tenNoiThat;
        this.moTa = moTa;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaNoiThat() {
        return maNoiThat;
    }

    public void setMaNoiThat(String maNoiThat) {
        this.maNoiThat = maNoiThat;
    }

    public String getTenNoiThat() {
        return tenNoiThat;
    }

    public void setTenNoiThat(String tenNoiThat) {
        this.tenNoiThat = tenNoiThat;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
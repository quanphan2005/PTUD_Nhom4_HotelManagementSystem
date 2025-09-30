package vn.iuh.entity;

import java.sql.Timestamp;

public class NoiThatTrongLoaiPhong {
    private String maNoiThatTrongLoaiPhong;
    private int soLuong;
    private String maLoaiPhong;
    private String maNoiThat;
    private Timestamp thoiGianTao;

    public NoiThatTrongLoaiPhong() {
    }

    public NoiThatTrongLoaiPhong(String maNoiThatTrongLoaiPhong, int soLuong, String maLoaiPhong, String maNoiThat, Timestamp thoiGianTao) {
        this.maNoiThatTrongLoaiPhong = maNoiThatTrongLoaiPhong;
        this.soLuong = soLuong;
        this.maLoaiPhong = maLoaiPhong;
        this.maNoiThat = maNoiThat;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaNoiThatTrongLoaiPhong() {
        return maNoiThatTrongLoaiPhong;
    }

    public void setMaNoiThatTrongLoaiPhong(String maNoiThatTrongLoaiPhong) {
        this.maNoiThatTrongLoaiPhong = maNoiThatTrongLoaiPhong;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getMaNoiThat() {
        return maNoiThat;
    }

    public void setMaNoiThat(String maNoiThat) {
        this.maNoiThat = maNoiThat;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
package vn.iuh.entity;

import java.sql.Timestamp;

public class ThongBao {
    private String maThongBao;
    private String noiDung;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    public ThongBao() {
    }

    public ThongBao(String maThongBao, String noiDung, String maPhienDangNhap, Timestamp thoiGianTao) {
        this.maThongBao = maThongBao;
        this.noiDung = noiDung;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaThongBao() {
        return maThongBao;
    }

    public void setMaThongBao(String maThongBao) {
        this.maThongBao = maThongBao;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public Timestamp getThoiGianTao() { return thoiGianTao; }

    public void setThoiGianTao(Timestamp createAt) { this.thoiGianTao = createAt; }
}

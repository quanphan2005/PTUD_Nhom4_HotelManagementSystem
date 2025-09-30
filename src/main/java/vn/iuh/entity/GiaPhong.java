package vn.iuh.entity;

import java.sql.Time;
import java.sql.Timestamp;

public class GiaPhong {
    private String maGiaPhong;
    private double giaNgayCu;
    private double giaGioCu;
    private double gioNgayMoi;
    private double gioGioMoi;
    private String maLoaiPhong;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    public GiaPhong() {
    }

    public GiaPhong(String maGiaPhong, double giaNgayCu, double giaGioCu, double gioNgayMoi, double gioGioMoi,
                    String maLoaiPhong, String maPhienDangNhap, Timestamp thoiGianTao) {
        this.maGiaPhong = maGiaPhong;
        this.giaNgayCu = giaNgayCu;
        this.giaGioCu = giaGioCu;
        this.gioNgayMoi = gioNgayMoi;
        this.gioGioMoi = gioGioMoi;
        this.maLoaiPhong = maLoaiPhong;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaGiaPhong() {
        return maGiaPhong;
    }

    public void setMaGiaPhong(String maGiaPhong) {
        this.maGiaPhong = maGiaPhong;
    }

    public double getGiaNgayCu() {
        return giaNgayCu;
    }

    public void setGiaNgayCu(double giaNgayCu) {
        this.giaNgayCu = giaNgayCu;
    }

    public double getGiaGioCu() {
        return giaGioCu;
    }

    public void setGiaGioCu(double giaGioCu) {
        this.giaGioCu = giaGioCu;
    }

    public double getGioNgayMoi() {
        return gioNgayMoi;
    }

    public void setGioNgayMoi(double gioNgayMoi) {
        this.gioNgayMoi = gioNgayMoi;
    }

    public double getGioGioMoi() {
        return gioGioMoi;
    }

    public void setGioGioMoi(double gioGioMoi) {
        this.gioGioMoi = gioGioMoi;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
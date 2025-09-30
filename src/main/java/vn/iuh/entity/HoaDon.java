package vn.iuh.entity;

import java.sql.Timestamp;

public class HoaDon {
    private String maHoaDon;
    private String phuongThucThanhToan;
    private double tongTien;
    private double tienThue;
    private double tongHoaDon;
    private String kieuHoaDon;
    private String tinhTrangThanhToan;
    private String maPhienDangNhap;
    private String maDonDatPhong;
    private String maKhachHang;
    private Timestamp thoiGianTao;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String phuongThucThanhToan, double tongTien, double tienThue, double tongHoaDon, String kieuHoaDon, String tinhTrangThanhToan, String maPhienDangNhap, String maDonDatPhong, String maKhachHang, Timestamp thoiGianTao) {
        this.maHoaDon = maHoaDon;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.tongTien = tongTien;
        this.tienThue = tienThue;
        this.tongHoaDon = tongHoaDon;
        this.kieuHoaDon = kieuHoaDon;
        this.tinhTrangThanhToan = tinhTrangThanhToan;
        this.maPhienDangNhap = maPhienDangNhap;
        this.maDonDatPhong = maDonDatPhong;
        this.maKhachHang = maKhachHang;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public double getTienThue() {
        return tienThue;
    }

    public void setTienThue(double tienThue) {
        this.tienThue = tienThue;
    }

    public double getTongHoaDon() {
        return tongHoaDon;
    }

    public void setTongHoaDon(double tongHoaDon) {
        this.tongHoaDon = tongHoaDon;
    }

    public String getKieuHoaDon() {
        return kieuHoaDon;
    }

    public void setKieuHoaDon(String kieuHoaDon) {
        this.kieuHoaDon = kieuHoaDon;
    }

    public String getTinhTrangThanhToan() {
        return tinhTrangThanhToan;
    }

    public void setTinhTrangThanhToan(String tinhTrangThanhToan) {
        this.tinhTrangThanhToan = tinhTrangThanhToan;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(String maDonDatPhong) {
        this.maDonDatPhong = maDonDatPhong;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

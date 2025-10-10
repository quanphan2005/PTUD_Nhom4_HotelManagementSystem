package vn.iuh.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private String phuongThucThanhToan;
    private String kieuHoaDon;
    private String tinhTrangThanhToan;
    private String maPhienDangNhap;
    private String maDonDatPhong;
    private String maKhachHang;
    private Timestamp thoiGianTao;
    private List<ChiTietHoaDon> chiTietHoaDonList;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String kieuHoaDon, String maPhienDangNhap, String maDonDatPhong, String maKhachHang) {
        this.maHoaDon = maHoaDon;
        this.kieuHoaDon = kieuHoaDon;
        this.maPhienDangNhap = maPhienDangNhap;
        this.maDonDatPhong = maDonDatPhong;
        this.maKhachHang = maKhachHang;
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
    public BigDecimal getTongTien(){
        BigDecimal tongTien = new BigDecimal(0);
        for(ChiTietHoaDon ct : this.chiTietHoaDonList){
            tongTien = tongTien.add(ct.tinhThanhTien());
        }
        return tongTien;
    }

}

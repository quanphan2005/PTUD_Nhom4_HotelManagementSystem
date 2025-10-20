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
    private BigDecimal tongTien;
    private BigDecimal tienThue;
    private BigDecimal tongHoaDon;
    private List<ChiTietHoaDon> chiTietHoaDonList;

    public HoaDon() {
    }

    public List<ChiTietHoaDon> getChiTietHoaDonList() {
        return chiTietHoaDonList;
    }

    public void setChiTietHoaDonList(List<ChiTietHoaDon> chiTietHoaDonList) {
        this.chiTietHoaDonList = chiTietHoaDonList;
    }

    public BigDecimal getTongHoaDon() {
        if (tongHoaDon != null){
            return tongHoaDon;
        }
        return this.getTongTien().add(this.getTienThue());
    }

    public void setTongHoaDon(BigDecimal tongHoaDon) {
        this.tongHoaDon = tongHoaDon;
    }

    public BigDecimal getTienThue() {
        return tienThue == null ? BigDecimal.ZERO : tienThue;
    }

    public void setTienThue(BigDecimal tienThue) {
        this.tienThue = tienThue;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
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
        if(tongTien != null){
            return this.tongTien;
        }

        if (this.chiTietHoaDonList == null || this.chiTietHoaDonList.isEmpty()){
            return new BigDecimal(0);
        }

        BigDecimal tongTien = new BigDecimal(0);
        for(ChiTietHoaDon ct : this.chiTietHoaDonList){
            tongTien = tongTien.add(ct.getTongTien());
        }
        return tongTien;
    }
}

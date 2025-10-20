package vn.iuh.dto.response;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class InvoiceStatistic {
    private String maHoaDon;
    private String tenKhachHang;
    private String tenNhanVien;
    private Timestamp ngayLap;
    private BigDecimal tienPhong;
    private BigDecimal tienDichVu;
    private BigDecimal tienThue;
    private BigDecimal tongHoaDon;


    public InvoiceStatistic(String maHoaDon, String tenKhachHang, String tenNhanVien, Timestamp ngayLap, BigDecimal tienPhong, BigDecimal tienDichVu, BigDecimal tienThue, BigDecimal tongHoaDon) {
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.tenNhanVien = tenNhanVien;
        this.ngayLap = ngayLap;
        this.tienPhong = tienPhong;
        this.tienDichVu = tienDichVu;
        this.tienThue = tienThue;
        this.tongHoaDon = tongHoaDon;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public BigDecimal getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(BigDecimal tienPhong) {
        this.tienPhong = tienPhong;
    }

    public BigDecimal getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(BigDecimal tienDichVu) {
        this.tienDichVu = tienDichVu;
    }

    public BigDecimal getTienThue() {
        return tienThue;
    }

    public void setTienThue(BigDecimal tienThue) {
        this.tienThue = tienThue;
    }

    public BigDecimal getTongHoaDon() {
        return tongHoaDon;
    }

    public void setTongHoaDon(BigDecimal tongHoaDon) {
        this.tongHoaDon = tongHoaDon;
    }
}

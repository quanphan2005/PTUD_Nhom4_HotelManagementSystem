package vn.iuh.dto.event.create;

import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.entity.PhongTinhPhuPhi;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceCreationEvent {
    private String maPhienDangNhap;
    private String maDonDatPhong;
    private String maKhachHang;
    private String kieuHoaDon;
    private String tenKhachHang;
    private String cccd;
    private String soDienThoai;
    private String tenNhanVien;
    private List<ChiTietHoaDon> chiTietHoaDonList;
    private List<PhongDungDichVu> phongDungDichVuList;
    private List<PhongTinhPhuPhi> phongTinhPhuPhiList;
    private BigDecimal tongTien;

    public InvoiceCreationEvent(String maPhienDangNhap, String maDonDatPhong, String maKhachHang,
                                String kieuHoaDon, String tenKhachHang, String cccd, String soDienThoai,
                                String tenNhanVien, List<ChiTietHoaDon> chiTietHoaDonList,
                                List<PhongDungDichVu> phongDungDichVuList, List<PhongTinhPhuPhi> phongTinhPhuPhiList,
                                BigDecimal tongTien) {
        this.maPhienDangNhap = maPhienDangNhap;
        this.maDonDatPhong = maDonDatPhong;
        this.maKhachHang = maKhachHang;
        this.kieuHoaDon = kieuHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.cccd = cccd;
        this.soDienThoai = soDienThoai;
        this.tenNhanVien = tenNhanVien;
        this.chiTietHoaDonList = chiTietHoaDonList;
        this.phongDungDichVuList = phongDungDichVuList;
        this.phongTinhPhuPhiList = phongTinhPhuPhiList;
        this.tongTien = tongTien;
    }

    public String getCccd() {
        return cccd;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public List<ChiTietHoaDon> getChiTietHoaDonList() {
        return chiTietHoaDonList;
    }

    public void setChiTietHoaDonList(List<ChiTietHoaDon> chiTietHoaDonList) {
        this.chiTietHoaDonList = chiTietHoaDonList;
    }

    public List<PhongDungDichVu> getPhongDungDichVuList() {
        return phongDungDichVuList;
    }

    public void setPhongDungDichVuList(List<PhongDungDichVu> phongDungDichVuList) {
        this.phongDungDichVuList = phongDungDichVuList;
    }

    public List<PhongTinhPhuPhi> getPhongTinhPhuPhiList() {
        return phongTinhPhuPhiList;
    }

    public void setPhongTinhPhuPhiList(List<PhongTinhPhuPhi> phongTinhPhuPhiList) {
        this.phongTinhPhuPhiList = phongTinhPhuPhiList;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
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

    public String getKieuHoaDon() {
        return kieuHoaDon;
    }

    public void setKieuHoaDon(String kieuHoaDon) {
        this.kieuHoaDon = kieuHoaDon;
    }
}

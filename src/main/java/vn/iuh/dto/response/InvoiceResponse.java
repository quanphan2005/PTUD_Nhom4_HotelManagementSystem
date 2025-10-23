package vn.iuh.dto.response;

import vn.iuh.entity.*;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceResponse {
    private String maPhienDangNhap;
    private DonDatPhong donDatPhong;
    private KhachHang khachHang;
    private HoaDon hoaDon;
    private NhanVien tenNhanVien;
    private BigDecimal tienCoc;
    private List<ChiTietHoaDon> chiTietHoaDonList;
    private List<PhongDungDichVu> phongDungDichVuList;
    private List<PhongTinhPhuPhi> phongTinhPhuPhiList;

    public InvoiceResponse(String maPhienDangNhap, BigDecimal tienCoc, DonDatPhong donDatPhong,KhachHang khachHang, HoaDon hoaDon, NhanVien tenNhanVien, List<ChiTietHoaDon> chiTietHoaDonList, List<PhongDungDichVu> phongDungDichVuList, List<PhongTinhPhuPhi> phongTinhPhuPhiList) {
        this.maPhienDangNhap = maPhienDangNhap;
        this.donDatPhong = donDatPhong;
        this.khachHang = khachHang;
        this.hoaDon = hoaDon;
        this.tenNhanVien = tenNhanVien;
        this.tienCoc = tienCoc;
        this.chiTietHoaDonList = chiTietHoaDonList;
        this.phongDungDichVuList = phongDungDichVuList;
        this.phongTinhPhuPhiList = phongTinhPhuPhiList;
    }

    public BigDecimal getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(BigDecimal tienCoc) {
        this.tienCoc = tienCoc;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public DonDatPhong getDonDatPhong() {
        return donDatPhong;
    }

    public void setDonDatPhong(DonDatPhong donDatPhong) {
        this.donDatPhong = donDatPhong;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public NhanVien getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(NhanVien tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
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
}

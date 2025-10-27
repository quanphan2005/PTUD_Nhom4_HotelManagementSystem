package vn.iuh.dto.response;

import vn.iuh.entity.*;

import java.util.List;

public class DepositInvoiceResponse {
    private String maPhienDangNhap;
    private DonDatPhong donDatPhong;
    private KhachHang khachHang;
    private HoaDon hoaDon;
    private NhanVien tenNhanVien;
    private List<ChiTietDatPhong> danhSachChiTietDatPhong;
    private List<PhongDungDichVu> danhSachDichVu;

    public DepositInvoiceResponse() {
    }

    public DepositInvoiceResponse(String maPhienDangNhap, DonDatPhong donDatPhong, KhachHang khachHang, HoaDon hoaDon, NhanVien tenNhanVien, List<ChiTietDatPhong> danhSachChiTietDatPhong, List<PhongDungDichVu> danhSachDichVu) {
        this.maPhienDangNhap = maPhienDangNhap;
        this.donDatPhong = donDatPhong;
        this.khachHang = khachHang;
        this.hoaDon = hoaDon;
        this.tenNhanVien = tenNhanVien;
        this.danhSachChiTietDatPhong = danhSachChiTietDatPhong;
        this.danhSachDichVu = danhSachDichVu;
    }

    public List<ChiTietDatPhong> getDanhSachChiTietDatPhong() {
        return danhSachChiTietDatPhong;
    }

    public void setDanhSachChiTietDatPhong(List<ChiTietDatPhong> danhSachChiTietDatPhong) {
        this.danhSachChiTietDatPhong = danhSachChiTietDatPhong;
    }

    public List<PhongDungDichVu> getDanhSachDichVu() {
        return danhSachDichVu;
    }

    public void setDanhSachDichVu(List<PhongDungDichVu> danhSachDichVu) {
        this.danhSachDichVu = danhSachDichVu;
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
}

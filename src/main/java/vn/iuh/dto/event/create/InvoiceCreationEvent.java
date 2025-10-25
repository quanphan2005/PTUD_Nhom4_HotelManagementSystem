package vn.iuh.dto.event.create;

import vn.iuh.entity.*;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceCreationEvent {
    private String maPhienDangNhap;
    private DonDatPhong donDatPhong;
    private KhachHang khachHang;
    private HoaDon hoaDon;
    private NhanVien nhanVien;
    private BigDecimal tienCoc;
    private List<ChiTietHoaDon> chiTietHoaDonList;
    private List<PhongDungDichVu> phongDungDichVuList;
    private List<PhongTinhPhuPhi> phongTinhPhuPhiList;

    public InvoiceCreationEvent(String maPhienDangNhap, BigDecimal tienCoc, DonDatPhong donDatPhong,KhachHang khachHang, HoaDon hoaDon, NhanVien nhanVien, List<ChiTietHoaDon> chiTietHoaDonList, List<PhongDungDichVu> phongDungDichVuList, List<PhongTinhPhuPhi> phongTinhPhuPhiList) {
        this.maPhienDangNhap = maPhienDangNhap;
        this.donDatPhong = donDatPhong;
        this.khachHang = khachHang;
        this.hoaDon = hoaDon;
        this.nhanVien = nhanVien;
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

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
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

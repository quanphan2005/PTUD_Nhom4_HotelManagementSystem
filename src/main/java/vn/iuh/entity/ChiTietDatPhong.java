package vn.iuh.entity;

import java.sql.Timestamp;
import java.util.List;

public class ChiTietDatPhong {
    private String maChiTietDatPhong;
    private Timestamp tgNhanPhong;
    private Timestamp tgTraPhong;
    private String kieuKetThuc;
    private String maDonDatPhong;
    private String maPhong;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    private List<PhongDungDichVu> dichVuDaSuDung;
    private List<PhongTinhPhuPhi> danhSachPhuPhi;

    public ChiTietDatPhong() {
    }

    public ChiTietDatPhong(String maChiTietDatPhong, Timestamp tgNhanPhong, Timestamp tgTraPhong, String kieuKetThuc,
                           String maDonDatPhong, String maPhong, String maPhienDangNhap, Timestamp thoiGianTao) {
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
        this.kieuKetThuc = kieuKetThuc;
        this.maDonDatPhong = maDonDatPhong;
        this.maPhong = maPhong;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public Timestamp getTgNhanPhong() {
        return tgNhanPhong;
    }

    public void setTgNhanPhong(Timestamp tgNhanPhong) {
        this.tgNhanPhong = tgNhanPhong;
    }

    public Timestamp getTgTraPhong() {
        return tgTraPhong;
    }

    public void setTgTraPhong(Timestamp tgTraPhong) {
        this.tgTraPhong = tgTraPhong;
    }

    public String getKieuKetThuc() {
        return kieuKetThuc;
    }

    public void setKieuKetThuc(String kieuKetThuc) {
        this.kieuKetThuc = kieuKetThuc;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(String maDonDatPhong) {
        this.maDonDatPhong = maDonDatPhong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
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

    public List<PhongDungDichVu> getDichVuDaSuDung() {
        return dichVuDaSuDung;
    }

    public void setDichVuDaSuDung(List<PhongDungDichVu> dichVuDaSuDung) {
        this.dichVuDaSuDung = dichVuDaSuDung;
    }

    public List<PhongTinhPhuPhi> getDanhSachPhuPhi() {
        return danhSachPhuPhi;
    }

    public void setDanhSachPhuPhi(List<PhongTinhPhuPhi> danhSachPhuPhi) {
        this.danhSachPhuPhi = danhSachPhuPhi;
    }
}

package vn.iuh.entity;

import java.sql.Timestamp;

public class PhongDungDichVu {
    private String maPhongDungDichVu;
    private int soLuong;
    private Timestamp thoiGianDung;
    private double giaThoiDiemDo;
    private boolean duocTang;
    private String maChiTietDatPhong;
    private String maDichVu;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    public PhongDungDichVu() {}

    public PhongDungDichVu(String maPhongDungDichVu, int soLuong, Timestamp thoiGianDung, double giaThoiDiemDo,
                           boolean duocTang, String maChiTietDatPhong, String maDichVu, String maPhienDangNhap,
                           Timestamp thoiGianTao) {
        this.maPhongDungDichVu = maPhongDungDichVu;
        this.soLuong = soLuong;
        this.thoiGianDung = thoiGianDung;
        this.giaThoiDiemDo = giaThoiDiemDo;
        this.duocTang = duocTang;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maDichVu = maDichVu;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaPhongDungDichVu() {
        return maPhongDungDichVu;
    }

    public void setMaPhongDungDichVu(String maPhongDungDichVu) {
        this.maPhongDungDichVu = maPhongDungDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public Timestamp getThoiGianDung() {
        return thoiGianDung;
    }

    public void setThoiGianDung(Timestamp thoiGianDung) {
        this.thoiGianDung = thoiGianDung;
    }

    public double getGiaThoiDiemDo() {
        return giaThoiDiemDo;
    }

    public void setGiaThoiDiemDo(double giaThoiDiemDo) {
        this.giaThoiDiemDo = giaThoiDiemDo;
    }

    public boolean getDuocTang() {
        return duocTang;
    }

    public void setDuocTang(boolean duocTang) {
        this.duocTang = duocTang;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
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

package vn.iuh.entity;

import java.sql.Timestamp;

public class DonDatPhong {
    private String maDonDatPhong;
    private String moTa;
    private Timestamp tgNhanPhong;
    private Timestamp tgTraPhong;
    private double tongTienDuTinh;
    private double tienDatCoc;
    private boolean daDatTruoc;
    private String loai;
    private String maKhachHang;
    private String maPhienDangNhap;
    private String thoiGianTao;
    private boolean daXoa;

    public DonDatPhong() {
    }

    public DonDatPhong(String maDonDatPhong, String moTa, Timestamp tgNhanPhong, Timestamp tgTraPhong,
                       double tongTienDuTinh, double tienDatCoc, boolean daDatTruoc, String loai, String maKhachHang,
                       String maPhienDangNhap, String thoiGianTao, boolean daXoa) {
        this.maDonDatPhong = maDonDatPhong;
        this.moTa = moTa;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
        this.tongTienDuTinh = tongTienDuTinh;
        this.tienDatCoc = tienDatCoc;
        this.daDatTruoc = daDatTruoc;
        this.loai = loai;
        this.maKhachHang = maKhachHang;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
        this.daXoa = daXoa;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(String maDonDatPhong) {
        this.maDonDatPhong = maDonDatPhong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
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

    public double getTongTienDuTinh() {
        return tongTienDuTinh;
    }

    public void setTongTienDuTinh(double tongTienDuTinh) {
        this.tongTienDuTinh = tongTienDuTinh;
    }

    public double getTienDatCoc() {
        return tienDatCoc;
    }

    public void setTienDatCoc(double tienDatCoc) {
        this.tienDatCoc = tienDatCoc;
    }

    public boolean isDaDatTruoc() {
        return daDatTruoc;
    }

    public void setDaDatTruoc(boolean daDatTruoc) {
        this.daDatTruoc = daDatTruoc;
    }

    public String getLoai() {
        return loai;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public String getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(String thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public boolean isDaXoa() {
        return daXoa;
    }
}

package vn.iuh.entity;

import java.sql.Timestamp;

public class DonDatPhong {
    private String maDonDatPhong;
    private String moTa;
    private Timestamp tgNhanPhong;
    private Timestamp tgRoiPhong;
    private boolean daDatTruoc;
    private double tongTienDuTinh;
    private double tienDatCoc;
    private String maKhachHang;
    private String maPhienDangNhap;
    private String thoiGianTao;

    public DonDatPhong() {
    }

    public DonDatPhong(String maDonDatPhong, String moTa, Timestamp tgNhanPhong, Timestamp tgRoiPhong,
                       boolean daDatTruoc,
                       double tongTienDuTinh, double tienDatCoc, String maKhachHang, String maPhienDangNhap,
                       String thoiGianTao) {
        this.maDonDatPhong = maDonDatPhong;
        this.moTa = moTa;
        this.tgNhanPhong = tgNhanPhong;
        this.tgRoiPhong = tgRoiPhong;
        this.daDatTruoc = daDatTruoc;
        this.tongTienDuTinh = tongTienDuTinh;
        this.tienDatCoc = tienDatCoc;
        this.maKhachHang = maKhachHang;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
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

    public Timestamp getTgRoiPhong() {
        return tgRoiPhong;
    }

    public void setTgRoiPhong(Timestamp tgRoiPhong) {
        this.tgRoiPhong = tgRoiPhong;
    }

    public boolean isDaDatTruoc() {
        return daDatTruoc;
    }

    public void setDaDatTruoc(boolean daDatTruoc) {
        this.daDatTruoc = daDatTruoc;
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
}

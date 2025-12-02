package vn.iuh.dto.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class RoomUsageServiceInfo {
    private String maPhongDungDichVu;
    private int soLuong;
    private double giaThoiDiemDo;
    private String maChiTietDatPhong;
    private String tenPhong;
    private String maDichVu;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;
    private String tenDichVu;
    private BigDecimal tongTien;

    public RoomUsageServiceInfo() {
    }

    public RoomUsageServiceInfo(String maPhongDungDichVu, int soLuong, double giaThoiDiemDo, boolean duocTang,
                                String maChiTietDatPhong, String tenPhong, String maDichVu, String maPhienDangNhap,
                                Timestamp thoiGianTao, String tenDichVu, BigDecimal tongTien) {
        this.maPhongDungDichVu = maPhongDungDichVu;
        this.soLuong = soLuong;
        this.giaThoiDiemDo = giaThoiDiemDo;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.tenPhong = tenPhong;
        this.maDichVu = maDichVu;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
        this.tenDichVu = tenDichVu;
        this.tongTien = tongTien;
    }

    public String getMaPhongDungDichVu() {
        return maPhongDungDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public double getGiaThoiDiemDo() {
        return giaThoiDiemDo;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }
}

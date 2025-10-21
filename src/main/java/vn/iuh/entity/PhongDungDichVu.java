package vn.iuh.entity;

import vn.iuh.util.PriceFormat;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PhongDungDichVu {
    private String maPhongDungDichVu;
    private int soLuong;
    private double giaThoiDiemDo;
    private boolean duocTang;
    private String maChiTietDatPhong;
    private String tenPhong;
    private String maDichVu;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;
    private String tenDichVu;
    private BigDecimal tongTien;

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public PhongDungDichVu() {}

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public PhongDungDichVu(String maPhongDungDichVu, int soLuong, double giaThoiDiemDo,
                           boolean duocTang, String maChiTietDatPhong, String maDichVu, String maPhienDangNhap,
                           Timestamp thoiGianTao) {
        this.maPhongDungDichVu = maPhongDungDichVu;
        this.soLuong = soLuong;
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

    public BigDecimal tinhThanhTien(){
        return BigDecimal.valueOf(giaThoiDiemDo).multiply(BigDecimal.valueOf(soLuong));
    }

    public BigDecimal getTongTien() {
        if(!this.duocTang){
            if(tongTien != null){
                return tongTien;
            }
            else {
                tongTien = this.tinhThanhTien();
                return tongTien;
            }
        }else {
            return BigDecimal.ZERO;
        }
    }

    public Object[] getSimpleObject(){
        return new Object[]{
            this.tenPhong,
            this.tenDichVu,
                PriceFormat.formatPrice(this.giaThoiDiemDo) + " VNĐ",
                this.soLuong,
                PriceFormat.formatPrice(this.getTongTien().doubleValue()) + " VNĐ",
                this.duocTang ? "Được tặng" : ""
        };
    }
}

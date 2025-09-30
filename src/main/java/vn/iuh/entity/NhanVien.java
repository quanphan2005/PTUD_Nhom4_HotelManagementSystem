package vn.iuh.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private String CCCD;
    private Timestamp ngaySinh;
    private String soDienThoai;
    private Timestamp thoiGianTao;

    public NhanVien() {
    }

    public NhanVien(String maNhanVien, String tenNhanVien, String CCCD, Timestamp ngaySinh, String soDienThoai,
                    Timestamp thoiGianTao) {
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.CCCD = CCCD;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public Timestamp getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Timestamp ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ThongTinNhanVien {
    private String tenNhanVien;
    private String CCCD;
    private Timestamp ngaySinh;
    private String soDienThoai;
    private String chucVu;
    private byte[] anhNhanVien;

    public ThongTinNhanVien() {
    }

    public ThongTinNhanVien(String tenNhanVien, String CCCD, Timestamp ngaySinh, String soDienThoai, String chucVu) {
        this.tenNhanVien = tenNhanVien;
        this.CCCD = CCCD;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.chucVu = chucVu;
        //this.anhNhanVien = anhNhanVien;
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

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public byte[] getAnhNhanVien() {
        return anhNhanVien;
    }

    public void setAnhNhanVien(byte[] anhNhanVien) {
        this.anhNhanVien = anhNhanVien;
    }
}

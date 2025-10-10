package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class PhieuDatPhong {
    private final String tenPhong;
    private final String tenKhachHang;
    private final String maDonDatPhong;
    private final Timestamp tgNhanPhong;
    private final Timestamp tgTraPhong;


    public PhieuDatPhong(String tenPhong, String tenKhachHang, String maDonDatPhong,
                         Timestamp tgNhanPhong,
                         Timestamp tgTraPhong) {
        this.tenPhong = tenPhong;
        this.tenKhachHang = tenKhachHang;
        this.maDonDatPhong = maDonDatPhong;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
    }

    @Override
    public String toString() {
        return "DonDatPhong{" +
               ", tenPhong='" + tenPhong + '\'' +
               ", tenKhachHang='" + tenKhachHang + '\'' +
               ", maDonDatPhong='" + maDonDatPhong + '\'' +
               ", tgNhanPhong=" + tgNhanPhong +
               ", tgTraPhong=" + tgTraPhong +
               '}';
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public Timestamp getTgNhanPhong() {
        return tgNhanPhong;
    }

    public Timestamp getTgTraPhong() {
        return tgTraPhong;
    }
}

package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ThongTinDatPhong {
    private final String maPhong;
    private final String tenKhachHang;
    private final String maChiTietDatPhong;
    private final Timestamp tgNhanPhong;
    private final Timestamp tgTraPhong;

    public ThongTinDatPhong(String maPhong, String tenKhachHang, String maChiTietDatPhong, Timestamp tgNhanPhong,
                            Timestamp tgTraPhong) {
        this.maPhong = maPhong;
        this.tenKhachHang = tenKhachHang;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
    }

    @Override
    public String toString() {
        return "BookingInfo{" +
               "maPhong='" + maPhong + '\'' +
               ", tenKhachHang='" + tenKhachHang + '\'' +
               ", tgNhanPhong=" + tgNhanPhong +
               ", tgTraPhong=" + tgTraPhong +
               '}';
    }

    public String getMaPhong() {
        return maPhong;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public Timestamp getTgNhanPhong() {
        return tgNhanPhong;
    }

    public Timestamp getTgTraPhong() {
        return tgTraPhong;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }
}

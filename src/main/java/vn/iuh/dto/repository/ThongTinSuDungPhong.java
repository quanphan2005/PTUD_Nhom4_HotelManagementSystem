package vn.iuh.dto.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ThongTinSuDungPhong {
    private String maDonDatPhong;
    private String maChiTietDatPhong;
    private String maPhong;
    private Timestamp tgNhanPhong;
    private Timestamp tgTraPhong;
    private Timestamp gioCheckIn;
    private String kieuKetThuc;
    private String maLoaiPhong;

    public ThongTinSuDungPhong(String maDonDatPhong, String maChiTietDatPhong, String maPhong, Timestamp tgNhanPhong, Timestamp tgTraPhong, Timestamp gioCheckIn, String kieuKetThuc, String maLoaiPhong) {
        this.maDonDatPhong = maDonDatPhong;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maPhong = maPhong;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
        this.gioCheckIn = gioCheckIn;
        this.kieuKetThuc = kieuKetThuc;
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public ThongTinSuDungPhong() {
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(String maDonDatPhong) {
        this.maDonDatPhong = maDonDatPhong;
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

    public Timestamp getGioCheckIn() {
        return gioCheckIn;
    }

    public void setGioCheckIn(Timestamp gioCheckIn) {
        this.gioCheckIn = gioCheckIn;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getKieuKetThuc() {
        return kieuKetThuc;
    }

    public void setKieuKetThuc(String kieuKetThuc) {
        this.kieuKetThuc = kieuKetThuc;
    }


}

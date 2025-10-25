package vn.iuh.gui.panel.statistic;

import vn.iuh.util.PriceFormat;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class RoomStatistic {
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private String maPhong;
    private String tenPhong;
    private int soLuotDat;
    private double thoiGianDat;
    private BigDecimal doanhThu;


    public RoomStatistic(String maLoaiPhong, String tenLoaiPhong, String maPhong, String tenPhong, int soLuotDat, double  thoiGianDat,BigDecimal doanhThu) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.maPhong = maPhong;
        this.thoiGianDat = thoiGianDat;
        this.tenPhong = tenPhong;
        this.soLuotDat = soLuotDat;
        this.doanhThu = doanhThu;
    }

    public double getThoiGianDat() {
        return thoiGianDat;
    }

    public void setThoiGianDat(double thoiGianDat) {
        this.thoiGianDat = thoiGianDat;
    }

    public RoomStatistic() {
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public int getSoLuotDat() {
        return soLuotDat;
    }

    public void setSoLuotDat(int soLuotDat) {
        this.soLuotDat = soLuotDat;
    }

    public BigDecimal getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(BigDecimal doanhThu) {
        this.doanhThu = doanhThu;
    }

    public Object[] getObject(){
        return new Object[]{
            this.maLoaiPhong,
            this.tenLoaiPhong,
            this.maPhong,
            this.tenPhong,
            this.getSoLuotDat() + " lượt",
            this.thoiGianDat + " giờ",
            formatCurrency(this.doanhThu)
        };
    }
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VND";
        value = PriceFormat.lamTronDenHangNghin(value);
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(value);
    }
}

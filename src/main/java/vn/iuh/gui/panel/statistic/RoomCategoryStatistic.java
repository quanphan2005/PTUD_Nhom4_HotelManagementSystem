package vn.iuh.gui.panel.statistic;

import java.math.BigDecimal;

public class RoomCategoryStatistic {
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int soLuotDat;
    private BigDecimal doanhThu;


    public RoomCategoryStatistic(String maLoaiPhong, String tenLoaiPhong, int soLuotDat, BigDecimal doanhThu) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.soLuotDat = soLuotDat;
        this.doanhThu = doanhThu;
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
}

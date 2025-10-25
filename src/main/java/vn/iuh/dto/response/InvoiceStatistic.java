package vn.iuh.dto.response;

import vn.iuh.util.PriceFormat;
import vn.iuh.util.TimeFormat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;

public class InvoiceStatistic {
    private String maHoaDon;
    private String tenKhachHang;
    private boolean laHoaDonDatCoc;
    private Timestamp ngayLap;
    private BigDecimal tienPhong;
    private BigDecimal tienDichVu;
    private BigDecimal tienThue;
    private BigDecimal phuPhi;
    private BigDecimal tongHoaDon;


    public InvoiceStatistic(String maHoaDon, String tenKhachHang,  boolean laHoaDonDatCoc,Timestamp ngayLap, BigDecimal tienPhong, BigDecimal tienDichVu,BigDecimal phuPhi, BigDecimal tienThue, BigDecimal tongHoaDon) {
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.ngayLap = ngayLap;
        this.phuPhi = phuPhi;
        this.laHoaDonDatCoc = laHoaDonDatCoc;
        this.tienPhong = tienPhong;
        this.tienDichVu = tienDichVu;
        this.tienThue = tienThue;
        this.tongHoaDon = tongHoaDon;
    }

    public BigDecimal getPhuPhi() {
        return phuPhi;
    }

    public void setPhuPhi(BigDecimal phuPhi) {
        this.phuPhi = phuPhi;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }


    public boolean isLaHoaDonDatCoc() {
        return laHoaDonDatCoc;
    }

    public void setLaHoaDonDatCoc(boolean laHoaDonDatCoc) {
        this.laHoaDonDatCoc = laHoaDonDatCoc;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }


    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public BigDecimal getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(BigDecimal tienPhong) {
        this.tienPhong = tienPhong;
    }

    public BigDecimal getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(BigDecimal tienDichVu) {
        this.tienDichVu = tienDichVu;
    }

    public BigDecimal getTienThue() {
        return tienThue;
    }

    public void setTienThue(BigDecimal tienThue) {
        this.tienThue = tienThue;
    }

    public BigDecimal getTongHoaDon() {
        return tongHoaDon;
    }

    public void setTongHoaDon(BigDecimal tongHoaDon) {
        this.tongHoaDon = tongHoaDon;
    }

    public Object[] getObject(){
        return new Object[]{
            this.maHoaDon,
            this.tenKhachHang,
            TimeFormat.formatTime(this.ngayLap),
            this.tienPhong,
            this.tienDichVu,
            this.phuPhi,
            this.tienThue,
                formatCurrency(this.tongHoaDon)
        };
    }
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VND";
        value = PriceFormat.lamTronDenHangNghin(value);
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(value);
    }


}

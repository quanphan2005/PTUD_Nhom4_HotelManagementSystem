package vn.iuh.dto.response;

import vn.iuh.util.PriceFormat;
import vn.iuh.util.TimeFormat;

import java.sql.Timestamp;

public class HistoryFeeResponse {
    private Timestamp ngayThayDoi;
    private double giaCapNhat;
    private String maNhanVien;
    private String tenNhanVien;

    public HistoryFeeResponse(Timestamp ngayThayDoi, double giaCapNhat, String maNhanVien, String tenNhanVien) {
        this.ngayThayDoi = ngayThayDoi;
        this.giaCapNhat = giaCapNhat;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
    }

    public Timestamp getNgayThayDoi() {
        return ngayThayDoi;
    }

    public void setNgayThayDoi(Timestamp ngayThayDoi) {
        this.ngayThayDoi = ngayThayDoi;
    }

    public double getGiaCapNhat() {
        return giaCapNhat;
    }

    public void setGiaCapNhat(double giaCapNhat) {
        this.giaCapNhat = giaCapNhat;
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

    public Object[] getObject(){
        return new Object[]{
                TimeFormat.formatTime(this.ngayThayDoi),
                this.giaCapNhat,
                this.tenNhanVien,
                this.maNhanVien
        };
    }
}

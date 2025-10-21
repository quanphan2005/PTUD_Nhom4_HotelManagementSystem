package vn.iuh.dto.event.update;

import vn.iuh.constraint.PaymentStatus;

public class InvoiceStatusUpdate {
    private String maHoaDon;
    private PaymentStatus phuongThucThanhToan;
    private String tinhTrangThanhToan;

    public InvoiceStatusUpdate(String maHoaDon, PaymentStatus phuongThucThanhToan, String tinhTrangThanhToan) {
        this.maHoaDon = maHoaDon;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.tinhTrangThanhToan = tinhTrangThanhToan;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public PaymentStatus getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(PaymentStatus phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTinhTrangThanhToan() {
        return tinhTrangThanhToan;
    }

    public void setTinhTrangThanhToan(String tinhTrangThanhToan) {
        this.tinhTrangThanhToan = tinhTrangThanhToan;
    }
}

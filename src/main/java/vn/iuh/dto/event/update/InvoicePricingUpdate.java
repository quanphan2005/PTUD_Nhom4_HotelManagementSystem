package vn.iuh.dto.event.update;

import java.math.BigDecimal;

public class InvoicePricingUpdate {
    private String maHoaDon;
    private BigDecimal tongTien;
    private BigDecimal tienThue;
    private BigDecimal tongHoaDon;

    public InvoicePricingUpdate(String maHoaDon, BigDecimal tongTien, BigDecimal tienThue, BigDecimal tongHoaDon) {
        this.tongTien = tongTien;
        this.maHoaDon = maHoaDon;
        this.tienThue = tienThue;
        this.tongHoaDon = tongHoaDon;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public BigDecimal getTienThue() {
        return tienThue;
    }

    public BigDecimal getTongHoaDon() {
        return tongHoaDon;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }
}

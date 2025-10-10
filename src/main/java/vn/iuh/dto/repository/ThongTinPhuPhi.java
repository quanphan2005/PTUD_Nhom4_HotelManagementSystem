package vn.iuh.dto.repository;

import java.math.BigDecimal;

public class ThongTinPhuPhi {
    private String maPhuPhi;
    private String tenPhuPhi;
    private BigDecimal giaHienTai;
    private boolean laPhanTram;

    public ThongTinPhuPhi() {
    }

    public ThongTinPhuPhi(String maPhuPhi, String tenPhuPhi, BigDecimal giaHienTai, boolean laPhanTram) {
        this.maPhuPhi = maPhuPhi;
        this.tenPhuPhi = tenPhuPhi;
        this.giaHienTai = giaHienTai;
        this.laPhanTram = laPhanTram;
    }

    public String getMaPhuPhi() {
        return maPhuPhi;
    }

    public void setMaPhuPhi(String maPhuPhi) {
        this.maPhuPhi = maPhuPhi;
    }

    public String getTenPhuPhi() {
        return tenPhuPhi;
    }

    public void setTenPhuPhi(String tenPhuPhi) {
        this.tenPhuPhi = tenPhuPhi;
    }

    public BigDecimal getGiaHienTai() {
        return giaHienTai;
    }

    public void setGiaHienTai(BigDecimal giaHienTai) {
        this.giaHienTai = giaHienTai;
    }

    public boolean isLaPhanTram() {
        return laPhanTram;
    }

    public void setLaPhanTram(boolean laPhanTram) {
        this.laPhanTram = laPhanTram;
    }
}

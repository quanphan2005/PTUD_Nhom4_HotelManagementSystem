package vn.iuh.dto.repository;

public class CustomerInfo {
    private final String maKhachHang;
    private final String CCCD;
    private final String tenKhachHang;
    private final String soDienThoai;

    public CustomerInfo(String maKhachHang, String CCCD, String tenKhachHang, String soDienThoai) {
        this.maKhachHang = maKhachHang;
        this.CCCD = CCCD;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getCCCD() {
        return CCCD;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }
}

package vn.iuh.dto.event.create;

public class InvoiceCreationEvent {
    private String maPhienDangNhap;
    private String maDonDatPhong;
    private String maKhachHang;
    private String kieuHoaDon;

    public InvoiceCreationEvent(String maPhienDangNhap, String maDonDatPhong, String maKhachHang, String kieuHoaDon) {
        this.maPhienDangNhap = maPhienDangNhap;
        this.maDonDatPhong = maDonDatPhong;
        this.maKhachHang = maKhachHang;
        this.kieuHoaDon = kieuHoaDon;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(String maDonDatPhong) {
        this.maDonDatPhong = maDonDatPhong;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getKieuHoaDon() {
        return kieuHoaDon;
    }

    public void setKieuHoaDon(String kieuHoaDon) {
        this.kieuHoaDon = kieuHoaDon;
    }
}

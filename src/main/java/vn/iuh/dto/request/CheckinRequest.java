package vn.iuh.dto.request;

public class CheckinRequest {
    private final String maDonDatPhong;
    private final String maChiTietDatPhong;
    private final String maPhienDangNhap;
    private final String maPhong;

    public CheckinRequest(String maDonDatPhong, String maChiTietDatPhong, String maPhienDangNhap, String maPhong) {
        this.maDonDatPhong = maDonDatPhong;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maPhienDangNhap = maPhienDangNhap;
        this.maPhong = maPhong;
    }

    public String getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public String getMaPhong() {
        return maPhong;
    }

    @Override
    public String toString() {
        return "CheckinRequest{" +
                "maDonDatPhong='" + maDonDatPhong + '\'' +
                ", maChiTietDatPhong='" + maChiTietDatPhong + '\'' +
                ", maPhienDangNhap='" + maPhienDangNhap + '\'' +
                ", maPhong='" + maPhong + '\'' +
                '}';
    }
}

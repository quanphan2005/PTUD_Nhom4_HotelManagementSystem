package vn.iuh.dto.repository;

public class ThongTinPhong {
    private final String maPhong;
    private final String tenPhong;
    private boolean dangHoatDong;
    private String tenTrangThai;
    private final String phanLoai;
    private final String soLuongKhach;
    private final double giaNgay;
    private final double giaGio;

    public ThongTinPhong(String maPhong, String tenPhong, boolean dangHoatDong, String tenTrangThai, String phanLoai, String soLuongKhach,
                         double giaNgay, double giaGio) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.dangHoatDong = dangHoatDong;
        this.tenTrangThai = tenTrangThai;
        this.phanLoai = phanLoai;
        this.soLuongKhach = soLuongKhach;
        this.giaNgay = giaNgay;
        this.giaGio = giaGio;
    }

    @Override
    public String toString() {
        return "ThongTinPhong{" +
                "maPhong='" + maPhong + '\'' +
                ", tenPhong='" + tenPhong + '\'' +
                ", dangHoatDong=" + dangHoatDong +
                ", tenTrangThai='" + tenTrangThai + '\'' +
                ", phanLoai='" + phanLoai + '\'' +
                ", soLuongKhach='" + soLuongKhach + '\'' +
                ", giaNgay=" + giaNgay +
                ", giaGio=" + giaGio +
                '}';
    }

    public String getMaPhong() {
        return maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public String getTenTrangThai() {
        return tenTrangThai;
    }

    public void setTenTrangThai(String tenTrangThai) {
        this.tenTrangThai = tenTrangThai;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public String getSoLuongKhach() {
        return soLuongKhach;
    }

    public double getGiaNgay() {
        return giaNgay;
    }

    public double getGiaGio() {
        return giaGio;
    }
}

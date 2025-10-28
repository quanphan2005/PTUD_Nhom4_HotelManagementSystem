package vn.iuh.dto.repository;

public class RoomWithCategory {
    private final String maPhong;
    private final String tenPhong;
    private final boolean dangHoatDong;
    private final String maLoaiPhong;
    private final String phanLoai;
    private final int soLuongKhach;
    private final double giaNgay;
    private final double giaGio;

    public RoomWithCategory(String maPhong, String tenPhong, boolean dangHoatDong, String maLoaiPhong, String phanLoai,
                            int soLuongKhach, double giaNgay, double giaGio) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.dangHoatDong = dangHoatDong;
        this.maLoaiPhong = maLoaiPhong;
        this.phanLoai = phanLoai;
        this.soLuongKhach = soLuongKhach;
        this.giaNgay = giaNgay;
        this.giaGio = giaGio;
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

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public double getGiaNgay() {
        return giaNgay;
    }

    public double getGiaGio() {
        return giaGio;
    }
}

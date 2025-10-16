package vn.iuh.dto.response;

import java.sql.Timestamp;

public class RoomCategoryResponse {
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int soLuongKhach;
    private String phanLoai;

    public RoomCategoryResponse() {
    }

    public RoomCategoryResponse(String maLoaiPhong, String tenLoaiPhong, int soLuongKhach, String phanLoai) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.soLuongKhach = soLuongKhach;
        this.phanLoai = phanLoai;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public String getPhanLoai() {
        return phanLoai;
    }
}

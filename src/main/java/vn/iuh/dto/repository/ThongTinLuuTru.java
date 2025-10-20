package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class ThongTinLuuTru {
    private String maChiTietDatPhong;
    private String maPhong;
    private Timestamp thoiGianVao;
    private Timestamp thoiGianRa;

    public ThongTinLuuTru(String maChiTietDatPhong, String maPhong, Timestamp thoiGianVao, Timestamp thoiGianRa) {
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maPhong = maPhong;
        this.thoiGianVao = thoiGianVao;
        this.thoiGianRa = thoiGianRa;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public Timestamp getThoiGianVao() {
        return thoiGianVao;
    }

    public Timestamp getThoiGianRa() {
        return thoiGianRa;
    }
}

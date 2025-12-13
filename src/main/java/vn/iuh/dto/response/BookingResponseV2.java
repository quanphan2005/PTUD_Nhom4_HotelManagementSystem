package vn.iuh.dto.response;

import java.sql.Timestamp;

public class BookingResponseV2 {
    private String maDonDatPhong;
    private Timestamp thoiGianTao;
    private Timestamp tgNhanPhong;
    private Timestamp tgTraPhong;
    private String loai; // loại đơn (ví dụ "Đặt 1 phòng" / "Đặt nhiều phòng" hoặc giá trị trong DB)
    private Double tienDatCoc;

    public BookingResponseV2() {}

    public BookingResponseV2(String maDonDatPhong, Timestamp thoiGianTao, Timestamp tgNhanPhong,
                           Timestamp tgTraPhong, String loai, Double tienDatCoc) {
        this.maDonDatPhong = maDonDatPhong;
        this.thoiGianTao = thoiGianTao;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
        this.loai = loai;
        this.tienDatCoc = tienDatCoc;
    }

    public String getMaDonDatPhong() { return maDonDatPhong; }
    public void setMaDonDatPhong(String maDonDatPhong) { this.maDonDatPhong = maDonDatPhong; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public Timestamp getTgNhanPhong() { return tgNhanPhong; }
    public void setTgNhanPhong(Timestamp tgNhanPhong) { this.tgNhanPhong = tgNhanPhong; }

    public Timestamp getTgTraPhong() { return tgTraPhong; }
    public void setTgTraPhong(Timestamp tgTraPhong) { this.tgTraPhong = tgTraPhong; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public Double getTienDatCoc() { return tienDatCoc; }
    public void setTienDatCoc(Double tienDatCoc) { this.tienDatCoc = tienDatCoc; }
}

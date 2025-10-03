package vn.iuh.dto.event.create;

import java.sql.Timestamp;
import java.util.List;

public class BookingCreationEvent {
    private String tenKhachHang;
    private String soDienThoai;
    private String CCCD;
    private String moTa;
    private Timestamp tgNhanPhong;
    private Timestamp tgTraPhong;
    private double tongTienDuTinh;
    private double tienDatCoc;
    private boolean daDatTruoc;
    private List<String> danhSachMaPhong;
    private List<DonGoiDichVu> danhSachDichVu;
    private String maPhienDangNhap;
    private Timestamp thoiGianTao;

    public BookingCreationEvent(String tenKhachHang, String soDienThoai, String CCCD, String moTa,
                                Timestamp tgNhanPhong,
                                Timestamp tgTraPhong, double tongTienDuTinh, double tienDatCoc, boolean daDatTruoc,
                                List<String> danhSachMaPhong, List<DonGoiDichVu> danhSachDichVu, String maPhienDangNhap,
                                Timestamp thoiGianTao) {
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.CCCD = CCCD;
        this.moTa = moTa;
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
        this.tongTienDuTinh = tongTienDuTinh;
        this.tienDatCoc = tienDatCoc;
        this.daDatTruoc = daDatTruoc;
        this.danhSachMaPhong = danhSachMaPhong;
        this.danhSachDichVu = danhSachDichVu;
        this.maPhienDangNhap = maPhienDangNhap;
        this.thoiGianTao = thoiGianTao;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getCCCD() {
        return CCCD;
    }

    public Timestamp getTgNhanPhong() {
        return tgNhanPhong;
    }

    public String getMoTa() {
        return moTa;
    }

    public Timestamp getTgTraPhong() {
        return tgTraPhong;
    }

    public double getTongTienDuTinh() {
        return tongTienDuTinh;
    }

    public double getTienDatCoc() {
        return tienDatCoc;
    }

    public boolean isDaDatTruoc() {
        return daDatTruoc;
    }

    public List<String> getDanhSachMaPhong() {
        return danhSachMaPhong;
    }

    public List<DonGoiDichVu> getDanhSachDichVu() {
        return danhSachDichVu;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
}

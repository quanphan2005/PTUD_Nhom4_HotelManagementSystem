package vn.iuh.dto.repository;

public class ThongTinDichVu {
    private String maDichVu;
    private String tenDichVu;
    private String loaiDichVu;
    private double donGia;

    public ThongTinDichVu(String maDichVu, String tenDichVu, String loaiDichVu, double donGia) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.loaiDichVu = loaiDichVu;
        this.donGia = donGia;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public String getLoaiDichVu() {
        return loaiDichVu;
    }

    public double getDonGia() {
        return donGia;
    }
}

package vn.iuh.dto.event.create;

public class DonGoiDichVu {
    private String maDichVu;
    private int soLuong;
    private String maChiTietDatPhong;

    public DonGoiDichVu() {
    }

    public DonGoiDichVu(String maDichVu, int soLuong, String maChiTietDatPhong) {
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.maChiTietDatPhong = maChiTietDatPhong;
    }
}

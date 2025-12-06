package vn.iuh.dto.event.create;

public class DonGoiDichVu {
    private String maDichVu;
    private String maPhong;
    private String tenPhong;
    private double giaThoiDiemDo;
    private int soLuong;

    public DonGoiDichVu() {
    }

    public DonGoiDichVu(String maDichVu, String tenPhong, double giaThoiDiemDo, int soLuong) {
        this.maDichVu = maDichVu;
        this.tenPhong = tenPhong;
        this.giaThoiDiemDo = giaThoiDiemDo;
        this.soLuong = soLuong;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public double getGiaThoiDiemDo() {
        return giaThoiDiemDo;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
}

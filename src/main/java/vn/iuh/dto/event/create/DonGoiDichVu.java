package vn.iuh.dto.event.create;

public class DonGoiDichVu {
    private String maDichVu;
    private double giaThoiDiemDo;
    private int soLuong;
    private boolean duocTang;

    public DonGoiDichVu() {
    }

    public DonGoiDichVu(String maDichVu, double giaThoiDiemDo, int soLuong, boolean duocTang) {
        this.maDichVu = maDichVu;
        this.giaThoiDiemDo = giaThoiDiemDo;
        this.soLuong = soLuong;
        this.duocTang = duocTang;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public double getGiaThoiDiemDo() {
        return giaThoiDiemDo;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public boolean isDuocTang() {
        return duocTang;
    }
}

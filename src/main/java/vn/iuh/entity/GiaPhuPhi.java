package vn.iuh.entity;

import java.sql.Timestamp;

public class GiaPhuPhi {
    private String maGiaPhuPhi;
    private double giaTruocDo;
    private double giaHienTai;
    private boolean laPhanTram;
    private String maPhienDangNhap;
    private String maPhuPhi;
    private Timestamp thoiGianTao;

    public GiaPhuPhi() {
    }

    public GiaPhuPhi(String maGiaPhuPhi, double giaTruocDo, double giaHienTai, boolean laPhanTram, String maPhienDangNhap, String maPhuPhi, Timestamp thoiGianTao) {
        this.maGiaPhuPhi = maGiaPhuPhi;
        this.giaTruocDo = giaTruocDo;
        this.giaHienTai = giaHienTai;
        this.laPhanTram = laPhanTram;
        this.maPhienDangNhap = maPhienDangNhap;
        this.maPhuPhi = maPhuPhi;
        this.thoiGianTao = thoiGianTao;
    }

    // Getters and Setters
    public String getMaGiaPhuPhi() {
        return maGiaPhuPhi;
    }

    public void setMaGiaPhuPhi(String maGiaPhuPhi) {
        this.maGiaPhuPhi = maGiaPhuPhi;
    }

    public double getGiaTruocDo() {
        return giaTruocDo;
    }

    public void setGiaTruocDo(double giaTruocDo) {
        this.giaTruocDo = giaTruocDo;
    }

    public double getGiaHienTai() {
        return giaHienTai;
    }

    public void setGiaHienTai(double giaHienTai) {
        this.giaHienTai = giaHienTai;
    }

    public boolean getLaPhanTram() {
        return laPhanTram;
    }

    public void setLaPhanTram(boolean laPhanTram) {
        this.laPhanTram = laPhanTram;
    }

    public String getMaPhienDangNhap() {
        return maPhienDangNhap;
    }

    public void setMaPhienDangNhap(String maPhienDangNhap) {
        this.maPhienDangNhap = maPhienDangNhap;
    }

    public String getMaPhuPhi() {
        return maPhuPhi;
    }

    public void setMaPhuPhi(String maPhuPhi) {
        this.maPhuPhi = maPhuPhi;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

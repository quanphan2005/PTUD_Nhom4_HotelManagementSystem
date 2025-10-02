package vn.iuh.entity;

import java.sql.Timestamp;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private int tonKho;
    private boolean coTheTang;
    private String maLoaiDichVu;
    private Timestamp thoiGianTao;

    public DichVu() {}

    public DichVu(String maDichVu, String tenDichVu, String maLoaiDichVu, Timestamp thoiGianTao) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.maLoaiDichVu = maLoaiDichVu;
        this.thoiGianTao = thoiGianTao;
    }

    public DichVu(String maDichVu, String tenDichVu, int tonKho, boolean coTheTang, String maLoaiDichVu,
                  Timestamp thoiGianTao) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.tonKho = tonKho;
        this.coTheTang = coTheTang;
        this.maLoaiDichVu = maLoaiDichVu;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    public boolean getCoTheTang() {
        return coTheTang;
    }

    public void setCoTheTang(boolean coTheTang) {
        this.coTheTang = coTheTang;
    }

    public String getMaLoaiDichVu() {
        return maLoaiDichVu;
    }

    public void setMaLoaiDichVu(String maLoaiDichVu) {
        this.maLoaiDichVu = maLoaiDichVu;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

package vn.iuh.entity;

import java.sql.Timestamp;

public class LoaiDichVu {
    private String maLoaiDichVu;
    private String tenDichVu;
    private Timestamp thoiGianTao;


    public LoaiDichVu() {
    }

    public LoaiDichVu(String maLoaiDichVu, String tenDichVu, Timestamp thoiGianTao) {
        this.maLoaiDichVu = maLoaiDichVu;
        this.tenDichVu = tenDichVu;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaLoaiDichVu() {
        return maLoaiDichVu;
    }

    public void setMaLoaiDichVu(String maLoaiDichVu) {
        this.maLoaiDichVu = maLoaiDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

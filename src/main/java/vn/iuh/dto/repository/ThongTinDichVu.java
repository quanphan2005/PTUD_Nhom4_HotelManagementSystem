package vn.iuh.dto.repository;

public class ThongTinDichVu {
    private String maDichVu;
    private String tenDichVu;
    private int tonKho;
    private boolean coTheTang;
    private double donGia;
    private String tenLoaiDichVu;


    public ThongTinDichVu(String maDichVu, String tenDichVu, int tonKho, boolean coTheTang, double donGia,
                          String tenLoaiDichVu) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.tonKho = tonKho;
        this.coTheTang = coTheTang;
        this.donGia = donGia;
        this.tenLoaiDichVu = tenLoaiDichVu;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public int getTonKho() {
        return tonKho;
    }

    public boolean isCoTheTang() {
        return coTheTang;
    }

    public double getDonGia() {
        return donGia;
    }

    public String getTenLoaiDichVu() {
        return tenLoaiDichVu;
    }
}

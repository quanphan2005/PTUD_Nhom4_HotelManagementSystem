package vn.iuh.dto.response;

import java.sql.Timestamp;

// Sử dụng cho table danh sách dịch vụ
public class ServiceResponse {
    private String maDichVu;
    private String tenDichVu;
    private int tonKho;
    private boolean coTheTang;
    private String maLoaiDichVu;
    private Double giaHienTai; // có thể null nếu chưa có giá
    private Timestamp thoiGianTao;

    public ServiceResponse() {}

    // getters / setters
    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }

    public boolean isCoTheTang() { return coTheTang; }
    public void setCoTheTang(boolean coTheTang) { this.coTheTang = coTheTang; }

    public String getMaLoaiDichVu() { return maLoaiDichVu; }
    public void setMaLoaiDichVu(String maLoaiDichVu) { this.maLoaiDichVu = maLoaiDichVu; }

    public Double getGiaHienTai() { return giaHienTai; }
    public void setGiaHienTai(Double giaHienTai) { this.giaHienTai = giaHienTai; }

    public Timestamp getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(Timestamp thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}

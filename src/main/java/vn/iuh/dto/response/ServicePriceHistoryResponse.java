package vn.iuh.dto.response;

import java.sql.Timestamp;

// Sử dụng cho lịch sử thay đổi giá dịch vụ
public class ServicePriceHistoryResponse {
    private Timestamp ngayThayDoi;
    private Double giaSauThayDoi;
    private String maNhanVien;
    private String tenNhanVien;
    private String tenDichVu;

    public ServicePriceHistoryResponse() {}

    public ServicePriceHistoryResponse(Timestamp ngayThayDoi, Double giaSauThayDoi, String maNhanVien, String tenNhanVien, String tenDichVu) {
        this.ngayThayDoi = ngayThayDoi;
        this.giaSauThayDoi = giaSauThayDoi;
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.tenDichVu = tenDichVu;
    }

    public Timestamp getNgayThayDoi() { return ngayThayDoi; }
    public void setNgayThayDoi(Timestamp ngayThayDoi) { this.ngayThayDoi = ngayThayDoi; }

    public Double getGiaSauThayDoi() { return giaSauThayDoi; }
    public void setGiaSauThayDoi(Double giaSauThayDoi) { this.giaSauThayDoi = giaSauThayDoi; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
}

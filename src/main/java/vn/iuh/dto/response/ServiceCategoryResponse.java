package vn.iuh.dto.response;

public class ServiceCategoryResponse {
    private String maLoai;
    private String tenLoai;
    private int soLuong;

    public ServiceCategoryResponse() {}

    public ServiceCategoryResponse(String maLoai, String tenLoai, int soLuong) {
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
        this.soLuong = soLuong;
    }

    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String maLoai) { this.maLoai = maLoai; }

    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
}

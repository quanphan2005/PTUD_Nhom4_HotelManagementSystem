package vn.iuh.dto.repository;

public class NoiThatAssignment {
    private String maNoiThat;
    private int soLuong;

    public NoiThatAssignment() {}
    public NoiThatAssignment(String maNoiThat, int soLuong) {
        this.maNoiThat = maNoiThat;
        this.soLuong = soLuong;
    }
    public String getMaNoiThat() { return maNoiThat; }
    public void setMaNoiThat(String maNoiThat) { this.maNoiThat = maNoiThat; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
}

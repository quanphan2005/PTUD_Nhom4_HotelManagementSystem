package vn.iuh.dto.response;

import java.sql.Timestamp;

public class RoomCategoryPriceHistory {
    private Timestamp time;
    private double giaGioCu;
    private double giaNgayCu;
    private double giaGioMoi;
    private double giaNgayMoi;
    private String actorName; // tên nhân viên thực hiện (có thể rỗng)

    public RoomCategoryPriceHistory() {}

    public RoomCategoryPriceHistory(Timestamp time, double giaGioCu, double giaNgayCu, double giaGioMoi, double giaNgayMoi, String actorName) {
        this.time = time;
        this.giaGioCu = giaGioCu;
        this.giaNgayCu = giaNgayCu;
        this.giaGioMoi = giaGioMoi;
        this.giaNgayMoi = giaNgayMoi;
        this.actorName = actorName;
    }

    public Timestamp getTime() { return time; }
    public void setTime(Timestamp time) { this.time = time; }

    public double getGiaGioCu() { return giaGioCu; }
    public void setGiaGioCu(double giaGioCu) { this.giaGioCu = giaGioCu; }

    public double getGiaNgayCu() { return giaNgayCu; }
    public void setGiaNgayCu(double giaNgayCu) { this.giaNgayCu = giaNgayCu; }

    public double getGiaGioMoi() { return giaGioMoi; }
    public void setGiaGioMoi(double giaGioMoi) { this.giaGioMoi = giaGioMoi; }

    public double getGiaNgayMoi() { return giaNgayMoi; }
    public void setGiaNgayMoi(double giaNgayMoi) { this.giaNgayMoi = giaNgayMoi; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
}

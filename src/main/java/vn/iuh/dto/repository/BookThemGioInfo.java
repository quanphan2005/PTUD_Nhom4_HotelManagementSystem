package vn.iuh.dto.repository;

import java.sql.Timestamp;

public class BookThemGioInfo {
    private final Timestamp tgNhanPhong;
    private final Timestamp tgTraPhong;
    private final int gioToiDaChoPhep;

    public BookThemGioInfo(Timestamp tgNhanPhong, Timestamp tgTraPhong, int gioToiDaChoPhep) {
        this.tgNhanPhong = tgNhanPhong;
        this.tgTraPhong = tgTraPhong;
        this.gioToiDaChoPhep = gioToiDaChoPhep;
    }

    public Timestamp getTgNhanPhong() {
        return tgNhanPhong;
    }

    public Timestamp getTgTraPhong() {
        return tgTraPhong;
    }

    public int getGioToiDaChoPhep() {
        return gioToiDaChoPhep;
    }

    @Override
    public String toString() {
        return "BookThemGioInfo{" +
                "tgNhanPhong=" + tgNhanPhong +
                ", tgTraPhong=" + tgTraPhong +
                ", gioToiDaChoPhep=" + gioToiDaChoPhep +
                '}';
    }
}

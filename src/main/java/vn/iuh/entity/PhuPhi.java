package vn.iuh.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class PhuPhi {
    private String maPhuPhi;
    private String tenPhuPhi;
    private Timestamp thoiGianTao;

    public PhuPhi() {
    }

    public PhuPhi(String maPhuPhi, String tenPhuPhi, Timestamp thoiGianTao) {
        this.maPhuPhi = maPhuPhi;
        this.tenPhuPhi = tenPhuPhi;
        this.thoiGianTao = thoiGianTao;
    }

    public String getMaPhuPhi() {
        return maPhuPhi;
    }

    public void setMaPhuPhi(String maPhuPhi) {
        this.maPhuPhi = maPhuPhi;
    }

    public String getTenPhuPhi() {
        return tenPhuPhi;
    }

    public void setTenPhuPhi(String tenPhuPhi) {
        this.tenPhuPhi = tenPhuPhi;
    }

    public Timestamp getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(Timestamp thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}

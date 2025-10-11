package vn.iuh.service;

import vn.iuh.entity.CongViec;

import java.sql.Timestamp;
import java.util.List;

public interface CongViecService {
    CongViec themCongViec(String tenTrangThai, Timestamp tgBatDau, Timestamp tgKetThuc, String maPhong);
    boolean removeOutDateJob(String jobId);
    List<CongViec> taoDanhSachCongViec(String tenTrangThai, Timestamp tgBatDau, Timestamp tgKetThuc, List<String> danhSachMaPhong);
}

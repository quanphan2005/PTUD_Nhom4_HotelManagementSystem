package vn.iuh.service;

import vn.iuh.entity.CongViec;

import java.sql.Timestamp;

public interface CongViecService {
    CongViec themCongViec(String tenTrangThai, Timestamp tgBatDau, Timestamp tgKetThuc, String maPhong);
    boolean removeOutDateJob(String jobId);
}

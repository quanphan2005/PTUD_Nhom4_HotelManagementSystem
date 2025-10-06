package vn.iuh.service;

import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;

import java.util.List;

public interface GoiDichVuService {
    List<ThongTinDichVu> timTatCaThongTinDichVu();
    boolean goiDichVu(String maChiTietDatPhong, List<DonGoiDichVu> danhSachDichVu, String maPhienDangNhap);
}

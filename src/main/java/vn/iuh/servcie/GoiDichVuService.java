package vn.iuh.servcie;

import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;

import java.util.List;

public interface GoiDichVuService {
    List<ThongTinDichVu> timTatCaThongTinDichVu();
    boolean goiDichVu(List<DonGoiDichVu> danhSachDichVu);
}

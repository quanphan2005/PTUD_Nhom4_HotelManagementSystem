package vn.iuh.servcie.impl;

import vn.iuh.dao.GoiDichVuDao;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.servcie.GoiDichVuService;

import java.util.List;

public class GoiDichVuServiceImpl implements GoiDichVuService {
    GoiDichVuDao goiDichVuDao;

    public GoiDichVuServiceImpl() {
        this.goiDichVuDao = new GoiDichVuDao();
    }

    public GoiDichVuServiceImpl(GoiDichVuDao goiDichVuDao) {
        this.goiDichVuDao = goiDichVuDao;
    }

    @Override
    public List<ThongTinDichVu> timTatCaThongTinDichVu() {
        List<ThongTinDichVu> danhSachThongTinDichVu = goiDichVuDao.timTatCaThongTinDichVu();

        if (danhSachThongTinDichVu.isEmpty())
            System.out.println("Không tìm thấy dịch vụ nào trong hệ thống.");

        return danhSachThongTinDichVu;
    }

    // TODO: implement method
    @Override
    public boolean goiDichVu(String maPhienDangNhap, List<DonGoiDichVu> danhSachDichVu) {
        return false;
    }
}

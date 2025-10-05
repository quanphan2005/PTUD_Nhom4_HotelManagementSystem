package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.GoiDichVuDao;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.service.GoiDichVuService;
import vn.iuh.util.EntityUtil;

import java.util.ArrayList;
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
    public boolean goiDichVu(String maChiTietDatPhong, List<DonGoiDichVu> danhSachDichVu, String maPhienDangNhap) {
        // 2.5. Create RoomUsageServiceEntity & insert to DB
        List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();
        PhongDungDichVu phongDungDichVuMoiNhat = goiDichVuDao.timPhongDungDichVuMoiNhat();
        String maPhongDungDichVuMoiNhat =
                phongDungDichVuMoiNhat == null ? null : phongDungDichVuMoiNhat.getMaPhongDungDichVu();

        for (DonGoiDichVu donGoiDichVu : danhSachDichVu) {
            PhongDungDichVu phongDungDichVu = createRoomUsageServiceEntity(
                    maPhongDungDichVuMoiNhat,
                    maChiTietDatPhong,
                    donGoiDichVu,
                    maPhienDangNhap
            );

            danhSachPhongDungDichVu.add(phongDungDichVu);
            maPhongDungDichVuMoiNhat = phongDungDichVu.getMaPhongDungDichVu();
        }

        try {
            goiDichVuDao.themPhongDungDichVu(danhSachPhongDungDichVu);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private PhongDungDichVu createRoomUsageServiceEntity(String maPhongDungDichVuMoiNhat,
                                                         String maChiTietDatPhong,
                                                         DonGoiDichVu dichVu,
                                                         String maPhienDangNhap) {
        String id;
        String prefix = EntityIDSymbol.ROOM_USAGE_SERVICE_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.ROOM_USAGE_SERVICE_PREFIX.getLength();

        if (maPhongDungDichVuMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(maPhongDungDichVuMoiNhat, prefix, numberLength);
        }

        return new PhongDungDichVu(
                id,
                dichVu.getSoLuong(),
                dichVu.getGiaThoiDiemDo(),
                dichVu.isDuocTang(),
                maChiTietDatPhong,
                dichVu.getMaDichVu(),
                maPhienDangNhap,
                null
        );
    }
}

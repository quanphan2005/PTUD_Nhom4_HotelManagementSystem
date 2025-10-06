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
    private final GoiDichVuDao goiDichVuDao;

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

    @Override
    public boolean goiDichVu(String maChiTietDatPhong, List<DonGoiDichVu> danhSachDichVu, String maPhienDangNhap) {

        // 1. Tạo danh sách phòng dùng dịch vụ từ danh sách đơn gọi dịch vụ
        PhongDungDichVu phongDungDichVuMoiNhat = goiDichVuDao.timPhongDungDichVuMoiNhat();
        if (danhSachDichVu.isEmpty()) {
            System.out.println("Danh sách dịch vụ trống. Vui lòng kiểm tra lại.");
            return false;
        }

        try {
            goiDichVuDao.khoiTaoGiaoTac();
            String maPhongDungDichVuMoiNhat =
                    phongDungDichVuMoiNhat == null ? null : phongDungDichVuMoiNhat.getMaPhongDungDichVu();

            // 2. Update Service Quantity
            for (DonGoiDichVu dichVu : danhSachDichVu) {
                goiDichVuDao.capNhatSoLuongTonKhoDichVu(dichVu.getMaDichVu(), dichVu.getSoLuong());
            }

            List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();
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

            // 3. Thêm mới phòng dùng dịch vụ
            goiDichVuDao.themPhongDungDichVu(danhSachPhongDungDichVu);

            goiDichVuDao.thucHienGiaoTac();
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi gọi dịch vụ: " + e.getMessage());
            goiDichVuDao.hoanTacGiaoTac();
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

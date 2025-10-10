package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.DonGoiDichVuDao;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.service.GoiDichVuService;
import vn.iuh.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;

public class GoiDichVuServiceImpl implements GoiDichVuService {
    private final DonGoiDichVuDao donGoiDichVuDao;

    public GoiDichVuServiceImpl() {
        this.donGoiDichVuDao = new DonGoiDichVuDao();
    }

    public GoiDichVuServiceImpl(DonGoiDichVuDao donGoiDichVuDao) {
        this.donGoiDichVuDao = donGoiDichVuDao;
    }

    @Override
    public List<ThongTinDichVu> timTatCaThongTinDichVu() {
        List<ThongTinDichVu> danhSachThongTinDichVu = donGoiDichVuDao.timTatCaThongTinDichVu();

        if (danhSachThongTinDichVu.isEmpty())
            System.out.println("Không tìm thấy dịch vụ nào trong hệ thống.");

        return danhSachThongTinDichVu;
    }

    @Override
    public boolean goiDichVu(String maChiTietDatPhong, List<DonGoiDichVu> danhSachDichVu, String maPhienDangNhap) {

        // 1. Tạo danh sách phòng dùng dịch vụ từ danh sách đơn gọi dịch vụ
        PhongDungDichVu phongDungDichVuMoiNhat = donGoiDichVuDao.timPhongDungDichVuMoiNhat();
        if (danhSachDichVu.isEmpty()) {
            System.out.println("Danh sách dịch vụ trống. Vui lòng kiểm tra lại.");
            return false;
        }

        try {
            donGoiDichVuDao.khoiTaoGiaoTac();
            String maPhongDungDichVuMoiNhat =
                    phongDungDichVuMoiNhat == null ? null : phongDungDichVuMoiNhat.getMaPhongDungDichVu();

            // 2. Update Service Quantity
            for (DonGoiDichVu dichVu : danhSachDichVu) {
                donGoiDichVuDao.capNhatSoLuongTonKhoDichVu(dichVu.getMaDichVu(), -dichVu.getSoLuong());
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
            donGoiDichVuDao.themPhongDungDichVu(danhSachPhongDungDichVu);

            donGoiDichVuDao.thucHienGiaoTac();
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi gọi dịch vụ: " + e.getMessage());
            donGoiDichVuDao.hoanTacGiaoTac();
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

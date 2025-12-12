package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.DonGoiDichVuDao;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.RoomUsageServiceInfo;
import vn.iuh.dto.repository.ThongTinDichVu;
import vn.iuh.dto.response.RoomUsageServiceResponse;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.service.GoiDichVuService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class GoiDichVuServiceImpl implements GoiDichVuService {
    private final DonGoiDichVuDao donGoiDichVuDao;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;

    public GoiDichVuServiceImpl() {
        this.donGoiDichVuDao = new DonGoiDichVuDao();
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
    }

    public GoiDichVuServiceImpl(DonGoiDichVuDao donGoiDichVuDao, ChiTietDatPhongDAO chiTietDatPhongDAO) {
        this.donGoiDichVuDao = donGoiDichVuDao;
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
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

        if (maChiTietDatPhong == null || maChiTietDatPhong.isEmpty()) {
            System.out.println("Mã chi tiết đặt phòng không hợp lệ. Vui lòng kiểm tra lại.");
            return false;
        }

        if (chiTietDatPhongDAO.timChiTietDatPhong(maChiTietDatPhong) == null) {
            System.out.println("Chi tiết đặt phòng không tồn tại. Vui lòng kiểm tra lại.");
            return false;
        }

        // 1. Tạo danh sách phòng dùng dịch vụ từ danh sách đơn gọi dịch vụ
        PhongDungDichVu phongDungDichVuMoiNhat = donGoiDichVuDao.timPhongDungDichVuMoiNhat();
        if (danhSachDichVu.isEmpty()) {
            System.out.println("Danh sách dịch vụ trống. Vui lòng kiểm tra lại.");
            return false;
        }

        try {
            DatabaseUtil.khoiTaoGiaoTac();
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

            DatabaseUtil.thucHienGiaoTac();
            return true;
        } catch (Exception e) {
            System.out.println("Lỗi gọi dịch vụ: " + e.getMessage());
            DatabaseUtil.hoanTacGiaoTac();
            return false;
        }
    }

    @Override
    public List<RoomUsageServiceResponse> timTatCaDonGoiDichVuBangMaChiTietDatPhong(String maChiTietDatPhong) {
        System.out.println("Find all room usage service by booking detail id: " + maChiTietDatPhong);
        List<RoomUsageServiceInfo> roomUsageServiceInfos =
                donGoiDichVuDao.timTatCaDonGoiDichVuBangChiTietDatPhong(maChiTietDatPhong);

        List<RoomUsageServiceResponse> responses = new ArrayList<>();
        for (RoomUsageServiceInfo info : roomUsageServiceInfos)
            responses.add(createRoomUsageServiceResponseFromInfo(info));

        return responses;
    }

    private RoomUsageServiceResponse createRoomUsageServiceResponseFromInfo(RoomUsageServiceInfo info) {
        return new RoomUsageServiceResponse(
                info.getMaPhongDungDichVu(),
                info.getTenPhong(),
                info.getTenDichVu(),
                info.getSoLuong(),
                info.getGiaThoiDiemDo(),
                info.getSoLuong() * info.getGiaThoiDiemDo()
        );
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
                maChiTietDatPhong,
                dichVu.getMaDichVu(),
                maPhienDangNhap,
                null
        );
    }
}

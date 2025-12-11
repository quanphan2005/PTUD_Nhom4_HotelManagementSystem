package vn.iuh.service.impl;

import com.github.lgooddatepicker.zinternaltools.Pair;
import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.*;
import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.entity.GiaPhong;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.exception.BusinessException;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.panel.statistic.FillterRoomStatistic;
import vn.iuh.gui.panel.statistic.RoomStatistic;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoaiPhongServiceImpl implements LoaiPhongService {
    private final LoaiPhongDAO loaiPhongDao;
    private final GiaPhongDAO giaPhongDAO;

    public LoaiPhongServiceImpl() {

        this.loaiPhongDao = new LoaiPhongDAO();
        this.giaPhongDAO = new GiaPhongDAO();
    }

    @Override
    public LoaiPhong getRoomCategoryByID(String id) {
        return null;
    }

    @Override
    public List<RoomCategoryResponse> getAllRoomCategories() {
        List<LoaiPhong> danhSachLoaiPhong = loaiPhongDao.layTatCaLoaiPhong();
        if(Objects.isNull(danhSachLoaiPhong) || danhSachLoaiPhong.isEmpty()){
            throw new BusinessException("Không tìm thấy loại phòng nào");
        }

        return danhSachLoaiPhong.stream().map(this::createRoomCategoryResponse).toList();
    }

    @Override
    public LoaiPhong createRoomCategory(LoaiPhong loaiPhong) {
        return null;
    }

    @Override
    public LoaiPhong updateRoomCategory(LoaiPhong loaiPhong) {
        return null;
    }

    @Override
    public boolean deleteRoomCategoryByID(String id) {
        return false;
    }

    @Override
    public BigDecimal layGiaTheoLoaiPhong(String maLoaiPhong, boolean isGiaNgay) {
        if(maLoaiPhong != null){
            Map<String, Double> listPrice = loaiPhongDao.layGiaLoaiPhongTheoId(maLoaiPhong);
            if(Objects.isNull(listPrice) || listPrice.isEmpty()){
                throw new RuntimeException("Không tìm thấy giá của mã phòng");
            }
            else {
                return isGiaNgay ? BigDecimal.valueOf(listPrice.get("gia_ngay")) : BigDecimal.valueOf(listPrice.get("gia_gio"));
            }
        }
        throw new RuntimeException("Mã loại phòng rỗng ko tìm thấy ");
    }

    public RoomCategoryResponse createRoomCategoryResponse(LoaiPhong loaiPhong){
        if(Objects.isNull(loaiPhong)){
            return null;
        }
        return new RoomCategoryResponse(
                loaiPhong.getMaLoaiPhong(),
                loaiPhong.getTenLoaiPhong(),
                loaiPhong.getSoLuongKhach(),
                loaiPhong.getPhanLoai()
        );
    }

    public List<RoomStatistic> getListRoomCategoryByFilter(FillterRoomStatistic filter){
        return loaiPhongDao.layThongKeTheoLoaiPhong(filter.getStartDate(), filter.getEndDate());
    }

    public LoaiPhong findLatestIncludingDeleted() {
        return loaiPhongDao.timLoaiPhongMoiNhatBaoGomDaXoa();
    }

    @Override
    public LoaiPhong createRoomCategoryV2(LoaiPhong loaiPhong, double giaNgay, double giaGio, List<NoiThatAssignment> itemsWithQty) {
        if (loaiPhong == null) throw new IllegalArgumentException("loaiPhong null");

        if (loaiPhong.getTenLoaiPhong() == null || loaiPhong.getTenLoaiPhong().isBlank()) {
            throw new BusinessException("Tên loại phòng không được rỗng");
        }

        // 1) kiểm tra trùng tên (case-insensitive)
        List<LoaiPhong> all = loaiPhongDao.layTatCaLoaiPhong();
        if (all != null) {
            for (LoaiPhong lp : all) {
                if (lp.getTenLoaiPhong() != null && lp.getTenLoaiPhong().equalsIgnoreCase(loaiPhong.getTenLoaiPhong())) {
                    throw new BusinessException("Tên loại phòng đã tồn tại: " + loaiPhong.getTenLoaiPhong());
                }
            }
        }

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnect();
            DatabaseUtil.khoiTaoGiaoTac();

            // DAO dùng chung connection để nằm trong cùng 1 transaction
            LoaiPhongDAO lpDao = new LoaiPhongDAO();
            GiaPhongDAO gpDao = new GiaPhongDAO();
            NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO();
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

            // --- Sinh ma_loai_phong nếu chưa có (theo quy ước LP + 8 chữ số) ---
            if (loaiPhong.getMaLoaiPhong() == null || loaiPhong.getMaLoaiPhong().isBlank()) {
                LoaiPhong latestLp = lpDao.timLoaiPhongMoiNhatBaoGomDaXoa();
                String lastLpId = (latestLp != null) ? latestLp.getMaLoaiPhong() : null;
                String newLpId = EntityUtil.increaseEntityID(
                        lastLpId,
                        EntityIDSymbol.LOAI_PHONG_PREFIX.getPrefix(),
                        EntityIDSymbol.LOAI_PHONG_PREFIX.getLength()
                );
                loaiPhong.setMaLoaiPhong(newLpId);
            }

            // 2.a) Insert loại phòng
            LoaiPhong inserted = lpDao.insertLoaiPhong(loaiPhong);
            if (inserted == null) {
                conn.rollback();
                throw new RuntimeException("Không thể tạo loại phòng");
            }
            String maLoai = inserted.getMaLoaiPhong();

            // 2.b) Insert giá cho loại phòng (tạo ma_gia_phong theo quy ước)
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // tìm mã giá phòng mới nhất (kể cả đã xóa) -> để tăng id
            GiaPhong latestGia = gpDao.timGiaPhongMoiNhat(); // giả sử method này đã được cài trong GiaPhongDAO
            String lastGiaId = (latestGia != null) ? latestGia.getMaGiaPhong() : null;
            String newGiaId = EntityUtil.increaseEntityID(
                    lastGiaId,
                    EntityIDSymbol.ROOM_PRICE.getPrefix(),
                    EntityIDSymbol.ROOM_PRICE.getLength()
            );

            // build GiaPhong object — giả sử có setter như bên dưới
            GiaPhong gp = new GiaPhong();
            gp.setMaGiaPhong(newGiaId);
            gp.setMaLoaiPhong(maLoai);
            // khi thêm mới, giá cũ để null hoặc 0 (tuỳ schema). Ta đặt giá_cu = 0 nếu không có.
            gp.setGiaNgayCu(0.0);
            gp.setGiaGioCu(0.0);
            gp.setGioNgayMoi(giaNgay);
            gp.setGioGioMoi(giaGio);
            gp.setMaPhienDangNhap(Main.getCurrentLoginSession());
            gp.setThoiGianTao(now);

            boolean priceInserted = gpDao.insertGiaPhong(gp);
            if (!priceInserted) {
                DatabaseUtil.hoanTacGiaoTac();
                throw new RuntimeException("Không thể thêm giá cho loại phòng");
            }

            // 2.c) Thêm mapping nội thất (với số lượng) — method trong DAO đã tự sinh id mapping (NP...)
            List<NoiThatAssignment> assignments = itemsWithQty == null ? new java.util.ArrayList<>() : itemsWithQty;
            boolean mappingsOk = ntlpDao.replaceMappingsWithQuantities(maLoai, assignments);
            if (!mappingsOk) {
                DatabaseUtil.hoanTacGiaoTac();
                throw new RuntimeException("Không thể gán nội thất cho loại phòng");
            }

            // 2.d) Ghi lịch sử thao tác (sinh id theo quy ước LT...)
            try {
                var lastHistory = lichSuDao.timLichSuThaoTacMoiNhat();
                String lastHistoryId = (lastHistory != null) ? lastHistory.getMaLichSuThaoTac() : null;
                String newHistoryId = EntityUtil.increaseEntityID(
                        lastHistoryId,
                        EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                        EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()
                );

                LichSuThaoTac wh = new LichSuThaoTac();
                wh.setMaLichSuThaoTac(newHistoryId);
                wh.setTenThaoTac(ActionType.CREATE_ROOM_CATEGORY.getActionName());
                wh.setMoTa(String.format("Tạo loại phòng %s - %s; Giá ngày=%.0f, Giá giờ=%.0f; Nội thất=%d",
                        maLoai, inserted.getTenLoaiPhong(), giaNgay, giaGio, assignments.size()));
                wh.setMaPhienDangNhap(Main.getCurrentLoginSession());
                wh.setThoiGianTao(now);
                lichSuDao.themLichSuThaoTac(wh);
            } catch (Exception e) {
                // Không rollback chỉ vì lỗi ghi lịch sử — log để debug
                System.err.println("Không thể ghi lịch sử thao tác (không rollback): " + e.getMessage());
            }

            // commit transaction
            DatabaseUtil.thucHienGiaoTac();

            // trả về LoaiPhong vừa tạo (lấy lại bằng DAO dùng cùng connection/hoặc mới tuỳ impl)
            return lpDao.getRoomCategoryByID(maLoai);
        } catch (BusinessException be) {
            try { if (conn != null) DatabaseUtil.hoanTacGiaoTac(); } catch (Exception ignored) {}
            throw be;
        } catch (Exception ex) {
            try { if (conn != null) DatabaseUtil.hoanTacGiaoTac(); } catch (Exception ignored) {}
            throw new RuntimeException("Lỗi khi tạo loại phòng: " + ex.getMessage(), ex);
        }
    }



    @Override
    public LoaiPhong updateRoomCategoryV2(LoaiPhong loaiPhong) {
        if (loaiPhong == null || loaiPhong.getMaLoaiPhong() == null) {
            throw new IllegalArgumentException("loaiPhong hoặc id rỗng");
        }
        return loaiPhongDao.capNhatLoaiPhong(loaiPhong);
    }

    @Override
    public boolean deleteRoomCategoryByIDV2(String id) {
        if (id == null || id.isBlank()) return false;
        return loaiPhongDao.xoaLoaiPhong(id);
    }

    public boolean deleteRoomCategoryWithAudit(String maLoaiPhong, String maPhienDangNhap) {
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) {
            throw new IllegalArgumentException("maLoaiPhong rỗng");
        }
        if (maPhienDangNhap == null) maPhienDangNhap = "SYSTEM";

        ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO();
        NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO();
        LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

        // 1) kiểm tra booking tương lai
        boolean hasFuture = ctDao.hasFutureBookingsForLoaiPhong(maLoaiPhong);
        if (hasFuture) {
            throw new BusinessException("Loại phòng đang có đặt phòng trong tương lai, không thể xóa.");
        }

        // 2) xóa (soft-delete) mapping nội thất
        int mappingsDeleted = 0;
        try {
            mappingsDeleted = ntlpDao.softDeleteByLoaiPhong(maLoaiPhong);
        } catch (Exception e) {
            // log (không dừng) - tiếp tục cố gắng xóa loại phòng nhưng bạn có thể rollback nếu muốn transaction
            System.err.println("Lỗi khi xóa mapping nội thất: " + e.getMessage());
        }

        // 3) soft-delete loại phòng
        boolean deleted = loaiPhongDao.xoaLoaiPhong(maLoaiPhong);

        // 4) ghi lịch sử thao tác vào LichSuThaoTac
        try {
            // tạo id mới cho LichSuThaoTac (giả sử prefix "LS" + 8 chữ số)
            String latestId = null;
            try {
                var latest = lichSuDao.timLichSuThaoTacMoiNhat();
                if (latest != null) latestId = latest.getMaLichSuThaoTac();
            } catch (Exception ignore) {}


            String newId = EntityUtil.increaseEntityID(latestId, "LT", 8);


            LichSuThaoTac wh = new LichSuThaoTac();
            wh.setMaLichSuThaoTac(newId);
            wh.setTenThaoTac(ActionType.DELETE_ROOM_CATEGORY.getActionName());
            String detail = String.format("Xóa loại phòng %s. Xóa mapping nội thất: %d. Kết quả xóa loại phòng: %s",
                    maLoaiPhong, mappingsDeleted, deleted ? "OK" : "FAIL");
            wh.setMoTa(detail);
            wh.setMaPhienDangNhap(Main.getCurrentLoginSession());

            lichSuDao.themLichSuThaoTac(wh);
        } catch (Exception e) {
            System.err.println("Không thể ghi lịch sử thao tác: " + e.getMessage());
        }

        return deleted;
    }

    public boolean updateRoomCategoryWithAudit(LoaiPhong loaiPhong, List<NoiThatAssignment> itemsWithQty, String maPhienDangNhap) {
        if (loaiPhong == null || loaiPhong.getMaLoaiPhong() == null) throw new IllegalArgumentException("loaiPhong null/không có mã");

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnect();
            DatabaseUtil.khoiTaoGiaoTac();

            // DAO dùng cùng connection để đảm bảo transaction
            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO();
            // phương thức bạn đã thêm: hasCurrentOrFutureBookingsForLoaiPhong(String maLoai)
            boolean hasBooking = ctDao.hasCurrentOrFutureBookingsForLoaiPhong(loaiPhong.getMaLoaiPhong());
            if (hasBooking) {
                conn.setAutoCommit(true);
                return false; // không cập nhật nếu đang có booking
            }

            LoaiPhongDAO lpDao = new LoaiPhongDAO();
            LoaiPhong updated = lpDao.capNhatLoaiPhong(loaiPhong);
            if (updated == null) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // cập nhật mapping nội thất (soft-delete cũ + insert mới) với số lượng
            NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO();
            boolean replaced = ntlpDao.replaceMappingsWithQuantities(loaiPhong.getMaLoaiPhong(),
                    itemsWithQty == null ? new java.util.ArrayList<>() : itemsWithQty);
            if (!replaced) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // ghi lịch sử thao tác
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();
            LichSuThaoTac wh = new LichSuThaoTac();
            // tạo id đơn giản (bạn có thể thay bằng EntityUtil tăng id nếu muốn)
            wh.setMaLichSuThaoTac("LS" + System.currentTimeMillis());
            wh.setTenThaoTac("CẬP_NHẬT_LOẠI_PHÒNG");
            wh.setMoTa(String.format("Cập nhật loại phòng %s; nội thất=%d", loaiPhong.getMaLoaiPhong(),
                    itemsWithQty == null ? 0 : itemsWithQty.size()));
            wh.setMaPhienDangNhap(maPhienDangNhap);
            lichSuDao.themLichSuThaoTac(wh);

            DatabaseUtil.thucHienGiaoTac();
            return true;
        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
            throw new RuntimeException("Lỗi khi cập nhật loại phòng (transaction): " + ex.getMessage(), ex);
        }
    }

    @Override
    public LoaiPhong getRoomCategoryByIDV2(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return loaiPhongDao.getRoomCategoryByID(id);
        } catch (Exception ex) {
            // log nếu cần
            System.out.println("Lỗi khi lấy LoaiPhong by id: " + ex.getMessage());
            return null;
        }
    }

}

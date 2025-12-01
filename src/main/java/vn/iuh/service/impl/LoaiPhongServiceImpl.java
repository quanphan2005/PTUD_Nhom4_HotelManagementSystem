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

        // 1) kiểm tra trùng tên
        List<LoaiPhong> all = null;
        try {
            all = loaiPhongDao.layTatCaLoaiPhong();
        } catch (Exception e) {
            try { all = new LoaiPhongDAO().layTatCaLoaiPhong(); } catch (Exception ignore) { all = null; }
        }
        if (all != null) {
            for (LoaiPhong lp : all) {
                if (lp.getTenLoaiPhong() != null && lp.getTenLoaiPhong().equalsIgnoreCase(loaiPhong.getTenLoaiPhong())) {
                    throw new BusinessException("Tên loại phòng đã tồn tại: " + loaiPhong.getTenLoaiPhong());
                }
            }
        }

        DatPhongDAO txManager = new DatPhongDAO();
        Connection conn = txManager.getConnection();

        try {
            txManager.khoiTaoGiaoTac();

            LoaiPhongDAO lpDao = new LoaiPhongDAO(conn);
            GiaPhongDAO gpDao = new GiaPhongDAO(conn);
            NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO(conn);
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO(conn);

            // sinh mã cho loại phòng mới
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

            // Insert loại phòng
            LoaiPhong inserted = lpDao.insertLoaiPhong(loaiPhong);
            if (inserted == null) {
                txManager.hoanTacGiaoTac();
                throw new RuntimeException("Không thể tạo loại phòng");
            }
            String maLoai = inserted.getMaLoaiPhong();

            // Insert giá
            Timestamp now = new Timestamp(System.currentTimeMillis());
            GiaPhong latestGia = gpDao.timGiaPhongMoiNhat();
            String lastGiaId = (latestGia != null) ? latestGia.getMaGiaPhong() : null;
            String newGiaId = EntityUtil.increaseEntityID(
                    lastGiaId,
                    EntityIDSymbol.ROOM_PRICE.getPrefix(),
                    EntityIDSymbol.ROOM_PRICE.getLength()
            );

            GiaPhong gp = new GiaPhong();
            gp.setMaGiaPhong(newGiaId);
            gp.setMaLoaiPhong(maLoai);
            gp.setGiaNgayCu(0.0);
            gp.setGiaGioCu(0.0);
            gp.setGiaNgayMoi(giaNgay);
            gp.setGiaGioMoi(giaGio);
            gp.setMaPhienDangNhap(Main.getCurrentLoginSession());
            gp.setThoiGianTao(now);

            boolean priceInserted = gpDao.insertGiaPhong(gp);
            if (!priceInserted) {
                txManager.hoanTacGiaoTac();
                throw new RuntimeException("Không thể thêm giá cho loại phòng");
            }

            // Thêm mapping nội thất
            List<NoiThatAssignment> assignments = itemsWithQty == null ? new java.util.ArrayList<>() : itemsWithQty;
            boolean mappingsOk = ntlpDao.replaceMappingsWithQuantities(maLoai, assignments);
            if (!mappingsOk) {
                txManager.hoanTacGiaoTac();
                throw new RuntimeException("Không thể gán nội thất cho loại phòng");
            }

            // Ghi lịch sử thao tác
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
                System.err.println("Không thể ghi lịch sử thao tác (không rollback): " + e.getMessage());
            }

            txManager.thucHienGiaoTac();

            return lpDao.getRoomCategoryByID(maLoai);

        } catch (BusinessException be) {
            try { txManager.hoanTacGiaoTac(); } catch (Exception ignored) {}
            throw be;
        } catch (Exception ex) {
            try { txManager.hoanTacGiaoTac(); } catch (Exception ignored) {}
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

    @Override
    public boolean deleteRoomCategoryWithAudit(String maLoaiPhong) {
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) {
            throw new IllegalArgumentException("maLoaiPhong rỗng");
        }

        DatPhongDAO tx = new DatPhongDAO();
        try {
            tx.khoiTaoGiaoTac();
            Connection conn = tx.getConnection();

            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO(conn);
            CongViecDAO cvDao = new CongViecDAO(conn);
            NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO(conn);
            GiaPhongDAO gpDao = new GiaPhongDAO(conn);
            LoaiPhongDAO lpDao = new LoaiPhongDAO(conn);
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO(conn);

            // 1) Kiểm tra: có booking tương lai hay có phòng đang "SỬ DỤNG" không
            boolean hasFuture;
            try {
                hasFuture = ctDao.hasFutureBookingsForLoaiPhong(maLoaiPhong);
            } catch (Exception e) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi kiểm tra đặt phòng tương lai: " + e.getMessage(), e);
            }

            boolean hasUsing;
            try {
                hasUsing = cvDao.existsUsingRoomByLoaiPhong(maLoaiPhong);
            } catch (Exception e) {
                // nếu kiểm tra lỗi
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi kiểm tra trạng thái sử dụng phòng: " + e.getMessage(), e);
            }

            if (hasFuture || hasUsing) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new BusinessException("Loại phòng đang được sử dụng hoặc có đặt phòng trong tương lai, không thể xóa.");
            }

            // 2) xóa nội thất
            int mappingsDeleted = 0;
            try {
                mappingsDeleted = ntlpDao.softDeleteByLoaiPhong(maLoaiPhong);
            } catch (Exception e) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi xóa mapping nội thất: " + e.getMessage(), e);
            }

            // 3) xóa tất cả giá phòng liên quan đến loại phòng
            int pricesDeleted = 0;
            try {
                pricesDeleted = gpDao.softDeleteByLoaiPhong(maLoaiPhong);
            } catch (Exception e) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi xóa giá phòng liên quan: " + e.getMessage(), e);
            }

            // 4) Xóa loại phòng
            boolean deleted;
            try {
                deleted = lpDao.xoaLoaiPhong(maLoaiPhong);
            } catch (Exception e) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi xóa loại phòng: " + e.getMessage(), e);
            }

            if (!deleted) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                return false;
            }

            // 5) Ghi lịch sử thao tác
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
                wh.setTenThaoTac(ActionType.DELETE_ROOM_CATEGORY.getActionName());

                String moTa = String.format("Xóa loại phòng %s; Kết quả xóa loại phòng: %s; Xóa mapping nội thất (số bản ghi): %d; Xóa giá phòng (số bản ghi): %d",
                        maLoaiPhong, deleted ? "OK" : "FAIL", mappingsDeleted, pricesDeleted);
                wh.setMoTa(moTa);

                wh.setMaPhienDangNhap(Main.getCurrentLoginSession());
                wh.setThoiGianTao(new Timestamp(System.currentTimeMillis()));

                try {
                    lichSuDao.themLichSuThaoTac(wh);
                } catch (Exception e) {
                    System.err.println("Không thể ghi lịch sử thao tác (không rollback): " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi chuẩn bị ghi lịch sử thao tác (không rollback): " + e.getMessage());
            }

            // 6) Commit
            tx.thucHienGiaoTac();
            return true;

        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
            throw new RuntimeException("Lỗi khi xóa loại phòng: " + ex.getMessage(), ex);
        }
    }



    @Override
    public boolean updateRoomCategoryWithAudit(LoaiPhong loaiPhong, List<NoiThatAssignment> itemsWithQty) {
        if (loaiPhong == null || loaiPhong.getMaLoaiPhong() == null || loaiPhong.getMaLoaiPhong().isBlank()) {
            throw new IllegalArgumentException("loaiPhong null hoặc không có mã");
        }

        DatPhongDAO tx = new DatPhongDAO();
        try {
            tx.khoiTaoGiaoTac();
            Connection conn = tx.getConnection();

            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO(conn);
            LoaiPhongDAO lpDao = new LoaiPhongDAO(conn);
            NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO(conn);
            GiaPhongDAO gpDao = new GiaPhongDAO(conn);
            LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO(conn);

            // 1) Kiểm tra: có booking hiện tại/tương lai cho loại phòng không
            boolean hasBooking;
            try {
                hasBooking = ctDao.hasCurrentOrFutureBookingsForLoaiPhong(loaiPhong.getMaLoaiPhong());
            } catch (Exception e) {
                // nếu kiểm tra lỗi
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi kiểm tra booking hiện tại/tương lai: " + e.getMessage(), e);
            }

            if (hasBooking) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                return false;
            }

            // 2) Cập nhật LoaiPhong
            LoaiPhong updated;
            try {
                updated = lpDao.capNhatLoaiPhong(loaiPhong);
                if (updated == null) {
                    tx.hoanTacGiaoTac();
                    return false;
                }
            } catch (Exception e) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi cập nhật loại phòng: " + e.getMessage(), e);
            }

            // 3) Cập nhật mapping nội thất
            boolean replaced;
            try {
                replaced = ntlpDao.replaceMappingsWithQuantities(loaiPhong.getMaLoaiPhong(),
                        itemsWithQty == null ? new java.util.ArrayList<>() : itemsWithQty);
                if (!replaced) {
                    tx.hoanTacGiaoTac();
                    return false;
                }
            } catch (Exception e) {
                try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
                throw new RuntimeException("Lỗi khi cập nhật mapping nội thất: " + e.getMessage(), e);
            }

            Double giaNgayMoi = null;
            Double giaGioMoi = null;
            try {
                try {
                    var m1 = loaiPhong.getClass().getMethod("getGiaNgayMoi");
                    Object v1 = m1.invoke(loaiPhong);
                    if (v1 instanceof Number) giaNgayMoi = ((Number) v1).doubleValue();
                } catch (NoSuchMethodException ignored) {}
                try {
                    var m2 = loaiPhong.getClass().getMethod("getGiaGioMoi");
                    Object v2 = m2.invoke(loaiPhong);
                    if (v2 instanceof Number) giaGioMoi = ((Number) v2).doubleValue();
                } catch (NoSuchMethodException ignored) {}
            } catch (Exception e) {
                System.err.println("Không thể đọc giá mới từ LoaiPhong (reflection): " + e.getMessage());
            }

            if (giaNgayMoi != null && giaGioMoi != null) {
                try {
                    // Tạo ID mới cho GiaPhong
                    GiaPhong latestGia = gpDao.timGiaPhongMoiNhat();
                    String lastGiaId = (latestGia != null) ? latestGia.getMaGiaPhong() : null;
                    String newGiaId = EntityUtil.increaseEntityID(
                            lastGiaId,
                            EntityIDSymbol.ROOM_PRICE.getPrefix(),
                            EntityIDSymbol.ROOM_PRICE.getLength()
                    );

                    GiaPhong gp = new GiaPhong();
                    gp.setMaGiaPhong(newGiaId);
                    gp.setMaLoaiPhong(loaiPhong.getMaLoaiPhong());
                    gp.setGiaNgayCu(0.0);
                    gp.setGiaGioCu(0.0);
                    gp.setGiaNgayMoi(giaNgayMoi);
                    gp.setGiaGioMoi(giaGioMoi);
                    gp.setMaPhienDangNhap(Main.getCurrentLoginSession());
                    gp.setThoiGianTao(new Timestamp(System.currentTimeMillis()));

                    boolean priceInserted = gpDao.insertGiaPhong(gp);
                    if (!priceInserted) {
                        // nếu chèn giá thất bại
                        System.err.println("Cảnh báo: không thể chèn giá mới cho loại phòng " + loaiPhong.getMaLoaiPhong());
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi khi chèn giá mới (không rollback): " + e.getMessage());
                }
            }

            // 5) Ghi lịch sử thao tác
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
                wh.setTenThaoTac(ActionType.EDIT_ROOM_CATEGORY.getActionName());
                String moTa = String.format("Cập nhật loại phòng %s; Tên=%s; SốNg=%d; Phân loại=%s; Nội thất count=%d",
                        loaiPhong.getMaLoaiPhong(),
                        loaiPhong.getTenLoaiPhong() != null ? loaiPhong.getTenLoaiPhong() : "",
                        loaiPhong.getSoLuongKhach(),
                        loaiPhong.getPhanLoai(),
                        itemsWithQty == null ? 0 : itemsWithQty.size()
                );
                wh.setMoTa(moTa);
                wh.setMaPhienDangNhap(Main.getCurrentLoginSession());
                wh.setThoiGianTao(new Timestamp(System.currentTimeMillis()));

                try {
                    lichSuDao.themLichSuThaoTac(wh);
                } catch (Exception e) {
                    System.err.println("Không thể ghi lịch sử thao tác (không rollback): " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi chuẩn bị ghi lịch sử thao tác (không rollback): " + e.getMessage());
            }

            // 6) Commit
            tx.thucHienGiaoTac();
            return true;

        } catch (Exception ex) {
            try { tx.hoanTacGiaoTac(); } catch (Exception ignored) {}
            throw new RuntimeException("Lỗi khi cập nhật loại phòng: " + ex.getMessage(), ex);
        }
    }


    @Override
    public LoaiPhong getRoomCategoryByIDV2(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return loaiPhongDao.getRoomCategoryByID(id);
        } catch (Exception ex) {
            System.out.println("Lỗi khi lấy LoaiPhong by id: " + ex.getMessage());
            return null;
        }
    }

    @Override
    public boolean isRoomCategoryInUse(String maLoaiPhong) {
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) return false;

        try {
            // 1) kiểm tra có booking hiện tại/tương lai (ChiTietDatPhongDAO)
            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO();
            boolean hasFutureOrCurrent = ctDao.hasCurrentOrFutureBookingsForLoaiPhong(maLoaiPhong);
            if (hasFutureOrCurrent) return true;

            // 2) kiểm tra có phòng đang "SỬ DỤNG" (CongViecDAO)
            CongViecDAO cvDao = new CongViecDAO();
            boolean hasUsing = cvDao.existsUsingRoomByLoaiPhong(maLoaiPhong);
            return hasUsing;
        } catch (Exception ex) {
            System.err.println("Lỗi kiểm tra isRoomCategoryInUse: " + ex.getMessage());
            return true;
        }
    }

}

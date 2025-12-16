package vn.iuh.service.impl;

import com.github.lgooddatepicker.zinternaltools.Pair;
import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.*;
import vn.iuh.dto.repository.NoiThatAssignment;
import vn.iuh.dto.response.RoomCategoryPriceHistory;
import vn.iuh.dto.response.RoomCategoryResponse;
import vn.iuh.entity.GiaPhong;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.exception.BusinessException;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.panel.QuanLyLoaiPhongPanel;
import vn.iuh.gui.panel.QuanLyPhongPanel;
import vn.iuh.gui.panel.statistic.FillterRoomStatistic;
import vn.iuh.gui.panel.statistic.RoomStatistic;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class LoaiPhongServiceImpl implements LoaiPhongService {
    private final LoaiPhongDAO loaiPhongDao;
    private final GiaPhongDAO giaPhongDAO;
    private volatile List<RoomCategoryResponse> cachedRoomCategories = new ArrayList<>();


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

        try {
            DatabaseUtil.khoiTaoGiaoTac();

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
                DatabaseUtil.hoanTacGiaoTac();
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

            boolean priceInserted = gpDao.insertGiaPhongV2(gp);
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

            // trả về LoaiPhong vừa tạo
            return lpDao.getRoomCategoryByID(maLoai);
        } catch (BusinessException be) {
            DatabaseUtil.hoanTacGiaoTac();
            throw be;
        } catch (Exception ex) {
            DatabaseUtil.hoanTacGiaoTac();
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

        // Sử dụng DAO phù hợp
        PhongDAO phongDao = new PhongDAO();
        NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO();
        LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();

        // 1) kiểm tra: nếu còn PHÒNG thuộc loại này (chưa bị xóa) -> không cho xóa
        boolean hasRooms = false;
        try {
            hasRooms = phongDao.existsRoomForLoaiPhong(maLoaiPhong);
        } catch (Exception e) {
            // nếu lỗi khi kiểm tra DB thì ném lên để caller biết
            throw new RuntimeException("Lỗi khi kiểm tra phòng thuộc loại phòng: " + e.getMessage(), e);
        }
        if (hasRooms) {
            throw new BusinessException("Loại phòng đang có phòng thuộc loại này, không thể xóa.");
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
            wh.setMaPhienDangNhap(maPhienDangNhap);

            lichSuDao.themLichSuThaoTac(wh);
        } catch (Exception e) {
            System.err.println("Không thể ghi lịch sử thao tác: " + e.getMessage());
        }

        return deleted;
    }

    @Override
    public boolean updateRoomCategoryWithAudit(LoaiPhong loaiPhong, List<NoiThatAssignment> itemsWithQty) {
        // gọi method mới với giá = null -> không thay đổi giá
        return updateRoomCategoryWithAudit(loaiPhong, itemsWithQty, null, null);
    }

    public boolean updateRoomCategoryWithAudit(LoaiPhong loaiPhong,
                                               List<NoiThatAssignment> itemsWithQty,
                                               Double giaGioVal,
                                               Double giaNgayVal) {
        if (loaiPhong == null || loaiPhong.getMaLoaiPhong() == null) throw new IllegalArgumentException("loaiPhong null/không có mã");

        try {
            DatabaseUtil.khoiTaoGiaoTac();

            String maLoai = loaiPhong.getMaLoaiPhong();
            String maPhienDangNhap = Main.getCurrentLoginSession();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // DAO dùng cùng connection
            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO();
            // 1) kiểm tra nếu có booking hiện/tương lai -> không cho cập nhật
            boolean hasBooking = ctDao.hasCurrentOrFutureBookingsForLoaiPhong(maLoai);
            if (hasBooking) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 2) cập nhật LoaiPhong
            LoaiPhongDAO lpDao = new LoaiPhongDAO();
            LoaiPhong updated = lpDao.capNhatLoaiPhong(loaiPhong);
            if (updated == null) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 3) cập nhật mapping nội thất
            NoiThatTrongLoaiPhongDAO ntlpDao = new NoiThatTrongLoaiPhongDAO();
            boolean replaced = ntlpDao.replaceMappingsWithQuantities(maLoai,
                    itemsWithQty == null ? new java.util.ArrayList<>() : itemsWithQty);
            if (!replaced) {
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 4) nếu có giá mới (cả 2 đều phải khác null) -> tạo bản ghi GiaPhong mới
            if (giaGioVal != null && giaNgayVal != null) {
                GiaPhongDAO gpDao = new GiaPhongDAO();

                // Lấy latest giá hiện tại cho loại phòng (nếu có)
                Double oldGiaGio = 0.0;
                Double oldGiaNgay = 0.0;
                List<vn.iuh.entity.GiaPhong> history = gpDao.findByLoaiPhong(maLoai);
                vn.iuh.entity.GiaPhong latestGia = (history != null && !history.isEmpty()) ? history.get(0) : null;
                if (latestGia != null) {
                    try { oldGiaGio = latestGia.getGioGioMoi(); } catch (Exception ignored) { oldGiaGio = 0.0; }
                    try { oldGiaNgay = latestGia.getGioNgayMoi(); } catch (Exception ignored) { oldGiaNgay = 0.0; }
                }

                // tạo id mới cho GiaPhong
                GiaPhong latestGlobal = gpDao.timGiaPhongMoiNhat(); // global latest id
                String lastGiaId = latestGlobal != null ? latestGlobal.getMaGiaPhong() : null;
                String newGiaId = EntityUtil.increaseEntityID(lastGiaId, EntityIDSymbol.ROOM_PRICE.getPrefix(), EntityIDSymbol.ROOM_PRICE.getLength());

                // build GiaPhong object
                GiaPhong newGia = new GiaPhong();
                newGia.setMaGiaPhong(newGiaId);
                newGia.setMaLoaiPhong(maLoai);
                // lưu giá cũ là giá hiện tại (nếu có)
                newGia.setGiaGioCu(oldGiaGio);
                newGia.setGiaNgayCu(oldGiaNgay);
                // đặt giá mới
                newGia.setGioGioMoi(giaGioVal);
                newGia.setGioNgayMoi(giaNgayVal);
                newGia.setMaPhienDangNhap(maPhienDangNhap);
                newGia.setThoiGianTao(now);

                boolean priceInserted = gpDao.insertGiaPhongV2(newGia);
                if (!priceInserted) {
                    DatabaseUtil.hoanTacGiaoTac();
                    throw new RuntimeException("Không thể thêm giá cho loại phòng");
                }
            }

            // 5) ghi lịch sử thao tác
            try {
                LichSuThaoTacDAO lichSuDao = new LichSuThaoTacDAO();
                var lastHistory = lichSuDao.timLichSuThaoTacMoiNhat();
                String lastHistoryId = (lastHistory != null) ? lastHistory.getMaLichSuThaoTac() : null;
                String newHistoryId = EntityUtil.increaseEntityID(lastHistoryId, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

                LichSuThaoTac wh = new LichSuThaoTac();
                wh.setMaLichSuThaoTac(newHistoryId);
                wh.setTenThaoTac(ActionType.UPDATE_ROOM_CATEGORY.getActionName()); // bạn có thể sửa tên nếu muốn
                String detail = String.format("Cập nhật loại phòng %s; nội thất=%d; %s", maLoai,
                        itemsWithQty == null ? 0 : itemsWithQty.size(),
                        (giaGioVal != null && giaNgayVal != null) ? String.format("Giá thay đổi: giờ=%.0f, ngày=%.0f", giaGioVal, giaNgayVal) : "Không thay đổi giá");
                wh.setMoTa(detail);
                wh.setMaPhienDangNhap(maPhienDangNhap);
                wh.setThoiGianTao(now);
                lichSuDao.themLichSuThaoTac(wh);
            } catch (Exception e) {
                // không rollback chỉ vì lỗi ghi lịch sử
                System.err.println("Không thể ghi lich su thao tac: " + e.getMessage());
            }

            // commit
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

    @Override
    public Map<String, Double> getLatestPriceMap(String maLoaiPhong) {
        Map<String, Double> result = new HashMap<>();
        result.put("gia_gio", 0.0);
        result.put("gia_ngay", 0.0);
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) return result;

        try {
            Map<String, Double> listPrice = loaiPhongDao.layGiaLoaiPhongTheoIdV2(maLoaiPhong);
            if (listPrice != null && !listPrice.isEmpty()) {
                Double gNgay = listPrice.get("gia_ngay");
                Double gGio  = listPrice.get("gia_gio");
                result.put("gia_gio", gGio == null ? 0.0 : gGio);
                result.put("gia_ngay", gNgay == null ? 0.0 : gNgay);
                return result;
            }

            // fallback: tìm từ bảng GiaPhong (mới nhất)
            GiaPhongDAO gpDao = new GiaPhongDAO();
            List<GiaPhong> list = gpDao.findByLoaiPhong(maLoaiPhong);
            if (list != null && !list.isEmpty()) {
                GiaPhong newest = list.get(0);
                result.put("gia_gio", newest.getGioGioMoi());
                result.put("gia_ngay", newest.getGioNgayMoi());
            }
        } catch (Throwable ex) {
            // log nếu cần
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public List<RoomCategoryPriceHistory> getPriceHistoryWithActor(String maLoaiPhong) {
        List<RoomCategoryPriceHistory> ret = new ArrayList<>();
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) return ret;

        try {
            GiaPhongDAO gpDao = new GiaPhongDAO();
            List<GiaPhong> list = gpDao.findByLoaiPhong(maLoaiPhong);
            if (list == null || list.isEmpty()) return ret;

            NhanVienDAO nvDao = new NhanVienDAO();
            for (GiaPhong g : list) {
                String actor = "";
                try {
                    String phien = g.getMaPhienDangNhap();
                    if (phien != null && !phien.isBlank()) {
                        actor = nvDao.findTenNhanVienByPhienDangNhap(phien);
                    }
                } catch (Throwable ignore) { actor = ""; }

                RoomCategoryPriceHistory dto = new RoomCategoryPriceHistory(
                        g.getThoiGianTao(),
                        g.getGiaGioCu(),
                        g.getGiaNgayCu(),
                        g.getGioGioMoi(),
                        g.getGioNgayMoi(),
                        actor == null ? "" : actor
                );
                ret.add(dto);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public boolean hasCurrentOrFutureBookingsForLoaiPhong(String maLoaiPhong) {
        if (maLoaiPhong == null || maLoaiPhong.isBlank()) return false;
        try {
            ChiTietDatPhongDAO ctDao = new ChiTietDatPhongDAO();
            return ctDao.hasCurrentOrFutureBookingsForLoaiPhong(maLoaiPhong);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi kiểm tra booking cho loại phòng " + maLoaiPhong + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<LoaiPhong> layTatCaLoaiPhongHienCo() {
        return loaiPhongDao.layTatCaLoaiPhong();
    }

}

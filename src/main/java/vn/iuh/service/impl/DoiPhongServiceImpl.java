package vn.iuh.service.impl;

import vn.iuh.constraint.*;
import vn.iuh.dao.*;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.*;
import vn.iuh.gui.base.Main;
import vn.iuh.service.DoiPhongService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;
import vn.iuh.util.FeeValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DoiPhongServiceImpl implements DoiPhongService {

    private final PhongDAO phongDAO;
    private final LoaiPhongDAO loaiPhongDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;

    private volatile String lastError;

    public DoiPhongServiceImpl() {
        this.phongDAO = new PhongDAO();
        this.loaiPhongDAO = new LoaiPhongDAO();
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
    }

    public String getLastError() {
        return lastError;
    }

    // Hàm để tìm các phòng phù hợp cho đổi phòng
    public List<BookingResponse> timPhongPhuHopChoDoiPhong(String currentRoomId, int neededPersons, Timestamp fromTime, Timestamp toTime) {
        List<BookingResponse> results = new ArrayList<>();

        // 1) Lấy ra list Phong phù hợp tìm được từ DAO
        List<Phong> candidatePhongs = phongDAO.timPhongUngVien(currentRoomId, neededPersons, fromTime, toTime);

        if (candidatePhongs == null || candidatePhongs.isEmpty()) {
            return results;
        }

        // 2) Map từng Phong -> BookingResponse
        for (Phong p : candidatePhongs) {
            double[] price = phongDAO.getLatestPriceForLoaiPhong(p.getMaLoaiPhong());
            double giaNgay = price != null && price.length > 0 ? price[0] : 0.0;
            double giaGio = price != null && price.length > 1 ? price[1] : 0.0;

            // Lấy tên loại phòng
            String tenLoai = null;
            try {
                LoaiPhong lp = loaiPhongDAO.getRoomCategoryByID(p.getMaLoaiPhong());
                if (lp != null) tenLoai = lp.getTenLoaiPhong();
            } catch (Exception ignored) {
                // nếu lỗi thì fallback về mã loại
            }

            BookingResponse br = new BookingResponse(
                    p.getMaPhong(),
                    p.getTenPhong(),
                    p.isDangHoatDong(),
                    RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
                    tenLoai != null ? tenLoai : p.getMaLoaiPhong(),
                    String.valueOf(neededPersons),
                    giaNgay,
                    giaGio
            );

            results.add(br);
        }

        return results;
    }

    // Hàm hiện thực đổi phòng
    public boolean changeRoom(String reservationId, String oldRoomId, String newRoomId, boolean applyFee) {
        this.lastError = null;

        DatPhongDAO datPhongDAO = new DatPhongDAO();
        try {
            // 1) Khởi tạo transaction
            DatabaseUtil.khoiTaoGiaoTac();

            // 2) DAO dùng chung connection (đảm bảo transaction chạy đúng cách)
            ChiTietDatPhongDAO chiTietDao = new ChiTietDatPhongDAO();
            LichSuDiVaoDAO lichSuDiVaoDAO = new LichSuDiVaoDAO();
            LichSuRaNgoaiDAO lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO();
            CongViecDAO congViecDAO = new CongViecDAO();
            LichSuThaoTacDAO lichSuThaoTacDAO = new LichSuThaoTacDAO();
            PhongTinhPhuPhiDAO phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
            PhuPhiDAO phuPhiDAO = new PhuPhiDAO();

            Timestamp now = new Timestamp(System.currentTimeMillis());

            // 3) Tìm ChiTietDatPhong thuộc mã đơn đặt phòng và mã phòng
            List<ChiTietDatPhong> details = chiTietDao.findByBookingId(reservationId);
            ChiTietDatPhong target = null;
            if (details != null) {
                for (ChiTietDatPhong c : details) {
                    if (c.getMaPhong() != null && c.getMaPhong().equalsIgnoreCase(oldRoomId)) {
                        target = c;
                        break;
                    }
                }
            }
            // fallback: lấy latest theo phòng nếu không tìm thấy trong danh sách
            if (target == null) {
                target = chiTietDao.findLastestByRoom(oldRoomId);
            }

            if (target == null) {
                lastError = "Không tìm thấy chi tiết đặt phòng phù hợp để đổi (room=" + oldRoomId + ", booking=" + reservationId + ")";
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            String maChiTiet = target.getMaChiTietDatPhong();
            Timestamp originalCheckout = target.getTgTraPhong();

            // 4) Kiểm tra đã checkin chưa (dựa vào LichSuDiVao)
            boolean alreadyCheckedIn = lichSuDiVaoDAO.daTonTai(maChiTiet);

            // Đổi phòng chưa checkin
            if (!alreadyCheckedIn) {
                // 5a) cập nhật mã phòng trên chi tiết đặt phòng
                boolean updated = chiTietDao.capNhatMaPhongChoChiTiet(maChiTiet, newRoomId, now);
                if (!updated) {
                    lastError = "Không thể cập nhật mã phòng cho ChiTietDatPhong.";
                    DatabaseUtil.hoanTacGiaoTac();
                    return false;
                }

                // 5b) áp phụ phí (nếu nhân viên lựa chọn)
                if (applyFee) {
                    // Tìm thông tin phụ phi
                    var thongTin = FeeValue.getInstance().get(Fee.DOI_PHONG);
                    String maPhuPhi = (thongTin == null) ? null : thongTin.getMaPhuPhi();

                    // Lấy giá hiện tại của phụ phí
                    BigDecimal amount = thongTin.getGiaHienTai();

                    var latestPtpp = phongTinhPhuPhiDAO.getLatest();
                    String lastPtppId = (latestPtpp == null) ? null : latestPtpp.getMaPhongTinhPhuPhi();
                    String newPtppId = EntityUtil.increaseEntityID(lastPtppId,
                            EntityIDSymbol.ROOM_FEE.getPrefix(),
                            EntityIDSymbol.ROOM_FEE.getLength());

                    PhongTinhPhuPhi ptpp = new PhongTinhPhuPhi();
                    ptpp.setMaPhongTinhPhuPhi(newPtppId);
                    ptpp.setMaChiTietDatPhong(maChiTiet);
                    ptpp.setMaPhuPhi(maPhuPhi);
                    ptpp.setDonGiaPhuPhi(amount);

                    boolean insertedFee = phongTinhPhuPhiDAO.insert(ptpp);
                    if (!insertedFee) {
                        lastError = "Không thể thêm bản ghi PhongTinhPhuPhi.";
                        DatabaseUtil.hoanTacGiaoTac();
                        return false;
                    }
                }

                // 5c) Cập nhật mã phòng cho công việc hiện tại
                // Lấy công việc hiện tại phù hợp để cập nhật
                CongViec oldJob = congViecDAO.layCongViecHienTaiCuaPhongChoCheckin(oldRoomId);
                if (oldJob != null) {
                    boolean jobUpdated = congViecDAO.capNhatMaPhongChoCongViec(oldJob.getMaCongViec(), newRoomId, now);
                    if (!jobUpdated) {
                        lastError = "Không thể cập nhật công việc hiện tại sang phòng mới.";
                        DatabaseUtil.hoanTacGiaoTac();
                        return false;
                    }
                }
                // 5d) Ghi lịch sử thao tác
                var lastWh = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
                String lastWhId = (lastWh == null) ? null : lastWh.getMaLichSuThaoTac();
                String newWhId = EntityUtil.increaseEntityID(lastWhId, EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

                String tenThaoTac = ActionType.CHANGE_ROOM_BEFORE_CHECKIN.getActionName();
                String moTa = String.format("Đổi phòng cho đơn %s: %s -> %s", reservationId, oldRoomId, newRoomId);
                String maPhien = Main.getCurrentLoginSession();
                LichSuThaoTac wh = new LichSuThaoTac(newWhId, tenThaoTac, moTa, maPhien, now);
                lichSuThaoTacDAO.themLichSuThaoTac(wh);

                // Commit transaction
                DatabaseUtil.thucHienGiaoTac();
                lastError = null;
                return true;
            }

            // POST-CHECKIN flow (khách đã checkin)
            // 6a) Thêm LichSuRaNgoai cho phòng cũ (khách rời phòng cũ)
            var lastLsRa = lichSuRaNgoaiDAO.timLichSuRaNgoaiMoiNhat();
            String lastLsRaId = (lastLsRa == null) ? null : lastLsRa.getMaLichSuRaNgoai();
            String newLsRaId = EntityUtil.increaseEntityID(lastLsRaId, EntityIDSymbol.HISTORY_CHECKOUT_PREFIX.getPrefix(), EntityIDSymbol.HISTORY_CHECKOUT_PREFIX.getLength());
            LichSuRaNgoai lsRa = new LichSuRaNgoai(newLsRaId, true, maChiTiet, now);
            lichSuRaNgoaiDAO.themLichSuRaNgoai(lsRa);

            // 6b) Kết thúc ChiTietDatPhong cũ (tg_tra_phong = now, kieu_ket_thuc = "TRẢ PHÒNG")
            String kieuKetThuc = RoomEndType.DOI_PHONG.getStatus();
            boolean ktEnd = chiTietDao.ketThucChiTietDatPhong(maChiTiet, now, kieuKetThuc);
            if (!ktEnd) {
                lastError = "Không thể kết thúc chi tiết đặt phòng cũ.";
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 6c) Tạo ChiTietDatPhong mới cho phòng mới
            ChiTietDatPhong lastCt = chiTietDao.timChiTietDatPhongMoiNhat();
            String lastCtId = (lastCt == null) ? null : lastCt.getMaChiTietDatPhong();
            String newCtId = EntityUtil.increaseEntityID(lastCtId, EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getPrefix(), EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getLength());

            ChiTietDatPhong newCt = new ChiTietDatPhong(newCtId, now, originalCheckout, null, reservationId, newRoomId, Main.getCurrentLoginSession(), now);
            boolean inserted = chiTietDao.insert(newCt);
            if (!inserted) {
                lastError = "Không thể tạo ChiTietDatPhong mới cho phòng được đổi.";
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // 6d) Thêm LichSuDiVao cho phòng mới (khách vào phòng mới)
            var lastLsDi = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
            String lastLsDiId = (lastLsDi == null) ? null : lastLsDi.getMaLichSuDiVao();
            String newLsDiId = EntityUtil.increaseEntityID(lastLsDiId, EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix(), EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength());
            LichSuDiVao lsDi = new LichSuDiVao(newLsDiId, true, newCtId, now);
            lichSuDiVaoDAO.themLichSuDiVao(lsDi);

            // 6e) Áp phụ phí cho chi tiết mới nếu yêu cầu
            if (applyFee) {
                String phuPhiName = Fee.DOI_PHONG.getStatus();
                var thongTin = phuPhiDAO.getThongTinPhuPhiByName(phuPhiName);
                String maPhuPhi = (thongTin == null) ? null : thongTin.getMaPhuPhi();
                BigDecimal amount = BigDecimal.valueOf(100000L);

                var latestPtpp = phongTinhPhuPhiDAO.getLatest();
                String lastPtppId2 = (latestPtpp == null) ? null : latestPtpp.getMaPhongTinhPhuPhi();
                String newPtppId2 = EntityUtil.increaseEntityID(lastPtppId2, EntityIDSymbol.ROOM_FEE.getPrefix(), EntityIDSymbol.ROOM_FEE.getLength());

                PhongTinhPhuPhi ptppNew = new PhongTinhPhuPhi();
                ptppNew.setMaPhongTinhPhuPhi(newPtppId2);
                ptppNew.setMaChiTietDatPhong(newCtId);
                ptppNew.setMaPhuPhi(maPhuPhi);
                ptppNew.setDonGiaPhuPhi(amount);

                boolean feeInserted = phongTinhPhuPhiDAO.insert(ptppNew);
                if (!feeInserted) {
                    lastError = "Không thể thêm phụ phí cho chi tiết mới.";
                    DatabaseUtil.hoanTacGiaoTac();
                    return false;
                }
            }

            // 6f) Xử lý job của phòng cũ: kết thúc job hiện tại và tùy trạng thái tạo job mới phù hợp
            CongViec oldJob2 = congViecDAO.layCongViecHienTaiCuaPhong(oldRoomId);
            if (oldJob2 != null) {
                // Kết thúc job hiện tại (đánh dấu da_xoa = true và tg_ket_thuc = now)
                congViecDAO.capNhatThoiGianKetThuc(oldJob2.getMaCongViec(), now, true);

                String currentStatus = oldJob2.getTenTrangThai() == null ? "" : oldJob2.getTenTrangThai().trim();

                // Lấy id job mới dựa trên job mới nhất
                CongViec lastJobForId = congViecDAO.timCongViecMoiNhat();
                String newOldJobId = EntityUtil.increaseEntityID(
                        lastJobForId == null ? null : lastJobForId.getMaCongViec(),
                        EntityIDSymbol.JOB_PREFIX.getPrefix(),
                        EntityIDSymbol.JOB_PREFIX.getLength()
                );

                // Nếu trạng thái cũ là "SỬ DỤNG" thì đưa về là dọn dẹp
                if (RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(currentStatus)) {
                    // Tạo côgn việc dọn dẹp cho phòng cũ
                    Timestamp cleaningEnd = new Timestamp(now.getTime() + 2L * 60L * 60L * 1000L);
                    CongViec cleaningJob = new CongViec(
                            newOldJobId,
                            RoomStatus.ROOM_CLEANING_STATUS.getStatus(),
                            now,
                            cleaningEnd,
                            oldRoomId,
                            now
                    );
                    congViecDAO.themCongViec(cleaningJob);
                }
            }


            // 6g) TẠO công việc mới cho phòng mới với trạng thái "KIỂM TRA" từ hiện tại đến 30p sau
            CongViec lastJob3 = congViecDAO.timCongViecMoiNhat();
            String newJobIdNew = EntityUtil.increaseEntityID(lastJob3 == null ? null : lastJob3.getMaCongViec(),
                    EntityIDSymbol.JOB_PREFIX.getPrefix(), EntityIDSymbol.JOB_PREFIX.getLength());
            Timestamp checkingStart = now;
            Timestamp checkingEnd = new Timestamp(now.getTime() + 30L * 60L * 1000L); // +30 min
            CongViec checkingJob = new CongViec(newJobIdNew, RoomStatus.ROOM_CHECKING_STATUS.getStatus(), checkingStart, checkingEnd, newRoomId, checkingStart);
            congViecDAO.themCongViec(checkingJob);

            // 6h) Ghi LichSuThaoTac (sau checkin)
            var lastWh2 = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String newWhId2 = EntityUtil.increaseEntityID(lastWh2 == null ? null : lastWh2.getMaLichSuThaoTac(),
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(), EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());
            String tenThaoTac2 = ActionType.CHANGE_ROOM_AFTER_CHECKIN.getActionName();
            String moTa2 = String.format("Đổi phòng cho đơn %s: %s -> %s (sau checkin)", reservationId, oldRoomId, newRoomId);
            LichSuThaoTac wh2 = new LichSuThaoTac(newWhId2, tenThaoTac2, moTa2, Main.getCurrentLoginSession(), now);
            lichSuThaoTacDAO.themLichSuThaoTac(wh2);

            // Commit transaction
            DatabaseUtil.thucHienGiaoTac();
            lastError = null;
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                DatabaseUtil.hoanTacGiaoTac();
            } catch (Exception e) {
                // hehe
            }
            lastError = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            return false;
        }
    }

    public String layMaDonDatPhong(String maChiTietDatPhong) {
        String maDonDatPhong = chiTietDatPhongDAO.findFormIDByDetail(maChiTietDatPhong);
        return maDonDatPhong;
    }

    public int timSoNguoiCan(String roomId) {
        int neededPersons = 1;
        try {
            PhongDAO phongDAO = new PhongDAO();
            Phong phong = phongDAO.timPhong(roomId);
            if (phong != null && phong.getMaLoaiPhong() != null) {
                LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
                LoaiPhong lp = loaiPhongDAO.getRoomCategoryByID(phong.getMaLoaiPhong());
                if (lp != null) {
                    neededPersons = lp.getSoLuongKhach();
                }
            }
        } catch (Exception ex) {
            System.out.println("Lỗi khi lấy số lượng khách: " + ex.getMessage());
        }
        return neededPersons;
    }

    public String timTenLoaiPhong(String roomId) {
        String roomType = "";
        try {
            PhongDAO phongDAO = new PhongDAO();
            Phong phong = phongDAO.timPhong(roomId);
            if (phong != null && phong.getMaLoaiPhong() != null) {
                LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
                LoaiPhong lp = loaiPhongDAO.getRoomCategoryByID(phong.getMaLoaiPhong());
                if (lp != null) {
                    roomType = lp.getTenLoaiPhong() != null ? lp.getTenLoaiPhong() : "";
                }
            }
        } catch (Exception ex) {
            System.out.println("Lỗi khi lấy loại phòng: " + ex.getMessage());
        }
        return roomType;
    }
}

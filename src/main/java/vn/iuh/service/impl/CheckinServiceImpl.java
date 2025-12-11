package vn.iuh.service.impl;

import vn.iuh.constraint.*;
import vn.iuh.dao.*;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.entity.*;
import vn.iuh.gui.base.Main;
import vn.iuh.service.CheckinService;
import vn.iuh.util.DatabaseUtil;
import vn.iuh.util.EntityUtil;
import vn.iuh.util.FeeValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckinServiceImpl implements CheckinService {

    private final CongViecDAO congViecDAO;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
    private final LichSuDiVaoDAO lichSuDiVaoDAO;
    private final PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
    private final PhuPhiDAO phuPhiDAO;

    private volatile String thongBaoLoi;

    public CheckinServiceImpl() {
        this.congViecDAO = new CongViecDAO();
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
        this.lichSuDiVaoDAO = new LichSuDiVaoDAO();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
        this.phuPhiDAO = new PhuPhiDAO();
    }

    @Override
    public boolean checkin(String maDonDatPhong,
                           String tenPhong) {

        String maPhienDangNhap = Main.getCurrentLoginSession();

        DatPhongDAO datPhongDAO = new DatPhongDAO();
        try {
            // 1) Bắt đầu transaction
            DatabaseUtil.khoiTaoGiaoTac();

            // 2) Khởi tạo DAO dùng chung connection (để rollback khi cần)
            LichSuDiVaoDAO lichSuDiVaoDAO = new LichSuDiVaoDAO();
            CongViecDAO congViecDAO = new CongViecDAO();
            LichSuThaoTacDAO lichSuThaoTacDAO = new LichSuThaoTacDAO();
            ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAO();
            PhongTinhPhuPhiDAO phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
            PhuPhiDAO phuPhiDAO = new PhuPhiDAO();
            PhongDAO phongDAO = new PhongDAO();

            // Xóa khoảng trắng cho các tham số truyền vào
            String maDonDatPhongMain = trimToNull(maDonDatPhong);
            String tenPhongMain = trimToNull(tenPhong);

            // Tìm mã phòng từ tên phòng
            String maPhongMain = resolveRoomIdentifier(tenPhongMain, maDonDatPhongMain, chiTietDatPhongDAO, phongDAO);

            // 3) Lấy danh sách chi tiết đặt phòng của đơn và tìm ChiTietDatPhong phù hợp
            List<ChiTietDatPhong> chiTietList = chiTietDatPhongDAO.findByBookingId(maDonDatPhongMain);
            ChiTietDatPhong chiTietDatPhong = null;

            // 3.1) Nếu đầu vào có danh sách thì tìm chi tiết phù hợp trong danh sách
            if (chiTietList != null && maPhongMain != null) {
                for (ChiTietDatPhong item : (chiTietList == null ? Collections.<ChiTietDatPhong>emptyList() : chiTietList)) {
                    if (Objects.equals(trimToNull(item.getMaPhong()), maPhongMain)) {
                        chiTietDatPhong = item;
                        break;
                    }
                }
            }

            // 3.2) Nếu chưa tìm được, gọi DAO tìm chi tiết gần nhất cho phòng
            if (chiTietDatPhong == null && maPhongMain != null) {
                chiTietDatPhong = chiTietDatPhongDAO.findLastestByRoom(maPhongMain);
            }

            // 4) Báo lỗi không tìm được
            if (chiTietDatPhong == null) {
                throw new IllegalArgumentException("Chi tiết đặt phòng không tồn tại (roomInput=" + tenPhongMain + ", booking=" + maDonDatPhongMain + ")");
            }

            // 5) Kiểm tra chi tiết có thuộc đơn hay không
            if (!Objects.equals(trimToNull(chiTietDatPhong.getMaDonDatPhong()), maDonDatPhongMain)) {
                throw new IllegalArgumentException("Chi tiết đặt phòng không thuộc DonDatPhong được cung cấp.");
            }

            // Lấy mã chi tiết từ chi tiết tìm được
            String maChiTietDatPhongMain = trimToNull(chiTietDatPhong.getMaChiTietDatPhong());
            String maPh = trimToNull(chiTietDatPhong.getMaPhong());
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // 6) Kiểm tra Checkin hay chưa
            boolean alreadyCheckedIn = lichSuDiVaoDAO.daTonTai(maChiTietDatPhongMain);
            if (alreadyCheckedIn) {
                thongBaoLoi = "Đơn này đã được check-in trước đó.";
                DatabaseUtil.hoanTacGiaoTac();
                return false;
            }

            // Trường hợp đặt nhiều và các phòng đang trong trạng thái "CHỜ CHECKIN"
            String loaiDonGlobal = chiTietDatPhongDAO.getLoaiDonDatPhong(maDonDatPhongMain);
            if (loaiDonGlobal != null && loaiDonGlobal.trim().equalsIgnoreCase("ĐẶT NHIỀU")) {
                // Lấy tất cả chi tiết chưa kết thúc trong đơn
                List<ChiTietDatPhong> allNotEnded = chiTietDatPhongDAO.findNotEndedByBookingId(maDonDatPhongMain);
                if (allNotEnded != null && !allNotEnded.isEmpty()) {
                    // Danh sách các chi tiết mà phòng hiện có job active và là CHỜ CHECKIN
                    List<ChiTietDatPhong> toProcess = new ArrayList<>();
                    for (ChiTietDatPhong ct : allNotEnded) {
                        String roomId = trimToNull(ct.getMaPhong());
                        if (roomId == null) continue;
                        CongViec job = congViecDAO.layCongViecHienTaiCuaPhong(roomId);
                        if (job == null) continue;
                        Timestamp jobStart = job.getTgBatDau();
                        Timestamp jobEnd = job.getTgKetThuc();
                        boolean started = jobStart != null && !now.before(jobStart);
                        boolean notFinished = jobEnd == null || jobEnd.after(now);
                        boolean active = started && notFinished;
                        if (!active) continue;
                        RoomStatus rs = RoomStatus.fromString(trimToNull(job.getTenTrangThai()));
                        if (rs == RoomStatus.ROOM_BOOKED_STATUS) {
                            toProcess.add(ct);
                        }
                    }

                    if (!toProcess.isEmpty()) {
                        // 1) Ghi LichSuDiVao cho từng chi tiết (tạo id tăng dần)
                        var lastLichSuDiVao = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
                        String prevCheckinId = (lastLichSuDiVao == null) ? null : trimToNull(lastLichSuDiVao.getMaLichSuDiVao());

                        // 2) Lấy lastJobId để tăng id cho các job mới
                        var lastJob = congViecDAO.timCongViecMoiNhat();
                        String prevJobId = (lastJob == null) ? null : trimToNull(lastJob.getMaCongViec());

                        for (ChiTietDatPhong ct : toProcess) {
                            // Tạo lịch sử đi vào cho từng chi tiết
                            String newCheckinId = EntityUtil.increaseEntityID(prevCheckinId,
                                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix(),
                                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength());
                            prevCheckinId = newCheckinId;
                            LichSuDiVao ls = new LichSuDiVao(newCheckinId, true, ct.getMaChiTietDatPhong(), now);
                            lichSuDiVaoDAO.themLichSuDiVao(ls);

                            // Kết thúc công việc cũ
                            CongViec oldJob = congViecDAO.layCongViecHienTaiCuaPhong(trimToNull(ct.getMaPhong()));
                            if (oldJob != null) {
                                boolean finished = congViecDAO.capNhatThoiGianKetThuc(oldJob.getMaCongViec(), now, true);
                                if (!finished) {
                                    throw new SQLException("Không thể kết thúc job CHỜ CHECKIN hiện tại: " + oldJob.getMaCongViec());
                                }
                            }

                            // Tạo công việc mới cho từng phòng
                            String newJobId = EntityUtil.increaseEntityID(prevJobId,
                                    EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                    EntityIDSymbol.JOB_PREFIX.getLength());
                            prevJobId = newJobId;
                            String tenTrangThaiChecking = RoomStatus.ROOM_CHECKING_STATUS.getStatus();
                            Timestamp tgBatDauChecking = now;
                            Timestamp tgKetThucChecking = new Timestamp(tgBatDauChecking.getTime() + WorkTimeCost.CHECKING_WAITING_TIME.getMinutes() * 60L * 1000L);
                            CongViec checkingJob = new CongViec(newJobId, tenTrangThaiChecking, tgBatDauChecking, tgKetThucChecking, trimToNull(ct.getMaPhong()), tgBatDauChecking);
                            congViecDAO.themCongViec(checkingJob);
                        }

                        // 3) Ghi 1 LichSuThaoTac cho toàn đơn
                        var lastLichSuThaoTac = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
                        String lastLichSuThaoTacId = (lastLichSuThaoTac == null) ? null : trimToNull(lastLichSuThaoTac.getMaLichSuThaoTac());
                        String newWorkingHistoryId = EntityUtil.increaseEntityID(lastLichSuThaoTacId,
                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

                        String tenThaoTac = ActionType.CHECKIN.getActionName();
                        String moTaThaoTac = "Checkin (từ trạng thái CHỜ CHECKIN) cho đơn đặt nhiều " + maDonDatPhongMain;
                        LichSuThaoTac newLichSuThaoTac = new LichSuThaoTac(newWorkingHistoryId, tenThaoTac, moTaThaoTac, maPhienDangNhap, now);
                        lichSuThaoTacDAO.themLichSuThaoTac(newLichSuThaoTac);

                        // Commit và return
                        DatabaseUtil.thucHienGiaoTac();
                        thongBaoLoi = null;
                        return true;
                    }
                }
            }

            // Đơn đặt phòng đơn ở trạng thái chờ checkin
            CongViec congViecHienTai = congViecDAO.layCongViecHienTaiCuaPhong(maPh);
            if (congViecHienTai != null) {
                Timestamp jobStart = congViecHienTai.getTgBatDau();
                Timestamp jobEnd = congViecHienTai.getTgKetThuc();
                boolean started = jobStart != null && !now.before(jobStart);
                boolean notFinished = jobEnd == null || jobEnd.after(now);
                boolean active = started && notFinished;

                if (active) {
                    RoomStatus roomStatus = RoomStatus.fromString(trimToNull(congViecHienTai.getTenTrangThai()));

                    // Xử lí khi phòng ở trạng thái chờ checkin
                    if (roomStatus == RoomStatus.ROOM_BOOKED_STATUS) {

                        // 1) Ghi lịch sử đi vào cho chi tiết hiện tại
                        var lastLichSuDiVao = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
                        String lastCheckinId = (lastLichSuDiVao == null) ? null : trimToNull(lastLichSuDiVao.getMaLichSuDiVao());
                        String newLichSuDiVaoId = EntityUtil.increaseEntityID(lastCheckinId,
                                EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix(),
                                EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength());

                        LichSuDiVao newLichSuDiVao = new LichSuDiVao(newLichSuDiVaoId, true, maChiTietDatPhongMain, now);
                        lichSuDiVaoDAO.themLichSuDiVao(newLichSuDiVao);

                        // 2) Kết thúc job CHỜ CHECKIN hiện tại (set tg_ket_thuc = now)
                        boolean finishedOldJob = congViecDAO.capNhatThoiGianKetThuc(congViecHienTai.getMaCongViec(), now, true);
                        if (!finishedOldJob) {
                            throw new SQLException("Không thể kết thúc job CHỜ CHECKIN hiện tại: " + congViecHienTai.getMaCongViec());
                        }

                        // 3) Tạo job mới: KIỂM TRA
                        var lastJob = congViecDAO.timCongViecMoiNhat();
                        String lastJobId = (lastJob == null) ? null : trimToNull(lastJob.getMaCongViec());
                        String newJobId = EntityUtil.increaseEntityID(lastJobId,
                                EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                EntityIDSymbol.JOB_PREFIX.getLength());

                        String tenTrangThaiChecking = RoomStatus.ROOM_CHECKING_STATUS.getStatus();
                        Timestamp tgBatDauChecking = now;
                        Timestamp tgKetThucChecking = new Timestamp(tgBatDauChecking.getTime() + WorkTimeCost.CHECKING_WAITING_TIME.getMinutes() * 60L * 1000L);
                        CongViec checkingJob = new CongViec(newJobId, tenTrangThaiChecking, tgBatDauChecking, tgKetThucChecking, maPh, tgBatDauChecking);
                        congViecDAO.themCongViec(checkingJob);

                        // 4) Ghi lịch sử thao tác cho nhân viên
                        var lastLichSuThaoTac = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
                        String lastLichSuThaoTacId = (lastLichSuThaoTac == null) ? null : trimToNull(lastLichSuThaoTac.getMaLichSuThaoTac());
                        String newWorkingHistoryId = EntityUtil.increaseEntityID(lastLichSuThaoTacId,
                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

                        String tenThaoTac = ActionType.CHECKIN.getActionName();
                        String moTaThaoTac = "Checkin (từ trạng thái CHỜ CHECKIN) cho chi tiết " + maChiTietDatPhongMain + " - phòng " + maPh;
                        LichSuThaoTac newLichSuThaoTac = new LichSuThaoTac(newWorkingHistoryId, tenThaoTac, moTaThaoTac, maPhienDangNhap, now);
                        lichSuThaoTacDAO.themLichSuThaoTac(newLichSuThaoTac);

                        // Commit và trả về
                        DatabaseUtil.thucHienGiaoTac();
                        thongBaoLoi = null;
                        return true;
                    }

                    // Các trạng thái phòng không cho phép checkin
                    boolean forbidden = roomStatus == RoomStatus.ROOM_CHECKING_STATUS
                            || roomStatus == RoomStatus.ROOM_USING_STATUS
                            || roomStatus == RoomStatus.ROOM_CHECKOUT_LATE_STATUS
                            || roomStatus == RoomStatus.ROOM_CLEANING_STATUS
                            || roomStatus == RoomStatus.ROOM_MAINTENANCE_STATUS;

                    if (forbidden) {
                        thongBaoLoi = "Phòng đang ở trạng thái '" + congViecHienTai.getTenTrangThai() + "'. Không thể check-in.";
                        DatabaseUtil.hoanTacGiaoTac();
                        return false;
                    }
                }
            }

            // 7) Nếu checkin sớm
            if (now.before(chiTietDatPhong.getTgNhanPhong())) {

                // Kiểm tra loại đơn (ĐẶT NHIỀU hay ĐẶT MỘT PHÒNG)
                String loaiDon = chiTietDatPhongDAO.getLoaiDonDatPhong(maDonDatPhongMain);

                if (loaiDon != null && loaiDon.trim().equalsIgnoreCase("ĐẶT NHIỀU")) {
                    // Nếu là đơn đặt nhiều

                    // 1) Cập nhật tg_nhan_phong của DonDatPhong
                    boolean updatedDon = chiTietDatPhongDAO.capNhatTgNhanPhongChoDonDatPhong(maDonDatPhongMain, now, maPhienDangNhap, now);
                    if (!updatedDon) {
                        throw new SQLException("Không cập nhật được tg_nhan_phong cho DonDatPhong: " + maDonDatPhongMain);
                    }

                    // 2) Lấy tất cả ChiTietDatPhong chưa kết thúc của đơn bằng DAO
                    List<ChiTietDatPhong> allNotEnded = chiTietDatPhongDAO.findNotEndedByBookingId(maDonDatPhongMain);

                    // 3) Cập nhật tg_nhan_phong cho từng chi tiết bằng DAO
                    if (allNotEnded != null) {
                        for (ChiTietDatPhong ct : allNotEnded) {
                            boolean updatedCt = chiTietDatPhongDAO.capNhatTgNhanPhong(ct.getMaChiTietDatPhong(), now, maPhienDangNhap, now);
                            if (!updatedCt) {
                                System.err.println("Không thể cập nhật tg_nhan_phong cho chi tiết: " + ct.getMaChiTietDatPhong());
                            }
                        }
                    }

                    // 4) Ghi LichSuDiVao cho mỗi chi tiết (sử dụng DAO LichSuDiVaoDAO)
                    var lastLichSuDiVao = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
                    String prevCheckinId = (lastLichSuDiVao == null) ? null : trimToNull(lastLichSuDiVao.getMaLichSuDiVao());

                    if (allNotEnded != null) {
                        for (ChiTietDatPhong ct : allNotEnded) {
                            String newCheckinId = EntityUtil.increaseEntityID(prevCheckinId,
                                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix(),
                                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength());
                            prevCheckinId = newCheckinId;

                            LichSuDiVao record = new LichSuDiVao(newCheckinId, true, ct.getMaChiTietDatPhong(), now);
                            lichSuDiVaoDAO.themLichSuDiVao(record);
                        }
                    }

                    // 5) Kết thúc job hiện tại nếu có và tạo job CHECKING cho tất cả các phòng tương ứng (sử dụng CongViecDAO)
                    var lastJob = congViecDAO.timCongViecMoiNhat();
                    String prevJobId = (lastJob == null) ? null : trimToNull(lastJob.getMaCongViec());

                    if (allNotEnded != null) {
                        for (ChiTietDatPhong ct : allNotEnded) {
                            String roomId = trimToNull(ct.getMaPhong());
                            if (roomId == null) continue;

                            CongViec jobCuForRoom = congViecDAO.layCongViecHienTaiCuaPhongChoCheckin(roomId);
                            if (jobCuForRoom != null) {
                                boolean finished = congViecDAO.removeJob(jobCuForRoom.getMaCongViec());
                                if (!finished) {
                                    throw new SQLException("Không thể kết thúc job hiện tại của phòng: " + jobCuForRoom.getMaCongViec());
                                }
                            }

                            String newJobId = EntityUtil.increaseEntityID(prevJobId,
                                    EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                    EntityIDSymbol.JOB_PREFIX.getLength());
                            prevJobId = newJobId;

                            String tenTrangThai = RoomStatus.ROOM_CHECKING_STATUS.getStatus();
                            Timestamp tgBatDau = now;
                            Timestamp tgKetThuc = new Timestamp(tgBatDau.getTime() + WorkTimeCost.CHECKING_WAITING_TIME.getMinutes() * 60L * 1000L);
                            CongViec jobMoi = new CongViec(newJobId, tenTrangThai, tgBatDau, tgKetThuc, roomId, tgBatDau);
                            congViecDAO.themCongViec(jobMoi);
                        }
                    }

                    // 6) Tính phụ phí cho các đơn đặt phòng
                    try {
                        ThongTinPhuPhi thongTinPhuPhi = FeeValue.getInstance().get(Fee.CHECK_IN_SOM);
                        if (thongTinPhuPhi != null) {
                            String maPhuPhi = trimToNull(thongTinPhuPhi.getMaPhuPhi());
                            BigDecimal donGia = thongTinPhuPhi.getGiaHienTai() == null ? BigDecimal.ZERO : thongTinPhuPhi.getGiaHienTai();

                            // Lấy id mới bắt đầu từ latest
                            var latestPtpp = phongTinhPhuPhiDAO.getLatest();
                            String prevPtppId = (latestPtpp == null) ? null : trimToNull(latestPtpp.getMaPhongTinhPhuPhi());

                            List<PhongTinhPhuPhi> dsPtpp = new ArrayList<>();
                            for (ChiTietDatPhong ct : allNotEnded) {
                                String maCt = trimToNull(ct.getMaChiTietDatPhong());
                                if (maCt == null) continue;

                                // Nếu đã có phụ phí cho chi tiết này thì bỏ qua
                                boolean already = phongTinhPhuPhiDAO.daTonTai(maCt, maPhuPhi);
                                if (already) continue;

                                // Tạo id mới cho PhongTinhPhuPhi
                                String newPtppId = EntityUtil.increaseEntityID(prevPtppId,
                                        EntityIDSymbol.ROOM_FEE.getPrefix(),
                                        EntityIDSymbol.ROOM_FEE.getLength());

                                // Set id cũ bằng id mới để tiếp tục sinh ID
                                prevPtppId = newPtppId;

                                PhongTinhPhuPhi pt = new PhongTinhPhuPhi(newPtppId, maCt, maPhuPhi, donGia);
                                try {
                                    pt.setTongTien(donGia);
                                } catch (NoSuchMethodError | Exception ignore) {

                                }

                                dsPtpp.add(pt);
                            }

                            if (!dsPtpp.isEmpty()) {
                                boolean ok = phongTinhPhuPhiDAO.themDanhSachPhuPhiChoCacPhong(dsPtpp);
                                if (!ok) {
                                    for (PhongTinhPhuPhi p : dsPtpp) {
                                        try {
                                            phongTinhPhuPhiDAO.insert(p);
                                        } catch (Exception ex) {
                                            System.err.println("Không thể thêm PhongTinhPhuPhi: " + p.getMaPhongTinhPhuPhi() + " -> " + ex.getMessage());
                                        }
                                    }
                                }
                            }
                        } else {
                            System.err.println("Không tìm thấy cấu hình phụ phí 'Check-in sớm' (Fee.CHECK_IN_SOM).");
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi áp phụ phí check-in-sớm cho đơn nhiều: " + ex.getMessage());
                        throw ex;
                    }


                    // 7) Ghi lịch sử thao tác (1 bản cho toàn đơn)
                    var lastLichSuThaoTac = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
                    String lastLichSuThaoTacId = (lastLichSuThaoTac == null) ? null : trimToNull(lastLichSuThaoTac.getMaLichSuThaoTac());
                    String newWorkingHistoryId = EntityUtil.increaseEntityID(lastLichSuThaoTacId,
                            EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                            EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

                    String tenThaoTac = ActionType.CHECKIN.getActionName();
                    String moTaThaoTac = "Checkin (đặt nhiều) cho đơn đặt phòng " + maDonDatPhongMain;
                    LichSuThaoTac newLichSuThaoTac = new LichSuThaoTac(newWorkingHistoryId, tenThaoTac, moTaThaoTac, maPhienDangNhap, now);
                    lichSuThaoTacDAO.themLichSuThaoTac(newLichSuThaoTac);

                    // Commit
                    DatabaseUtil.thucHienGiaoTac();
                    thongBaoLoi = null;
                    return true;

                } else {
                    // TRƯỜNG HỢP ĐƠN CHỈ ĐẶT MỘT PHÒNG

                    // Cập nhật thời gian nhận phòng cho đơn đặt phòng
                    boolean updatedDon = chiTietDatPhongDAO.capNhatTgNhanPhongChoDonDatPhong(maDonDatPhongMain, now, maPhienDangNhap, now);
                    if (!updatedDon) {
                        throw new SQLException("Không cập nhật được tg_nhan_phong cho DonDatPhong: " + maDonDatPhongMain);
                    }

                    // Cập nhật ChiTietDatPhong hiện tại: đặt tg_nhan_phong = now
                    boolean updated = chiTietDatPhongDAO.capNhatTgNhanPhong(maChiTietDatPhongMain, now, maPhienDangNhap, now);
                    if (!updated) {
                        throw new SQLException("Không thể cập nhật ChiTietDatPhong cho checkin sớm: " + maChiTietDatPhongMain);
                    }

                    // Áp phụ phí cho chi tiết đặt phòng
                    String tenPhuPhi = Fee.CHECK_IN_SOM.getStatus();
                    ThongTinPhuPhi thongTinPhuPhi = FeeValue.getInstance().get(Fee.CHECK_IN_SOM);
                    if (thongTinPhuPhi == null) {
                        throw new IllegalStateException("Không tìm thấy thông tin phụ phí cho: " + tenPhuPhi);
                    }

                    String maPhuPhi = trimToNull(thongTinPhuPhi.getMaPhuPhi());
                    BigDecimal giaHienTai = thongTinPhuPhi.getGiaHienTai();
                    if (giaHienTai == null) giaHienTai = BigDecimal.ZERO;

                    // Kiểm tra phụ phí đã tồn tại cho chi tiết đặt phòng chưa
                    boolean feeAlreadyAppliedForNewCt = phongTinhPhuPhiDAO.daTonTai(maChiTietDatPhongMain, maPhuPhi);
                    if (!feeAlreadyAppliedForNewCt) {
                        var latestFee = phongTinhPhuPhiDAO.getLatest();
                        String latestFeeId = (latestFee == null) ? null : trimToNull(latestFee.getMaPhongTinhPhuPhi());
                        String newFeeId = EntityUtil.increaseEntityID(latestFeeId,
                                EntityIDSymbol.ROOM_FEE.getPrefix(),
                                EntityIDSymbol.ROOM_FEE.getLength());

                        PhongTinhPhuPhi feeEntity = new PhongTinhPhuPhi(newFeeId, maChiTietDatPhongMain, maPhuPhi, giaHienTai);
                        boolean insertedFee = phongTinhPhuPhiDAO.insert(feeEntity);
                        if (!insertedFee) {
                            System.err.println("Thêm PhongTinhPhuPhi cho chi tiết checkin-sớm không thành công!!");
                        }
                    }
                }
            }

            // ===== phần tiếp theo xử lý cho trường hợp normal (không phải checkin-sớm-ĐẶT_NHIỀU) =====
            var lastLichSuDiVao = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
            String lastCheckinId = (lastLichSuDiVao == null) ? null : trimToNull(lastLichSuDiVao.getMaLichSuDiVao());
            String newLichSuDiVaoId = EntityUtil.increaseEntityID(lastCheckinId,
                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix(),
                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength());

            LichSuDiVao newLichSuDiVao = new LichSuDiVao(newLichSuDiVaoId, true, maChiTietDatPhongMain, now);
            lichSuDiVaoDAO.themLichSuDiVao(newLichSuDiVao);

            CongViec jobCu = congViecDAO.layCongViecHienTaiCuaPhongChoCheckin(maPh);
            if (jobCu != null) {
                boolean finished = congViecDAO.capNhatThoiGianKetThuc(jobCu.getMaCongViec(), now, true);
                if (!finished) {
                    throw new SQLException("Không thể kết thúc job hiện tại của phòng: " + jobCu.getMaCongViec());
                }
            }

            var lastJob = congViecDAO.timCongViecMoiNhat();
            String lastJobId = (lastJob == null) ? null : trimToNull(lastJob.getMaCongViec());
            String newJobId = EntityUtil.increaseEntityID(lastJobId,
                    EntityIDSymbol.JOB_PREFIX.getPrefix(),
                    EntityIDSymbol.JOB_PREFIX.getLength());

            String tenTrangThai = RoomStatus.ROOM_CHECKING_STATUS.getStatus();
            Timestamp tgBatDau = now;
            Timestamp tgKetThuc = new Timestamp(tgBatDau.getTime() + WorkTimeCost.CHECKING_WAITING_TIME.getMinutes()  * 60L * 1000L);
            CongViec jobMoi = new CongViec(newJobId, tenTrangThai, tgBatDau, tgKetThuc, maPh, tgBatDau);
            congViecDAO.themCongViec(jobMoi);

            var lastLichSuThaoTac = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String lastLichSuThaoTacId = (lastLichSuThaoTac == null) ? null : trimToNull(lastLichSuThaoTac.getMaLichSuThaoTac());
            String newWorkingHistoryId = EntityUtil.increaseEntityID(lastLichSuThaoTacId,
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            String tenThaoTac = ActionType.CHECKIN.getActionName();
            String moTaThaoTac = "Checkin cho đơn đặt phòng " + maDonDatPhongMain;
            LichSuThaoTac newLichSuThaoTac = new LichSuThaoTac(newWorkingHistoryId, tenThaoTac, moTaThaoTac, maPhienDangNhap, now);
            lichSuThaoTacDAO.themLichSuThaoTac(newLichSuThaoTac);

            // Commit nếu mọi thứ OK
            DatabaseUtil.thucHienGiaoTac();
            thongBaoLoi = null;
            return true;

        } catch (Exception e) {
            // Rollback transaction nếu có lỗi
            try {
                DatabaseUtil.hoanTacGiaoTac();
            } catch (Exception ex) {
                System.err.println("Lỗi khi rollback trong checkin: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
            thongBaoLoi = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return false;
        }
    }



    // In lỗi ra thoi hehe
    @Override
    public String getLastError() {
        return thongBaoLoi;
    }

    // Tìm mã phòng từ tên phòng
    private String resolveRoomIdentifier(String input, String maDonDatPhong,
                                         ChiTietDatPhongDAO chiTietDatPhongDAO, PhongDAO phongDAO) {
        String v = trimToNull(input);
        if (v == null) return null;
        String trimmed = v.trim();

        // 1) Nếu đã là mã PH + 8 số
        if (trimmed.matches("(?i)^PH\\d{8}$")) {
            return trimmed.toUpperCase(Locale.ROOT);
        }

        // 2) Tìm trong chi tiết đặt phòng của đơn
        List<ChiTietDatPhong> chiTietList = chiTietDatPhongDAO.findByBookingId(trimToNull(maDonDatPhong));
        if (chiTietList != null && !chiTietList.isEmpty()) {
            String normInput = normalizeForCompare(trimmed);
            for (ChiTietDatPhong item : chiTietList) {
                String itemMaPhong = trimToNull(item.getMaPhong());
                if (itemMaPhong == null) continue;
                var p = phongDAO.timPhong(itemMaPhong);
                if (p == null) continue;
                String tenPhongNorm = normalizeForCompare(p.getTenPhong());
                if (tenPhongNorm.contains(normInput) || normInput.contains(tenPhongNorm) || tenPhongNorm.equals(normInput)) {
                    return itemMaPhong;
                }
            }
        }

        // 3) Dựng mã
        Matcher m = Pattern.compile("(\\d+)").matcher(trimmed);
        if (m.find()) {
            try {
                int n = Integer.parseInt(m.group(1));
                String idNumber = String.format("%08d", n);
                String candidateId = "PH" + idNumber;
                // Kiểm tra phòng tồn tại
                try {
                    var p = phongDAO.timPhong(candidateId);
                    if (p != null) return candidateId;
                } catch (Exception ex) {
                    System.err.println("Lỗi khi kiểm tra phòng bằng id: " + ex.getMessage());
                }
            } catch (NumberFormatException ignore) {
            }
        }

        // 4) give up :((
        return null;
    }

    private String normalizeForCompare(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        // Loại bỏ dấu để so sánh không phụ thuộc dấu tiếng Việt
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        // Loại bỏ tiền tố "Phòng" hoặc 'p' nếu có
        t = t.replaceAll("^phòng\\s*", "");
        t = t.replaceAll("^p\\s*", "");
        // Xóa mọi ký tự không phải chữ hoặc số
        t = t.replaceAll("[^a-z0-9]+", "");
        return t;
    }

    // Xóa khoảng trắng
    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public String layMaDonDatPhongTuMaChiTiet(String maChiTietDatPhong) {
        String maDonDatPhong = chiTietDatPhongDAO.findFormIDByDetail(maChiTietDatPhong);
        return maDonDatPhong;
    }
}

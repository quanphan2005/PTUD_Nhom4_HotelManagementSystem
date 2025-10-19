package vn.iuh.service.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.Fee;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.*;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.entity.*;
import vn.iuh.gui.base.Main;
import vn.iuh.service.CheckinService;
import vn.iuh.util.EntityUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
            datPhongDAO.khoiTaoGiaoTac();
            Connection conn = datPhongDAO.getConnection();

            // 2) Khởi tạo DAO dùng chung connection (để rollback khi cần)
            LichSuDiVaoDAO lichSuDiVaoDAO = new LichSuDiVaoDAO(conn);
            CongViecDAO congViecDAO = new CongViecDAO(conn);
            LichSuThaoTacDAO lichSuThaoTacDAO = new LichSuThaoTacDAO(conn);
            ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAO(conn);
            PhongTinhPhuPhiDAO phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO(conn);
            PhuPhiDAO phuPhiDAO = new PhuPhiDAO(conn);
            PhongDAO phongDAO = new PhongDAO(conn);

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

            // 6) Kiểm tra Checkin hay chưa (nếu đã checkin -> rollback và trả false)
            boolean alreadyCheckedIn = lichSuDiVaoDAO.daTonTai(maChiTietDatPhongMain);
            if (alreadyCheckedIn) {
                thongBaoLoi = "Đơn này đã được check-in trước đó.";
                datPhongDAO.hoanTacGiaoTac();
                return false;
            }

            // Kiểm tra trạng thái công việc hiện tại của phòng
            CongViec congViecHienTai = congViecDAO.layCongViecHienTaiCuaPhong(maPh);
            if (congViecHienTai != null) {
                Timestamp jobStart = congViecHienTai.getTgBatDau();
                Timestamp jobEnd = congViecHienTai.getTgKetThuc();
                boolean started = jobStart != null && !now.before(jobStart);
                boolean notFinished = jobEnd == null || jobEnd.after(now);
                boolean active = started && notFinished;

                if (active) {
                    RoomStatus roomStatus = RoomStatus.fromString(trimToNull(congViecHienTai.getTenTrangThai()));

                    // Các trạng thái không cho phép checkin:
                    boolean forbidden = roomStatus == RoomStatus.ROOM_CHECKING_STATUS
                            || roomStatus == RoomStatus.ROOM_USING_STATUS
                            || roomStatus == RoomStatus.ROOM_CHECKOUT_LATE_STATUS
                            || roomStatus == RoomStatus.ROOM_CLEANING_STATUS
                            || roomStatus == RoomStatus.ROOM_MAINTENANCE_STATUS;

                    if (forbidden) {
                        thongBaoLoi = "Phòng đang ở trạng thái '" + congViecHienTai.getTenTrangThai() + "'. Không thể check-in.";
                        datPhongDAO.hoanTacGiaoTac();
                        return false;
                    }
                }
            }

            // 7) Nếu checkin sớm
            if (now.before(chiTietDatPhong.getTgNhanPhong())) {
                // Cập nhật ChiTietDatPhong hiện tại: đặt tg_nhan_phong = now
                boolean updated = chiTietDatPhongDAO.capNhatTgNhanPhong(maChiTietDatPhongMain, now, maPhienDangNhap, now);
                if (!updated) {
                    throw new SQLException("Không thể cập nhật ChiTietDatPhong cho checkin sớm: " + maChiTietDatPhongMain);
                }

                // Áp phụ phí cho chi tiết đặt phòng
                String tenPhuPhi = Fee.CHECK_IN_SOM.getStatus();
                ThongTinPhuPhi thongTinPhuPhi = phuPhiDAO.getThongTinPhuPhiByName(tenPhuPhi);
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

            // 8) Ghi lịch sử checkin (sử dụng maChiTietDatPhongMain đã có)
            var lastLichSuDiVao = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
            String lastCheckinId = (lastLichSuDiVao == null) ? null : trimToNull(lastLichSuDiVao.getMaLichSuDiVao());
            String newLichSuDiVaoId = EntityUtil.increaseEntityID(lastCheckinId,
                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix(),
                    EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength());

            LichSuDiVao newLichSuDiVao = new LichSuDiVao(newLichSuDiVaoId, true, maChiTietDatPhongMain, now);
            lichSuDiVaoDAO.themLichSuDiVao(newLichSuDiVao);

            // 9) Kết thúc job hiện tại nếu có
            CongViec jobCu = congViecDAO.layCongViecHienTaiCuaPhongChoCheckin(maPh);
            if (jobCu != null) {
                boolean finished = congViecDAO.capNhatThoiGianKetThuc(jobCu.getMaCongViec(), now, true);
                if (!finished) {
                    throw new SQLException("Không thể kết thúc job hiện tại của phòng: " + jobCu.getMaCongViec());
                }
            }

            // 10) Tạo job mới CHECKING
            var lastJob = congViecDAO.timCongViecMoiNhat();
            String lastJobId = (lastJob == null) ? null : trimToNull(lastJob.getMaCongViec());
            String newJobId = EntityUtil.increaseEntityID(lastJobId,
                    EntityIDSymbol.JOB_PREFIX.getPrefix(),
                    EntityIDSymbol.JOB_PREFIX.getLength());

            String tenTrangThai = RoomStatus.ROOM_CHECKING_STATUS.getStatus();
            Timestamp tgBatDau = now;
            Timestamp tgKetThuc = new Timestamp(tgBatDau.getTime() + 30L * 60L * 1000L);
            CongViec jobMoi = new CongViec(newJobId, tenTrangThai, tgBatDau, tgKetThuc, maPh, tgBatDau);
            congViecDAO.themCongViec(jobMoi);

            // 11) Ghi lịch sử thao tác
            var lastLichSuThaoTac = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String lastLichSuThaoTacId = (lastLichSuThaoTac == null) ? null : trimToNull(lastLichSuThaoTac.getMaLichSuThaoTac());
            String newWorkingHistoryId = EntityUtil.increaseEntityID(lastLichSuThaoTacId,
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            String tenThaoTac = ActionType.CHECKIN.getActionName();
            String moTaThaoTac = "Checkin cho đơn đặt phòng " + maDonDatPhongMain;
            LichSuThaoTac newLichSuThaoTac = new LichSuThaoTac(newWorkingHistoryId, tenThaoTac, moTaThaoTac, maPhienDangNhap, now);
            lichSuThaoTacDAO.themLichSuThaoTac(newLichSuThaoTac);

            // 12) Commit nếu mọi thứ OK
            datPhongDAO.thucHienGiaoTac();
            thongBaoLoi = null;
            return true;

        } catch (Exception e) {
            // Rollback transaction nếu có lỗi
            try {
                datPhongDAO.hoanTacGiaoTac();
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
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}+,", "");
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
}

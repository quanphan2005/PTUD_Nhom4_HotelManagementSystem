package vn.iuh.service.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.RoomEndType;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.DatPhongDAO;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dto.repository.BookThemGioInfo;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.gui.base.Main;
import vn.iuh.service.BookThemGioService;
import vn.iuh.util.EntityUtil;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;


public class BookThemGioServiceImpl implements BookThemGioService {

    private final ChiTietDatPhongDAO chiTietDao;
    // Giá trị mặc định
    private static final int DEFAULT_MAX_HOURS = 72;

    // Dùng để trừ đi 2 giờ cho việc dọn dẹp
    private static final long CLEANUP_MILLIS = 2L * 3600L * 1000L;

    public BookThemGioServiceImpl() {
        this.chiTietDao = new ChiTietDatPhongDAO();
    }

    // Lấy các thông tin để hiển thị lên BookThemGioDialog
    @Override
    public BookThemGioInfo layThongTinChoBookThemGio(String maChiTietDatPhong, String maPhong) {
        Timestamp tgNhanPhong = null;
        Timestamp tgTraPhong = null;
        int gioToiDaChoPhep = DEFAULT_MAX_HOURS;

        try {
            // 1) Lấy tgNhan/tgTra từ danh sách chi tiết theo đơn (nếu có maChiTietDatPhong)
            String maDon = null;
            if (maChiTietDatPhong != null && !maChiTietDatPhong.isEmpty()) {
                maDon = chiTietDao.findFormIDByDetail(maChiTietDatPhong);
            }

            List<ChiTietDatPhong> danhSach = null;
            if (maDon != null && !maDon.isEmpty()) {
                danhSach = chiTietDao.findByBookingId(maDon);
            }

            if (danhSach != null && !danhSach.isEmpty()) {
                // Lấy tgNhan từ first và tgTra từ last (dùng cho hiển thị)
                ChiTietDatPhong first = danhSach.get(0);
                if (first.getTgNhanPhong() != null) tgNhanPhong = first.getTgNhanPhong();

                ChiTietDatPhong last = danhSach.get(danhSach.size() - 1);
                if (last.getTgTraPhong() != null) tgTraPhong = last.getTgTraPhong();
            }

            // 2) Tính giờ tối đa
            // Nếu không có maPhong truyền vào, cố gắng lấy từ thông tin
            if ((maPhong == null || maPhong.isEmpty()) && danhSach != null && !danhSach.isEmpty()) {
                // cố lấy phòng từ chi tiết hiện tại nếu có
                for (ChiTietDatPhong c : danhSach) {
                    if (c.getMaPhong() != null && !c.getMaPhong().isEmpty()) {
                        maPhong = c.getMaPhong();
                        break;
                    }
                }
            }

            // Nếu không có danh sách chi tiết hoặc tgTraPhong không có
            if (maDon == null || maDon.isEmpty() || danhSach == null || danhSach.isEmpty()) {

                if (maPhong != null && !maPhong.isEmpty() && tgTraPhong != null) {
                    ChiTietDatPhong next = chiTietDao.timChiTietDatPhongTiepTheoCuaPhong(maPhong, tgTraPhong);
                    if (next != null && next.getTgNhanPhong() != null) {
                        long allowedMillis = next.getTgNhanPhong().getTime() - CLEANUP_MILLIS - tgTraPhong.getTime();
                        long hours = allowedMillis / (1000L * 3600L);
                        if (hours < 0L) hours = 0L;
                        gioToiDaChoPhep = (int) hours;
                    } else {
                        gioToiDaChoPhep = -1;
                    }
                } else {
                    // Không đủ thông tin: giữ mặc định
                    if (tgTraPhong == null) gioToiDaChoPhep = DEFAULT_MAX_HOURS;
                    else gioToiDaChoPhep = -1;
                }
                return new BookThemGioInfo(tgNhanPhong, tgTraPhong, gioToiDaChoPhep);
            }

            // Kiểm tra xem có phải đơn đặt nhiều hay không
            String loaiDon = chiTietDao.getLoaiDonDatPhong(maDon);
            boolean isMulti = loaiDon != null && loaiDon.toUpperCase().contains("NHIỀU");

            if (!isMulti) {
                // Tìm chi tiết đặt phòng tiếp theo của phòng
                if (maPhong != null && !maPhong.isEmpty() && tgTraPhong != null) {
                    ChiTietDatPhong next = chiTietDao.timChiTietDatPhongTiepTheoCuaPhong(maPhong, tgTraPhong);
                    if (next != null && next.getTgNhanPhong() != null) {
                        long allowedMillis = next.getTgNhanPhong().getTime() - CLEANUP_MILLIS - tgTraPhong.getTime();
                        long hours = allowedMillis / (1000L * 3600L); // floor
                        if (hours < 0L) hours = 0L;
                        gioToiDaChoPhep = (int) hours;
                    } else {
                        gioToiDaChoPhep = -1;
                    }
                } else {
                    if (tgTraPhong == null) gioToiDaChoPhep = DEFAULT_MAX_HOURS;
                    else gioToiDaChoPhep = -1;
                }
                return new BookThemGioInfo(tgNhanPhong, tgTraPhong, gioToiDaChoPhep);
            }

            // Xử lí case đặt nhiều phòng
            long minAllowedMillis = Long.MAX_VALUE;
            boolean foundAnyConstraint = false;

            // Duyệt qua tất cả chi tiết đặt phòng trong đơn đặt phòng
            for (ChiTietDatPhong ct : danhSach) {
                // Chỉ kiểm tra những chi tiết đặt phòng chưa kết thúc trong đơn
                String kkt = ct.getKieuKetThuc();
                if (kkt != null && !kkt.trim().isEmpty()) continue;

                Timestamp ctTgTra = ct.getTgTraPhong();
                String ctRoom = ct.getMaPhong();
                if (ctRoom == null || ctRoom.isEmpty()) continue;

                if (ctTgTra == null) {
                    // Không có thời gian trả phòng ==> Không thể tính
                    continue;
                }

                // Tìm booking tiếp theo cho phòng này
                ChiTietDatPhong next = chiTietDao.timChiTietDatPhongTiepTheoCuaPhong(ctRoom, ctTgTra);
                if (next != null && next.getTgNhanPhong() != null) {
                    long allowedMillis = next.getTgNhanPhong().getTime() - CLEANUP_MILLIS - ctTgTra.getTime();
                    if (allowedMillis < 0L) allowedMillis = 0L;
                    if (allowedMillis < minAllowedMillis) minAllowedMillis = allowedMillis;
                    foundAnyConstraint = true;
                } else {
                    // Nếu không có đơn đặt tiếp theo => Bỏ qua
                }
            }

            if (foundAnyConstraint) {
                long hours = minAllowedMillis / (1000L * 3600L);
                gioToiDaChoPhep = (int) Math.max(0L, hours);
            } else {
                // Nếu không có phòng nào bị giới hạn ==> Đặt là không giới hạn
                gioToiDaChoPhep = -1;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            gioToiDaChoPhep = DEFAULT_MAX_HOURS;
        }

        return new BookThemGioInfo(tgNhanPhong, tgTraPhong, gioToiDaChoPhep);
    }



    public boolean bookThemGio(String maChiTietDatPhong, String maPhong, long millisToAdd) {
        if (maChiTietDatPhong == null || maChiTietDatPhong.isEmpty()
                || maPhong == null || maPhong.isEmpty()
                || millisToAdd <= 0L) {
            return false;
        }

        DatPhongDAO datPhongDAO = new DatPhongDAO();
        try {
            // mở transaction
            datPhongDAO.khoiTaoGiaoTac();
            Connection conn = datPhongDAO.getConnection();

            // khai báo các DAO (sử dụng cùng connection)
            ChiTietDatPhongDAO chiDaoTx = new ChiTietDatPhongDAO(conn);
            CongViecDAO congViecDaoTx = new CongViecDAO(conn);
            LichSuThaoTacDAO lichSuDaoTx = new LichSuThaoTacDAO(conn);

            // 1) Lấy chi tiết đặt phòng hiện tại
            ChiTietDatPhong current = chiDaoTx.timChiTietDatPhong(maChiTietDatPhong);
            if (current == null) {
                datPhongDAO.hoanTacGiaoTac();
                return false;
            }

            // 3) Kiểm tra kiểu kết thúc
            String kieuKetThuc = current.getKieuKetThuc();
            if (kieuKetThuc != null && !kieuKetThuc.trim().isEmpty()) {
                String kk = kieuKetThuc.trim();
                String tra = RoomEndType.TRA_PHONG.getStatus();
                String traLoi = RoomEndType.TRA_PHONG_LOI.getStatus();
                String huyPhong = RoomEndType.HUY_PHONG.getStatus();
                if (kk.equalsIgnoreCase(tra) || kk.equalsIgnoreCase(traLoi) || kk.equalsIgnoreCase(huyPhong)) {
                    // không cho gia hạn nếu đã trả phòng (bình thường hoặc do lỗi) hoặc đã hủy phòng
                    datPhongDAO.hoanTacGiaoTac();
                    return false;
                }
            }

            // 4) Kiểm tra trạng thái hiện tại của phòng, nếu là đang CHECKOUT trễ thì không cho phép book thêm giờ
            CongViec congViecHienTai = congViecDaoTx.layCongViecHienTaiCuaPhong(maPhong);
            if (congViecHienTai != null && congViecHienTai.getTenTrangThai() != null) {
                String ten = congViecHienTai.getTenTrangThai().toUpperCase();
                if (ten.contains("TRỄ") || ten.contains("CHECKOUT TRỄ")) {
                    datPhongDAO.hoanTacGiaoTac();
                    return false;
                }
            }

            // 5) Cập nhật tg_tra_phong trong ChiTietDatPhong (chi tiết hiện tại)
            Timestamp oldTra = current.getTgTraPhong();
            if (oldTra == null) oldTra = new Timestamp(System.currentTimeMillis());
            Timestamp newTra = new Timestamp(oldTra.getTime() + millisToAdd);

            boolean updatedCT = chiDaoTx.ketThucChiTietDatPhong(current.getMaChiTietDatPhong(), newTra, current.getKieuKetThuc());
            if (!updatedCT) {
                datPhongDAO.hoanTacGiaoTac();
                return false;
            }

            // Cập nhật tg_tra_phong của đơn đặt phòng tương ứng
            String maDon = current.getMaDonDatPhong();
            if (maDon != null && !maDon.isEmpty()) {
                boolean donOk = chiDaoTx.updateDonTraPhong(maDon, newTra);
                if (!donOk) {
                    System.out.println("Warning: Không cập nhật được DonDatPhong.tg_tra_phong cho " + maDon);
                }

                // Nếu là đơn đặt nhiều thì cập nhật tg_tra_phong cho tất cả các chi tiết đặt phòng
                String loaiDon = chiDaoTx.getLoaiDonDatPhong(maDon);
                if (loaiDon != null && loaiDon.toUpperCase().contains("NHIỀU")) {
                    int updatedRows = chiDaoTx.updateAllChiTietTraPhongIfNotEnded(maDon, newTra);
                    System.out.println("Cập nhật tg_tra_phong cho " + updatedRows + " chi tiết của đơn " + maDon);
                }
            }

            // 6) Nếu có công việc hiện tại, chỉ cập nhật tg_ket_thuc nếu công việc đó là "SỬ DỤNG"
            if (congViecHienTai != null) {
                String tenCV = congViecHienTai.getTenTrangThai();
                if (tenCV != null && tenCV.equalsIgnoreCase(RoomStatus.ROOM_USING_STATUS.getStatus())) {
                    Timestamp jobOldEnd = congViecHienTai.getTgKetThuc();
                    Timestamp jobNewEnd;
                    if (jobOldEnd != null) jobNewEnd = new Timestamp(jobOldEnd.getTime() + millisToAdd);
                    else jobNewEnd = new Timestamp(newTra.getTime());

                    boolean jobOk = congViecDaoTx.capNhatThoiGianKetThuc(congViecHienTai.getMaCongViec(), jobNewEnd, false);
                    if (!jobOk) {
                        datPhongDAO.hoanTacGiaoTac();
                        return false;
                    }
                }
            }

            // 7) Ghi lịch sử thao tác cho nhân viên
            var latest = lichSuDaoTx.timLichSuThaoTacMoiNhat();
            String latestId = (latest == null) ? null : latest.getMaLichSuThaoTac();
            String newId = EntityUtil.increaseEntityID(latestId,
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                    EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength());

            String tenThaoTac = ActionType.EXTEND.getActionName();
            String moTa = String.format("Gia hạn chi tiết %s thêm %d giờ cho phòng %s",
                    maChiTietDatPhong, millisToAdd / (3600L * 1000L), maPhong);

            String maPhienDangNhap = Main.getCurrentLoginSession();

            Timestamp now = new Timestamp(System.currentTimeMillis());
            LichSuThaoTac newLSTT = new LichSuThaoTac(newId, tenThaoTac, moTa, maPhienDangNhap, now);
            lichSuDaoTx.themLichSuThaoTac(newLSTT);

            // 8) Commit transaction
            datPhongDAO.thucHienGiaoTac();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                datPhongDAO.hoanTacGiaoTac();
            } catch (Exception ignore) {}
            return false;
        }
    }
}

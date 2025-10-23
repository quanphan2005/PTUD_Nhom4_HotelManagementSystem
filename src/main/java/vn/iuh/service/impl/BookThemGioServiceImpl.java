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
            if (maChiTietDatPhong != null && !maChiTietDatPhong.isEmpty()) {
                String maDon = chiTietDao.findFormIDByDetail(maChiTietDatPhong);
                if (maDon != null && !maDon.isEmpty()) {
                    List<ChiTietDatPhong> danhSach = chiTietDao.findByBookingId(maDon);
                    if (danhSach != null && !danhSach.isEmpty()) {
                        ChiTietDatPhong first = danhSach.get(0);
                        if (first.getTgNhanPhong() != null) tgNhanPhong = first.getTgNhanPhong();

                        ChiTietDatPhong last = danhSach.get(danhSach.size() - 1);
                        if (last.getTgTraPhong() != null) tgTraPhong = last.getTgTraPhong();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 2) Tính giờ tối đa bằng cách kiểm tra đơn đặt tiếp theo của phòng
        try {
            if (maPhong != null && !maPhong.isEmpty() && tgTraPhong != null) {
                // tìm booking tiếp theo có
                ChiTietDatPhong next = chiTietDao.timChiTietDatPhongTiepTheoCuaPhong(maPhong, tgTraPhong);
                if (next != null && next.getTgNhanPhong() != null) {
                    long allowedMillis = next.getTgNhanPhong().getTime() - CLEANUP_MILLIS - tgTraPhong.getTime();
                    long hours = allowedMillis / (1000L * 3600L); // floor
                    if (hours < 0L) hours = 0L;
                    gioToiDaChoPhep = (int) hours;
                } else {
                    // Nếu khôgn có đơn tiếp theo thì đánh dấu là không giới hạn thời gian có thể book thêm
                    gioToiDaChoPhep = -1;
                }
            } else {
                // Nếu tg trả phòng không rõ thì gán giờ tối đa cho phép là 72 giờ
                if (tgTraPhong == null) {
                    gioToiDaChoPhep = DEFAULT_MAX_HOURS;
                } else {
                    // Mã phòng null nhưng thời gian trả phòng có
                    gioToiDaChoPhep = -1;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            gioToiDaChoPhep = DEFAULT_MAX_HOURS;
        }

        return new BookThemGioInfo(tgNhanPhong, tgTraPhong, gioToiDaChoPhep);
    }


    // Xử lí chức năng book thêm giờ
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

            // khai báo các DAO
            ChiTietDatPhongDAO chiDaoTx = new ChiTietDatPhongDAO(conn);
            CongViecDAO congViecDaoTx = new CongViecDAO(conn);
            LichSuThaoTacDAO lichSuDaoTx = new LichSuThaoTacDAO(conn);

            // 1) Lấy chi tiết đặt phòng hiện tại
            ChiTietDatPhong current = chiDaoTx.timChiTietDatPhong(maChiTietDatPhong);
            if (current == null) {
                datPhongDAO.hoanTacGiaoTac();
                return false;
            }

            // 2) Kiểm tra đã check-in chưa
            boolean checkedIn = false;
            try {
                String maDon = chiDaoTx.findFormIDByDetail(maChiTietDatPhong);
                if (maDon != null && !maDon.isEmpty()) {
                    var thongTin = chiDaoTx.layThongTinSuDungPhong(maDon);
                    if (thongTin != null) {
                        for (var t : thongTin) {
                            if (maChiTietDatPhong.equals(t.getMaChiTietDatPhong()) && t.getGioCheckIn() != null) {
                                checkedIn = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            if (!checkedIn) {
                // không thể gia hạn nếu chưa checkin
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
                if (kk.equalsIgnoreCase(tra) || kk.equalsIgnoreCase(traLoi) || tra.equalsIgnoreCase(huyPhong)) {
                    // không cho gia hạn nếu đã trả phòng (bình thường hoặc do lỗi) hoặc đã hủy phòng
                    datPhongDAO.hoanTacGiaoTac();
                    return false;
                }
                // Nếu kieu_ket_thuc khác thì tiếp tục xử lí theo yêu cầu
            }

            // 4) Kiểm tra trạng thái hiện tại của phòng, nếu là đang CHECKOUT trễ thì không
            // cho phép book thêm giờ
            CongViec congViecHienTai = congViecDaoTx.layCongViecHienTaiCuaPhong(maPhong);
            if (congViecHienTai != null && congViecHienTai.getTenTrangThai() != null) {
                String ten = congViecHienTai.getTenTrangThai().toUpperCase();
                if (ten.contains("TRỄ") || ten.contains("CHECKOUT TRỄ")) {
                    datPhongDAO.hoanTacGiaoTac();
                    return false;
                }
            }

            // 5) Cập nhật tg_tra_phong trong ChiTietDatPhong
            Timestamp oldTra = current.getTgTraPhong();
            if (oldTra == null) oldTra = new Timestamp(System.currentTimeMillis());
            Timestamp newTra = new Timestamp(oldTra.getTime() + millisToAdd);

            boolean updatedCT = chiDaoTx.ketThucChiTietDatPhong(current.getMaChiTietDatPhong(), newTra, current.getKieuKetThuc());
            if (!updatedCT) {
                datPhongDAO.hoanTacGiaoTac();
                return false;
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
                } else {
                    // Nếu công việc không phải là "SỬ DỤNG" thì không cần xử lí
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

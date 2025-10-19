package vn.iuh.service.impl;

import vn.iuh.constraint.*;
import vn.iuh.dao.*;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.dto.repository.ThongTinSuDungPhong;
import vn.iuh.entity.*;
import vn.iuh.exception.BusinessException;
import vn.iuh.gui.base.Main;
import vn.iuh.service.CheckOutService;
import vn.iuh.service.CongViecService;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.util.EntityUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckOutServiceImpl implements CheckOutService {
    private final DatPhongDAO datPhongDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
    private final LichSuRaNgoaiDAO lichSuRaNgoaiDAO;
    private final LoaiPhongService loaiPhongService;
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private final PhuPhiDAO phuPhiDAO;
    private final CongViecService congViecService;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
    private final CongViecDAO congViecDAO;
    public CheckOutServiceImpl() {
        this.datPhongDAO = new DatPhongDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.congViecDAO = new CongViecDAO();
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
        this.congViecService = new CongViecServiceImpl();
        this.phuPhiDAO = new PhuPhiDAO();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.loaiPhongService = new LoaiPhongServiceImpl();
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
        this.lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO();
    }

    public DonDatPhong validateDonDatPhong(String maDonDatPhong){
        var reservation = datPhongDAO.getDonDatPhongById(maDonDatPhong);
//        var existingInvoice = hoaDonDAO.findInvoiceForReservation(maDonDatPhong);
        if(reservation == null){
            throw new BusinessException("Không tìm thấy đơn đặt phòng");
        }
//
//        if(existingInvoice != null){
//            throw new BusinessException("Đã tạo hóa đơn thanh toán cho đơn đặt phòng này");
//        }
        return reservation;
    }

    private List<ThongTinSuDungPhong> filterUsageRoom(String reservationId){
        List<ThongTinSuDungPhong> chiTietSuDungList = chiTietDatPhongDAO.layThongTinSuDungPhong(reservationId);
        //Lọc những phòng cần phải tính tiền
        List<ThongTinSuDungPhong> noneUsageRoom = new ArrayList<>();
        for(ThongTinSuDungPhong tt : chiTietSuDungList){
            if(tt.getGioCheckIn() == null || RoomEndType.TRA_PHONG_LOI.getStatus().equalsIgnoreCase(tt.getKieuKetThuc())){
                noneUsageRoom.add(tt);
            }
        }
        chiTietSuDungList.removeAll(noneUsageRoom);
        return chiTietSuDungList;
    }

    @Override
    public boolean checkOutReservation(String reservationId){
        try {
            datPhongDAO.khoiTaoGiaoTac();
        //Tìm đơn đặt phòng
            var reservation = validateDonDatPhong(reservationId);

    //      Lấy tất cả chi tiết thông tin sử dụng phòng của đơn đặt phòng
            List<ThongTinSuDungPhong> chiTietSuDungList = filterUsageRoom(reservationId);
            if(chiTietSuDungList.isEmpty()){
                throw new BusinessException("Không tìm thấy thông tin sử dụng của đơn đặt phòng này");
            }

            List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();
            List<String> danhSachMaPhongDangSuDung = new ArrayList<>();
            List<String> danhSachMaChiTietDatPhong = new ArrayList<>();

            //Tạo hóa đơn thanh toán
            HoaDon hoaDonThanhToan = createInvoiceFromEntity(reservation);
            hoaDonDAO.createInvoice(hoaDonThanhToan);

            String maChiTietHoaDonMoiNhat = null;
            boolean isCheckOutTre = false;


            for (ThongTinSuDungPhong ct : chiTietSuDungList) {
                Timestamp tgBatDau;
                if (ct.getGioCheckIn().after(ct.getTgNhanPhong()))
                    tgBatDau = ct.getTgNhanPhong();
                else
                    tgBatDau = ct.getGioCheckIn();

                //tính thời gian sử dụng theo giờ
                double thoiGianSuDung = tinhKhoangCachGio(tgBatDau, ct.getTgTraPhong());

                //Lấy giá theo giờ hay theo ngày
                boolean isDatTheoNgay = thoiGianSuDung > 12;
                BigDecimal donGia = loaiPhongService.layGiaTheoLoaiPhong(ct.getMaLoaiPhong(), isDatTheoNgay);


                //Chưa có kiểu kết thúc tức đang sử dung
                if (ct.getKieuKetThuc() == null) {
                    danhSachMaPhongDangSuDung.add(ct.getMaPhong());
                    danhSachMaChiTietDatPhong.add(ct.getMaChiTietDatPhong());
                    Timestamp thoiDiemHienTai = new Timestamp(System.currentTimeMillis());
                    if (thoiDiemHienTai.after(ct.getTgTraPhong())) {
                        isCheckOutTre = true;
                    }
                }
                ChiTietHoaDon chiTietHoaDon = createInvoiceDetailFromEntity(ct, hoaDonThanhToan.getMaHoaDon(), donGia, thoiGianSuDung, maChiTietHoaDonMoiNhat);
                danhSachChiTietHoaDon.add(chiTietHoaDon);
                maChiTietHoaDonMoiNhat = chiTietHoaDon.getMaChiTietHoaDon();
            }

            //Thêm phụ phí check-out trễ nếu có
            if(isCheckOutTre){
                List<PhongTinhPhuPhi> danhSachPhongTinhPhuPhiMoi = createLateCheckOutFee(danhSachMaChiTietDatPhong);
                phongTinhPhuPhiDAO.themDanhSachPhuPhiChoCacPhong(danhSachPhongTinhPhuPhiMoi);
            }

            //Cập nhật ChiTietDatPhong thành trả phòng
            chiTietDatPhongDAO.capNhatKetThucCTDP(danhSachMaChiTietDatPhong);

            //Chèn danh sách chi tiết hóa đơn đã tạo
            chiTietHoaDonDAO.themDanhSachChiTietHoaDon(danhSachChiTietHoaDon);

            //Thêm danh sách ra ngoài lần cuối cùng
            List<LichSuRaNgoai> danhSachLichSuRaNgoaiLanCuoi = taoDanhSachRaNgoaiLanCuoi(danhSachMaChiTietDatPhong);
            lichSuRaNgoaiDAO.themDanhSachLichSuRaNgoai(danhSachLichSuRaNgoaiLanCuoi);

            //tạo các job dọn dẹp cho phòng vừa sử dụng
            List<CongViec> danhSachCongViecMoi = createCleaningJobForRoom(danhSachMaPhongDangSuDung);
            if(danhSachCongViecMoi != null){
                congViecDAO.themDanhSachCongViec(danhSachCongViecMoi);
            }

            //Thêm lịch sử thao tác
            LichSuThaoTac lichSuCheckOut = createWorkingHistory(reservationId, danhSachMaPhongDangSuDung);
            lichSuThaoTacDAO.themLichSuThaoTac(lichSuCheckOut);

        }catch (BusinessException e){
            System.out.println(e.getMessage());
            datPhongDAO.hoanTacGiaoTac();
            System.out.println("Lỗi khi check out");
            return false;
        } catch (Exception e){
            e.printStackTrace();
        }
        datPhongDAO.thucHienGiaoTac();
        return true;
    }

    private LichSuThaoTac createWorkingHistory(String maDonDatPhong, List<String> maPhongSuDung){
        try {
            LichSuThaoTac lichSuThaoTacMoiNhat = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String workingHistoryId = lichSuThaoTacMoiNhat == null ? null : lichSuThaoTacMoiNhat.getMaLichSuThaoTac();

            String listStringRoomId = maPhongSuDung.stream().map(String::toString).collect(Collectors.joining(", "));
            String actionDescription = "Check-out đơn đặt phòng - " + maDonDatPhong + " - Phòng: [" + listStringRoomId + "]";
            return new LichSuThaoTac(
                    EntityUtil.increaseEntityID(workingHistoryId,
                            EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                            EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()),
                    ActionType.CHECKOUT.getActionName(),
                    actionDescription,
                    Main.getCurrentLoginSession(),
                    new Timestamp(System.currentTimeMillis())
            );
        }catch (RuntimeException e){
            throw new BusinessException("Lỗi khi thêm lịch sử thao tác");
        }
    }

    private List<CongViec> createCleaningJobForRoom(List<String> danhSachPhongDangSuDung){
        try {
            Timestamp tgBatDau = Timestamp.valueOf(LocalDateTime.now());
            Timestamp tgKetThuc = Timestamp.valueOf(LocalDateTime.now().plusMinutes(WorkTimeCost.CLEANING_TIME.getMinutes()));
            return congViecService.taoDanhSachCongViec(
                     RoomStatus.ROOM_CLEANING_STATUS.getStatus(),
                     tgBatDau,
                     tgKetThuc,
                     danhSachPhongDangSuDung);
        }catch (BusinessException e){
            System.out.println(e.getMessage());
            throw new BusinessException("Lỗi khi tạo công việc dọn dẹp cho các phòng");
        }
    }


    private List<PhongTinhPhuPhi> createLateCheckOutFee(List<String> maChiTietDatPhong){
        List<PhongTinhPhuPhi> danhSachPhongTinhPhuPhi = new ArrayList<>();
        try {
            var latest = phongTinhPhuPhiDAO.getLatest();
            String maPhongTinhPhuPhiMoiNhat = (latest == null) ? null : latest.getMaPhongTinhPhuPhi();
            ThongTinPhuPhi pp = phuPhiDAO.getThongTinPhuPhiByName(Fee.CHECK_OUT_TRE.status);
            for(String ct : maChiTietDatPhong){
                maPhongTinhPhuPhiMoiNhat = EntityUtil.increaseEntityID(
                        maPhongTinhPhuPhiMoiNhat,
                        EntityIDSymbol.ROOM_FEE.getPrefix(),
                        EntityIDSymbol.ROOM_FEE.getLength());
                danhSachPhongTinhPhuPhi.add(new PhongTinhPhuPhi(maPhongTinhPhuPhiMoiNhat, ct, pp.getMaPhuPhi(), pp.getGiaHienTai()));
            }
        }catch (RuntimeException e){
            System.out.println("Lỗi khi tính phụ phí cho phòng check-out trễ");
            throw new BusinessException("Lỗi khi tạo danh sách phòng tính phụ phí mới");
        }
        return danhSachPhongTinhPhuPhi;
    }


    private double tinhKhoangCachGio(Timestamp tgBatDau, Timestamp tgKetThuc) {
        if (tgBatDau == null || tgKetThuc == null) return 0;
        long endMillis = tgKetThuc.getTime();
        long startMillis = tgBatDau.getTime();
        return (endMillis - startMillis) / (60.0 * 60 * 1000);
    }

    private String taoMaHoaDonMoi() {
        var latest = hoaDonDAO.timHoaDonMoiNhat();
        String maHD = (latest == null) ? null : latest.getMaHoaDon();
        return EntityUtil.increaseEntityID(
                maHD,
                EntityIDSymbol.INVOICE_PREFIX.getPrefix(),
                EntityIDSymbol.INVOICE_PREFIX.getLength()
        );
    }
    private HoaDon createInvoiceFromEntity(DonDatPhong ddp) {
        return new HoaDon(taoMaHoaDonMoi(),
                InvoiceType.THANH_TOAN.getStatus(),
                Main.getCurrentLoginSession(),
                ddp.getMaDonDatPhong(),
                ddp.getMaKhachHang()
                );
    }

    private List<LichSuRaNgoai> taoDanhSachRaNgoaiLanCuoi(List<String> danhSachMaChiTietDatPhong) {
        List<LichSuRaNgoai> historyCheckOuts = null;
        try {
            historyCheckOuts = new ArrayList<>();
            LichSuRaNgoai lichSuRaNgoaiMoiNhat = lichSuRaNgoaiDAO.timLichSuRaNgoaiMoiNhat();
            String maLichSuRaNgoaiMoiNhat = lichSuRaNgoaiMoiNhat == null ? null : lichSuRaNgoaiMoiNhat.getMaLichSuRaNgoai();

            for (String ct : danhSachMaChiTietDatPhong) {
                LichSuRaNgoai lichSuRaNgoai = createHistoryCheckOutEntity(maLichSuRaNgoaiMoiNhat, ct, true);
                maLichSuRaNgoaiMoiNhat = lichSuRaNgoai.getMaLichSuRaNgoai();
                historyCheckOuts.add(lichSuRaNgoai);
            }
        } catch (RuntimeException e) {
            throw new BusinessException("Lỗi khi tạo danh sách ra ngoài lần cuối");
        }
        return historyCheckOuts;
    }

    private LichSuRaNgoai createHistoryCheckOutEntity(String maLichSuRaNgoaiMoiNhat, String maChiTietDatPhong, boolean isFinal) {
        String id;
        String prefix = EntityIDSymbol.HISTORY_CHECKOUT_PREFIX.getPrefix();

        int numberLength = EntityIDSymbol.HISTORY_CHECKOUT_PREFIX.getLength();
        if (maLichSuRaNgoaiMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(maLichSuRaNgoaiMoiNhat, prefix, numberLength);
        }

        return new LichSuRaNgoai(
                id,
                isFinal,
                maChiTietDatPhong,
                null
        );
    }


    private ChiTietHoaDon createInvoiceDetailFromEntity(ThongTinSuDungPhong thongTinSuDungPhong,String maHoaDon,BigDecimal donGia, double thoiGianSuDung, String maChiTietHoaDonMoiNhat){
        return new ChiTietHoaDon(
                taoMaChiTietHoaDon(maChiTietHoaDonMoiNhat),
                maHoaDon,
                thongTinSuDungPhong.getMaPhong(),
                thongTinSuDungPhong.getMaChiTietDatPhong(),
                donGia,
                thoiGianSuDung
        );
    }

    private String taoMaChiTietHoaDon(String maChiTietHoaDonMoiNhat){
        if(maChiTietHoaDonMoiNhat == null){
            var latest = chiTietHoaDonDAO.layChiTietHoaDonMoiNhat();
            if(latest != null){
                maChiTietHoaDonMoiNhat = latest.getMaChiTietHoaDon();
            }
        }
        return EntityUtil.increaseEntityID(
                maChiTietHoaDonMoiNhat,
                EntityIDSymbol.INVOICE_DETAIL_PREFIX.getPrefix(),
                EntityIDSymbol.INVOICE_DETAIL_PREFIX.getLength()
        );
    }

    public boolean checkOutByReservationDetail(String reservationDetail){
        String maDonDatPhong = chiTietDatPhongDAO.findFormIDByDetail(reservationDetail);
        System.out.println("trả phòng cho " + maDonDatPhong);
        return this.checkOutReservation(maDonDatPhong);
    }

    public boolean createHoaDonForAutoCheckout(String reservationDetail){
        String maDonDatPhong = chiTietDatPhongDAO.findFormIDByDetail(reservationDetail);
        var reservation = validateDonDatPhong(maDonDatPhong);

//        Lấy tất cả chi tiết thông tin sử dụng phòng của đơn đặt phòng
        List<ThongTinSuDungPhong> chiTietSuDungList = filterUsageRoom(maDonDatPhong);
        if(chiTietSuDungList.isEmpty()){
            throw new BusinessException("Không tìm thấy thông tin sử dụng của đơn đặt phòng này");
        }

        List<ChiTietHoaDon> danhSachChiTietHoaDon = new ArrayList<>();
        List<String> danhSachMaPhongDangSuDung = new ArrayList<>();
        List<String> danhSachMaChiTietDatPhong = new ArrayList<>();

        try {
            datPhongDAO.khoiTaoGiaoTac();

            //Tạo hóa đơn thanh toán
            HoaDon hoaDonThanhToan = createInvoiceFromEntity(reservation);
            hoaDonDAO.createInvoice(hoaDonThanhToan);

            String maChiTietHoaDonMoiNhat = null;
            boolean isCheckOutTre = false;


            for (ThongTinSuDungPhong ct : chiTietSuDungList) {
                Timestamp tgBatDau;
                if (ct.getGioCheckIn().after(ct.getTgNhanPhong()))
                    tgBatDau = ct.getTgNhanPhong();
                else
                    tgBatDau = ct.getGioCheckIn();

                //tính thời gian sử dụng theo giờ
                double thoiGianSuDung = tinhKhoangCachGio(tgBatDau, ct.getTgTraPhong());

                //Lấy giá theo giờ hay theo ngày
                boolean isDatTheoNgay = thoiGianSuDung > 12;
                BigDecimal donGia = loaiPhongService.layGiaTheoLoaiPhong(ct.getMaLoaiPhong(), isDatTheoNgay);


                //Chưa có kiểu kết thúc tức đang sử dung
                if (ct.getKieuKetThuc() == null) {
                    danhSachMaPhongDangSuDung.add(ct.getMaPhong());
                    danhSachMaChiTietDatPhong.add(ct.getMaChiTietDatPhong());
                    Timestamp thoiDiemHienTai = new Timestamp(System.currentTimeMillis());
                    if (thoiDiemHienTai.after(ct.getTgTraPhong())) {
                        isCheckOutTre = true;
                    }
                }
                ChiTietHoaDon chiTietHoaDon = createInvoiceDetailFromEntity(ct, hoaDonThanhToan.getMaHoaDon(), donGia, thoiGianSuDung, maChiTietHoaDonMoiNhat);
                danhSachChiTietHoaDon.add(chiTietHoaDon);
                maChiTietHoaDonMoiNhat = chiTietHoaDon.getMaChiTietHoaDon();
            }

            //Thêm phụ phí check-out trễ nếu có
            if (isCheckOutTre) {
                List<PhongTinhPhuPhi> danhSachPhongTinhPhuPhiMoi = createLateCheckOutFee(danhSachMaChiTietDatPhong);
                phongTinhPhuPhiDAO.themDanhSachPhuPhiChoCacPhong(danhSachPhongTinhPhuPhiMoi);
            }

            //Cập nhật ChiTietDatPhong thành trả phòng
            chiTietDatPhongDAO.capNhatKetThucCTDP(danhSachMaChiTietDatPhong);

            //Chèn danh sách chi tiết hóa đơn đã tạo
            chiTietHoaDonDAO.themDanhSachChiTietHoaDon(danhSachChiTietHoaDon);
        }catch (BusinessException e){
            System.out.println("Lỗi khi tạo hóa đơn");
            datPhongDAO.hoanTacGiaoTac();
        }
        datPhongDAO.thucHienGiaoTac();
        return true;
    }
}

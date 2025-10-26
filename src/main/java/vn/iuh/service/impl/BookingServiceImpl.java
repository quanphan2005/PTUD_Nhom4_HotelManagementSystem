package vn.iuh.service.impl;

import vn.iuh.constraint.*;
import vn.iuh.dao.*;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.*;
import vn.iuh.dto.response.*;
import vn.iuh.entity.*;
import vn.iuh.service.BookingService;
import vn.iuh.util.EntityUtil;
import vn.iuh.util.TimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

public class BookingServiceImpl implements BookingService {
    private final DatPhongDAO datPhongDAO;
    private final DonGoiDichVuDao donGoiDichVuDao;
    private final KhachHangDAO khachHangDAO;
    private final LichSuDiVaoDAO lichSuDiVaoDAO;
    private final LichSuRaNgoaiDAO lichSuRaNgoaiDAO;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
    private final CongViecDAO congViecDAO;
    private final HoaDonDAO hoaDonDAO;

    public BookingServiceImpl() {
        this.datPhongDAO = new DatPhongDAO();
        this.donGoiDichVuDao = new DonGoiDichVuDao();
        this.khachHangDAO = new KhachHangDAO();
        this.lichSuDiVaoDAO = new LichSuDiVaoDAO();
        this.lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO();
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
        this.congViecDAO = new CongViecDAO();
        this.hoaDonDAO = new HoaDonDAO();
    }

    @Override
    public EventResponse createBooking(BookingCreationEvent bookingCreationEvent) {

        // 1. find Customer by CCCD
        KhachHang khachHang = khachHangDAO.timKhachHangBangCCCD(bookingCreationEvent.getCCCD());

        // 1.1 Create Customer if not exist
        if (Objects.isNull(khachHang)) {
            KhachHang kh = khachHangDAO.timKhachHangMoiNhat();
            String maKH = kh == null ? null : kh.getMaKhachHang();

            khachHangDAO.themKhachHang(new KhachHang(
                    EntityUtil.increaseEntityID(maKH,
                                                EntityIDSymbol.CUSTOMER_PREFIX.getPrefix(),
                                                EntityIDSymbol.CUSTOMER_PREFIX.getLength()),
                    bookingCreationEvent.getCCCD(),
                    bookingCreationEvent.getTenKhachHang(),
                    bookingCreationEvent.getSoDienThoai(),
                    null
            ));
            khachHang = khachHangDAO.timKhachHangBangCCCD(bookingCreationEvent.getCCCD());
            System.out.println("khachHang: " + khachHang);
        } else {
            // If customer already exists, check Name and Phone number match
            if (!Objects.equals(khachHang.getTenKhachHang(), bookingCreationEvent.getTenKhachHang())
                || !Objects.equals(khachHang.getSoDienThoai(), bookingCreationEvent.getSoDienThoai())
            ) {
                String message = "Đặt phòng thất bại! Thông tin khách hàng không khớp với CCCD đã có trong hệ thống.\n"
                                 + "Vui lòng kiểm tra lại tên và số điện thoại.";
                return new EventResponse(ResponseType.ERROR, message);
            }
        }

        List<ThongTinDatPhong> danhSachThongTinDatPhong = datPhongDAO.timThongTinDatPhongTrongKhoang(
                bookingCreationEvent.getTgNhanPhong(),
                bookingCreationEvent.getTgTraPhong(),
                bookingCreationEvent.getDanhSachMaPhong()
        );

        // 1.2 If any room is already booked in the given time range, return false
        if (!danhSachThongTinDatPhong.isEmpty()) {
            System.out.println("Có phòng đã được đặt trong khoảng thời gian này: ");
            for (ThongTinDatPhong thongTinDatPhong : danhSachThongTinDatPhong) {
                System.out.println(thongTinDatPhong);
            }
            StringBuilder message =
                    new StringBuilder("Đặt phòng thất bại! Có phòng đã được đặt trong khoảng thời gian này: \n ");
            for (ThongTinDatPhong thongTinDatPhong : danhSachThongTinDatPhong) {
                message.append("- Phòng: ")
                       .append(thongTinDatPhong.getMaPhong())
                       .append(" đã được đặt bởi khách hàng: ")
                       .append(thongTinDatPhong.getTenKhachHang())
                       .append(" từ: ")
                       .append(TimeFormat.formatTime(thongTinDatPhong.getTgNhanPhong()))
                       .append(" đến: ")
                       .append(TimeFormat.formatTime(thongTinDatPhong.getTgTraPhong()))
                       .append("\n");
            }
            return new EventResponse(ResponseType.ERROR, message.toString());
        }

        try {
            datPhongDAO.khoiTaoGiaoTac();

            // 2.1. Create ReservationFormEntity & insert to DB
            DonDatPhong donDatPhongMoiNhat = datPhongDAO.timDonDatPhongMoiNhat();
            DonDatPhong donDatPhong = createReservationFormEntity(bookingCreationEvent,
                                                                  donDatPhongMoiNhat.getMaDonDatPhong(),
                                                                  khachHang.getMaKhachHang());
            datPhongDAO.themDonDatPhong(donDatPhong);

            // 2.1.1 Create Invoice Advance Payment
            if (bookingCreationEvent.isDaDatTruoc()) {
                HoaDon hoaDonMoiNhat = hoaDonDAO.timHoaDonMoiNhat();
                String maHoaDonMoiNhat = hoaDonMoiNhat == null ? null : hoaDonMoiNhat.getMaHoaDon();

                HoaDon hoaDonDatCoc = createInvoiceEntity(
                        EntityUtil.increaseEntityID(maHoaDonMoiNhat,
                                                    EntityIDSymbol.INVOICE_PREFIX.getPrefix(),
                                                    EntityIDSymbol.INVOICE_PREFIX.getLength()),
                        donDatPhong,
                        bookingCreationEvent.getPhuongThucThanhToan()
                );

                hoaDonDAO.createInvoice(hoaDonDatCoc);
            }

            // 2.2. Create RoomReservationDetail Entity & insert to DB
            List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();
            ChiTietDatPhong chiTietDatPhongMoiNhat = datPhongDAO.timChiTietDatPhongMoiNhat();
            String maChiTietDatPhongMoiNhat = chiTietDatPhongMoiNhat == null
                    ? null : chiTietDatPhongMoiNhat.getMaChiTietDatPhong();

            for (String roomId : bookingCreationEvent.getDanhSachMaPhong()) {
                ChiTietDatPhong chiTietDatPhong = createRoomReservationDetailEntity(bookingCreationEvent,
                                                                                    maChiTietDatPhongMoiNhat,
                                                                                    roomId,
                                                                                    donDatPhong.getMaDonDatPhong());
                chiTietDatPhongs.add(chiTietDatPhong);
                maChiTietDatPhongMoiNhat = chiTietDatPhong.getMaChiTietDatPhong();
            }
            datPhongDAO.themChiTietDatPhong(donDatPhong, chiTietDatPhongs);

            // 2.3. Create HistoryCheckInEntity & insert to DB
            if (!bookingCreationEvent.isDaDatTruoc()) {
                List<LichSuDiVao> historyCheckIns = new ArrayList<>();
                LichSuDiVao lichSuDiVaoMoiNhat = lichSuDiVaoDAO.timLichSuDiVaoMoiNhat();
                String maLichSuDiVaoMoiNhat = lichSuDiVaoMoiNhat == null ? null : lichSuDiVaoMoiNhat.getMaLichSuDiVao();

                for (ChiTietDatPhong chiTietDatPhong : chiTietDatPhongs) {
                    LichSuDiVao lichSuDiVao =
                            createHistoryCheckInEntity(maLichSuDiVaoMoiNhat, chiTietDatPhong.getMaChiTietDatPhong());

                    historyCheckIns.add(lichSuDiVao);
                    maLichSuDiVaoMoiNhat = lichSuDiVao.getMaLichSuDiVao();
                }

                datPhongDAO.themLichSuDiVao(historyCheckIns);
            }

            // 2.4 Update Service Quantity
            for (DonGoiDichVu dichVu : bookingCreationEvent.getDanhSachDichVu()) {
                donGoiDichVuDao.capNhatSoLuongTonKhoDichVu(dichVu.getMaDichVu(), -dichVu.getSoLuong());
            }

            // 2.5. Create RoomUsageServiceEntity & insert to DB
            List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();
            PhongDungDichVu phongDungDichVuMoiNhat = donGoiDichVuDao.timPhongDungDichVuMoiNhat();
            String maPhongDungDichVuMoiNhat =
                    phongDungDichVuMoiNhat == null ? null : phongDungDichVuMoiNhat.getMaPhongDungDichVu();

            for (DonGoiDichVu dichVu : bookingCreationEvent.getDanhSachDichVu()) {

                // If booking multiple rooms, divide service equally to each booked room
                dichVu.setSoLuong(dichVu.getSoLuong() / bookingCreationEvent.getDanhSachMaPhong().size());

                for (ChiTietDatPhong chiTietDatPhong : chiTietDatPhongs) {
                    phongDungDichVuMoiNhat =
                            createRoomUsageServiceEntity(bookingCreationEvent,
                                                         maPhongDungDichVuMoiNhat,
                                                         chiTietDatPhong.getMaChiTietDatPhong(),
                                                         dichVu);
                    maPhongDungDichVuMoiNhat = phongDungDichVuMoiNhat.getMaPhongDungDichVu();
                    danhSachPhongDungDichVu.add(phongDungDichVuMoiNhat);
                }
            }

            donGoiDichVuDao.themPhongDungDichVu(danhSachPhongDungDichVu);

            // 2.6. Create Job for each booked room
            List<CongViec> congViecs = new ArrayList<>();
            CongViec congViec = congViecDAO.timCongViecMoiNhat();
            String jobId = congViec == null ? null : congViec.getMaCongViec();

            for (String roomId : bookingCreationEvent.getDanhSachMaPhong()) {
                String newId = EntityUtil.increaseEntityID(jobId,
                                                           EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                                           EntityIDSymbol.JOB_PREFIX.getLength());
                int thoiGianKiemTra = 30; //phút
                if (bookingCreationEvent.isDaDatTruoc()) {
                    congViecs.add(new CongViec(newId, RoomStatus.ROOM_BOOKED_STATUS.getStatus(),
                                               bookingCreationEvent.getTgNhanPhong(),
                                               bookingCreationEvent.getTgTraPhong(),
                                               roomId, null));
                } else {
                    congViecs.add(new CongViec(newId, RoomStatus.ROOM_CHECKING_STATUS.getStatus(),
                                               bookingCreationEvent.getTgNhanPhong(), new Timestamp(
                            bookingCreationEvent.getTgNhanPhong().getTime() + thoiGianKiemTra * 60 * 1000),
                                               roomId, null));
                }
                jobId = newId;
            }

            congViecDAO.themDanhSachCongViec(congViecs);

            // 2.7. Update WorkingHistory
            LichSuThaoTac lichSuThaoTacMoiNhat = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String workingHistoryId = lichSuThaoTacMoiNhat == null ? null : lichSuThaoTacMoiNhat.getMaLichSuThaoTac();

            String actionDescription = "Đặt phòng cho khách hàng " + bookingCreationEvent.getTenKhachHang()
                                       + " - CCCD: " + bookingCreationEvent.getCCCD() + " - Phòng: " +
                                       bookingCreationEvent.getDanhSachMaPhong().toString();
            lichSuThaoTacDAO.themLichSuThaoTac(new LichSuThaoTac(
                    EntityUtil.increaseEntityID(workingHistoryId,
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()),
                    bookingCreationEvent.isDaDatTruoc() ? ActionType.PRE_BOOKING.getActionName()
                            : ActionType.BOOKING.getActionName(),
                    actionDescription,
                    bookingCreationEvent.getMaPhienDangNhap(),
                    new Timestamp(System.currentTimeMillis())
            ));

        } catch (Exception e) {
            System.out.println("Lỗi khi đặt phòng: " + e.getMessage());
            System.out.println("Rollback transaction");
            e.printStackTrace();
            datPhongDAO.hoanTacGiaoTac();
            return new EventResponse(ResponseType.ERROR, "Đặt phòng thất bại! Vui lòng thử lại sau.");
        }

        datPhongDAO.thucHienGiaoTac();
        System.out.println("Đặt phòng: " + bookingCreationEvent.getDanhSachMaPhong().toString() +
                           " cho khách hàng: " + bookingCreationEvent.getTenKhachHang()
                           + " thành công!");
        String message = "Đặt phòng thành công!";
        if (bookingCreationEvent.getDanhSachMaPhong().size() > 1) {
            message += " Các phòng đã được đặt:\n";
            for (String roomId : bookingCreationEvent.getDanhSachMaPhong()) {
                message += "- Phòng " + roomId + "\n";
            }
        }
        if (bookingCreationEvent.isDaDatTruoc()) {
            message += " Khách hàng đã đặt phòng trước. Vui lòng hoàn tất thủ tục check-in khi đến nhận phòng.";
        } else {
            message += " Khách hàng sẽ tiến hành check-in ngay bây giờ.";
        }
        return new EventResponse(ResponseType.SUCCESS, message);
    }

    @Override
    public List<PreReservationResponse> getAllReservationForms() {
        System.out.println("Fetching all reservation forms...");
        List<PhieuDatPhong> danhSachPhieuDatPhong = datPhongDAO.timTatCaPhieuDatPhong();

        List<PreReservationResponse> preReservationRespons = new ArrayList<>();
        for (PhieuDatPhong phieuDatPhong : danhSachPhieuDatPhong) {
            preReservationRespons.add(new PreReservationResponse(
                    phieuDatPhong.getTenKhachHang(),
                    phieuDatPhong.getMaDonDatPhong(),
                    phieuDatPhong.getMaPhong(),
                    phieuDatPhong.getTenPhong(),
                    phieuDatPhong.getTgNhanPhong(),
                    phieuDatPhong.getTgTraPhong()
            ));
        }

        return preReservationRespons;
    }

    @Override
    public List<PreReservationResponse> getReseravtionFormByRoomId(String id) {
        System.out.println("Fetching reservation forms for room ID: " + id);
        List<PhieuDatPhong> danhSachPhieuDatPhong = datPhongDAO.timThongTinDatPhongBangMaPhong(id);

        List<PreReservationResponse> preReservationRespons = new ArrayList<>();
        for (PhieuDatPhong phieuDatPhong : danhSachPhieuDatPhong) {
            preReservationRespons.add(new PreReservationResponse(
                    phieuDatPhong.getTenKhachHang(),
                    phieuDatPhong.getMaDonDatPhong(),
                    phieuDatPhong.getMaPhong(),
                    phieuDatPhong.getTenPhong(),
                    phieuDatPhong.getTgNhanPhong(),
                    phieuDatPhong.getTgTraPhong()
            ));
        }

        return preReservationRespons;
    }

    @Override
    public List<BookingResponse> getAllBookingInfo() {
        // 1.Get All Room Info
        List<ThongTinPhong> thongTinPhongs = datPhongDAO.timTatCaThongTinPhong();

        // 2. Get All non-available Room Ids
        List<String> nonAvailableRoomIds = new ArrayList<>();

        // 3. Create BookingResponse each Room info
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (ThongTinPhong thongTinPhong : thongTinPhongs) {
            // Set default Room Status if null or empty
            if (Objects.isNull(thongTinPhong.getTenTrangThai()) || thongTinPhong.getTenTrangThai().isEmpty()) {
                thongTinPhong.setTenTrangThai(thongTinPhong.isDangHoatDong()
                                                      ? RoomStatus.ROOM_EMPTY_STATUS.getStatus()
                                                      : RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus()
                );
            }

            if (Objects.equals(thongTinPhong.getTenTrangThai(), RoomStatus.ROOM_BOOKED_STATUS.getStatus()) ||
                Objects.equals(thongTinPhong.getTenTrangThai(), RoomStatus.ROOM_CHECKING_STATUS.getStatus()) ||
                Objects.equals(thongTinPhong.getTenTrangThai(), RoomStatus.ROOM_USING_STATUS.getStatus()) ||
                Objects.equals(thongTinPhong.getTenTrangThai(), RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus())
            ) {

                nonAvailableRoomIds.add(thongTinPhong.getMaPhong());
            }

            bookingResponses.add(createBookingResponse(thongTinPhong));
        }

        // 4. Find all Booking Info for non-available rooms
        List<ThongTinDatPhong> thongTinDatPhongs =
                datPhongDAO.timTatCaThongTinDatPhongTheoDanhSachMaPhong(nonAvailableRoomIds);

        // 5. Update BookingResponse with Booking Info
        for (ThongTinDatPhong thongTinDatPhong : thongTinDatPhongs) {
            for (BookingResponse bookingResponse : bookingResponses) {
                if (Objects.equals(bookingResponse.getRoomId(), thongTinDatPhong.getMaPhong())) {
                    bookingResponse.updateBookingInfo(
                            thongTinDatPhong.getTenKhachHang(),
                            thongTinDatPhong.getMaChiTietDatPhong(),
                            thongTinDatPhong.getTgNhanPhong(),
                            thongTinDatPhong.getTgTraPhong()
                    );
                }
            }
        }
        return bookingResponses;
    }

    @Override
    public List<BookingResponse> getAllEmptyRoomInRange(Timestamp timeIn, Timestamp timeOut) {
        // 1.Get All Room Info
        List<ThongTinPhong> danhSachPhongTrong = datPhongDAO.timTatCaPhongTrongKhoangThoiGian(timeIn, timeOut);

        // 2. Create BookingResponse each Room info
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (ThongTinPhong phongTrong : danhSachPhongTrong) {
            bookingResponses.add(createBookingResponse(phongTrong));
        }

        return bookingResponses;
    }

    @Override
    public List<String> getAllNonEmptyRoomInRange(Timestamp timeIn, Timestamp timeOut) {
        return datPhongDAO.timTatCaPhongKhongKhaDungTrongKhoang(timeIn, timeOut);
    }

    @Override
    public CustomerInfoResponse getCustomerInfoByBookingId(String maChiTietDatPhong) {
        CustomerInfo customerInfo = datPhongDAO.timThongTinKhachHangBangMaChiTietDatPhong(maChiTietDatPhong);
        if (Objects.isNull(customerInfo)) {
            System.out.println("Không tìm thấy thông tin khách hàng cho mã chi tiết đặt phòng: " + maChiTietDatPhong);
            return null;
        }

        return new CustomerInfoResponse(
                customerInfo.getMaKhachHang(),
                customerInfo.getCCCD(),
                customerInfo.getTenKhachHang(),
                customerInfo.getSoDienThoai()
        );
    }

    public boolean cancelReservation(String maDatPhong) {
        // 1. Find ReservationForm by id
        DonDatPhong donDatPhong = datPhongDAO.timDonDatPhong(maDatPhong);
        if (Objects.isNull(donDatPhong)) {
            System.out.println("Không tìm thấy đơn đặt phòng, mã: " + maDatPhong);
            return false;
        }

        // 2. Find all RoomReservationDetail by ReservationForm id
        List<PhongDungDichVu> danhSachPhongDungDichVu = donGoiDichVuDao.timDonGoiDichVuBangMaDatPhong(maDatPhong);
        try {
            datPhongDAO.khoiTaoGiaoTac();
            for (PhongDungDichVu phongDungDichVu : danhSachPhongDungDichVu) {
                // 3. Update Service Quantity
                donGoiDichVuDao.capNhatSoLuongTonKhoDichVu(phongDungDichVu.getMaDichVu(), phongDungDichVu.getSoLuong());
            }

            // 4. Delete RoomUsageService by ReservationForm id
            List<String> dvIDs = danhSachPhongDungDichVu.stream().map(PhongDungDichVu::getMaPhongDungDichVu).toList();
            donGoiDichVuDao.huyDanhSachPhongDungDichVu(dvIDs);

            // 5. Delete all reservavtionDetail
            List<ChiTietDatPhong> danhSachChiTietDatPhong = datPhongDAO.timChiTietDatPhongBangMaDatPhong(maDatPhong);
            List<String> danhSachChiTietMaDatPhong =
                    danhSachChiTietDatPhong.stream().map(ChiTietDatPhong::getMaChiTietDatPhong).toList();
            datPhongDAO.huyDanhSachChiTietDatPhong(danhSachChiTietMaDatPhong);

            // 6. Delete ReservationForm
            datPhongDAO.huyDonDatPhong(maDatPhong);

            // 7. Update WorkingHistory
            LichSuThaoTac lichSuThaoTacMoiNhat = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String workingHistoryId = lichSuThaoTacMoiNhat == null ? null : lichSuThaoTacMoiNhat.getMaLichSuThaoTac();

            String actionDescription = "Hủy đặt phòng cho khách hàng " + donDatPhong.getMaKhachHang()
                                       + " - Mã đặt phòng: " + donDatPhong.getMaDonDatPhong();
            lichSuThaoTacDAO.themLichSuThaoTac(new LichSuThaoTac(
                    EntityUtil.increaseEntityID(workingHistoryId,
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()),
                    ActionType.CANCEL_RESERVATION.getActionName(),
                    actionDescription,
                    donDatPhong.getMaPhienDangNhap(),
                    null
            ));

            // 8. Remove related Jobs
            // 8.1 Find related Jobs ()
            List<String> danhSachMaPhong = danhSachChiTietDatPhong.stream().map(ChiTietDatPhong::getMaPhong).toList();
            List<CongViec> danhSachCongViec = congViecDAO.layCongViecHienTaiCuaCacPhong(danhSachMaPhong);

            // 8.2 Prepare list of Job IDs need to be deleted
            List<String> danhSachMaCongViecCanXoa = new ArrayList<>();
            for (CongViec congViec : danhSachCongViec) {
                for (ChiTietDatPhong chiTietDatPhong : danhSachChiTietDatPhong) {
                    if (Objects.equals(congViec.getMaPhong(), chiTietDatPhong.getMaPhong())
                        && Objects.equals(congViec.getTgBatDau(), chiTietDatPhong.getTgNhanPhong())
                        && Objects.equals(congViec.getTgKetThuc(), chiTietDatPhong.getTgTraPhong())
                    ) {
                        danhSachMaCongViecCanXoa.add(congViec.getMaCongViec());
                    }
                }
            }

            // 8.3 Delete related Jobs
            congViecDAO.xoaDanhSachCongViec(danhSachMaCongViecCanXoa);

            datPhongDAO.thucHienGiaoTac();
            System.out.println("Hủy đặt phòng thành công, mã: " + maDatPhong + "thành công!");
            return true;

        } catch (Exception e) {
            System.out.println("Lỗi khi hủy đặt phòng, mã: " + maDatPhong + " " + e.getMessage());
            System.out.println("Rollback transaction");
            e.printStackTrace();
            datPhongDAO.hoanTacGiaoTac();
            return false;
        }
    }

    @Override
    public boolean cancelRoomReservation(String maDatPhong, String maPhong) {
        // 1. Find ReservationForm by id
        DonDatPhong donDatPhong = datPhongDAO.timDonDatPhong(maDatPhong);
        if (Objects.isNull(donDatPhong)) {
            System.out.println("Không tìm thấy đơn đặt phòng, mã: " + maDatPhong);
            return false;
        }

        // 2. Find reservationDetail by ReservationForm id and Room id
        ChiTietDatPhong chiTietDatPhong = datPhongDAO.timChiTietDatPhongBangMaDatPhong(maDatPhong, maPhong);
        if (Objects.isNull(chiTietDatPhong)) {
            System.out.println(
                    "Không tìm thấy chi tiết đặt phòng cho mã đặt phòng: " + maDatPhong + " và mã phòng: " + maPhong);
            return false;
        }

        // 2.1 Check weather ReservationDetail has been checked-in
        List<LichSuDiVao> lichSuDiVao =
                lichSuDiVaoDAO.timLichSuDiVaoBangMaChiTietDatPhong(chiTietDatPhong.getMaChiTietDatPhong());
        for (LichSuDiVao ls : lichSuDiVao) {
            if (ls.getLaLanDauTien()) {
                System.out.println("Không thể hủy đặt phòng cho mã đặt phòng: " + maDatPhong + " và mã phòng: " + maPhong +
                                   " vì đã thực hiện check-in.");
                return false;
            }
        }

        try {
            datPhongDAO.khoiTaoGiaoTac();

            // 3. Delete specific reservavtionDetail
            datPhongDAO.huyChiTietDatPhong(chiTietDatPhong.getMaChiTietDatPhong());

            // 4. Delete ReservationForm if no more reservationDetail
            List<ChiTietDatPhong> danhSachChiTietDatPhong =
                    datPhongDAO.timTatCaChiTietDatPhongBangMaDatPhong(maDatPhong);
            if (danhSachChiTietDatPhong.isEmpty()) {
                datPhongDAO.huyDonDatPhong(maDatPhong);
            } else
                if (danhSachChiTietDatPhong.size() == 1) {
                    ChiTietDatPhong chiTietDatPhongCuoiCung = danhSachChiTietDatPhong.get(0);
                    if (Objects.equals(chiTietDatPhongCuoiCung.getMaChiTietDatPhong(),
                                       chiTietDatPhong.getMaChiTietDatPhong()))
                        datPhongDAO.huyDonDatPhong(maDatPhong);
                }

            // 5. Handle RoomUsageService
            List<PhongDungDichVu> danhSachPhongDungDichVu =
                    donGoiDichVuDao.timDonGoiDichVuBangChiTietDatPhong(chiTietDatPhong.getMaChiTietDatPhong());
            if (!danhSachPhongDungDichVu.isEmpty()) {
                for (PhongDungDichVu phongDungDichVu : danhSachPhongDungDichVu)
//                  // 4.1 Update Service Quantity if any
                    donGoiDichVuDao.capNhatSoLuongTonKhoDichVu(phongDungDichVu.getMaDichVu(),
                                                               phongDungDichVu.getSoLuong());

                // 4.2 Delete RoomUsageService by ReservationDetail id
                List<String> danhSachMaDonGoiDichVu =
                        danhSachPhongDungDichVu.stream().map(PhongDungDichVu::getMaPhongDungDichVu).toList();
                donGoiDichVuDao.huyDanhSachPhongDungDichVu(danhSachMaDonGoiDichVu);
            }

            // 6. Update WorkingHistory
            LichSuThaoTac lichSuThaoTacMoiNhat = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String workingHistoryId = lichSuThaoTacMoiNhat == null ? null :
                    lichSuThaoTacMoiNhat.getMaLichSuThaoTac();
            String actionDescription =
                    "Hủy đơn đặt tại phòng " + maPhong + " cho khách hàng " + donDatPhong.getMaKhachHang()
                    + " - Mã đặt phòng: " + donDatPhong.getMaDonDatPhong();
            lichSuThaoTacDAO.themLichSuThaoTac(new LichSuThaoTac(
                    EntityUtil.increaseEntityID(workingHistoryId,
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()),
                    ActionType.CANCEL_RESERVATION.getActionName(),
                    actionDescription,
                    donDatPhong.getMaPhienDangNhap(),
                    null
            ));

            // 7. Remove room`s job
            congViecDAO.xoaCongViecChoCheckIn(chiTietDatPhong.getMaChiTietDatPhong());

            datPhongDAO.thucHienGiaoTac();
            System.out.println("Hủy đặt phòng tại phòng " + maPhong + ", mã: " + maDatPhong + "thành công!");
            return true;

        } catch (Exception e) {
            System.out.println("Lỗi khi hủy đặt phòng, mã: " + maDatPhong + " " + e.getMessage());
            System.out.println("Rollback transaction");
            e.printStackTrace();
            datPhongDAO.hoanTacGiaoTac();
            return false;
        }
    }

    @Override
    public List<ReservationResponse> getAllCurrentReservationsWithStatus() {
        Set<ReservationResponse> responses = new HashSet<>();

        // 1. Get all current reservation
        List<DonDatPhong> currentReservation = datPhongDAO.getAllCurrentReservation();
        for (DonDatPhong donDatPhong : currentReservation) {
            ReservationResponse response = new ReservationResponse();
            response.setMaKhachHang(donDatPhong.getMaKhachHang());
            response.setMaDonDatPhong(donDatPhong.getMaDonDatPhong());
            response.setType(donDatPhong.getLoai());
            response.setTimeIn(donDatPhong.getTgNhanPhong());
            response.setTimeOut(donDatPhong.getTgTraPhong());
            response.setDeleted(donDatPhong.isDaXoa());
            responses.add(response);
        }

        List<String> currentReservationIds = new ArrayList<>();
        for (DonDatPhong donDatPhong : currentReservation)
            currentReservationIds.add(donDatPhong.getMaDonDatPhong());

        // 2. Get all customer info for current reservation IDs
        List<KhachHang> customers =
                datPhongDAO.getCustomerInfoByReservationIds(currentReservationIds);
        for (ReservationResponse response : responses) {
            for (KhachHang customer : customers) {
                if (Objects.equals(response.getMaKhachHang(), customer.getMaKhachHang())) {
                    response.setCCCD(customer.getTenKhachHang());
                    response.setCustomerName(customer.getTenKhachHang());
                }
            }
        }

        // 3. Get reservation status for reservation IDs
        List<ReservationStatusRepository> allCurrentReservationsWithStatus =
                datPhongDAO.getAllCurrentReservationsWithStatus(currentReservationIds);
        for (ReservationResponse response : responses) {
            for (ReservationStatusRepository statusRepository : allCurrentReservationsWithStatus) {
                if (Objects.equals(response.getMaDonDatPhong(), statusRepository.getMaDonDatPhong())) {
                    response.setStatus(getReservationStatus(statusRepository));
                }
            }
        }

        return new ArrayList<>(responses);
    }

    private String getReservationStatus(ReservationStatusRepository statusRepository) {
        if (statusRepository.isCheckin() == null || !statusRepository.isCheckin()) {
            return ReservationStatus.CHECKED_IN.getStatus();
        } else if (statusRepository.getCheckinDate().getTime() <= new Date().getTime()
                   && statusRepository.getCheckinDate().getTime() >= new Date().getTime() - 30 * 60 * 1000) {
            return ReservationStatus.CHECKING.getStatus();
        } else if (statusRepository.getCheckoutTime().after(new Date())) {
            return ReservationStatus.USING.getStatus();
        } else if (statusRepository.getCheckoutTime().before(new Date())) {
            return ReservationStatus.CHECKOUT_LATE.getStatus();
        }

        return "UNKNOWN";
    }

    @Override
    public List<ReservationResponse> getAllPastReservationsWithStatusInRange(Timestamp startDate, Timestamp endDate) {
        List<ReservationResponse> allPassReservationsWithStatusInRange =
                datPhongDAO.getAllPassReservationsWithStatusInRange(startDate, endDate);
        for (ReservationResponse reservation : allPassReservationsWithStatusInRange) {
            if (reservation.isDeleted()) {
                reservation.setStatus(ReservationStatus.CANCELLED.getStatus());
            } else {
                reservation.setStatus(ReservationStatus.COMPLETED.getStatus());
            }
        }

        System.out.println("Found " + allPassReservationsWithStatusInRange.size() + " past reservations in range.");
        return allPassReservationsWithStatusInRange;
    }

    @Override
    public ReservationInfoDetailResponse getReservationDetailInfo(String maDonDatPhong) {
        // 1. Find customer info by ReservationForm ID
        CustomerInfo customerInfo = datPhongDAO.timThongTinKhachHangBangMaDonDatPhong(maDonDatPhong);

        // 2. Find ReservationForm by ID
        DonDatPhong donDatPhong = datPhongDAO.getDonDatPhongById(maDonDatPhong, true);

        // 3. Find all ReservationDetail IDs by ReservationForm ID
        Set<String> danhSachMaChiTietDatPhong = new HashSet<>();
        List<ReservationDetailRepository> danhSachChiTietDatPhong = datPhongDAO.getReservationDetailByReservationId(maDonDatPhong);
        for (ReservationDetailRepository chiTietDatPhong : danhSachChiTietDatPhong) {
            danhSachMaChiTietDatPhong.add(chiTietDatPhong.getReservationDetailId());
        }

        // 4. Find all related info by ReservationDetail IDs (usage services, check-in history, check-out history)
//        List<LichSuDiVao> lichSuDiVaoTheoChiTietDatPhong = lichSuDiVaoDAO.timLichSuDiVaoBangDanhSachMaChiTietDatPhong(new ArrayList<>(danhSachMaChiTietDatPhong));
        List<LichSuDiVao> lichSuDiVaoTheoChiTietDatPhong = lichSuDiVaoDAO.timTatCaLichSuDiVaoBangMaDatPhong(donDatPhong.getMaDonDatPhong());

//        List<LichSuRaNgoai> lichSuDiRaTheoChiTietDatPhong = lichSuRaNgoaiDAO.timLichSuRaNgoaiBangDanhSachMaChiTietDatPhong(new ArrayList<>(danhSachMaChiTietDatPhong));
        List<LichSuRaNgoai> lichSuDiRaTheoChiTietDatPhong = lichSuRaNgoaiDAO.timTatCaLichSuRaNgoaiBangMaDatPhong(donDatPhong.getMaDonDatPhong());

        List<RoomUsageServiceInfo> phongDungDichVuTheoChiTietDatPhong = donGoiDichVuDao.timTatCaDonGoiDichVuBangMaDatPhong(donDatPhong.getMaDonDatPhong());

        return createReservationInfoDetailResponse(customerInfo,
                                                   donDatPhong,
                                                   danhSachChiTietDatPhong,
                                                   lichSuDiVaoTheoChiTietDatPhong,
                                                   lichSuDiRaTheoChiTietDatPhong,
                                                   phongDungDichVuTheoChiTietDatPhong);
    }

    private DonDatPhong createReservationFormEntity(BookingCreationEvent bookingCreationEvent,
                                                    String donDatPhongMoiNhat,
                                                    String customerId) {
        String id;
        String prefix = EntityIDSymbol.RESERVATION_FORM_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.RESERVATION_FORM_PREFIX.getLength();

        if (donDatPhongMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(donDatPhongMoiNhat, prefix, numberLength);
        }

        return new DonDatPhong(
                id,
                bookingCreationEvent.getMoTa(),
                bookingCreationEvent.getTgNhanPhong(),
                bookingCreationEvent.getTgTraPhong(),
                bookingCreationEvent.getTongTienDuTinh(),
                bookingCreationEvent.getTienDatCoc(),
                bookingCreationEvent.isDaDatTruoc(),
                bookingCreationEvent.getDanhSachMaPhong().size() > 1
                        ? ReservationType.MULTI.getType() : ReservationType.SINGLE.getType(),
                customerId,
                bookingCreationEvent.getMaPhienDangNhap(),
                null,
                false
        );
    }

    private HoaDon createInvoiceEntity(String maHD, DonDatPhong donDatPhong, String phuongThucThanhToan) {
        HoaDon hoaDon = new HoaDon(
                maHD,
                InvoiceType.DEPOSIT_INVOICE.getStatus(),
                donDatPhong.getMaPhienDangNhap(),
                donDatPhong.getMaDonDatPhong(),
                donDatPhong.getMaKhachHang()
        );
        hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
        hoaDon.setTinhTrangThanhToan(PaymentStatus.PAID.getStatus());
        hoaDon.setThoiGianTao(null);
        hoaDon.setTongTien(BigDecimal.valueOf(donDatPhong.getTienDatCoc()));
        hoaDon.setTienThue(calculatePriceWithTaxPrice(BigDecimal.valueOf(donDatPhong.getTienDatCoc())));
        hoaDon.setTongHoaDon(hoaDon.getTongTien().add(hoaDon.getTienThue()));
        hoaDon.setChiTietHoaDonList(null);

        return hoaDon;
    }

    private ChiTietDatPhong createRoomReservationDetailEntity(BookingCreationEvent bookingCreationEvent,
                                                              String chiTietDatPhongMoiNhat,
                                                              String roomId, String reservationFormId) {
        String id;
        String prefix = EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getLength();

        if (chiTietDatPhongMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(chiTietDatPhongMoiNhat, prefix, numberLength);
        }


        return new ChiTietDatPhong(
                id,
                bookingCreationEvent.getTgNhanPhong(),
                bookingCreationEvent.getTgTraPhong(),
                null,
                reservationFormId,
                roomId,
                bookingCreationEvent.getMaPhienDangNhap(),
                null
        );
    }

    private PhongDungDichVu createRoomUsageServiceEntity(BookingCreationEvent bookingCreationEvent,
                                                         String maPhongDungDichVuMoiNhat,
                                                         String maChiTietDatPhong,
                                                         DonGoiDichVu dichVu) {
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
                dichVu.isDuocTang(),
                maChiTietDatPhong,
                dichVu.getMaDichVu(),
                bookingCreationEvent.getMaPhienDangNhap(),
                null
        );
    }

    private LichSuDiVao createHistoryCheckInEntity(String maLichSuDiVaoMoiNhat, String maChiTietDatPhong) {
        String id;
        String prefix = EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix();

        int numberLength = EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength();
        if (maLichSuDiVaoMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(maLichSuDiVaoMoiNhat, prefix, numberLength);
        }

        return new LichSuDiVao(
                id,
                true,
                maChiTietDatPhong,
                null
        );
    }

    private BookingResponse createBookingResponse(ThongTinPhong thongTinPhong) {
        return new BookingResponse(
                thongTinPhong.getMaPhong(),
                thongTinPhong.getTenPhong(),
                thongTinPhong.isDangHoatDong(),
                thongTinPhong.getTenTrangThai(),
                thongTinPhong.getPhanLoai(),
                thongTinPhong.getSoLuongKhach(),
                thongTinPhong.getGiaNgay(),
                thongTinPhong.getGiaGio()
        );
    }

    private ReservationInfoDetailResponse createReservationInfoDetailResponse(CustomerInfo thongTinKhachHang,
                                                                              DonDatPhong donDatPhong,
                                                                              List<ReservationDetailRepository> danhSachChiTietDatPhong,
                                                                              List<LichSuDiVao> lichSuDiVaoTheoChiTietDatPhong,
                                                                              List<LichSuRaNgoai> lichSuDiRaTheoChiTietDatPhong,
                                                                              List<RoomUsageServiceInfo> phongDungDichVuTheoChiTietDatPhong
                                                                              )
    {

        // 1. Create base response
        List<ReservationDetailResponse> reservationDetailResponses = new ArrayList<>();
        for (ReservationDetailRepository reservationDetailRepository : danhSachChiTietDatPhong) {
            if (reservationDetailRepository.getEndType() == null) {
                reservationDetailResponses.add(new ReservationDetailResponse(
                        reservationDetailRepository.getReservationDetailId(),
                        reservationDetailRepository.getRoomId(),
                        reservationDetailRepository.getRoomName(),
                        reservationDetailRepository.getTimeIn(),
                        reservationDetailRepository.getTimeOut()
                ));
            }
        }

        // 2. Create list of RoomUsageServiceResponse
        List<RoomUsageServiceResponse> roomUsageServiceResponses = new ArrayList<>();
        for (RoomUsageServiceInfo phongDungDichVu : phongDungDichVuTheoChiTietDatPhong) {
            roomUsageServiceResponses.add(new RoomUsageServiceResponse(
                    phongDungDichVu.getMaPhongDungDichVu(),
                    phongDungDichVu.getTenPhong(),
                    phongDungDichVu.getTenDichVu(),
                    phongDungDichVu.getSoLuong(),
                    phongDungDichVu.isDuocTang()
            ));
        }

        // 3. Create list of MovingHistoryResponse
        Map<String, String> reservationIdToRoomId = new HashMap<>();
        Map<String, String> reservationIdToRoomName = new HashMap<>();

        for (ReservationDetailRepository reservationDetailRepository : danhSachChiTietDatPhong) {
            reservationIdToRoomId.put(reservationDetailRepository.getReservationDetailId(),
                                      reservationDetailRepository.getRoomId());

            reservationIdToRoomName.put(reservationDetailRepository.getReservationDetailId(),
                                        reservationDetailRepository.getRoomName());
        }

        List<MovingHistoryResponse> movingHistoryResponses = new ArrayList<>();
        int i = 0;
        int j = 0;

        // 3.1 Merge two check-in and check-out history lists
        while (i < lichSuDiVaoTheoChiTietDatPhong.size() && j < lichSuDiRaTheoChiTietDatPhong.size()) {
            LichSuDiVao lsdv = lichSuDiVaoTheoChiTietDatPhong.get(i);
            LichSuRaNgoai lsrn = lichSuDiRaTheoChiTietDatPhong.get(j);

            if (lsdv.getLaLanDauTien()) {
                // Handle first check-in separately
                movingHistoryResponses.add(new MovingHistoryResponse(
                        lsdv.getMaChiTietDatPhong(),
                        reservationIdToRoomId.get(lsdv.getMaChiTietDatPhong()),
                        reservationIdToRoomName.get(lsdv.getMaChiTietDatPhong()),
                        lsdv.getThoiGianTao(),
                        null,
                        "Checkin"
                ));
                i++;
                continue;
            }

            if (lsrn.isLaLanCuoiCung()) {
                // Handle last check-out separately
                movingHistoryResponses.add(new MovingHistoryResponse(
                        lsrn.getMaChiTietDatPhong(),
                        reservationIdToRoomId.get(lsrn.getMaChiTietDatPhong()),
                        reservationIdToRoomName.get(lsrn.getMaChiTietDatPhong()),
                        null,
                        lsrn.getThoiGianTao(),
                        "Checkout"
                ));
                j++;
                continue;
            }

            // Handle normal case: both check-in and check-out
            String maChiTietDatPhongIn =
                    Objects.equals(lsdv.getMaChiTietDatPhong(), lsrn.getMaChiTietDatPhong())
                            ? lsrn.getMaChiTietDatPhong()
                            : null;

            movingHistoryResponses.add(new MovingHistoryResponse(
                    maChiTietDatPhongIn,
                    reservationIdToRoomId.get(maChiTietDatPhongIn),
                    reservationIdToRoomName.get(maChiTietDatPhongIn),
                    lsdv.getThoiGianTao(),
                    lsrn.getThoiGianTao(),
                    null
            ));

            i++;
            j++;
        }

        // 3.2 Add remaining check-in history if any
        if (i < lichSuDiVaoTheoChiTietDatPhong.size()) {
            while (i < lichSuDiVaoTheoChiTietDatPhong.size()) {
                LichSuDiVao lsdv = lichSuDiVaoTheoChiTietDatPhong.get(i);
                movingHistoryResponses.add(new MovingHistoryResponse(
                        lsdv.getMaChiTietDatPhong(),
                        reservationIdToRoomId.get(lsdv.getMaChiTietDatPhong()),
                        reservationIdToRoomName.get(lsdv.getMaChiTietDatPhong()),
                        lsdv.getThoiGianTao(),
                        null,
                        "Checkin"
                ));
                i++;
            }
        }

        // 3.3 Add remaining check-out history if any
        if (j < lichSuDiRaTheoChiTietDatPhong.size()) {
            while (j < lichSuDiRaTheoChiTietDatPhong.size()) {
                LichSuRaNgoai lsrn = lichSuDiRaTheoChiTietDatPhong.get(j);
                movingHistoryResponses.add(new MovingHistoryResponse(
                        lsrn.getMaChiTietDatPhong(),
                        reservationIdToRoomId.get(lsrn.getMaChiTietDatPhong()),
                        reservationIdToRoomName.get(lsrn.getMaChiTietDatPhong()),
                        null,
                        lsrn.getThoiGianTao(),
                        "Checkout"
                ));
                j++;
            }
        }

        // 4. Check this reservation is completed or canceled or still in progress
        String status = null;
        if (donDatPhong.isDaXoa())
            status = ReservationStatus.CANCELLED.getStatus();
        else if (lichSuDiVaoTheoChiTietDatPhong.size() == 0)
            status = ReservationStatus.CHECKED_IN.getStatus();
        else {
            boolean allCheckedOut = true;
            for (ReservationDetailRepository reservationDetailRepository : danhSachChiTietDatPhong) {
                boolean hasCheckedOut = false;
                for (LichSuRaNgoai lsrn : lichSuDiRaTheoChiTietDatPhong) {
                    if (Objects.equals(lsrn.getMaChiTietDatPhong(), reservationDetailRepository.getReservationDetailId())
                        && lsrn.isLaLanCuoiCung()) {
                        hasCheckedOut = true;
                        break;
                    }
                }
                if (!hasCheckedOut) {
                    allCheckedOut = false;
                    break;
                }
            }
            if (allCheckedOut)
                status = ReservationStatus.COMPLETED.getStatus();
            else if (donDatPhong.getTgNhanPhong().getTime() <= new Date().getTime()
                             && donDatPhong.getTgNhanPhong().getTime() >= new Date().getTime() - 30 * 60 * 1000)
                status = ReservationStatus.CHECKING.getStatus();
            else
                status = ReservationStatus.USING.getStatus();
        }

        return new ReservationInfoDetailResponse(
            thongTinKhachHang.getCCCD(),
            thongTinKhachHang.getTenKhachHang(),
            donDatPhong.getMaDonDatPhong(),
            status,
            donDatPhong.isDaDatTruoc(),
            reservationDetailResponses,
            roomUsageServiceResponses,
            movingHistoryResponses
        );
    }


    private BigDecimal calculatePriceWithTaxPrice(BigDecimal price){
        ThongTinPhuPhi thue = vn.iuh.util.FeeValue.getInstance().get(Fee.THUE);
        return price.multiply(thue.getGiaHienTai()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}

package vn.iuh.service.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.*;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.repository.ThongTinDatPhong;
import vn.iuh.dto.repository.ThongTinPhong;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dto.response.ReservationFormResponse;
import vn.iuh.entity.*;
import vn.iuh.service.BookingService;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookingServiceImpl implements BookingService {
    private final DatPhongDAO datPhongDAO;
    private final GoiDichVuDao goiDichVuDao;
    private final KhachHangDAO khachHangDAO;
    private final LichSuDiVaoDAO lichSuDiVaoDAO;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
    private final CongViecDAO congViecDAO;

    public BookingServiceImpl() {
        this.datPhongDAO = new DatPhongDAO();
        this.goiDichVuDao = new GoiDichVuDao();
        this.khachHangDAO = new KhachHangDAO();
        this.lichSuDiVaoDAO = new LichSuDiVaoDAO();
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
        this.congViecDAO = new CongViecDAO();
    }

    @Override
    public boolean createBooking(BookingCreationEvent bookingCreationEvent) {

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
                    bookingCreationEvent.getTenKhachHang(),
                    bookingCreationEvent.getSoDienThoai(),
                    bookingCreationEvent.getCCCD(),
                    null
            ));
            khachHang = khachHangDAO.timKhachHangBangCCCD(bookingCreationEvent.getCCCD());
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
            return false;
        }

        try {
            datPhongDAO.khoiTaoGiaoTac();

            // 2.1. Create ReservationFormEntity & insert to DB
            DonDatPhong donDatPhongMoiNhat = datPhongDAO.timDonDatPhongMoiNhat();
            DonDatPhong donDatPhong = createReservationFormEntity(bookingCreationEvent,
                                                                  donDatPhongMoiNhat.getMaDonDatPhong(),
                                                                  khachHang.getMaKhachHang());
            datPhongDAO.themDonDatPhong(donDatPhong);

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

            // 2.4 Update Service Quantity
            for (DonGoiDichVu dichVu : bookingCreationEvent.getDanhSachDichVu()) {
                goiDichVuDao.capNhatSoLuongTonKhoDichVu(dichVu.getMaDichVu(), dichVu.getSoLuong());
            }

            // 2.5. Create RoomUsageServiceEntity & insert to DB
            List<PhongDungDichVu> danhSachPhongDungDichVu = new ArrayList<>();
            PhongDungDichVu phongDungDichVuMoiNhat = goiDichVuDao.timPhongDungDichVuMoiNhat();
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

            goiDichVuDao.themPhongDungDichVu(danhSachPhongDungDichVu);

            // 2.6. Create Job for each booked room
            List<CongViec> congViecs = new ArrayList<>();
            CongViec congViec = congViecDAO.timCongViecMoiNhat();
            String jobId = congViec == null ? null : congViec.getMaCongViec();

            for (String roomId : bookingCreationEvent.getDanhSachMaPhong()) {
                String newId = EntityUtil.increaseEntityID(jobId,
                                                           EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                                           EntityIDSymbol.JOB_PREFIX.getLength());

//                String statusName = bookingCreationEvent.isDaDatTruoc()
//                        ? RoomStatus.ROOM_BOOKED_STATUS.getStatus()
//                        : RoomStatus.ROOM_CHECKING_STATUS.getStatus();
//
//                congViecs.add(new CongViec(newId,
//                                           statusName,
//                                           bookingCreationEvent.getTgNhanPhong(),
//                                           bookingCreationEvent.getTgTraPhong(),
//                                           roomId,
//                                           null
//                ));
                int thoiGianKiemTra = 30; //phút
                if(bookingCreationEvent.isDaDatTruoc()){
                    congViecs.add(new CongViec(newId, RoomStatus.ROOM_BOOKED_STATUS.getStatus(),
                            bookingCreationEvent.getTgNhanPhong(), bookingCreationEvent.getTgTraPhong(),
                            roomId, null));
                }
                else{
                    congViecs.add(new CongViec(newId, RoomStatus.ROOM_CHECKING_STATUS.getStatus(),
                            bookingCreationEvent.getTgNhanPhong(), new Timestamp(bookingCreationEvent.getTgNhanPhong().getTime() + thoiGianKiemTra * 60 * 1000),
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
            return false;
        }

        datPhongDAO.thucHienGiaoTac();
        System.out.println("Đặt phòng thành công!");
        return true;
    }

    @Override
    public List<ReservationFormResponse> getAllReservationForms() {
        System.out.println("Fetching all reservation forms...");
        List<ThongTinDatPhong> danhSachThongTinDatPhong = datPhongDAO.timTatCaThongTinDatPhong();

        List<ReservationFormResponse> reservationFormResponses = new ArrayList<>();
        for (ThongTinDatPhong thongTinDatPhong : danhSachThongTinDatPhong) {
            reservationFormResponses.add(new ReservationFormResponse(
                    thongTinDatPhong.getTenKhachHang(),
                    thongTinDatPhong.getMaDonDatPhong(),
                    thongTinDatPhong.getMaPhong(),
                    thongTinDatPhong.getTgNhanPhong(),
                    thongTinDatPhong.getTgTraPhong()
            ));
        }

        return reservationFormResponses;
    }

    @Override
    public List<BookingResponse> getAllBookingInfo() {
        // Get All Room Info
        List<ThongTinPhong> thongTinPhongs = datPhongDAO.timTatCaThongTinPhong();

        // Get All non-available Room Ids
        List<String> nonAvailableRoomIds = new ArrayList<>();

        // Create BookingResponse each Room info
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

        // Find all Booking Info for non-available rooms
        List<ThongTinDatPhong> thongTinDatPhongs = datPhongDAO.timTatCaThongTinDatPhongTrongKhoang(nonAvailableRoomIds);

        // Update BookingResponse with Booking Info
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

    // TODO Implement cancelBooking
    private boolean cancelBooking(String maDatPhong) {
        return false;
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
                customerId,
                bookingCreationEvent.getMaPhienDangNhap(),
                null
        );
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

}

package vn.iuh.servcie.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.DatPhongDAO;
import vn.iuh.dao.KhachHangDAO;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.LichSuThaoTacDAO;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.repository.ThongTinDatPhong;
import vn.iuh.dto.repository.ThongTinPhong;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.*;
import vn.iuh.servcie.BookingService;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookingServiceImpl implements BookingService {
    private final DatPhongDAO datPhongDAO;
    private final KhachHangDAO khachHangDAO;
    private final LichSuThaoTacDAO lichSuThaoTacDAO;
    private final CongViecDAO congViecDAO;

    public BookingServiceImpl() {
        this.datPhongDAO = new DatPhongDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
        this.congViecDAO = new CongViecDAO();
    }

    @Override
    public boolean createBooking(BookingCreationEvent bookingCreationEvent) {

        // 1. find Customer by CCCD
        KhachHang khachHang = khachHangDAO.timKhachHangBangCCCD(bookingCreationEvent.getCCCD());

        try {
            datPhongDAO.khoiTaoGiaoTac();

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
            }

            // 2.1. Create ReservationFormEntity & insert to DB
            DonDatPhong donDatPhong = createReservationFormEntity(bookingCreationEvent, null);
            datPhongDAO.themDonDatPhong(donDatPhong);

            // 2.2. Create RoomReservationDetail Entity & insert to DB
            List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();
            for (String roomId : bookingCreationEvent.getDanhSachMaPhong())
                chiTietDatPhongs.add(
                        createRoomReservationDetailEntity(bookingCreationEvent, roomId, donDatPhong.getMaDonDatPhong()));

            datPhongDAO.themChiTietDatPhong(donDatPhong, chiTietDatPhongs);

            // 2.3. Create HistoryCheckInEntity & insert to DB
            List<LichSuDiVao> historyCheckIns = new ArrayList<>();
            for (ChiTietDatPhong chiTietDatPhong : chiTietDatPhongs) {
                historyCheckIns.add(createHistoryCheckInEntity(chiTietDatPhong));
            }

            datPhongDAO.themLichSuDiVao(historyCheckIns);

            // 2.4. Create RoomUsageServiceEntity & insert to DB
            List<PhongDungDichVu> phongDungDichVus = new ArrayList<>();
            for (String serviceId : bookingCreationEvent.getDanhSachMaDichVu())
                phongDungDichVus.add(
                        createRoomUsageServiceEntity(bookingCreationEvent, serviceId));

            datPhongDAO.themPhongDungDichVu(donDatPhong, phongDungDichVus);

            // 2.5. Create Job for each booked room
            List<CongViec> congViecs = new ArrayList<>();
            CongViec congViec = congViecDAO.timCongViecMoiNhat();
            String jobId = congViec == null ? null : congViec.getMaCongViec();
            for (String roomId : bookingCreationEvent.getDanhSachMaPhong()) {
                String newId = EntityUtil.increaseEntityID(jobId,
                                                           EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                                           EntityIDSymbol.JOB_PREFIX.getLength());

                String statusName = bookingCreationEvent.isDaDatTruoc()
                        ? RoomStatus.ROOM_BOOKED_STATUS.getStatus()
                        : RoomStatus.ROOM_USING_STATUS.getStatus();

                congViecs.add(new CongViec(newId,
                                           statusName,
                                           bookingCreationEvent.getTgNhanPhong(),
                                           bookingCreationEvent.getTgTraPhong(),
                                           roomId,
                                           null
                ));
                jobId = newId;
            }

            congViecDAO.themDanhSachCongViec(congViecs);

            // 2.6. Update WorkingHistory
            LichSuThaoTac lichSuThaoTacMoiNhat = lichSuThaoTacDAO.timLichSuThaoTacMoiNhat();
            String workingHistoryId = lichSuThaoTacMoiNhat == null ? null : lichSuThaoTacMoiNhat.getMaLichSuThaoTac();

            String actionDescription = "Đặt phòng cho khách hàng " + bookingCreationEvent.getTenKhachHang()
                                       + " - CCCD: " + bookingCreationEvent.getCCCD() + "Phòng: " + bookingCreationEvent.getDanhSachMaPhong().toString();
            lichSuThaoTacDAO.themLichSuThaoTac(new LichSuThaoTac(
                    EntityUtil.increaseEntityID(workingHistoryId,
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()),
                    ActionType.BOOKING.getActionName(),
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
                                               ? RoomStatus.ROOM_AVAILABLE_STATUS.getStatus()
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
        List<ThongTinDatPhong> thongTinDatPhongs = datPhongDAO.timTatCaThongTinDatPhong(nonAvailableRoomIds);

        // Update BookingResponse with Booking Info
        for (ThongTinDatPhong thongTinDatPhong : thongTinDatPhongs) {
            for (BookingResponse bookingResponse : bookingResponses) {
                if (Objects.equals(bookingResponse.getRoomId(), thongTinDatPhong.getMaPhong())) {
                    bookingResponse.updateBookingInfo(
                            thongTinDatPhong.getTenKhachHang(),
                            thongTinDatPhong.getTgNhanPhong(),
                            thongTinDatPhong.getTgTraPhong()
                    );
                }
            }
        }

        return bookingResponses;
    }

    private DonDatPhong createReservationFormEntity(BookingCreationEvent bookingCreationEvent, String customerId) {
        String id;
        String prefix = EntityIDSymbol.RESERVATION_FORM_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.RESERVATION_FORM_PREFIX.getLength();

        DonDatPhong lastedDonDatPhong = datPhongDAO.timDonDatPhongMoiNhat();
        if (lastedDonDatPhong == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedDonDatPhong.getMaDonDatPhong(), prefix, numberLength);
        }

        return new DonDatPhong(
                id,
                bookingCreationEvent.getMoTa(),
                bookingCreationEvent.getTgTraPhong(),
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
                                                              String roomId, String reservationFormId) {
        String id;
        String prefix = EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getLength();

        ChiTietDatPhong lastedReservationDetail = datPhongDAO.timChiTietDatPhongMoiNhat();
        if (lastedReservationDetail == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedReservationDetail.getMaChiTietDatPhong(), prefix, numberLength);
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

    private PhongDungDichVu createRoomUsageServiceEntity(BookingCreationEvent bookingCreationEvent, String serviceId) {
        String id;
        String prefix = EntityIDSymbol.ROOM_USAGE_SERVICE_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.ROOM_USAGE_SERVICE_PREFIX.getLength();

        PhongDungDichVu phongDungDichVuMoiNhat = datPhongDAO.timPhongDungDichVuMoiNhat();
        if (phongDungDichVuMoiNhat == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(phongDungDichVuMoiNhat.getMaPhongDungDichVu(), prefix, numberLength);
        }

        return new PhongDungDichVu(
                id,
                0,
                1,
                bookingCreationEvent.getTgNhanPhong(),
                0,
                null,
                serviceId,
                bookingCreationEvent.getMaPhienDangNhap(),
                null
        );
    }

    private LichSuDiVao createHistoryCheckInEntity(ChiTietDatPhong chiTietDatPhong) {
        String id;
        String prefix = EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength();

        LichSuDiVao lastedHistoryCheckIn = datPhongDAO.timLichSuDiVaoMoiNhat();
        if (lastedHistoryCheckIn == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedHistoryCheckIn.getMaLichSuDiVao(), prefix, numberLength);
        }

        return new LichSuDiVao(
                id,
                true,
                chiTietDatPhong.getMaChiTietDatPhong(),
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

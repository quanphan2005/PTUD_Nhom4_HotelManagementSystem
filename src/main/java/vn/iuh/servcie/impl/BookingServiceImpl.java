package vn.iuh.servcie.impl;

import vn.iuh.constraint.ActionType;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.BookingDAO;
import vn.iuh.dao.KhachHangDAO;
import vn.iuh.dao.JobDAO;
import vn.iuh.dao.WorkingHistoryDAO;
import vn.iuh.dto.event.create.BookingCreationEvent;
import vn.iuh.dto.event.create.RoomFilter;
import vn.iuh.dto.repository.BookingInfo;
import vn.iuh.dto.repository.RoomInfo;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.*;
import vn.iuh.servcie.BookingService;
import vn.iuh.util.EntityUtil;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookingServiceImpl implements BookingService {
    private final BookingDAO bookingDAO;
    private final KhachHangDAO khachHangDAO;
    private final WorkingHistoryDAO workingHistoryDAO;
    private final JobDAO jobDAO;

    public BookingServiceImpl() {
        this.bookingDAO = new BookingDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.workingHistoryDAO = new WorkingHistoryDAO();
        this.jobDAO = new JobDAO();
    }

    @Override
    public boolean createBooking(BookingCreationEvent bookingCreationEvent) {

        // 1. find Customer by CCCD
        Customer customer = khachHangDAO.timKhachHangBangCCCD(bookingCreationEvent.getCCCD());

        try {
            bookingDAO.beginTransaction();

            // 1.1 Create Customer if not exist
            if (Objects.isNull(customer)) {
                khachHangDAO.themKhachHang(new Customer(
                        EntityUtil.increaseEntityID(null,
                                                    EntityIDSymbol.CUSTOMER_PREFIX.getPrefix(),
                                                    EntityIDSymbol.CUSTOMER_PREFIX.getLength()),
                        bookingCreationEvent.getCustomerName(),
                        bookingCreationEvent.getPhoneNumber(),
                        bookingCreationEvent.getCCCD()));
            }

            // 2.1. Create ReservationFormEntity & insert to DB
            DonDatPhong donDatPhong = createReservationFormEntity(bookingCreationEvent, null);
            bookingDAO.insertReservationForm(donDatPhong);

            // 2.2. Create RoomReservationDetail Entity & insert to DB
            List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<>();
            for (String roomId : bookingCreationEvent.getRoomIds())
                chiTietDatPhongs.add(
                        createRoomReservationDetailEntity(bookingCreationEvent, roomId, donDatPhong.getMaDonDatPhong()));

            bookingDAO.insertRoomReservationDetail(donDatPhong, chiTietDatPhongs);

            // 2.3. Create HistoryCheckInEntity & insert to DB
            List<HistoryCheckIn> historyCheckIns = new ArrayList<>();
            for (ChiTietDatPhong chiTietDatPhong : chiTietDatPhongs) {
                historyCheckIns.add(createHistoryCheckInEntity(chiTietDatPhong));
            }

            bookingDAO.insertHistoryCheckIn(donDatPhong, historyCheckIns);

            // 2.4. Create RoomUsageServiceEntity & insert to DB
            List<PhongDungDichVu> phongDungDichVus = new ArrayList<>();
            for (String serviceId : bookingCreationEvent.getServiceIds())
                phongDungDichVus.add(
                        createRoomUsageServiceEntity(bookingCreationEvent, serviceId, donDatPhong.getMaDonDatPhong()));

            bookingDAO.insertRoomUsageService(donDatPhong, phongDungDichVus);

            // 2.5. Create Job for each booked room
            List<CongViec> congViecs = new ArrayList<>();
            CongViec congViec = jobDAO.findLastJob();
            String jobId = congViec == null ? null : congViec.getMaCongViec();
            for (String roomId : bookingCreationEvent.getRoomIds()) {
                String newId = EntityUtil.increaseEntityID(jobId,
                                                           EntityIDSymbol.JOB_PREFIX.getPrefix(),
                                                           EntityIDSymbol.JOB_PREFIX.getLength());

                String statusName = bookingCreationEvent.isAdvanced()
                        ? RoomStatus.ROOM_BOOKED_STATUS.getStatus()
                        : RoomStatus.ROOM_USING_STATUS.getStatus();

                congViecs.add(new CongViec(newId,
                                           bookingCreationEvent.getCheckInDate(),
                                           bookingCreationEvent.getCheckOutDate(),
                                           statusName,
                                           roomId));
                jobId = newId;
            }

            jobDAO.insertJobs(congViecs);

            // 2.6. Update WorkingHistory
            LichSuThaoTac lastLichSuThaoTac = workingHistoryDAO.findLastWorkingHistory();
            String workingHistoryId = lastLichSuThaoTac == null ? null : lastLichSuThaoTac.getMaLichSuThaoTac();

            String actionDescription = "Đặt phòng cho khách hàng " + bookingCreationEvent.getCustomerName()
                                       + " - CCCD: " + bookingCreationEvent.getCCCD() + "Phòng: " + bookingCreationEvent.getRoomIds().toString();
            workingHistoryDAO.insertWorkingHistory(new LichSuThaoTac(
                    EntityUtil.increaseEntityID(workingHistoryId,
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getPrefix(),
                                                EntityIDSymbol.WORKING_HISTORY_PREFIX.getLength()),
                    ActionType.BOOKING.getActionName(),
                    new Timestamp(System.currentTimeMillis()),
                    actionDescription,
                    bookingCreationEvent.getShiftAssignmentId()
            ));

        } catch (Exception e) {
            System.out.println("Lỗi khi đặt phòng: " + e.getMessage());
            System.out.println("Rollback transaction");
            e.printStackTrace();
            bookingDAO.rollbackTransaction();
            return false;
        }

        bookingDAO.commitTransaction();
        return true;
    }

    @Override
    public List<BookingResponse> getAllEmptyRooms() {
        List<RoomInfo> RoomInfos = bookingDAO.findAllEmptyRooms();

        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (RoomInfo roomInfo : RoomInfos) {
            bookingResponses.add(createBookingResponse(roomInfo));
        }

        return bookingResponses;
    }

    @Override
    public List<BookingResponse> getRoomsByFilter(RoomFilter roomFilter) {
//        return bookingDAO.findRoomsByFilter(roomFilter);
        return null;
    }

    @Override
    public List<BookingResponse> getAllBookingInfo() {
        // Get All Room Info
        List<RoomInfo> roomInfos = bookingDAO.findAllRoomInfo();

        // Get All non-available Room Ids
        List<String> nonAvailableRoomIds = new ArrayList<>();

        // Create BookingResponse each Room info
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (RoomInfo roomInfo : roomInfos) {

            // Set default Room Status if null or empty
            if (Objects.isNull(roomInfo.getRoomStatus()) || roomInfo.getRoomStatus().isEmpty()) {
                roomInfo.setRoomStatus(roomInfo.isActive()
                                               ? RoomStatus.ROOM_AVAILABLE_STATUS.getStatus()
                                               : RoomStatus.ROOM_MAINTENANCE_STATUS.getStatus()
                );
            }

            if (Objects.equals(roomInfo.getRoomStatus(), RoomStatus.ROOM_BOOKED_STATUS.getStatus()) ||
                Objects.equals(roomInfo.getRoomStatus(), RoomStatus.ROOM_CHECKING_STATUS.getStatus()) ||
                Objects.equals(roomInfo.getRoomStatus(), RoomStatus.ROOM_USING_STATUS.getStatus()) ||
                Objects.equals(roomInfo.getRoomStatus(), RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus())
            ) {

                nonAvailableRoomIds.add(roomInfo.getId());
            }

            bookingResponses.add(createBookingResponse(roomInfo));
        }

        // Find all Booking Info for non-available rooms
        List<BookingInfo> bookingInfos = bookingDAO.findAllBookingInfo(nonAvailableRoomIds);

        // Update BookingResponse with Booking Info
        for (BookingInfo bookingInfo : bookingInfos) {
            for (BookingResponse bookingResponse : bookingResponses) {
                if (Objects.equals(bookingResponse.getRoomId(), bookingInfo.getRoomId())) {
                    bookingResponse.updateBookingInfo(
                            bookingInfo.getCustomerName(),
                            bookingInfo.getTimeIn(),
                            bookingInfo.getTimeOut()
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

        DonDatPhong lastedDonDatPhong = bookingDAO.findLastReservationForm();
        if (lastedDonDatPhong == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedDonDatPhong.getMaDonDatPhong(), prefix, numberLength);
        }

        return new DonDatPhong(
                id,
                bookingCreationEvent.getReserveDate(),
                bookingCreationEvent.getNote(),
                bookingCreationEvent.getCheckInDate(),
                bookingCreationEvent.getCheckOutDate(),
                bookingCreationEvent.getInitialPrice(),
                bookingCreationEvent.getDepositPrice(),
                bookingCreationEvent.isAdvanced(),
                customerId,
                bookingCreationEvent.getShiftAssignmentId()
        );
    }

    private ChiTietDatPhong createRoomReservationDetailEntity(BookingCreationEvent bookingCreationEvent,
                                                              String roomId, String reservationFormId) {
        String id;
        String prefix = EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.ROOM_RESERVATION_DETAIL_PREFIX.getLength();

        ChiTietDatPhong lastedReservationDetail = bookingDAO.findLastRoomReservationDetail();
        if (lastedReservationDetail == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedReservationDetail.getMaChiTietDatPhong(), prefix, numberLength);
        }


        return new ChiTietDatPhong(
                id,
                bookingCreationEvent.getCheckInDate(),
                bookingCreationEvent.getCheckOutDate(),
                null,
                reservationFormId,
                roomId,
                bookingCreationEvent.getShiftAssignmentId()
        );
    }

    private PhongDungDichVu createRoomUsageServiceEntity(BookingCreationEvent bookingCreationEvent, String serviceId,
                                                         String reservationFormId) {
        String id;
        String prefix = EntityIDSymbol.ROOM_USAGE_SERVICE_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.ROOM_USAGE_SERVICE_PREFIX.getLength();

        PhongDungDichVu lastedPhongDungDichVu = bookingDAO.findLastRoomUsageService();
        if (lastedPhongDungDichVu == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedPhongDungDichVu.getMaPhongDungDichVu(), prefix, numberLength);
        }

        return new PhongDungDichVu(
                id,
                10,
                1,
                Date.valueOf(java.time.LocalDate.now()),
                serviceId,
                reservationFormId,
                bookingCreationEvent.getShiftAssignmentId()
        );
    }

    private HistoryCheckIn createHistoryCheckInEntity(ChiTietDatPhong chiTietDatPhong) {
        String id;
        String prefix = EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getPrefix();
        int numberLength = EntityIDSymbol.HISTORY_CHECKIN_PREFIX.getLength();

        HistoryCheckIn lastedHistoryCheckIn = bookingDAO.findLastHistoryCheckIn();
        if (lastedHistoryCheckIn == null) {
            id = EntityUtil.increaseEntityID(null, prefix, numberLength);
        } else {
            id = EntityUtil.increaseEntityID(lastedHistoryCheckIn.getId(), prefix, numberLength);
        }

        return new HistoryCheckIn(
                id,
                chiTietDatPhong.getTgNhanPhong(),
                true,
                chiTietDatPhong.getMaChiTietDatPhong()
        );
    }

    private BookingResponse createBookingResponse(RoomInfo roomInfo) {
        return new BookingResponse(
                roomInfo.getId(),
                roomInfo.getRoomName(),
                roomInfo.isActive(),
                roomInfo.getRoomStatus(),
                roomInfo.getRoomType(),
                roomInfo.getNumberOfCustomers(),
                roomInfo.getDailyPrice(),
                roomInfo.getHourlyPrice()
        );
    }

}

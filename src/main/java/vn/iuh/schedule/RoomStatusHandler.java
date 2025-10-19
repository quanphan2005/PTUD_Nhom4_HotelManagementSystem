package vn.iuh.schedule;

import org.quartz.*;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.constraint.WorkTimeCost;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.DatPhongDAO;
import vn.iuh.dto.repository.RoomJob;
import vn.iuh.dto.repository.ThongTinDatPhong;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.CongViec;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.service.BookingService;
import vn.iuh.service.CheckOutService;
import vn.iuh.service.CongViecService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.service.impl.CheckOutServiceImpl;
import vn.iuh.service.impl.CongViecServiceImpl;
import vn.iuh.util.SchedulerUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoomStatusHandler implements Job {
    private final CongViecService congViecService = new CongViecServiceImpl();
    private final CongViecDAO congViecDAO = new CongViecDAO();
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final BookingService bookingService = new BookingServiceImpl();
    private final CheckOutService checkOutService = new CheckOutServiceImpl();
    private final CongViecDAO jobDAO = new CongViecDAO();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            GridRoomPanel gridRoomPanel = (GridRoomPanel) context.getMergedJobDataMap().get("gridRoomPanel");
            updateAllRoomStatus(gridRoomPanel);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
//
//    public void updateAllRoomStatus(GridRoomPanel gridRoomPanel) {
//        System.out.println("Running room status handler at " + LocalDateTime.now());
//        List<RoomJob> roomJobs = congViecDAO.findAllRoomJobNow();
//        List<RoomItem> roomItems = gridRoomPanel.getRoomItems();
//        List<BookingResponse> updatedBookingResponse = new ArrayList<>();
//        Timestamp pivot = new Timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime() + 35_000);
//        for (RoomJob rj : roomJobs) {
//            if (rj.getEndTime() == null || rj.getStartTime() == null || rj.isDeleted()) continue;
//            String status = rj.getStatusName();
//            BookingResponse res = roomItems.stream().filter(ri -> ri.getRoomId().equalsIgnoreCase(rj.getRoomId())).map(RoomItem::getBookingResponse).findFirst().orElse(null);
//            if(Objects.isNull(res)){
//                continue;
//            }
//            if (pivot.after(rj.getEndTime())) {
//                String newStatus = "";
//                if (RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(status)) {
//                    newStatus = handleCheckingRoomSuccess(rj, res);
//                    res.setRoomStatus(newStatus);
//                    updatedBookingResponse.add(res);
//                } else if (RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(status)) {
//                    newStatus = handleLateOvertimeJob(rj);
//                    res.setRoomStatus(newStatus);
//                    updatedBookingResponse.add(res);
//                } else if (RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus().equalsIgnoreCase(status)) {
//                    handleAutoCheckOut(rj, res);
//                    updatedBookingResponse.add(new BookingResponse(res.getRoomId(),
//                            res.getRoomName(),
//                            res.isActive(),
//                            RoomStatus.ROOM_CLEANING_STATUS.getStatus(),
//                            res.getRoomType(), res.getNumberOfCustomers(),
//                            res.getDailyPrice(), res.getHourlyPrice()));
//                }else if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(status)){
//                    congViecService.removeOutDateJob(rj.getJobId());
//                    updatedBookingResponse.add(new BookingResponse(res.getRoomId(),
//                            res.getRoomName(),
//                            res.isActive(),
//                            RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
//                            res.getRoomType(), res.getNumberOfCustomers(),
//                            res.getDailyPrice(), res.getHourlyPrice()));
//                }else if(RoomStatus.ROOM_CLEANING_STATUS.getStatus().equalsIgnoreCase(status)){
//                    congViecService.removeOutDateJob(rj.getJobId());
//                    updatedBookingResponse.add(new BookingResponse(res.getRoomId(),
//                            res.getRoomName(),
//                            res.isActive(),
//                            RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
//                            res.getRoomType(), res.getNumberOfCustomers(),
//                            res.getDailyPrice(), res.getHourlyPrice()));
//                }
//            }
//            else if(!rj.getStatusName().equalsIgnoreCase(res.getRoomStatus())){
//                if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(rj.getStatusName()) ||
//                    RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(rj.getStatusName())){
//                    ThongTinDatPhong thongTinDatPhong = datPhongDAO.timDonDatPhongChoCheckInCuaPhong(rj.getRoomId(), rj.getStartTime(), rj.getEndTime());
//                    if(Objects.nonNull(thongTinDatPhong)){
//                        res.updateBookingInfo(thongTinDatPhong.getTenKhachHang(), thongTinDatPhong.getMaChiTietDatPhong(), thongTinDatPhong.getTgNhanPhong(), thongTinDatPhong.getTgTraPhong());
//                        res.setRoomStatus(rj.getStatusName());
//                        updatedBookingResponse.add(res);
//                        System.out.println(res);
//                    }
//                    else {
//                        System.out.println("Lỗi không tìm thấy thông tin đặt phòng cho phòng " + rj.getRoomId() + " với thời gian " + rj.getStartTime() + " đến " + rj.getEndTime());
//                    }
//                }
//            }
//            gridRoomPanel.updateRoomItemStatus(updatedBookingResponse);
//        }
//    }
//
//    private String handleCheckingRoomSuccess(RoomJob rj,BookingResponse res) {
//        if (res == null) {
//            System.out.println("lỗi thêm công việc cho phòng" + rj.getRoomId());
//            return null;
//        }
//        CongViec newCv = congViecService.themCongViec(RoomStatus.ROOM_USING_STATUS.getStatus(),
//                rj.getEndTime(),
//                res.getTimeOut(),
//                rj.getRoomId());
//        return newCv != null ? newCv.getTenTrangThai() : null;
//    }
//
//    private String handleLateOvertimeJob(RoomJob rj) {
//        System.out.println("Xử lý chuyển trạng thái phòng " + rj.getRoomId() + " từ đang sử dụng sang trễ trả phòng");
//        return congViecService.themCongViec(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus(),
//                rj.getEndTime(),
//                Timestamp.valueOf(rj.getEndTime().toLocalDateTime().plusMinutes(WorkTimeCost.CHECKOUT_LATE_MIN.getMinutes())),
//                rj.getRoomId()).getTenTrangThai();
//    }
//
//    private String handleAutoCheckOut(RoomJob rj, BookingResponse res) {
//        System.out.println("Xử lý tự động trả phòng cho phòng " + rj.getJobId());
//        checkOutService.checkOutByReservationDetail(res.getMaChiTietDatPhong());
//        return congViecService.themCongViec(RoomStatus.ROOM_CLEANING_STATUS.getStatus(),
//                rj.getEndTime(),
//                Timestamp.valueOf(rj.getEndTime().toLocalDateTime().plusMinutes(WorkTimeCost.CLEANING_TIME.getMinutes())),
//                rj.getRoomId()).getTenTrangThai();
//    }

    public void updateAllRoomStatus(GridRoomPanel gridRoomPanel) {
        System.out.println("Running room status handler at " + LocalDateTime.now());
        List<RoomJob> roomJobs = congViecDAO.findAllRoomJobNow();
        List<RoomItem> roomItems = gridRoomPanel.getRoomItems();
        List<BookingResponse> updatedBookingResponse = new ArrayList<>();

        List<CongViec> congViecCanThem = new ArrayList<>();
        List<String> congViecCanKetThuc = new ArrayList<>();

        Timestamp pivot = new Timestamp(System.currentTimeMillis() + 35_000);
        String maCongViecMoiNhat = congViecService.taoMaCongViecMoi(null);

        for (RoomJob rj : roomJobs) {
            if (rj.getEndTime() == null || rj.getStartTime() == null || rj.isDeleted()) continue;

            String currentStatus = rj.getStatusName();
            BookingResponse res = roomItems.stream()
                    .filter(ri -> ri.getRoomId().equalsIgnoreCase(rj.getRoomId()))
                    .map(RoomItem::getBookingResponse)
                    .findFirst()
                    .orElse(null);

            if (Objects.isNull(res)) continue;

            // Trường hợp công việc hiện tại đã kết thúc
            if (pivot.after(rj.getEndTime())) {
                String newStatus = null;
                Timestamp tgBatDau = new Timestamp(rj.getEndTime().getTime() + 1);
                Timestamp tgKetThuc = null;
                String roomId = rj.getRoomId();

                //kiểm tra -> sử dụng
                if (currentStatus.equalsIgnoreCase(RoomStatus.ROOM_CHECKING_STATUS.getStatus())) {
                    newStatus = RoomStatus.ROOM_USING_STATUS.getStatus();
                    tgKetThuc = res.getTimeOut();

                    //sử dụng ->checkout trễ
                } else if (currentStatus.equalsIgnoreCase(RoomStatus.ROOM_USING_STATUS.getStatus())) {
                    newStatus = RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus();
                    tgKetThuc = Timestamp.valueOf(rj.getEndTime().toLocalDateTime()
                            .plusMinutes(WorkTimeCost.CHECKOUT_LATE_MIN.getMinutes()));
                    createMessageForLateCheckOut(rj.getRoomId());

                    //trễ checkout -> dọn dẹp
                } else if (currentStatus.equalsIgnoreCase(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus())) {
                    newStatus = RoomStatus.ROOM_CLEANING_STATUS.getStatus();
                    tgKetThuc = Timestamp.valueOf(rj.getEndTime().toLocalDateTime()
                            .plusMinutes(WorkTimeCost.CLEANING_TIME.getMinutes()));
                    checkOutService.createHoaDonForAutoCheckout(res.getMaChiTietDatPhong());
                    createMessageForAutoCheckOut(rj.getRoomId());

                    //chờ check-in hoặc dọn dẹp hết hạn -> xoá công việc
                } else if (
                        currentStatus.equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus()) ||
                                currentStatus.equalsIgnoreCase(RoomStatus.ROOM_CLEANING_STATUS.getStatus())
                ) {
                    congViecService.removeOutDateJob(rj.getJobId());
                    res.setRoomStatus(RoomStatus.ROOM_EMPTY_STATUS.getStatus());
                    updatedBookingResponse.add(res);
                    // kết thúc vòng lặp vì không tạo công việc mới
                    continue;
                } else {
                    continue;
                }
                if (newStatus != null) {
                    congViecCanThem.add(new CongViec(
                            maCongViecMoiNhat,
                            newStatus,
                            tgBatDau,
                            tgKetThuc,
                            roomId,
                            null
                    ));
                    congViecCanKetThuc.add(rj.getJobId());
                    maCongViecMoiNhat = congViecService.taoMaCongViecMoi(maCongViecMoiNhat);
                    res.setRoomStatus(newStatus);
                    updatedBookingResponse.add(res);
                }
            }
            else if(!rj.getStatusName().equalsIgnoreCase(res.getRoomStatus())){
                if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(rj.getStatusName())){
                    ThongTinDatPhong thongTinDatPhong = datPhongDAO.timDonDatPhongChoCheckInCuaPhong(rj.getRoomId(), rj.getStartTime(), rj.getEndTime());
                    if(Objects.nonNull(thongTinDatPhong)){
                        res.updateBookingInfo(thongTinDatPhong.getTenKhachHang(), thongTinDatPhong.getMaChiTietDatPhong(), thongTinDatPhong.getTgNhanPhong(), thongTinDatPhong.getTgTraPhong());
                        res.setRoomStatus(rj.getStatusName());
                        updatedBookingResponse.add(res);
                    }
                    else {
                        System.out.println("Lỗi không tìm thấy thông tin đặt phòng cho phòng " + rj.getRoomId() + " với thời gian " + rj.getStartTime() + " đến " + rj.getEndTime());
                    }
                }
            }
        }


        //Xử lý xóa và thêm công việc batch
        if (!congViecCanKetThuc.isEmpty()) {
            jobDAO.xoaDanhSachCongViec(congViecCanKetThuc);
        }

        if (!congViecCanThem.isEmpty()) {
            jobDAO.themDanhSachCongViec(congViecCanThem);
        }
        //Cập nhật giao diện
        gridRoomPanel.updateRoomItemStatus(updatedBookingResponse);
        System.out.printf("Đã thêm %d công việc mới, xóa %d công việc cũ%n",
                congViecCanThem.size(), congViecCanKetThuc.size());
    }

    private void createMessageForLateCheckOut(String roomId){
        try {
            Scheduler scheduler = SchedulerUtil.getInstance();
            String jobName = "room_" + roomId;
            String jobGroup = "roomLateCheckOutGroup";
            JobKey jobKey = new JobKey(jobName, jobGroup);

            if (scheduler.checkExists(jobKey)) {
                return;
            }
            JobDetail jobDetail = JobBuilder.newJob(SendMessageLateCheckOut.class)
                    .withIdentity(jobKey)
                    .usingJobData("roomId", roomId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_room_" + roomId, jobGroup)
                    .startAt(DateBuilder.futureDate(5, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    private void createMessageForAutoCheckOut(String roomId){
        try {
            Scheduler scheduler = SchedulerUtil.getInstance();
            String jobName = "room_" + roomId;
            String jobGroup = "roomAutoCheckOutGroup";
            JobKey jobKey = new JobKey(jobName, jobGroup);

            if (scheduler.checkExists(jobKey)) {
                return;
            }
            JobDetail jobDetail = JobBuilder.newJob(SendMessageAutoCheckOut.class)
                    .withIdentity(jobKey)
                    .usingJobData("roomId", roomId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_room_" + roomId, jobGroup)
                    .startAt(DateBuilder.futureDate(5, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}

package vn.iuh.schedule;

import org.quartz.*;
import vn.iuh.constraint.RoomEndType;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.constraint.WorkTimeCost;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.DatPhongDAO;
import vn.iuh.dao.PhongDAO;
import vn.iuh.dto.repository.RoomJob;
import vn.iuh.dto.repository.ThongTinDatPhong;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.Phong;
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
import java.util.*;

public class RoomStatusHandler implements Job {
    private final CongViecService congViecService = new CongViecServiceImpl();
    private final CongViecDAO congViecDAO = new CongViecDAO();
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final CheckOutService checkOutService = new CheckOutServiceImpl();
    private final CongViecDAO jobDAO = new CongViecDAO();
    private final ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            GridRoomPanel gridRoomPanel = (GridRoomPanel) context.getMergedJobDataMap().get("gridRoomPanel");
            updateAllRoomStatus(gridRoomPanel);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateAllRoomStatus(GridRoomPanel gridRoomPanel) {
        System.out.println("Running room status handler at " + LocalDateTime.now());
        List<RoomJob> roomJobs = congViecDAO.findAllRoomJobNow();
        List<RoomItem> roomItems = gridRoomPanel.getRoomItems();
        List<BookingResponse> updatedBookingResponse = new ArrayList<>();

        List<CongViec> congViecCanThem = new ArrayList<>();
        List<String> congViecCanKetThuc = new ArrayList<>();
        List<String> maCTDPCapNhat = new ArrayList<>();

        Timestamp pivot = new Timestamp(System.currentTimeMillis() + 35_000);
        String maCongViecMoiNhat = congViecService.taoMaCongViecMoi(null);

        // dùng để lấy danh sách phòng trễ checkout
        List<String> danhSachMaCTDPTreCheckout = new ArrayList<>();
        Map<String, RoomJob> mapMaCTDPToRoomJob = new HashMap<>();
        Map<String, BookingResponse> mapMaCTDPToResponse = new HashMap<>();

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
                    //Đưa vào danh sách để xử lý check-out tự động sau
                    danhSachMaCTDPTreCheckout.add(res.getMaChiTietDatPhong());
                    mapMaCTDPToRoomJob.put(res.getMaChiTietDatPhong(), rj);
                    mapMaCTDPToResponse.put(res.getMaChiTietDatPhong(), res);
                    continue;

                    //chờ check-in hoặc dọn dẹp hết hạn -> xoá công việc
                } else if (
                        currentStatus.equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus()) ||
                                currentStatus.equalsIgnoreCase(RoomStatus.ROOM_CLEANING_STATUS.getStatus())
                ) {
                    if(currentStatus.equalsIgnoreCase(RoomStatus.ROOM_BOOKED_STATUS.getStatus())){
                        maCTDPCapNhat.add(res.getMaChiTietDatPhong());
                    }
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

        int soLanCheckout = 0;
        while (!danhSachMaCTDPTreCheckout.isEmpty()) {
            soLanCheckout++;
            // Lấy mã chi tiết đặt phòng đầu tiên để checkout
            String maCTDPCanCheckout = danhSachMaCTDPTreCheckout.getFirst();

            System.out.printf("Lần checkout %d: Xử lý CTDP %s (Còn %d phòng trễ chưa xử lý)%n",
                    soLanCheckout, maCTDPCanCheckout, danhSachMaCTDPTreCheckout.size());

            // Gọi hàm checkout, hàm này sẽ checkout tất cả phòng trong cùng đơn
            List<String> danhSachMaCTDPDaCheckout = checkOutService.createHoaDonForAutoCheckout(maCTDPCanCheckout);

            List<String> danhSachTenPhongCheckout = new ArrayList<>();

            // Xử lý cập nhật trạng thái cho tất cả các phòng đã được checkout
            for (String maCTDP : danhSachMaCTDPDaCheckout) {
                RoomJob rj = mapMaCTDPToRoomJob.get(maCTDP);
                BookingResponse res = mapMaCTDPToResponse.get(maCTDP);

                if (rj != null && res != null) {
                    // Chuyển sang trạng thái dọn dẹp
                    String newStatus = RoomStatus.ROOM_CLEANING_STATUS.getStatus();
                    Timestamp tgBatDau = new Timestamp(rj.getEndTime().getTime() + 1);
                    Timestamp tgKetThuc = Timestamp.valueOf(rj.getEndTime().toLocalDateTime()
                            .plusMinutes(WorkTimeCost.CLEANING_TIME.getMinutes()));

                    congViecCanThem.add(new CongViec(
                            maCongViecMoiNhat,
                            newStatus,
                            tgBatDau,
                            tgKetThuc,
                            rj.getRoomId(),
                            null
                    ));
                    congViecCanKetThuc.add(rj.getJobId());
                    maCongViecMoiNhat = congViecService.taoMaCongViecMoi(maCongViecMoiNhat);

                    res.setRoomStatus(newStatus);
                    updatedBookingResponse.add(res);

                    Phong phong = phongDAO.timPhong(rj.getRoomId());
                    if (phong != null) {
                        danhSachTenPhongCheckout.add(phong.getTenPhong());
                    }
                }
            }

            if (!danhSachTenPhongCheckout.isEmpty()) {
                createMessageForAutoCheckOutBatch(danhSachTenPhongCheckout);
            }

            // Loại bỏ các mã chi tiết đã được checkout khỏi danh sách
            danhSachMaCTDPTreCheckout.removeAll(danhSachMaCTDPDaCheckout);
        }

        if (soLanCheckout > 0) {
            System.out.printf("Hoàn thành xử lý checkout trễ: %d lần checkout cho tổng số phòng trễ%n", soLanCheckout);
        }

        // Xử lý xóa và thêm công việc batch
        if (!congViecCanKetThuc.isEmpty()) {
            jobDAO.xoaDanhSachCongViec(congViecCanKetThuc);
        }
        if (!congViecCanThem.isEmpty()) {
            jobDAO.themDanhSachCongViec(congViecCanThem);
        }

        // Cập nhật kiểu kết thúc của các CTDP không nhận phòng
        if (!maCTDPCapNhat.isEmpty()) {
            updateCTDPForLateCheckIn(maCTDPCapNhat);
        }

        // Cập nhật giao diện
        gridRoomPanel.updateRoomItemStatus(updatedBookingResponse);
        System.out.printf("Đã thêm %d công việc mới, xóa %d công việc cũ%n",
                congViecCanThem.size(), congViecCanKetThuc.size());
    }

    private void updateCTDPForLateCheckIn(List<String> maCTDPKetThuc){
        if(maCTDPKetThuc == null || maCTDPKetThuc.isEmpty()) return;
        chiTietDatPhongDAO.capNhatKetThucCTDP(maCTDPKetThuc, RoomEndType.KHONG_NHAN_PHONG.status);
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

    private void createMessageForAutoCheckOutBatch(List<String> danhSachTenPhong) {
        try {
            Scheduler scheduler = SchedulerUtil.getInstance();
            String jobName = "auto_checkout_" + System.currentTimeMillis();
            String jobGroup = "roomAutoCheckOutGroup";
            JobKey jobKey = new JobKey(jobName, jobGroup);

            // Tạo message content
            String roomList = String.join(", ", danhSachTenPhong);

            JobDetail jobDetail = JobBuilder.newJob(SendMessageAutoCheckOut.class)
                    .withIdentity(jobKey)
                    .usingJobData("roomNames", roomList)
                    .usingJobData("isBatch", true)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_" + jobName, jobGroup)
                    .startAt(DateBuilder.futureDate(5, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}

package vn.iuh.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dao.DatPhongDAO;
import vn.iuh.dto.repository.RoomJob;
import vn.iuh.dto.repository.ThongTinDatPhong;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.CongViec;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.service.BookingService;
import vn.iuh.service.CongViecService;
import vn.iuh.service.impl.BookingServiceImpl;
import vn.iuh.service.impl.CongViecServiceImpl;

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
        Timestamp pivot = new Timestamp(Timestamp.valueOf(LocalDateTime.now()).getTime() + 35_000);
        for (RoomJob rj : roomJobs) {
            if (rj.getEndTime() == null || rj.getStartTime() == null || rj.isDeleted()) continue;
            String status = rj.getStatusName();
            BookingResponse res = roomItems.stream().filter(ri -> ri.getRoomId().equalsIgnoreCase(rj.getRoomId())).map(RoomItem::getBookingResponse).findFirst().orElse(null);
            if(Objects.isNull(res)){
                continue;
            }
            if (pivot.after(rj.getEndTime())) {
                String newStatus = "";
                if (RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(status)) {
                    newStatus = handleCheckingRoomSuccess(rj, res);
                    res.setRoomStatus(newStatus);
                    updatedBookingResponse.add(res);
                } else if (RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(status)) {
                    newStatus = handleLateOvertimeJob(rj);
                    res.setRoomStatus(newStatus);
                    updatedBookingResponse.add(res);
                } else if (RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus().equalsIgnoreCase(status)) {
                    handleAutoCheckOut(rj);
                    updatedBookingResponse.add(new BookingResponse(res.getRoomId(),
                            res.getRoomName(),
                            res.isActive(),
                            RoomStatus.ROOM_CLEANING_STATUS.getStatus(),
                            res.getRoomType(), res.getNumberOfCustomers(),
                            res.getDailyPrice(), res.getHourlyPrice()));
                }else if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(status)){
                    congViecService.removeOutDateJob(rj.getJobId());
                    updatedBookingResponse.add(new BookingResponse(res.getRoomId(),
                            res.getRoomName(),
                            res.isActive(),
                            RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
                            res.getRoomType(), res.getNumberOfCustomers(),
                            res.getDailyPrice(), res.getHourlyPrice()));
                }else if(RoomStatus.ROOM_CLEANING_STATUS.getStatus().equalsIgnoreCase(status)){
                    congViecService.removeOutDateJob(rj.getJobId());
                    updatedBookingResponse.add(new BookingResponse(res.getRoomId(),
                            res.getRoomName(),
                            res.isActive(),
                            RoomStatus.ROOM_EMPTY_STATUS.getStatus(),
                            res.getRoomType(), res.getNumberOfCustomers(),
                            res.getDailyPrice(), res.getHourlyPrice()));
                }
            }
            else if(!rj.getStatusName().equalsIgnoreCase(res.getRoomStatus())){
                if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(rj.getStatusName()) ||
                    RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(rj.getStatusName())){
                    ThongTinDatPhong thongTinDatPhong = datPhongDAO.timDonDatPhongChoCheckInCuaPhong(rj.getRoomId(), rj.getStartTime(), rj.getEndTime());
                    if(Objects.nonNull(thongTinDatPhong)){
                        res.updateBookingInfo(thongTinDatPhong.getTenKhachHang(), thongTinDatPhong.getTgNhanPhong(), thongTinDatPhong.getTgTraPhong());
                        res.setRoomStatus(rj.getStatusName());
                        updatedBookingResponse.add(res);
                        System.out.println(res);
                    }
                    else {
                        System.out.println("Lỗi không tìm thấy thông tin đặt phòng cho phòng " + rj.getRoomId() + " với thời gian " + rj.getStartTime() + " đến " + rj.getEndTime());
                    }
                }
            }
            gridRoomPanel.updateRoomItemStatus(updatedBookingResponse);
        }
    }

    private String handleCheckingRoomSuccess(RoomJob rj,BookingResponse res) {
        System.out.println("Xử lý chuyển trạng thái phòng " + rj.getRoomId() + " từ đang check-in sang đang sử dụng");
        if (res == null) {
            System.out.println("lỗi thêm công việc cho phòng" + rj.getRoomId());
            return null;
        }
        CongViec newCv = congViecService.themCongViec(RoomStatus.ROOM_USING_STATUS.getStatus(),
                rj.getEndTime(),
                res.getTimeOut(),
                rj.getRoomId());
        return newCv != null ? newCv.getTenTrangThai() : null;
    }

    private String handleLateOvertimeJob(RoomJob rj) {
        System.out.println("Xử lý chuyển trạng thái phòng " + rj.getRoomId() + " từ đang sử dụng sang trễ trả phòng");
        return congViecService.themCongViec(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus(),
                rj.getEndTime(),
                Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)),
                rj.getRoomId()).getTenTrangThai();
    }

    private void handleAutoCheckOut(RoomJob rj) {
//        return true;
    }
}

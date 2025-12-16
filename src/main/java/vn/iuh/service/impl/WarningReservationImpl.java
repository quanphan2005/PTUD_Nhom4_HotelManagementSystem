package vn.iuh.service.impl;

import vn.iuh.constraint.RoomStatus;
import vn.iuh.constraint.WorkTimeCost;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.dto.repository.WarningReservation;
import vn.iuh.entity.CongViec;
import vn.iuh.exception.BusinessException;
import vn.iuh.service.CheckOutService;
import vn.iuh.service.CongViecService;
import vn.iuh.service.WarningReservationService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarningReservationImpl implements WarningReservationService {
    private final CongViecDAO congViecDAO;
    private final CongViecService congViecService;
    private final CheckOutService checkOutService;

    public WarningReservationImpl() {
        this.congViecDAO = new CongViecDAO();
        congViecService = new CongViecServiceImpl();
        checkOutService = new CheckOutServiceImpl();
    }

    @Override
    public void excute(){
        List<WarningReservation> warningList = congViecDAO.getAllWarningReservations();
        if(warningList == null || warningList.isEmpty()) return;

        Map<RoomStatus, List<vn.iuh.dto.repository.WarningReservation>> seperatedMap = new HashMap<>();

        for (RoomStatus rs : RoomStatus.values()) {
            seperatedMap.put(rs, new ArrayList<>());
        }

        for (WarningReservation wr : warningList) {
            RoomStatus status = mapToRoomStatus(wr.getJobName());
            seperatedMap.get(status).add(wr);
        }

        List<String> deletedJobIdList = warningList.stream().map(WarningReservation::getJobId).toList();
        congViecDAO.xoaDanhSachCongViec(deletedJobIdList);

        handleCheckOutReservation(seperatedMap.get(RoomStatus.ROOM_CHECKOUT_LATE_STATUS));
        handleCheckOutReservation(seperatedMap.get(RoomStatus.ROOM_USING_STATUS));
        handleOutdatedWaitingForCheckInJob(seperatedMap.get(RoomStatus.ROOM_BOOKED_STATUS));
        handleOutdatedCheckingJob(seperatedMap.get(RoomStatus.ROOM_CHECKING_STATUS));
    }

    private RoomStatus mapToRoomStatus(String jobName) {
        if(jobName == null) return RoomStatus.ROOM_EMPTY_STATUS;

        return switch (jobName.trim().toUpperCase()) {
            case "CHỜ CHECKIN" -> RoomStatus.ROOM_BOOKED_STATUS;
            case "KIỂM TRA" -> RoomStatus.ROOM_CHECKING_STATUS;
            case "SỬ DỤNG"-> RoomStatus.ROOM_USING_STATUS;
            case "CHECKOUT TRỄ" -> RoomStatus.ROOM_CHECKOUT_LATE_STATUS;
            default -> RoomStatus.ROOM_EMPTY_STATUS;
        };
    }

    private void handleCheckOutReservation(List<vn.iuh.dto.repository.WarningReservation> list){
        List<String> alreadyCheckOutReservationId = new ArrayList<>();
        for(vn.iuh.dto.repository.WarningReservation wr : list){
            if(!alreadyCheckOutReservationId.contains(wr.getReservationId())){
                checkOutService.handleSimpleAutoCheckOut(wr);
                alreadyCheckOutReservationId.add(wr.getReservationId());
            }
        }
    }

    private void handleOutdatedWaitingForCheckInJob(List<vn.iuh.dto.repository.WarningReservation> list){
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        //Chỉ tạo công việc đang kiểm tra cho những phòng có thời gian trả phòng sau thời điểm hiện tại
        //Những phòng còn lại coi như đã xóa Job
        List<CongViec> checkingJobs = createCheckingJobForRooms(list.stream()
                .filter(wr -> wr.getCheckoutTime().after(now))
                .map(vn.iuh.dto.repository.WarningReservation::getRoomId)
                .toList());

        congViecDAO.themDanhSachCongViec(checkingJobs);
    }

    private void handleOutdatedCheckingJob(List<vn.iuh.dto.repository.WarningReservation> list){
        List<CongViec> usageJobs = new ArrayList<>();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        String newJobId = null;
        for(vn.iuh.dto.repository.WarningReservation wr : list){
            if(wr.getCheckoutTime().after(now)){
                newJobId = congViecService.taoMaCongViecMoi(newJobId);
                Timestamp startTime = new Timestamp(wr.getEndTimeJob().getTime() + 1);
                usageJobs.add(new CongViec(newJobId,
                        RoomStatus.ROOM_USING_STATUS.status,
                        startTime,
                        wr.getCheckoutTime(),
                        wr.getRoomId(),
                        null));
            }
        }
        congViecDAO.themDanhSachCongViec(usageJobs);
    }

    private List<CongViec> createCheckingJobForRooms(List<String> roomIdList){
        try {
            Timestamp tgBatDau = Timestamp.valueOf(LocalDateTime.now());
            Timestamp tgKetThuc = Timestamp.valueOf(LocalDateTime.now().plusMinutes(WorkTimeCost.CHECKING_WAITING_TIME.getMinutes()));
            return congViecService.taoDanhSachCongViec(
                    RoomStatus.ROOM_CHECKING_STATUS.getStatus(),
                    tgBatDau,
                    tgKetThuc,
                    roomIdList);
        }catch (BusinessException e){
            System.out.println(e.getMessage());
            throw new BusinessException("Lỗi khi tạo công việc kiểm tra cho các phòng");
        }
    }

}

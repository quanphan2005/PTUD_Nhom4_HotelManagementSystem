package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.CongViec;
import vn.iuh.exception.BusinessException;
import vn.iuh.service.CongViecService;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CongViecServiceImpl implements CongViecService {
    private final CongViecDAO jobDAO;
    private final ChiTietDatPhongDAO bookingDetailDAO;
    private final Map<String, List<String>> validTransitions = Map.of(
            RoomStatus.ROOM_BOOKED_STATUS.getStatus(), List.of(RoomStatus.ROOM_CHECKING_STATUS.getStatus()),
            RoomStatus.ROOM_CHECKING_STATUS.getStatus(), List.of(RoomStatus.ROOM_USING_STATUS.getStatus(), RoomStatus.ROOM_CLEANING_STATUS.getStatus()),
            RoomStatus.ROOM_USING_STATUS.getStatus(), List.of(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus(), RoomStatus.ROOM_CLEANING_STATUS.getStatus()),
            RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus(), List.of(RoomStatus.ROOM_CLEANING_STATUS.getStatus())
    );

    public CongViecServiceImpl() {
        this.jobDAO = new CongViecDAO();
        this.bookingDetailDAO = new ChiTietDatPhongDAO();
    }


    @Override
    public CongViec themCongViec(String tenTrangThai, Timestamp tgBatDau, Timestamp tgKetThuc, String maPhong) {
        tgBatDau = new Timestamp(tgBatDau.getTime() + 1);
        System.out.println("Thêm công việc " + tenTrangThai + " cho phòng " + maPhong + " từ " + tgBatDau + " đến " + tgKetThuc);
//        var isOverlap = jobDAO.kiemTraThoiGianCVTaiPhong(maPhong, tgBatDau, tgKetThuc);
//        if (isOverlap) {
//            throw new RuntimeException("Đã có công việc tại phòng này và tại thời gian này");
//        }
//        else {
            String id = taoMaCongViecMoi(null);
            var congViecHientai = jobDAO.layCongViecHienTaiCuaPhong(maPhong);
            if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(tenTrangThai)){
                return jobDAO.themCongViec(new CongViec(id, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
            }
            else if(RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(tenTrangThai)){
                if(congViecHientai == null){
                    return jobDAO.themCongViec(new CongViec(id, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
                }
                else if(RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())){
//              Kết thúc chờ check_in tại thời điểm hiện tại
                    var isFinished = jobDAO.capNhatThoiGianKetThuc(congViecHientai.getMaCongViec(), new Timestamp(System.currentTimeMillis()), true);
                    if(isFinished){
                        return jobDAO.themCongViec(new CongViec(id, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
                    } else {
                        throw new RuntimeException("Không thể kết thúc công việc " + congViecHientai.getTenTrangThai() +" của phòng");
                    }
                } else {
                    throw new RuntimeException("Phòng hiện tại không được đặt trước");
                }
            }
            else if(RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(tenTrangThai)){
                if(RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())){
                    var isFinished = jobDAO.capNhatThoiGianKetThuc(congViecHientai.getMaCongViec(), new Timestamp(System.currentTimeMillis()), true);
                    if(isFinished){
                        return jobDAO.themCongViec(new CongViec(id, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
                    } else {
                        throw new RuntimeException("Không thể kết thúc công việc " + congViecHientai.getTenTrangThai() +" hiện tại của phòng");
                    }
                }
                else {
                    System.out.println("Không thêm được trạng thái " + tenTrangThai + " cho phòng " + maPhong + " vì trạng thái hiện tại là " + congViecHientai.getTenTrangThai());
                    throw new RuntimeException("Phòng hiện tại không được checkin");
                }
            }
            else if (RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus().equalsIgnoreCase(tenTrangThai)){
                if(RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())){
                    var isFinished = jobDAO.capNhatThoiGianKetThuc(congViecHientai.getMaCongViec(), new Timestamp(System.currentTimeMillis()), true);
                    if(isFinished) {
                        return jobDAO.themCongViec(new CongViec(id, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
                    }
                    else {
                        throw new RuntimeException("Không thể kết thúc công việc " + congViecHientai.getTenTrangThai() +" hiện tại của phòng");
                    }
//          }

                } else {
                    System.out.println("Không thêm được trạng thái " + tenTrangThai + " cho phòng " + maPhong + " vì trạng thái hiện tại là " + congViecHientai.getTenTrangThai());
                    throw new RuntimeException("Phòng hiện tại không được checkin");
                }
            }else if(RoomStatus.ROOM_CLEANING_STATUS.getStatus().equalsIgnoreCase(tenTrangThai)){
                if(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())
                        || RoomStatus.ROOM_USING_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())
                        || RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())){
                    var isFinished = jobDAO.capNhatThoiGianKetThuc(congViecHientai.getMaCongViec(), new Timestamp(System.currentTimeMillis()), true);
                    if(isFinished){
                        return jobDAO.themCongViec(new CongViec(id, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
                    } else {
                        throw new RuntimeException("Không thể kết thúc công việc " + congViecHientai.getTenTrangThai() +" hiện tại của phòng");
                    }
                }
                else {
                    System.out.println("Không thêm được trạng thái " + tenTrangThai + " cho phòng " + maPhong + " vì trạng thái hiện tại là " + congViecHientai.getTenTrangThai());
                    throw new RuntimeException("Phòng hiện tại không được checkin");
                }
            }
            else {
                throw new RuntimeException("Trạng thái công việc không hợp lệ");
            }
//        }
    }

    public boolean giaHanCheckOutTre(String roomId){
        var congViecHientai = jobDAO.layCongViecHienTaiCuaPhong(roomId);
        ChiTietDatPhong theLastest = bookingDetailDAO.findLastestByRoom(roomId);
        double soGioChoToiNguoiTiepTheo = tinhKhoangCachGio(theLastest.getTgNhanPhong());
        int maxPeriodWhenNotExistsNextRD = 480; //phút
        int maxPeriodWhenExists = 120; // phút
        if(RoomStatus.ROOM_CHECKOUT_LATE_STATUS.getStatus().equalsIgnoreCase(congViecHientai.getTenTrangThai())){
            if(soGioChoToiNguoiTiepTheo > 8){
                Timestamp newEndTime = new Timestamp(System.currentTimeMillis() + maxPeriodWhenNotExistsNextRD * 60 * 1000); // gia hạn thêm 6 tiếng
                return jobDAO.capNhatThoiGianKetThuc(congViecHientai.getMaCongViec(), newEndTime, false);
            }
            else {
                Timestamp newEndTime = new Timestamp(theLastest.getTgNhanPhong().getTime() - maxPeriodWhenExists * 60 * 1000); // gia hạn đến 2 tiếng trước khi khách tiếp theo nhận phòng
                return jobDAO.capNhatThoiGianKetThuc(congViecHientai.getMaCongViec(), newEndTime, false);
            }
        } else {
            throw new RuntimeException("Phòng hiện tại không trong trạng thái trễ check out");
        }
    }


    public String taoMaCongViecMoi(String maCongViecMoi){
        if(maCongViecMoi == null){
            maCongViecMoi = jobDAO.timCongViecMoiNhat().getMaCongViec();
        }
        return EntityUtil.increaseEntityID( maCongViecMoi,
                EntityIDSymbol.JOB_PREFIX.getPrefix(),
                EntityIDSymbol.JOB_PREFIX.getLength());
    }

    /**
     * Tính khoảng cách giờ giữa thời điểm hiện tại và một timestamp
     * @param timestamp thời điểm cần so sánh
     * @return số giờ (có thể là số thập phân)
     */
    private double tinhKhoangCachGio(Timestamp timestamp) {
        if (timestamp == null) return 0;
        long currentMillis = System.currentTimeMillis();
        long otherMillis = timestamp.getTime();
        long diffMillis = Math.abs(currentMillis - otherMillis);
        return diffMillis / (60.0 * 60 * 1000);
    }

    @Override
    public boolean removeOutDateJob(String jobId){
        return jobDAO.removeJob(jobId);
    }

    public List<CongViec> taoDanhSachCongViec(String tenTrangThai, Timestamp tgBatDau, Timestamp tgKetThuc, List<String> danhSachMaPhong){
        String maCongViecMoi = taoMaCongViecMoi(null);
        List<CongViec> danhSachCongViecMoi = new ArrayList<>();
        List<String> danhSachCongViecCanKetThuc = new ArrayList<>();
        for(String maPhong : danhSachMaPhong){
            tgBatDau = new Timestamp(tgBatDau.getTime() + 1);
            var congViecHienTai = jobDAO.layCongViecHienTaiCuaPhong(maPhong);

            // Nếu phòng chưa có công việc trước đó
            if (congViecHienTai == null) {
                if (RoomStatus.ROOM_BOOKED_STATUS.getStatus().equalsIgnoreCase(tenTrangThai) ||
                        RoomStatus.ROOM_CHECKING_STATUS.getStatus().equalsIgnoreCase(tenTrangThai)) {
                    danhSachCongViecMoi.add(new CongViec(maCongViecMoi, tenTrangThai, tgBatDau, tgKetThuc, maPhong, null));
                    continue;
                }
                throw new BusinessException("Không thể thêm trạng thái " + tenTrangThai + " vì phòng chưa được đặt.");
            }
            String currentStatus = congViecHienTai.getTenTrangThai();

            // Map các trạng thái cho phép chuyển tiếp
            List<String> allowedNextStatuses = validTransitions.getOrDefault(currentStatus, List.of());

            if (!allowedNextStatuses.contains(tenTrangThai)) {
                System.out.printf("Không thể chuyển từ %s sang %s cho phòng %s%n", currentStatus, tenTrangThai, maPhong);
                throw new BusinessException("Không thể thêm trạng trái " + tenTrangThai + " vì trạng thái trước đó là "+ currentStatus);
            }
            danhSachCongViecMoi.add(new CongViec(maCongViecMoi, tenTrangThai,tgBatDau, tgKetThuc, maPhong, null));
            danhSachCongViecCanKetThuc.add(congViecHienTai.getMaCongViec());
            maCongViecMoi = taoMaCongViecMoi(maCongViecMoi);
        }
        try {
            int affectedRows = jobDAO.xoaDanhSachCongViec(danhSachCongViecCanKetThuc);
            if(affectedRows != danhSachCongViecCanKetThuc.size()){
                throw new RuntimeException();
            }
        }catch(RuntimeException e){
            throw new BusinessException("Không thể xóa xóa trạng thái cũ của tất cả các phòng");
        }
        return danhSachCongViecMoi;
    }


}

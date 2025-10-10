package vn.iuh.service.impl;

import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.CongViecDAO;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.CongViec;
import vn.iuh.service.CongViecService;
import vn.iuh.util.EntityUtil;

import java.sql.Timestamp;

public class CongViecServiceImpl implements CongViecService {
    private final CongViecDAO jobDAO;
    private final ChiTietDatPhongDAO bookingDetailDAO;
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
            String id = taoMaCongViecMoi();
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


    public String taoMaCongViecMoi(){
        CongViec cv = jobDAO.timCongViecMoiNhat();
        String maCV = (cv == null) ? null: cv.getMaCongViec();
        return EntityUtil.increaseEntityID( maCV,
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


}

package vn.iuh.service.impl;

import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.DatPhongDAO;
import vn.iuh.dao.LichSuRaNgoaiDAO;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.LichSuRaNgoai;
import vn.iuh.service.CheckOutService;

import java.util.List;
import java.util.Objects;

public class CheckOutServiceImpl implements CheckOutService {
    private final DatPhongDAO datPhongDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
    private final LichSuRaNgoaiDAO lichSuRaNgoaiDAO;
    private final LichSuRaNgoaiServiceimpl lichSuRaNgoaiServiceimpl ;

    public CheckOutServiceImpl() {
        this.datPhongDAO = new DatPhongDAO();
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
        this.lichSuRaNgoaiDAO = new LichSuRaNgoaiDAO();
        this.lichSuRaNgoaiServiceimpl = new LichSuRaNgoaiServiceimpl();
    }

    public boolean checkOutReservation(String reservationId){

        //Tìm đơn đặt phòng
        var reservation = datPhongDAO.getDonDatPhongById(reservationId);
        if(Objects.isNull(reservation)){
            throw new RuntimeException("Không tìm thấy đơn đặt phòng với mã: " + reservationId);
        }
//        Lấy tất cả chi tiết đặt phòng của đơn đặt phòng
        List<ChiTietDatPhong> chiTietDatPhongList = chiTietDatPhongDAO.findByBookingId(reservationId);

        //Cập nhật ChiTietDatPhong thành trả phòng
        int ketQuaCapNhatKetThucCTDP = chiTietDatPhongDAO.capNhatKetThucCTDP(chiTietDatPhongList);

        //Thêm lịch sử check-out lần cuối cùng
        lichSuRaNgoaiServiceimpl.themLichSuRaNgoai(chiTietDatPhongList);

        return false;
    }
}

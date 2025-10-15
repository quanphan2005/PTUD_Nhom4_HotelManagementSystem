package vn.iuh.service.impl;

import vn.iuh.dao.*;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.CongViec;
import vn.iuh.entity.LichSuThaoTac;
import vn.iuh.entity.PhienDangNhap;
import vn.iuh.service.BookThemGioService;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class BookThemGioServiceImpl {
//
//    private final DatPhongDAO datPhongDAO;
//    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
//    private final CongViecDAO congViecDAO;
//    private final LichSuThaoTacDAO lichSuThaoTacDAO;
//    private final PhienDangNhapDAO phienDangNhapDAO;
//
//    public BookThemGioServiceImpl() {
//        this.congViecDAO = new CongViecDAO();
//        this.lichSuThaoTacDAO = new LichSuThaoTacDAO();
//        this.datPhongDAO = new DatPhongDAO();
//        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
//        this.phienDangNhapDAO = new PhienDangNhapDAO();
//    }
//
//    @Override
//    public boolean bookThemGio(String maPhong, String maDonDatPhong) {
//        PhienDangNhap phienDangNhapMoiNhat = phienDangNhapDAO.timPhienDangNhapMoiNhat();
//        String maPhienDangNhap = phienDangNhapMoiNhat.getMaPhienDangNhap();
//
//        DatPhongDAO datPhongDAO =  new DatPhongDAO();
//        try {
//            datPhongDAO.khoiTaoGiaoTac();
//            Connection conn = datPhongDAO.getConnection();
//
//            CongViecDAO congViecDAO = new CongViecDAO(conn);
//            ChiTietDatPhongDAO chiTietDatPhongDAO = new ChiTietDatPhongDAO(conn);
//            LichSuThaoTacDAO LichSuThaoTacDAO = new LichSuThaoTacDAO(conn);
//
//            ChiTietDatPhong chiTietDatPhongToCheck = chiTietDatPhongDAO.timChiTietDatPhongMoiNhatTheoDonDatPhong(maDonDatPhong);
//            if(chiTietDatPhongToCheck == null) {
//                throw new IllegalArgumentException("Đơn đặt phòng " + maDonDatPhong + " không tồn tại!");
//            }
//
//            if(chiTietDatPhongToCheck.getMaPhong() == null || !chiTietDatPhongToCheck.getMaPhong().equals(maPhong)) {
//                throw new IllegalArgumentException("Mã phòng không khớp với mã đơn đặt phòng!!");
//            }
//
//            Timestamp tgTraPhong= chiTietDatPhongToCheck.getTgTraPhong();
//
//            if(tgTraPhong!=null){
//                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
//                if(now.after(tgTraPhong)){
//                    throw new IllegalArgumentException("Đơn đặt phòng này đã kết thúc, không thể book thêm giờ!");
//                }
//            }
//
//        }
//    }
}

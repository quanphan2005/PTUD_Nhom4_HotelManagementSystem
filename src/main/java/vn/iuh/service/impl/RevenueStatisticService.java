package vn.iuh.service.impl;

import vn.iuh.dao.*;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.response.InvoiceStatistic;
import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.HoaDon;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.entity.PhongTinhPhuPhi;
import vn.iuh.gui.panel.statistic.FilterStatistic;
import vn.iuh.service.LoaiPhongService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RevenueStatisticService {
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
    private final PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private final DonGoiDichVuDao donGoiDichVuDao;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final LoaiPhongDAO loaiPhongDAO;
    public RevenueStatisticService() {
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
        this.donGoiDichVuDao = new DonGoiDichVuDao();
        this.chiTietHoaDonDAO= new ChiTietHoaDonDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
        this.loaiPhongDAO = new LoaiPhongDAO();
    }

//
//    public List<InvoiceStatistic> layThongKeVoiDieuKien(FilterStatistic dieuKien){
//        var danhSachHoaDon = hoaDonDAO.layDanhSachHoaDonTrongKhoang(dieuKien.getStartDate(), dieuKien.getEndDate(), dieuKien.getEmployeeName());
//        List<InvoiceStatistic> danhSachKetQua = new ArrayList<>();
//        for(HoaDon hd : danhSachHoaDon){
//            var danhSachChiTietHoaDon = chiTietHoaDonDAO.getInvoiceDetaiByInvoiceId(hd.getMaHoaDon());
//            var danhSachChiTietDatPhong = chiTietDatPhongDAO.findByBookingId(hd.getMaDonDatPhong());
//            for(ChiTietHoaDon ct : danhSachChiTietHoaDon){
//                var chiTietDatPhong = danhSachChiTietDatPhong.stream().filter(ctdp -> ct.getMaPhong().equalsIgnoreCase(ctdp.getMaPhong())).findFirst().orElse(null);
//
//                Map<String,BigDecimal> giaPhong = loaiPhongDAO.layGiaLoaiPhongTheoMaPhong(ct.getMaPhong());
//                BigDecimal donGiaNgay = giaPhong.get("gia_ngay");
//                BigDecimal donGiaGio = giaPhong.get("gia_gio");
//                BigDecimal finalDonGiaHienThi;
//                BigDecimal thanhTien;
//                double thoiGianSuDung = ct.getThoiGianSuDung();
//
//                if(thoiGianSuDung > 12){
//                    // tính theo ngày + giờ lẻ
//                    finalDonGiaHienThi = donGiaNgay;
//                    int soNgay = (int) Math.floor(thoiGianSuDung/24);
//                    double soGio = thoiGianSuDung % 24;
//
//                    if(soNgay == 0){
//                        thanhTien = donGiaNgay;
//                    }else{
//                        thanhTien = donGiaNgay.multiply(BigDecimal.valueOf(soNgay));
//                        if(soGio > 0 && soGio <= 12){
//                            BigDecimal tienGioLe = donGiaGio.multiply(BigDecimal.valueOf(soGio));
//                            thanhTien = thanhTien.add(tienGioLe);
//                        }else if(soGio > 12){
//                            thanhTien = donGiaNgay.multiply(BigDecimal.valueOf(soNgay + 1));
//                        }
//                    }
//                }else{   // tính theo giờ
//                    finalDonGiaHienThi = donGiaGio;
//                    thanhTien = donGiaGio.multiply(BigDecimal.valueOf(thoiGianSuDung));
//                }
//                ct.setTongTien(thanhTien);
//                List<PhongDungDichVu> danhSachPhongDungDichVu = donGoiDichVuDao.timDonGoiDichVuBangDonDatPhong(hd.getMaDonDatPhong());
//                List<PhongTinhPhuPhi> danhSachPhongTinhPhuPhi = phongTinhPhuPhiDAO.getPhuPhiTheoMaHoaDon(hd.getMaHoaDon());
//
//                BigDecimal tongTien = BigDecimal.ZERO;
//                BigDecimal tongTienPhong = BigDecimal.ZERO;
//                BigDecimal tongDichVu = BigDecimal.ZERO;
//                BigDecimal tongPhuPhi = BigDecimal.ZERO;
//
//                for(ChiTietHoaDon cthd : danhSachChiTietHoaDon){
//                    tongTien = tongTien.add(cthd.getTongTien());
//                    tongTienPhong = tongTienPhong.add(cthd.getTongTien());
//                }
//
//                for(PhongDungDichVu pddv : danhSachPhongDungDichVu){
//                    tongTien = tongTien.add(pddv.tinhThanhTien());
//                    tongDichVu = tongDichVu.add(pddv.tinhThanhTien());
//                }
//
//                for(PhongTinhPhuPhi ptpp : danhSachPhongTinhPhuPhi){
//                    tongTien = tongTien.add(ptpp.getDonGiaPhuPhi());
//                    tongPhuPhi = tongPhuPhi.add(ptpp.getDonGiaPhuPhi());
//                }
//
//
//
//            }
//        }
//    }
}

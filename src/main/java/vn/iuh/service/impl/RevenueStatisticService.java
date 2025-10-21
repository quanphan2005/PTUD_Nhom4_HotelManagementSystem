package vn.iuh.service.impl;

import vn.iuh.constraint.InvoiceType;
import vn.iuh.dao.*;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.response.InvoiceStatistic;
import vn.iuh.entity.*;
import vn.iuh.gui.panel.statistic.FilterStatistic;
import vn.iuh.service.LoaiPhongService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RevenueStatisticService {
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietDatPhongDAO chiTietDatPhongDAO;
    private final PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private final DonGoiDichVuDao donGoiDichVuDao;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final KhachHangDAO khachHangDAO;
    private final NhanVienDAO nhanVienDAO;
    public RevenueStatisticService() {
        this.chiTietDatPhongDAO = new ChiTietDatPhongDAO();
        this.donGoiDichVuDao = new DonGoiDichVuDao();
        this.chiTietHoaDonDAO= new ChiTietHoaDonDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.nhanVienDAO = new NhanVienDAO();
        this.khachHangDAO = new KhachHangDAO();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
    }


    public List<InvoiceStatistic> layThongKeVoiDieuKien(FilterStatistic dieuKien){
        var danhSachHoaDon = hoaDonDAO.layDanhSachHoaDonTrongKhoang(dieuKien.getStartDate(), dieuKien.getEndDate(), dieuKien.getEmployeeName());
        List<InvoiceStatistic> danhSachKetQua = new ArrayList<>();

        for(HoaDon hd : danhSachHoaDon){
            var danhSachChiTietHoaDon = chiTietHoaDonDAO.getInvoiceDetaiByInvoiceId(hd.getMaHoaDon());
            var danhSachPhongDungDichVu = donGoiDichVuDao.timDonGoiDichVuBangDonDatPhong(hd.getMaDonDatPhong());
            var danhSachPhongTinhPhuPhi = phongTinhPhuPhiDAO.getPhuPhiTheoMaHoaDon(hd.getMaHoaDon());
            BigDecimal tongTienPhong = danhSachChiTietHoaDon.stream()
                    .map(ChiTietHoaDon::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tongTienDichVu = danhSachPhongDungDichVu.stream()
                    .map(PhongDungDichVu::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tongPhuPhi  = danhSachPhongTinhPhuPhi.stream()
                    .map(PhongTinhPhuPhi::getTongTien)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            KhachHang kh  = khachHangDAO.timKhachHang(hd.getMaKhachHang());
            danhSachKetQua.add(new InvoiceStatistic(hd.getMaHoaDon(),
                                                    kh.getTenKhachHang(),
                                                    hd.getKieuHoaDon().equalsIgnoreCase(InvoiceType.DEPOSIT_INVOICE.getStatus()) ,
                                                    hd.getThoiGianTao(),
                                                    tongTienPhong,
                                                    tongTienDichVu,
                                                    tongPhuPhi,
                                                    hd.getTienThue(),
                                                    hd.getTongHoaDon()));
        }
        return danhSachKetQua;
    }

}

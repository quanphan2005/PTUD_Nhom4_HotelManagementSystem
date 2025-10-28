package vn.iuh.service.impl;

import vn.iuh.constraint.InvoiceType;
import vn.iuh.constraint.PaymentStatus;
import vn.iuh.dao.*;
import vn.iuh.dto.event.create.DonGoiDichVu;
import vn.iuh.dto.response.InvoiceStatistic;
import vn.iuh.entity.*;
import vn.iuh.gui.panel.statistic.FilterStatistic;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.util.PriceFormat;

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

    public List<InvoiceStatistic> layThongKeVoiDieuKien(FilterStatistic dieuKien) {
        var danhSachHoaDon = hoaDonDAO.layDanhSachHoaDonTrongKhoang(dieuKien.getStartDate(), dieuKien.getEndDate(), dieuKien.getEmployeeId());
        List<InvoiceStatistic> danhSachKetQua = new ArrayList<>();

        for (HoaDon hd : danhSachHoaDon) {
            if (InvoiceType.PAYMENT_INVOICE.getStatus().equalsIgnoreCase(hd.getKieuHoaDon())
                    && PaymentStatus.PAID.getStatus().equalsIgnoreCase(hd.getTinhTrangThanhToan())
            ) {
                var danhSachChiTietHoaDon = chiTietHoaDonDAO.getInvoiceDetaiByInvoiceId(hd.getMaHoaDon());
                var danhSachPhongDungDichVu = donGoiDichVuDao.timDonGoiDichVuBangDonDatPhong(hd.getMaDonDatPhong());
                var danhSachPhongTinhPhuPhi = phongTinhPhuPhiDAO.timPhuPhiTheoMaDonDatPhong(hd.getMaDonDatPhong());
                BigDecimal tongTienPhong = danhSachChiTietHoaDon.stream()
                        .map(ChiTietHoaDon::getTongTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal tongTienDichVu = danhSachPhongDungDichVu.stream()
                        .map(PhongDungDichVu::getTongTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal tongPhuPhi = danhSachPhongTinhPhuPhi.stream()
                        .map(PhongTinhPhuPhi::getTongTien)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                KhachHang kh = khachHangDAO.timKhachHang(hd.getMaKhachHang());
                danhSachKetQua.add(new InvoiceStatistic(hd.getMaHoaDon(),
                                kh.getTenKhachHang(),
                                hd.getKieuHoaDon().equalsIgnoreCase(InvoiceType.DEPOSIT_INVOICE.getStatus()),
                                hd.getThoiGianTao(),
                                PriceFormat.lamTronDenHangNghin(tongTienPhong),
                                PriceFormat.lamTronDenHangNghin(tongTienDichVu),
                                PriceFormat.lamTronDenHangNghin(tongPhuPhi),
                                PriceFormat.lamTronDenHangNghin(hd.getTienThue()),
                                PriceFormat.lamTronDenHangNghin(hd.getTongHoaDon())
                ));
            }
        }
        return danhSachKetQua;
    }
}

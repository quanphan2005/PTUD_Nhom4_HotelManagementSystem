package vn.iuh.service.impl;

import vn.iuh.dao.ChiTietHoaDonDAO;
import vn.iuh.dao.DonGoiDichVuDao;
import vn.iuh.dao.HoaDonDAO;
import vn.iuh.dao.PhongTinhPhuPhiDAO;
import vn.iuh.dto.event.create.InvoiceCreationEvent;
import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.HoaDon;
import vn.iuh.service.HoaDonService;

import java.util.List;

public class HoaDonServiceImpl implements HoaDonService {
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private final DonGoiDichVuDao phongDungDichVuDAO;
    
    public HoaDonServiceImpl() {
        this.hoaDonDAO = new HoaDonDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
        this.phongDungDichVuDAO = new DonGoiDichVuDao();
    }

    @Override
    public HoaDon getInvoiceByID(String id) {

        return null;
    }

    @Override
    public HoaDon createInvoice(InvoiceCreationEvent event) {
        return null;
    }

    @Override
    public HoaDon getLatestInvoice() {
        return null;
    }

    @Override
    public List<ChiTietHoaDon> insertListChiTietHoaDon(List<ChiTietHoaDon> chiTietHoaDonList) {
        return List.of();
    }
}

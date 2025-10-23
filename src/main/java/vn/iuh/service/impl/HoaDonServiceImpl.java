package vn.iuh.service.impl;

import vn.iuh.dao.*;
import vn.iuh.dto.event.create.InvoiceCreationEvent;
import vn.iuh.entity.*;
import vn.iuh.gui.dialog.InvoiceDialog;
import vn.iuh.service.HoaDonService;

import javax.swing.*;
import java.util.List;

public class HoaDonServiceImpl implements HoaDonService {
    private final HoaDonDAO hoaDonDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;
    private final PhongTinhPhuPhiDAO phongTinhPhuPhiDAO;
    private final DonGoiDichVuDao phongDungDichVuDAO;
    private final KhachHangDAO khachHangDAO;
    private final NhanVienDAO nhanVienDAO;

    public HoaDonServiceImpl() {
        this.hoaDonDAO = new HoaDonDAO();
        this.chiTietHoaDonDAO = new ChiTietHoaDonDAO();
        this.phongTinhPhuPhiDAO = new PhongTinhPhuPhiDAO();
        this.phongDungDichVuDAO = new DonGoiDichVuDao();
        this.khachHangDAO = new KhachHangDAO();
        this.nhanVienDAO = new NhanVienDAO();
    }

    @Override
    public HoaDon getInvoiceByID(String id) {

        return null;
    }

    @Override
    public InvoiceCreationEvent createInvoice(InvoiceCreationEvent event) {
        SwingUtilities.invokeLater(() -> {
            InvoiceDialog dialog = new InvoiceDialog(event);
            dialog.setVisible(true);
        });
        return event;
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

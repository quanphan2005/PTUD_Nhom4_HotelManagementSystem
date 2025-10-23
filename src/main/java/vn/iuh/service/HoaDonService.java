package vn.iuh.service;

import vn.iuh.dto.event.create.InvoiceCreationEvent;
import vn.iuh.dto.response.InvoiceResponse;
import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.HoaDon;

import java.util.List;

public interface HoaDonService {
    HoaDon getInvoiceByID(String id);
    InvoiceResponse createInvoice(InvoiceResponse event);
    HoaDon getLatestInvoice();
    List<ChiTietHoaDon> insertListChiTietHoaDon(List<ChiTietHoaDon> chiTietHoaDonList);
}

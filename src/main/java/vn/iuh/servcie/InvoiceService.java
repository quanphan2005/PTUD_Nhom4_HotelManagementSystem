package vn.iuh.servcie;

import vn.iuh.entity.HoaDon;

public interface InvoiceService {
    HoaDon getInvoiceByID(String id);
    HoaDon createInvoice(HoaDon hoaDon);
    HoaDon updateInvoice(HoaDon hoaDon);
    boolean deleteInvoiceByID(String id);
}

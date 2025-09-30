package vn.iuh.servcie;

import vn.iuh.entity.Invoice;

public interface InvoiceService {
    Invoice getInvoiceByID(String id);
    Invoice createInvoice(Invoice invoice);
    Invoice updateInvoice(Invoice invoice);
    boolean deleteInvoiceByID(String id);
}

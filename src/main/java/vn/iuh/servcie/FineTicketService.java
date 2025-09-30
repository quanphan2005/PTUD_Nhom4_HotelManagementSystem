package vn.iuh.servcie;

import vn.iuh.entity.BienBan;

public interface FineTicketService {
    BienBan getFineTicketByID(String id);
    BienBan createFineTicket(BienBan bienBan);
    BienBan updateFineTicket(BienBan bienBan);
    boolean deleteFineTicketByID(String id);
}

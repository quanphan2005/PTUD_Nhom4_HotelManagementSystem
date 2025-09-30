package vn.iuh.servcie;

import vn.iuh.entity.FineTicket;

public interface FineTicketService {
    FineTicket getFineTicketByID(String id);
    FineTicket createFineTicket(FineTicket fineTicket);
    FineTicket updateFineTicket(FineTicket fineTicket);
    boolean deleteFineTicketByID(String id);
}

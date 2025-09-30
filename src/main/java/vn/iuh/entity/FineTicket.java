package vn.iuh.entity;

import java.sql.Timestamp;

public class FineTicket {
    private String id;
    private Timestamp createTime;
    private String ticketDescription;
    private Double totalFine;
    private String reservationFormId;

    public FineTicket() {
    }

    public FineTicket(String id, Timestamp createTime, String ticketDescription, Double totalFine,
                      String reservationFormId) {
        this.id = id;
        this.createTime = createTime;
        this.ticketDescription = ticketDescription;
        this.totalFine = totalFine;
        this.reservationFormId = reservationFormId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public void setTicketDescription(String ticketDescription) {
        this.ticketDescription = ticketDescription;
    }

    public Double getTotalFine() {
        return totalFine;
    }

    public void setTotalFine(Double totalFine) {
        this.totalFine = totalFine;
    }

    public String getReservationFormId() {
        return reservationFormId;
    }

    public void setReservationFormId(String reservationFormId) {
        this.reservationFormId = reservationFormId;
    }
}

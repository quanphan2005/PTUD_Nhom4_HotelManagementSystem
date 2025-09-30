package vn.iuh.entity;

import java.sql.Time;

public class AdditionalFeeFineTicket {
    private String id;
    private Time createTime;
    private String fineTicketId;
    private String additionalFeeId;

    public AdditionalFeeFineTicket() {
    }

    public AdditionalFeeFineTicket(String id, Time createTime, String fineTicketId, String additionalFeeId) {
        this.id = id;
        this.createTime = createTime;
        this.fineTicketId = fineTicketId;
        this.additionalFeeId = additionalFeeId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Time getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Time createTime) {
        this.createTime = createTime;
    }

    public String getFineTicketId() {
        return fineTicketId;
    }

    public void setFineTicketId(String fineTicketId) {
        this.fineTicketId = fineTicketId;
    }

    public String getAdditionalFeeId() {
        return additionalFeeId;
    }

    public void setAdditionalFeeId(String additionalFeeId) {
        this.additionalFeeId = additionalFeeId;
    }
}

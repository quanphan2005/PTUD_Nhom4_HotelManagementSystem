package vn.iuh.entity;

import java.sql.Timestamp;

public class Invoice {
    private String id;
    private Timestamp creatDate;
    private String payment;
    private double totalPrice;
    private double taxPrice;
    private double totalDue;
    private String invoiceType;
    private String invoiceStatus;
    private String shiftAssignmentId;
    private String reservationFormId;
    private String customerId;

    public Invoice() {
    }

    public Invoice(String id, Timestamp creatDate, String payment, double totalPrice, double taxPrice, double totalDue,
                   String invoiceType, String invoiceStatus, String shiftAssignmentId, String reservationFormId,
                   String customerId) {
        this.id = id;
        this.creatDate = creatDate;
        this.payment = payment;
        this.totalPrice = totalPrice;
        this.taxPrice = taxPrice;
        this.totalDue = totalDue;
        this.invoiceType = invoiceType;
        this.invoiceStatus = invoiceStatus;
        this.shiftAssignmentId = shiftAssignmentId;
        this.reservationFormId = reservationFormId;
        this.customerId = customerId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getCreatDate() {
        return creatDate;
    }

    public void setCreatDate(Timestamp creatDate) {
        this.creatDate = creatDate;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getTaxPrice() {
        return taxPrice;
    }

    public void setTaxPrice(double taxPrice) {
        this.taxPrice = taxPrice;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public void setShiftAssignmentId(String shiftAssignmentId) {
        this.shiftAssignmentId = shiftAssignmentId;
    }

    public String getReservationFormId() {
        return reservationFormId;
    }

    public void setReservationFormId(String reservationFormId) {
        this.reservationFormId = reservationFormId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}

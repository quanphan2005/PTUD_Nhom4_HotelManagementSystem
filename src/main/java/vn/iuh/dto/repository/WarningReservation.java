package vn.iuh.dto.repository;

import vn.iuh.constraint.ReservationType;

import java.sql.Timestamp;

public class WarningReservation {
    private String reservationId;
    private String reservationType;
    private boolean isAdvanced;
    private Timestamp checkinTime;
    private Timestamp checkoutTime;
    private String reservationDetailId;
    private String roomId;
    private String jobId;
    private Timestamp startTimeJob;
    private Timestamp endTimeJob;
    private String jobName;
    private long overDueMillisecond;

    public WarningReservation(String reservationId, String reservationType, boolean isAdvanced, Timestamp checkinTime, Timestamp checkoutTime, String reservationDetailId, String roomId, String jobId, Timestamp startTimeJob, Timestamp endTimeJob, String jobName, long overDueMillisecond) {
        this.reservationId = reservationId;
        this.reservationType = reservationType;
        this.isAdvanced = isAdvanced;
        this.checkinTime = checkinTime;
        this.checkoutTime = checkoutTime;
        this.reservationDetailId = reservationDetailId;
        this.roomId = roomId;
        this.jobId = jobId;
        this.startTimeJob = startTimeJob;
        this.endTimeJob = endTimeJob;
        this.jobName = jobName;
        this.overDueMillisecond = overDueMillisecond;
    }

    public long getOverDueMillisecond() {
        return overDueMillisecond;
    }

    public void setOverDueMillisecond(long overDueMillisecond) {
        this.overDueMillisecond = overDueMillisecond;
    }

    public WarningReservation() {
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }

    public void setAdvanced(boolean advanced) {
        isAdvanced = advanced;
    }

    public Timestamp getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(Timestamp checkinTime) {
        this.checkinTime = checkinTime;
    }

    public Timestamp getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(Timestamp checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public String getReservationDetailId() {
        return reservationDetailId;
    }

    public void setReservationDetailId(String reservationDetailId) {
        this.reservationDetailId = reservationDetailId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Timestamp getStartTimeJob() {
        return startTimeJob;
    }

    public void setStartTimeJob(Timestamp startTimeJob) {
        this.startTimeJob = startTimeJob;
    }

    public Timestamp getEndTimeJob() {
        return endTimeJob;
    }

    public void setEndTimeJob(Timestamp endTimeJob) {
        this.endTimeJob = endTimeJob;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}

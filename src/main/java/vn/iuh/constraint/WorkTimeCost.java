package vn.iuh.constraint;

public enum WorkTimeCost {
    // in minutes
    CHECKING_WAITING_TIME(30),
    CHECKOUT_LATE_MIN(60),
    CHECKOUT_LATE_MAX(6 * 60),
    CLEANING_TIME(120),
    ;

    public final int minutes;

    WorkTimeCost(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }
}

package vn.iuh.util;

import java.util.Date;

public class TimeFilterHelper {
    private static Date checkinTime;
    private static Date checkoutTime;

    public static Date getCheckinTime() {
        return checkinTime;
    }

    public static void setCheckinTime(Date checkinTime) {
        TimeFilterHelper.checkinTime = checkinTime;
    }

    public static Date getCheckoutTime() {
        return checkoutTime;
    }

    public static void setCheckoutTime(Date checkoutTime) {
        TimeFilterHelper.checkoutTime = checkoutTime;
    }
}

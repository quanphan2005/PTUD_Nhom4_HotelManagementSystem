package vn.iuh.util;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class TimeFormat {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public static DateTimeFormatter getFormatter() {
        return formatter;
    }

    public static String formatTime(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return timestamp.toLocalDateTime().format(formatter);
    }

    public static double calculateHoursBetween(Timestamp tgNhanPhong, Timestamp tgTraPhong) {
        if (tgNhanPhong == null || tgTraPhong == null) {
            return 0;
        }
        long milliseconds = tgTraPhong.getTime() - tgNhanPhong.getTime();
        double hours = milliseconds / (1000.0 * 60 * 60);
        return hours;
    }
}

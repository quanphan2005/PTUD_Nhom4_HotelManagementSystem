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
}

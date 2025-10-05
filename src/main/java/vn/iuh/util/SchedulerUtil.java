package vn.iuh.util;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerUtil {
    private static Scheduler instance;

    private SchedulerUtil() {
        // ko làm gì cả hêhe, do synchronized ở dưới tạo instance ngay khi load class này
    }

    public static synchronized Scheduler getInstance() throws SchedulerException {
        if (instance == null) {
            instance = StdSchedulerFactory.getDefaultScheduler();
        }
        return instance;
    }
}

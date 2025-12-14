package vn.iuh.util;
import org.quartz.*;
import vn.iuh.schedule.AutomaticallyBackupDif;
import vn.iuh.schedule.AutomaticallyBackupFull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BackupDatabase {
    public static boolean backupDifDatabase(String filePath) throws SQLException {
        Connection connection = DatabaseUtil.getConnect();

        try (connection){
            String backupQuery = "BACKUP DATABASE [QLKS] TO DISK = '" + filePath + "' WITH DIFFERENTIAL ";
            Statement statement = connection.createStatement();
            statement.execute(backupQuery);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean backupFullDatabase(String filePath) throws SQLException {
        Connection connection = DatabaseUtil.getConnect();
        String backupQuery = "BACKUP DATABASE [QLKS] TO DISK = '" + filePath + "' ";

        try (Statement st = connection.createStatement()) {
            st.execute(backupQuery);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void scheduleFullBackup(){
        try {
            Scheduler scheduler = SchedulerUtil.getInstance();

            JobDetail job = JobBuilder.newJob(AutomaticallyBackupFull.class)
                    .withIdentity("fullBackupJob", "backupGroup")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("fullBackupTrigger", "backupGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 ? * SUN"))
                    .build();

            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            System.out.println("Failed to create automatically full backup cronjob");
        }
    }

    public static void scheduleDiffBackup() {
        try {
            Scheduler scheduler = SchedulerUtil.getInstance();
            JobDetail job = JobBuilder.newJob(AutomaticallyBackupDif.class)
                    .withIdentity("diffBackupJob", "backupGroup")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("diffBackupTrigger", "backupGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                    .build();

            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            System.out.println("Failed to create automatically dif backup cronjob");
        }
    }
}

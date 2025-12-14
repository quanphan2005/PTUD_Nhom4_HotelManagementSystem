package vn.iuh.schedule;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import vn.iuh.util.BackupDatabase;

import java.sql.SQLException;

public class AutomaticallyBackupDif implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
            String folderPath = dataMap.getString("folderPath");
            BackupDatabase.backupDifDatabase(folderPath);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}

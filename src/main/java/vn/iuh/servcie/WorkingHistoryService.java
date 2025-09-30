package vn.iuh.servcie;

import vn.iuh.entity.WorkingHistory;

public interface WorkingHistoryService {
    WorkingHistory getWorkingHistoryByID(String id);
    WorkingHistory createWorkingHistory(WorkingHistory workingHistory);
    WorkingHistory updateWorkingHistory(WorkingHistory workingHistory);
    boolean deleteWorkingHistoryByID(String id);
}

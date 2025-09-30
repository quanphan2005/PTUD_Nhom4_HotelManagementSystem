package vn.iuh.servcie;

import vn.iuh.entity.LichSuThaoTac;

public interface WorkingHistoryService {
    LichSuThaoTac getWorkingHistoryByID(String id);
    LichSuThaoTac createWorkingHistory(LichSuThaoTac lichSuThaoTac);
    LichSuThaoTac updateWorkingHistory(LichSuThaoTac lichSuThaoTac);
    boolean deleteWorkingHistoryByID(String id);
}

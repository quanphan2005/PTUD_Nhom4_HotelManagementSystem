package vn.iuh.gui.panel.statistic;

import java.sql.Timestamp;
import java.time.LocalDate;

public class FilterStatistic {
    private Timestamp startDate;
    private Timestamp endDate;
    private String employeeName;

    public FilterStatistic(Timestamp startDate, Timestamp endDate, String employeeName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.employeeName = employeeName;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public String getEmployeeName() {
        return employeeName;
    }
}

package vn.iuh.gui.panel.statistic;

import java.time.LocalDate;

public class FilterStatistic {
    private LocalDate startDate;
    private LocalDate endDate;
    private String employeeName;

    public FilterStatistic(LocalDate startDate, LocalDate endDate, String employeeName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.employeeName = employeeName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getEmployeeName() {
        return employeeName;
    }
}

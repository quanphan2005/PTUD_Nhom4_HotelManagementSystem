package vn.iuh.gui.panel.statistic;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterStatistic that = (FilterStatistic) o;
        return Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(employeeName, that.employeeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate, employeeName);
    }
}

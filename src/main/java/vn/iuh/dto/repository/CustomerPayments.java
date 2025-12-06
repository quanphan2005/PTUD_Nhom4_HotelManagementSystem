package vn.iuh.dto.repository;

import vn.iuh.util.PriceFormat;

import java.math.BigDecimal;

public class CustomerPayments {
    private BigDecimal advancePayment;
    private BigDecimal totalServiceCost;

    public CustomerPayments(BigDecimal advancePayment, BigDecimal totalServiceCost) {
        this.advancePayment = advancePayment;
        this.totalServiceCost = totalServiceCost;
    }

    public BigDecimal getAdvancePayment() {
        return advancePayment;
    }

    public BigDecimal getTotalServiceCost() {
        return totalServiceCost;
    }

    public void setTotalServiceCost(BigDecimal totalServiceCost) {
        this.totalServiceCost = totalServiceCost;
    }

    public void setAdvancePayment(BigDecimal advancePayment) {
        this.advancePayment = advancePayment;
    }
}

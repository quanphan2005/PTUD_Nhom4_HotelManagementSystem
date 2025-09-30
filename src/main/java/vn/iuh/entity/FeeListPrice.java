package vn.iuh.entity;

public class FeeListPrice {
    private String id;
    private double previousUnit;
    private double updatedUnit;
    private boolean isPercentage;
    private String shiftAssignmentId;
    private String additionalFeeId;

    public FeeListPrice() {
    }

    public FeeListPrice(String id, double previousUnit, double updatedUnit, boolean isPercentage,
                        String shiftAssignmentId, String additionalFeeId) {
        this.id = id;
        this.previousUnit = previousUnit;
        this.updatedUnit = updatedUnit;
        this.isPercentage = isPercentage;
        this.shiftAssignmentId = shiftAssignmentId;
        this.additionalFeeId = additionalFeeId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPreviousUnit() {
        return previousUnit;
    }

    public void setPreviousUnit(double previousUnit) {
        this.previousUnit = previousUnit;
    }

    public double getUpdatedUnit() {
        return updatedUnit;
    }

    public void setUpdatedUnit(double updatedUnit) {
        this.updatedUnit = updatedUnit;
    }

    public boolean getIsPercentage() {
        return isPercentage;
    }

    public void setIsPercentage(boolean isPercentage) {
        this.isPercentage = isPercentage;
    }

    public String getShiftAssignmentId() {
        return shiftAssignmentId;
    }

    public void setShiftAssignmentId(String shiftAssignmentId) {
        this.shiftAssignmentId = shiftAssignmentId;
    }

    public String getAdditionalFeeId() {
        return additionalFeeId;
    }

    public void setAdditionalFeeId(String additionalFeeId) {
        this.additionalFeeId = additionalFeeId;
    }
}

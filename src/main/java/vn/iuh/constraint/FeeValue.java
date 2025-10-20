package vn.iuh.constraint;

public enum FeeValue {
    TAX(0.1)
    ;

    public final double value;

    FeeValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}

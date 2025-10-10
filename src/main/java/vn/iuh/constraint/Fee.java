package vn.iuh.constraint;

public enum Fee {
    CHECK_OUT_TRE("Check-out trễ"),
    CHECK_IN_SOM("Check-in sớm")
    ;

    public String status;
    Fee(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

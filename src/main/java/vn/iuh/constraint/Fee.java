package vn.iuh.constraint;

public enum Fee {
    CHECK_OUT_TRE("Check-out trễ"),
    CHECK_IN_SOM("Check-in sớm"),
    THUE("Thuế giá trị gia tăng"),
    DOI_PHONG("Đổi phòng");

    public String name;
    Fee(String name) {
        this.name = name;
    }

    public String getStatus() {
        return name;
    }
}

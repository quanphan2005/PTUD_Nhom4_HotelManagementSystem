package vn.iuh.constraint;

public enum UserRole {
    QUAN_LY("CV002"),
    NHAN_VIEN("CV001"),
    ADMIN("CV003"),
    KHONG_XAC_DINH("CV004");
    public final String maChucVu;

    UserRole(String maChucVu) {
        this.maChucVu = maChucVu;
    }
    public static UserRole fromString(String maChucVuDB) {
        for (UserRole viTri : values()) {
            if (viTri.maChucVu != null && viTri.maChucVu.equals(maChucVuDB)) {
                return viTri;
            }
        }
        return null;
    }
}

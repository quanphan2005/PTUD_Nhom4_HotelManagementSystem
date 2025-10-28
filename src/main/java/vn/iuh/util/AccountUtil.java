package vn.iuh.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class AccountUtil {
    public String taoTenDangNhap(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return "nguoidung";
        }

        String tenSach = boDau(hoTen.trim().toLowerCase());

        // Sử dụng \\s+ để xử lý trường hợp có nhiều khoảng trắng
        String[] parts = tenSach.split("\\s+");

        if (parts.length == 0) {
            return "nguoidung";
        }
        if (parts.length == 1) {
            return parts[0];
        }
        String ten = parts[parts.length - 1];
        String ho = parts[0];

        return ten + "." + ho;
    }

    public String boDau(String str) {
        if (str == null) {
            return null;
        }

        // 1. Chuẩn hóa chuỗi về dạng NFD (Canonical Decomposition)
        // (Tách ký tự và dấu thành 2 phần, ví dụ: 'á' -> 'a' + '´')
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);

        // 2. Sử dụng Regex để loại bỏ các dấu
        // (Giữ lại ký tự 'a' và bỏ '´')
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(nfdNormalizedString).replaceAll("");

        // 3. Xử lý đặc biệt chữ 'Đ' và 'đ'
        // Vì 'Đ' không bị phân tách ở bước 1
        result = result.replaceAll("[Đđ]", "d");

        return result;
    }
}

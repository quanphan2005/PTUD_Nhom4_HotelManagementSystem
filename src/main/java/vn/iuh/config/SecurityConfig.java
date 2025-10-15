package vn.iuh.config;

import org.mindrot.jbcrypt.BCrypt;

public class SecurityConfig {
    // Mã hóa password
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Kiểm tra password khi đăng nhập
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

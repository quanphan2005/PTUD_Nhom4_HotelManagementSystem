package vn.iuh.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String url;
    private static final String username;
    private static final String password;
    private static Connection connection;

    static {
        Dotenv dotenv = Dotenv.load();
        url = dotenv.get("DB_URL");
        username = dotenv.get("DB_USERNAME");
        password = dotenv.get("DB_PASSWORD");

        connection = createConnection();
    }

    /*
        * Get connection to database (singleton pattern)
        * @return Connection object
    */
    public static synchronized Connection getConnect() {
        if (connection == null || isConnectionDead(connection)) {
            connection = createConnection();
        }
        return connection;
    }

    private static boolean isConnectionDead(Connection conn) {
        if (conn == null) {
            return true;
        }
        try {
            conn.prepareStatement("SELECT 1").execute();
            return false;
        } catch (SQLException e) {
            System.err.println("Connection is dead: " + e.getMessage());
            return true;
        }
    }

    /*
        * Get a new connection to database
        * @return Connection object
    */
    public static Connection getNewConnect() {
        return createConnection();
    }

    public static void enableTransaction(Connection connection) {
        try {
            if (connection != null && connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi setAutoCommit = false : " + e.getMessage());
            closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public static void disableTransaction(Connection connection) {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi setAutoCommit = true: " + e.getMessage());
            closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Lỗi đóng kết nối: " + e.getMessage());
            }
        }
    }

    private static Connection createConnection() {
        while (true) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection conn = DriverManager.getConnection(url, username, password);
                System.out.println("Kết nối thành công!");
                return conn;

            } catch (Exception e) {
                System.err.println("Lỗi tạo kết nối: " + e.getMessage());
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }
        }
    }

    public static void khoiTaoGiaoTac() {
        DatabaseUtil.enableTransaction(connection);
    }

    public static void thucHienGiaoTac() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.commit();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi commit transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }

    public static void hoanTacGiaoTac() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                DatabaseUtil.disableTransaction(connection);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi rollback transaction: " + e.getMessage());
            DatabaseUtil.closeConnection(connection);
            throw new RuntimeException(e);
        }
    }
}

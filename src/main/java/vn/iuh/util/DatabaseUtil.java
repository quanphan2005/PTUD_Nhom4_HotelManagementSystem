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
    public static Connection getConnect() {
        if (connection == null)
            connection = createConnection();


        return connection;
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
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, username, password);
            if (connection != null) {
                System.out.println("Kết nối thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("SQLServerDriver không tìm thấy: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }

        return connection;
    }
}

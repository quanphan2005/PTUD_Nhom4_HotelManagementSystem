package vn.iuh.util;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BackupDatabase {
    public static boolean backupDifDatabase(String filePath) throws SQLException {
        Connection connection = DatabaseUtil.getConnect();

        try (connection){
            String backupQuery = "BACKUP DATABASE [QLKS] TO DISK = '" + filePath + "' WITH DIFFERENTIAL ";
            Statement statement = connection.createStatement();
            statement.execute(backupQuery);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean backupFullDatabase(String filePath) throws SQLException {
        Connection connection = DatabaseUtil.getConnect();
        String backupQuery = "BACKUP DATABASE [QLKS] TO DISK = '" + filePath + "' ";

        try (Statement st = connection.createStatement()) {
            st.execute(backupQuery);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

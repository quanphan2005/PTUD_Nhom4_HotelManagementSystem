package vn.iuh.util;

import java.sql.Connection;
import java.sql.Statement;

public class RestoreDataBase {
    public static void restoreFullAndDiff(String fullBackupPath, String diffBackupPath) {
        try (Connection conn = DatabaseUtil.getConnect()) {
            Statement stmt = conn.createStatement();
            stmt.execute("USE MASTER");
            stmt.execute("ALTER DATABASE [QLKS] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;");
            stmt.execute(
                    "RESTORE DATABASE [QLKS] FROM DISK = '" + fullBackupPath + "' " +
                            "WITH REPLACE, NORECOVERY;"
            );
            stmt.execute(
                    "RESTORE DATABASE [QLKS] FROM DISK = '" + diffBackupPath + "' " +
                            "WITH REPLACE, RECOVERY;"
            );
            stmt.execute("ALTER DATABASE [QLKS] SET MULTI_USER;");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Restore full+diff failed: " + e.getMessage());
        }
    }
}

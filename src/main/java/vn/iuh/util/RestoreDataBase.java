package vn.iuh.util;

import java.sql.Connection;
import java.sql.Statement;

public class RestoreDataBase {
    public void restoreFullAndDiff(String fullBackupPath, String diffBackupPath) {
        try (Connection conn = DatabaseUtil.getConnect()) {
            Statement stmt = conn.createStatement();
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

            System.out.println("Full + diff restore completed.");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Restore full+diff failed: " + e.getMessage());
        }
    }

}

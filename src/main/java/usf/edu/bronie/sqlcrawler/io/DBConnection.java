package usf.edu.bronie.sqlcrawler.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/crawler";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, "kevin", "Super1Password");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

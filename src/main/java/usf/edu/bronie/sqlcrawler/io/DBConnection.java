package usf.edu.bronie.sqlcrawler.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, "root", "");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

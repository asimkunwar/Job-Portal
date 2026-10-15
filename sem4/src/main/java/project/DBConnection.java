package project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ===== DATABASE CONFIGURATION =====
    private static final String URL = "jdbc:mysql://10.65.125.163:3306/job_portal?useSSL=false&serverTimezone=UTC";
    private static final String USER = "jobuser";
    private static final String PASSWORD = "1234"; // XAMPP default password

    // ===== GET DATABASE CONNECTION =====
    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }
        return con;
    }
}

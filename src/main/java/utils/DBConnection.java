package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông số mặc định của XAMPP
    private static final String URL = "jdbc:mysql://localhost:3306/auction_system_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Kết nối Database thành công!");
        } 
        catch (ClassNotFoundException e) {
            System.out.println("❌ Lỗi: Không tìm thấy JDBC Driver!");
            e.printStackTrace();
        } 
        catch (SQLException e) {
            System.out.println("❌ Lỗi: Sai thông tin kết nối MySQL!");
            e.printStackTrace();
        }
        return conn;
        }

    public static void main(String[] args) {
        DBConnection.getConnection();
    }
}

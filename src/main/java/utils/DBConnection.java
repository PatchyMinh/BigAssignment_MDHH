package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông số mặc định của XAMPP
    private static final String URL = "jdbc:mysql://localhost:3306/auction_system_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Mặc định XAMPP để trống mật khẩu
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Phương thức trả về đối tượng Connection để thực thi SQL
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Đăng ký Driver với DriverManager
            Class.forName(DRIVER);

            // Thực hiện kết nối
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Lỗi: Không tìm thấy MySQL Driver. Hãy kiểm tra lại thư viện .jar hoặc Dependency!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Lỗi: Không thể kết nối đến Database. Hãy kiểm tra XAMPP (Apache/MySQL)!");
            e.printStackTrace();
        }
        return conn;
    }

    // Hàm main để chạy thử xem kết nối đã thông chưa
    public static void main(String[] args) {
        if (getConnection() != null) {
            System.out.println("🎉 Chúc mừng! Kết nối Java với MySQL thành công.");
        }
    }
}
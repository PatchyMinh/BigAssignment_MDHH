package dao;

import model.User;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private DBConnection dbConnection = new DBConnection();
    @Override
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, real_name, email, phone_number, role, balance) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRealName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhoneNumber());
            pstmt.setString(6, user.getRole().name());
            pstmt.setDouble(7, user.getBalance());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Lấy ID tự sinh từ MySQL và gán ngược lại cho object user
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setID(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public User login(String username, String password) {
        // Dấu ? là tham số sẽ được điền vào sau, giúp chống hack SQL Injection
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        User user = null;

        // Dùng try-with-resources để Java tự động đóng kết nối sau khi dùng xong
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Nếu tìm thấy, nhét dữ liệu từ Database vào đối tượng Java
                    user = new User();
                    user.setID(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));

                    user.setRealName(rs.getString("real_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phone_number"));

                    user.setBalance(rs.getDouble("balance"));
                    user.setFrozenBalance(rs.getDouble("frozen_balance"));
                    
                    user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        // Câu lệnh SQL lấy tất cả các cột của mọi dòng trong bảng users
        String sql = "SELECT * FROM users";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // Duyệt qua từng dòng dữ liệu trả về
            while (rs.next()) {
                User user = new User();
                user.setID(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setBalance(rs.getDouble("balance"));
                user.setFrozenBalance(rs.getDouble("frozen_balance"));
                user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
                userList.add(user);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy danh sách User: " + e.getMessage());
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public boolean updateBalance(int userId, double newBalance, double newFrozenBalance) {
        // Câu lệnh SQL cập nhật 2 cột balance và frozen_balance dựa trên id của user
        String sql = "UPDATE users SET balance = ?, frozen_balance = ? WHERE id = ?";
        boolean isUpdated = false;

        try (Connection conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Gán giá trị cho 3 dấu hỏi chấm (?)
            pstmt.setDouble(1, newBalance);
            pstmt.setDouble(2, newFrozenBalance);
            pstmt.setInt(3, userId);
            
            // Thực thi lệnh cập nhật và kiểm tra xem có dòng nào bị ảnh hưởng không
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                isUpdated = true;
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi cập nhật số dư: " + e.getMessage());
            e.printStackTrace();
        }
        
        return isUpdated;
    }

    @Override
    public List<User> searchUsers(String keyword) {
        List<User> userList = new ArrayList<>();
        // Dùng toán tử LIKE để tìm kiếm chứa từ khóa
        String sql = "SELECT * FROM users WHERE username LIKE ?";

        try (Connection conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Thêm ký tự % vào 2 đầu để tìm kiếm linh hoạt (Ví dụ: gõ "ali" sẽ ra "alice_seller")
            pstmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // Dùng vòng lặp while vì kết quả có thể trả ra nhiều dòng (nhiều user)
                while (rs.next()) {
                    User user = new User();
                    user.setID(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setBalance(rs.getDouble("balance"));
                    user.setFrozenBalance(rs.getDouble("frozen_balance"));
                    user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
                    // Lấy xong 1 user thì nhét vào danh sách
                    userList.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }
    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    // Lưu ý: Map đúng các trường như hàm login
                    user.setID(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));

                    // BỔ SUNG CÁC TRƯỜNG MỚI
                    user.setRealName(rs.getString("real_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhoneNumber(rs.getString("phone_number"));

                    user.setBalance(rs.getDouble("balance"));
                    user.setFrozenBalance(rs.getDouble("frozen_balance"));
                    user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));

                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public boolean updateStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (
            Connection conn = dbConnection.getConnection(); 
            PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
                pstmt.setString(1, status); // 'LOCKED' hoặc 'ACTIVE'
                pstmt.setInt(2, userId);
                    return pstmt.executeUpdate() > 0;
    } 
        catch (SQLException e) {
            e.printStackTrace();
            return false;
    }
}
    @Override
    public int countUsersByStatus(int status) {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
            pstmt.setInt(1, status);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) 
                    return rs.getInt(1);
                }
    } 
    catch (SQLException e) { e.printStackTrace(); }
    return 0;
}
    /**
     * Đóng băng tiền: Trừ balance, cộng vào frozenBalance.
     * Chỉ thành công khi số dư (balance) >= số tiền cần đóng băng.
     */
    @Override
    public boolean freezeMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        // Câu SQL vàng: Kiểm tra số dư ngay trong lúc UPDATE
        String sql = "UPDATE users SET balance = balance - ?, frozenBalance = frozenBalance + ? WHERE id = ? AND balance >= ?";
        
        // Dùng PreparedStatement bình thường, NHƯNG KHÔNG ĐÓNG Connection ở đây
        // vì Connection này do Service quản lý.
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, userId);
            pstmt.setDouble(4, amount);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Trả về true nếu update thành công (đủ tiền)
        }
    }

    /**
     * Hoàn tiền: Trừ frozenBalance, trả lại vào balance.
     */
    @Override
    public boolean refundMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ?, frozenBalance = frozenBalance - ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    /**
     * Trừ hẳn tiền đóng băng của người thắng cuộc.
     * Cực kỳ an toàn: Có check điều kiện frozenBalance >= amount để chống âm tiền ảo.
     */
    @Override
    public boolean deductFrozenMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        // Chỉ trừ ở cột frozenBalance, vì tiền này đã được bế từ balance sang lúc đặt giá rồi
        String sql = "UPDATE users SET frozenBalance = frozenBalance - ? WHERE id = ? AND frozenBalance >= ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            pstmt.setDouble(3, amount); // Đảm bảo số tiền đóng băng hiện tại đủ để trừ
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Cộng tiền vào tài khoản thực (dùng cho người bán khi chốt đơn, hoặc chức năng nạp tiền).
     */
    @Override
    public boolean addMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}


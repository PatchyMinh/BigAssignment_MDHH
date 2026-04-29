package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.User;
import utils.DBConnection;

public class UserDAOImpl implements UserDAO {
    DBConnection dbConnection = new DBConnection();

    // =========================================================================
    // NHÓM 1: CÁC HÀM ĐỌC/GHI ĐỘC LẬP
    // =========================================================================
    
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

            if (pstmt.executeUpdate() > 0) {
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
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                userList.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public List<User> searchUsers(String keyword) {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE username LIKE ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userList.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public int countUsersByStatus(int status) {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =========================================================================
    // NHÓM 2: CÁC HÀM TRUY XUẤT CƠ BẢN (NẠP CHỒNG)
    // =========================================================================

    // 2.1 Bản dùng trong Transaction (Không bắt lỗi, quăng ra ngoài cho Service bắt)
    @Override
    public User getUserById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // 2.2 Bản gọi độc lập
    @Override
    public User getUserById(int id) {
        try (Connection conn = dbConnection.getConnection()) {
            return getUserById(conn, id); // Tái sử dụng logic hàm trên
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================================================
    // NHÓM 3: CÁC HÀM CẬP NHẬT TRẠNG THÁI / THÔNG TIN (NẠP CHỒNG)
    // =========================================================================

    // 3.1 Bản dùng trong Transaction
    @Override
    public boolean updateStatus(Connection conn, int userId, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 3.2 Bản gọi độc lập
    @Override
    public boolean updateStatus(int userId, String status) {
        try (Connection conn = dbConnection.getConnection()) {
            return updateStatus(conn, userId, status);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3.3 Bản dùng trong Transaction
    @Override
    public boolean updateBalance(Connection conn, int userId, double newBalance, double newFrozenBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ?, frozen_balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setDouble(2, newFrozenBalance);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 3.4 Bản gọi độc lập
    @Override
    public boolean updateBalance(int userId, double newBalance, double newFrozenBalance) {
        try (Connection conn = dbConnection.getConnection()) {
            return updateBalance(conn, userId, newBalance, newFrozenBalance);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // NHÓM 4: CÁC HÀM GIAO DỊCH TIỀN BẠC ATOMIC (NẠP CHỒNG)
    // Lưu ý: Đã sửa frozenBalance thành frozen_balance cho đúng chuẩn Database
    // =========================================================================

    // --- FREEZE MONEY ---
    @Override
    public boolean freezeMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance - ?, frozen_balance = frozen_balance + ? WHERE id = ? AND balance >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, userId);
            pstmt.setDouble(4, amount);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean freezeMoneyAtomic(int userId, double amount) {
        try (Connection conn = dbConnection.getConnection()) {
            return freezeMoneyAtomic(conn, userId, amount);
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- REFUND MONEY ---
    @Override
    public boolean refundMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ?, frozen_balance = frozen_balance - ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean refundMoneyAtomic(int userId, double amount) {
        try (Connection conn = dbConnection.getConnection()) {
            return refundMoneyAtomic(conn, userId, amount);
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- DEDUCT FROZEN MONEY ---
    @Override
    public boolean deductFrozenMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET frozen_balance = frozen_balance - ? WHERE id = ? AND frozen_balance >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            pstmt.setDouble(3, amount);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deductFrozenMoneyAtomic(int userId, double amount) {
        try (Connection conn = dbConnection.getConnection()) {
            return deductFrozenMoneyAtomic(conn, userId, amount);
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- ADD MONEY ---
    @Override
    public boolean addMoneyAtomic(Connection conn, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean addMoneyAtomic(int userId, double amount) {
        try (Connection conn = dbConnection.getConnection()) {
            return addMoneyAtomic(conn, userId, amount);
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // =========================================================================
    // HÀM TIỆN ÍCH (Utility) DÙNG NỘI BỘ TRONG DAO
    // =========================================================================
    
    // Hàm này giúp gom code tạo đối tượng User lại 1 chỗ, tránh lặp lại ở hàm login, getAllUsers, searchUsers...
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("real_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                User.Role.valueOf(rs.getString("role").toUpperCase()),
                rs.getDouble("balance"),
                rs.getDouble("frozen_balance")
        );
    }
}
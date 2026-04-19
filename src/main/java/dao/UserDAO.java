package dao;
import model.User; // Giả sử bạn đã có class User

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    boolean register(User user);

    // Hàm đăng nhập (trả về đối tượng User nếu đúng, null nếu sai)
    User login(String username, String password);
     
    // Hàm cập nhật số dư khi nạp tiền hoặc đặt cọc, model only
    boolean updateBalance(int userId, double newBalance, double newFrozenBalance);

    List<User> searchUsers(String keyword);

    // admin only
    List<User> getAllUsers();
   
    User getUserById(int id);

    boolean updateStatus(int userId, String status);

    int countUsersByStatus(int status);

    boolean freezeMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;

    boolean refundMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;

    boolean deductFrozenMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;

    boolean addMoneyAtomic(Connection conn, int userId, double amount) throws SQLException;

    /*
    Các hàm cộng, trừ tiền đều có "conn" và Atomic để đảm bảo tính 
    nhất quán khi thực hiện nhiều bước trong cùng một transaction (như trong SettlementService).
    Ngoài ra, các hàm cũng có thể đảm bảo toàn vẹn dữ liệu nếu như có một hàm không thể thực hiện được
    (ví dụ: trừ tiền mà không đủ số dư đóng băng thì sẽ trả về false và transaction sẽ bị rollback).
    */
}

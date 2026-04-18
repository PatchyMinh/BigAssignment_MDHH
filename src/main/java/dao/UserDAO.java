package dao;
import model.User; // Giả sử bạn đã có class User
import java.util.List;

public interface UserDAO {
    // Thêm hàm đăng ký
    public boolean register(User user);
    // Hàm đăng nhập (trả về đối tượng User nếu đúng, null nếu sai)
    User login(String username, String password);
    
    // Hàm lấy danh sách tất cả user (dành cho Admin)
    List<User> getAllUsers();
    
    // Hàm cập nhật số dư khi nạp tiền hoặc đặt cọc
    boolean updateBalance(int userId, double newBalance, double newFrozenBalance);

    List<User> searchUsers(String keyword);
    public User getUserById(int id);
}

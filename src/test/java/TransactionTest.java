// package main.java;

// Import các thư viện của JUnit 5
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    private User testUser;
    private Transaction transaction;

    // Hàm này sẽ chạy TRƯỚC MỖI hàm @Test để reset lại dữ liệu cho sạch
    @BeforeEach
    public void setUp() {
        testUser = new User("Nguyen Van A", "nva99", "a@gmail.com", "pass123", "0987654321");
        transaction = new Transaction();
    }

    @Test
    public void testDepositSuccess() {
        transaction.deposit(testUser, 500000);
        
        // Kiểm tra xem số dư có đúng là 500000 không
        // Cú pháp: assertEquals(Giá_trị_kỳ_vọng, Giá_trị_thực_tế, Lời_nhắn_nếu_lỗi)
        assertEquals(500000, testUser.getBalance(), "Số dư phải là 500k sau khi nạp");
    }

    @Test
    public void testWithdrawSuccess() {
        // Chuẩn bị tiền
        transaction.deposit(testUser, 500000);
        
        // Thực hiện rút
        transaction.withdraw(testUser, 200000);
        
        // Kiểm tra số dư còn lại
        assertEquals(300000, testUser.getBalance(), "Số dư phải còn 300k sau khi rút 200k");
    }

    @Test
    public void testWithdrawInsufficientFunds() {
        transaction.deposit(testUser, 100000);
        
        // Thử rút lố tiền
        transaction.withdraw(testUser, 500000);
        
        // Số dư phải giữ nguyên là 100k, không được bị trừ thành số âm
        assertEquals(100000, testUser.getBalance(), "Số dư không được đổi nếu rút quá giới hạn");
    }

    @Test
    public void testWithdrawExactAmount() {
        transaction.deposit(testUser, 500000);
        
        // Thử rút sạch tiền
        transaction.withdraw(testUser, 500000);
        
        // Số dư phải về 0
        assertEquals(0, testUser.getBalance(), "Số dư phải về 0 khi rút toàn bộ tiền");
    }
}
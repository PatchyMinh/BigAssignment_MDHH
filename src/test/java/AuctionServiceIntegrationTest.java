// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;
// import static org.junit.jupiter.api.Assertions.*;

// import service.AuctionService;

// public class AuctionServiceIntegrationTest {

//     private AuctionService auctionService;

//     @BeforeEach
//     public void setUp() {
//         // Khởi tạo Service thật, nó sẽ tự động dùng DAO thật và kết nối DB thật của bạn
//         auctionService = new AuctionService();
//     }

//     @Test
//     @DisplayName("Test đặt giá tích hợp với Database thật")
//     public void testPlaceBidWithRealDatabase() {
//         System.out.println("Bắt đầu test đặt giá...");

//         // 🚨 CHÚ Ý QUAN TRỌNG: 
//         // Bạn PHẢI đổi 3 giá trị dưới đây cho khớp với dữ liệu ĐANG CÓ THẬT trong MySQL của bạn
//         int realBidderId = 2;              // Đảm bảo User có ID = 2 tồn tại và đủ tiền
//         String realSessionId = "SS_001";   // Đảm bảo Phiên này đang ở trạng thái OPEN
//         double bidAmount = 60000.0;        // Đảm bảo mức giá này lớn hơn (Giá hiện tại + Bước giá)

//         try {
//             // Chạy thẳng vào hệ thống thật
//             boolean isSuccess = auctionService.placeBid(realBidderId, realSessionId, bidAmount);
            
//             // Dùng JUnit để chốt kết quả: Kỳ vọng là TRUE
//             assertTrue(isSuccess, "Lỗi: Đặt giá thất bại. Hãy kiểm tra lại số dư hoặc trạng thái phiên trong DB.");

//             // Nếu code chạy đến dòng này, tức là assertTrue đã pass (màu xanh)
//             System.out.println("✅ Đặt giá THÀNH CÔNG! Hãy mở MySQL lên để kiểm tra:");
//             System.out.println(" - Bảng 'users': Tiền của user ID " + realBidderId + " đã bị trừ chưa?");
//             System.out.println(" - Bảng 'bids': Đã có dòng lịch sử đặt giá " + bidAmount + " mới chưa?");
            
//         } catch (Exception e) {
//             // Nếu có lỗi SQL hoặc lỗi code, đánh sập test ngay lập tức và in lỗi ra
//             e.printStackTrace();
//             fail("Test bị sập do Exception: " + e.getMessage());
//         }
//     }
// }
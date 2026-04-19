// import dao.*;
// import factory.*;
// import model.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;
// import java.time.LocalDate;

// // Import thư viện kiểm tra (Assertions) của JUnit 5
// import static org.junit.jupiter.api.Assertions.*;

// public class AuctionSystemTest {

//     // Khai báo các DAO ở cấp độ class để dùng chung
//     private UserDAO userDAO;
//     private ItemDAO itemDAO;
//     private AuctionSessionDAO sessionDAO;
//     private BidDAO bidDAO;

//     @BeforeEach
//     void setUp() {
//         // @BeforeEach sẽ tự động chạy trước mỗi hàm @Test
//         // Nơi hoàn hảo để khởi tạo các đối tượng
//         userDAO = new UserDAOImpl();
//         itemDAO = new ItemDAOImpl();
//         sessionDAO = new AuctionSessionDAOImpl();
//         bidDAO = new BidDAOImpl();
//     }

//     @Test
//     @DisplayName("Test luồng: Đăng nhập -> Tạo Hàng -> Mở Phiên -> Đặt giá")
//     void testFullAuctionFlow() {
//         // ==========================================
//         // BƯỚC 1: ĐĂNG NHẬP NGƯỜI BÁN
//         // ==========================================
//         User seller = userDAO.login("admin", "123");
        
//         // Thay thế if-else bằng Assertions. 
//         // Nếu seller là null, Test sẽ đỏ ngay lập tức và in ra thông báo.
//         assertNotNull(seller, "❌ Lỗi: Không tìm thấy tài khoản admin.");
//         assertEquals("admin", seller.getUsername(), "Tên đăng nhập không khớp.");


//         // ==========================================
//         // BƯỚC 2: TẠO HÀNG HÓA & LƯU VÀO DB
//         // ==========================================
//         // 2.1 Tạo bức tranh (Arts)
//         ItemsAttributes artAttr = new ItemsAttributes();
//         artAttr.setOwner(seller.getUsername());
//         artAttr.setStartingPrice(5000.0);
//         artAttr.setDescription("Bức tranh Đêm Đầy Sao (Bản sao siêu cấp)");
//         artAttr.setArtistName("Vincent van Gogh");
//         artAttr.setReleaseDate(LocalDate.of(1889, 6, 1));
        
//         Items artItem = new TypeArts().createItems(artAttr);
//         itemDAO.addItem(artItem);
//         // Giả sử sau khi thêm vào DB, ID phải được tạo (khác null hoặc > 0)
//         assertNotNull(artItem.getItemID(), "❌ Lỗi: ItemID của bức tranh chưa được tạo.");


//         // ==========================================
//         // BƯỚC 3: TẠO PHIÊN ĐẤU GIÁ CHO BỨC TRANH
//         // ==========================================
//         AuctionSession session = new AuctionSession(
//                 seller,
//                 artItem.getStartingPrice(), // Giá khởi điểm 5000
//                 500.0,                      // Bước giá
//                 3                           // Thời gian 3 ngày
//         );
        
//         boolean isSessionCreated = sessionDAO.createSession(session, artItem.getItemID());
//         assertTrue(isSessionCreated, "❌ Lỗi: Không thể tạo phiên đấu giá trong DB.");
        
//         // Lấy đúng ID vừa được IDGenerator tự động sinh ra trong Constructor
//         String actualSessionId = session.getSessionID();
//         assertNotNull(actualSessionId, "❌ Lỗi: SessionID chưa được sinh ra.");


//         // ==========================================
//         // BƯỚC 4: NGƯỜI MUA ĐẶT GIÁ (BID)
//         // ==========================================
//         User buyer = userDAO.login("nguoimua", "123");
//         assertNotNull(buyer, "❌ Lỗi: Không tìm thấy tài khoản người mua.");

//         // Người mua đặt giá 6000 (Lớn hơn giá khởi điểm 5000)
//         Bid newBid = new Bid(buyer, 6000.0);
        
//         // Dùng actualSessionId của phiên vừa tạo, thay vì chuỗi tự gõ
//         boolean isBidSuccess = bidDAO.addBid(actualSessionId, newBid);
//         assertTrue(isBidSuccess, "❌ Lỗi: Người mua đặt giá thất bại.");
//         assertEquals(6000.0, newBid.getAmount(), "Số tiền đặt giá không khớp.");
//     }
// }
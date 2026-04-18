package test;

import dao.*;
import model.*;

import java.time.LocalDate;

public class    MainTest {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU KIỂM THỬ HỆ THỐNG ===");

        // 1. Khởi tạo các DAO
        UserDAO userDAO = new UserDAOImpl();
        ItemDAO itemDAO = new ItemDAOImpl();
        AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
        BidDAO bidDAO = new BidDAOImpl();

        // 2. TEST ĐĂNG KÝ & ĐĂNG NHẬP (USER DAO)
        System.out.println("\n--- 1. TEST USER ---");
        // Dùng Constructor đăng ký (chưa có ID)
        User seller = new User("Nguyen Van Ban", "seller_pro", "seller@gmail.com", "123456", "0988888888");
        User buyer = new User("Tran Van Mua", "buyer_vip", "buyer@gmail.com", "123456", "0911111111");

        //userDAO.register(seller);
        //userDAO.register(buyer);

        // Đăng nhập để lấy User đầy đủ ID từ DB
        User loggedSeller = userDAO.login("seller_pro", "123456");
        User loggedBuyer = userDAO.login("buyer_vip", "123456");

        if (loggedSeller != null && loggedBuyer != null) {
            System.out.println("✅ Đăng ký và Đăng nhập thành công!");
            System.out.println("ID Người bán: " + loggedSeller.getID() + " | ID Người mua: " + loggedBuyer.getID());
        } else {
            System.err.println("❌ Lỗi Đăng ký/Đăng nhập. Hãy check lại DB!");
            return; // Dừng nếu lỗi
        }

        // 3. TEST TẠO MÓN HÀNG (ITEM DAO)
        System.out.println("\n--- 2. TEST ITEM ---");
        // ID truyền 0 vì chưa insert DB, DAO sẽ tự cập nhật ID
        Arts monaLisa = new Arts(0, loggedSeller.getUsername(), 1000.0, "Tranh Mona Lisa Real", "Da Vinci", LocalDate.of(1503, 1, 1));
        itemDAO.addItem(monaLisa);

        System.out.println("✅ Đã thêm vật phẩm mới vào Database!");
        System.out.println("Item ID vừa được sinh ra: " + monaLisa.getItemID());

        // 4. TEST TẠO PHIÊN ĐẤU GIÁ (AUCTION SESSION DAO)
        System.out.println("\n--- 3. TEST PHIÊN ĐẤU GIÁ ---");
        String sessionId = "SS_" + System.currentTimeMillis(); // Tạo ID phiên ngẫu nhiên
        // Constructor tạo phiên mới (chưa bắt đầu)
        AuctionSession session = new AuctionSession(loggedSeller, sessionId, monaLisa.getStartingPrice(), 50.0, 3);

        boolean isSessionCreated = sessionDAO.createSession(session, monaLisa.getItemID());
        if (isSessionCreated) {
            System.out.println("✅ Tạo phiên đấu giá thành công: " + sessionId);
        }

        // 5. TEST ĐẶT GIÁ (BID DAO)
        System.out.println("\n--- 4. TEST ĐẶT GIÁ (BID) ---");
        Bid bid1 = new Bid(loggedBuyer, 1050.0);
        boolean isBidAdded = bidDAO.addBid(sessionId, bid1);
        if (isBidAdded) {
            System.out.println("✅ User [" + loggedBuyer.getUsername() + "] đã đặt giá: " + bid1.getAmount());
        }

        // 6. TEST LẤY DỮ LIỆU ĐÃ MAP TOÀN BỘ TỪ DB
        System.out.println("\n--- 5. KIỂM TRA TÍNH TOÀN VẸN CỦA DỮ LIỆU (MAP BẰNG CONSTRUCTOR) ---");
        AuctionSession fetchedSession = sessionDAO.getSessionById(sessionId);

        if (fetchedSession != null) {
            System.out.println("Lấy thông tin phiên thành công!");
            System.out.println("- Giá khởi điểm ban đầu: " + fetchedSession.getStartingPrice());
            System.out.println("- Giá cao nhất hiện tại : " + fetchedSession.getCurrentPrice());
            if (fetchedSession.getHighestBidder() != null) {
                System.out.println("- Người đang thắng thế  : " + fetchedSession.getHighestBidder().getUsername());
            }
            System.out.println("- Trạng thái phiên      : " + fetchedSession.status);
            System.out.println("✅ MỌI THỨ HOẠT ĐỘNG HOÀN HẢO THEO ĐÚNG OOP!");
        }
    }
}
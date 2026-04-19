package service;

import dao.*;
import model.*;
import utils.*;
import java.sql.Connection;
import java.sql.SQLException;

public class AuctionService {
    
    private UserDAO userDAO;
    private BidDAO bidDAO;
    private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl(); 
    private DBConnection dbConnection = new DBConnection();

    public AuctionService() {
        this.userDAO = new UserDAOImpl();
        this.bidDAO = new BidDAOImpl();
    }
    /**
     * Luồng xử lý khi một người dùng bấm nút "Đặt giá"
     */
    public boolean placeBid(int currentUserId, String sessionId, double bidAmount) {
        Connection conn = null;
        try {
            // 1. Lấy kết nối và TẮT auto-commit để bắt đầu Transaction
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); 
            
            AuctionSession session = sessionDAO.getSessionById(sessionId);
        if (session == null) {
            System.out.println("❌ Lỗi: Phiên đấu giá không tồn tại!");
            return false;
        }

        // 2. Check trạng thái: Chỉ cho phép bid khi phiên đang MỞ (OPEN)
        if (session.getStatus() != AuctionSession.Status.OPEN) {
            System.out.println("❌ Lỗi: Phiên đấu giá chưa mở hoặc đã đóng!");
            return false;
        }

        // 3. Lấy giá cao nhất hiện tại (nếu có)
        Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
        
        // 4. Tính toán mức giá HỢP LỆ TỐI THIỂU
        double minValidBid;
        if (highestBid == null) {
            // Chưa có ai đặt: Giá đặt tối thiểu có thể là Giá khởi điểm
            // (Hoặc nếu luật của team khắt khe hơn: Giá khởi điểm + Bước giá)
            minValidBid = session.getStartingPrice(); 
        } else {
            // Đã có người đặt: Bắt buộc = Giá cao nhất + Bước giá
            minValidBid = highestBid.getAmount() + session.getIncrementStep();
        }

        // 5. Chặn ngay nếu giá đặt thấp hơn mức cho phép
        if (bidAmount < minValidBid) {
            System.out.println("❌ Lỗi: Giá đặt không hợp lệ. Phải lớn hơn hoặc bằng " + minValidBid + "$");
            return false;
        }

            // 4. THAO TÁC 1: Khóa và trừ tiền người mua MỚI
            boolean isDeducted = userDAO.freezeMoneyAtomic(conn, currentUserId, bidAmount);
            if (!isDeducted) {
                System.out.println("❌ Số dư không đủ để đặt giá!");
                conn.rollback(); // Hủy bỏ toàn bộ thao tác
                return false;
            }

            // 5. THAO TÁC 2: Hoàn tiền cho người mua CŨ (nếu có)
            if (highestBid != null) {
                int previousUserId = highestBid.getBidder().getID();
                double previousAmount = highestBid.getAmount();
                userDAO.refundMoneyAtomic(conn, previousUserId, previousAmount);
            }

            // 6. THAO TÁC 3: Lưu lịch sử đặt giá vào DB
            Bid newBid = new Bid(userDAO.getUserById(currentUserId), bidAmount);
            bidDAO.addBid(sessionId, newBid);

            // ==========================================
            // 7. CHỐT SỔ: Nếu tất cả 3 thao tác trên đều thành công
            // ==========================================
            conn.commit();
            System.out.println("✅ Đặt giá thành công!");
            return true;

        } catch (Exception e) {
            // Nếu có bất kỳ lỗi gì (đứt mạng, lỗi SQL, v.v.), HOÀN TÁC TOÀN BỘ!
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("⛔ Lỗi hệ thống: Đã hoàn tác giao dịch bảo vệ tài sản.");
            return false;
        } finally {
            // Trả lại connection cho hệ thống
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close(); 
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}
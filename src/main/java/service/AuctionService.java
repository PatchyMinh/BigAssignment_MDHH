package service;

import java.sql.Connection;
import java.sql.SQLException;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.AuctionSession;
import model.Bid;
import utils.DBConnection;

public class AuctionService {
    
    final private UserDAO userDAO;
    final private BidDAO bidDAO;
    final private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl(); 
    final private DBConnection dbConnection = new DBConnection();

    public AuctionService() {
        this.userDAO = new UserDAOImpl();
        this.bidDAO = new BidDAOImpl();
    }
    /**
     * Luồng xử lý khi một người dùng bấm nút "Đặt giá"
     */
    public synchronized boolean placeBid(int currentUserId, String sessionId, double bidAmount) {
    Connection conn = null;
    try {
        conn = dbConnection.getConnection();
        conn.setAutoCommit(false); 
        
        AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
        if (session == null) {
            System.out.println("Lỗi: Phiên đấu giá không tồn tại!");
            return false;
        }

        // ... (Giữ nguyên các đoạn check trạng thái và tính minValidBid của bạn) ...
        if (session.getStatus() != AuctionSession.Status.OPEN) {
            System.out.println("❌ Lỗi: Phiên đấu giá chưa mở hoặc đã đóng!");
            return false;
        }

        // 3. Lấy giá cao nhất hiện tại (nếu có)
        Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
        
        // 4. Tính toán mức giá HỢP LỆ TỐI THIỂU
        double minValidBid;
        if (highestBid == null) {
            minValidBid = session.getStartingPrice(); 
        } else {
            // Đã có người đặt: Bắt buộc = Giá cao nhất + Bước giá
            minValidBid = highestBid.getAmount() + session.getIncrementStep();
        }

        // 5. Chặn ngay nếu giá đặt thấp hơn mức cho phép
        if (bidAmount < minValidBid) {
            System.out.println("❌ Lỗi: Giá đặt không hợp lệ. Phải lớn hơn hoặc bằng " + minValidBid + "$");
            conn.rollback();
            return false;
        }
        // 4. Thao tác 1: Trừ tiền
        boolean isDeducted = userDAO.freezeMoneyAtomic(conn, currentUserId, bidAmount);
        if (!isDeducted) {
            System.out.println("Lỗi: Số dư không đủ để đặt giá!");
            conn.rollback(); 
            return false;
        }

        // 5. Thao tác 2: Hoàn tiền cũ
        if (highestBid != null) {
            int previousUserId = highestBid.getBidder().getID();
            double previousAmount = highestBid.getAmount();
            userDAO.refundMoneyAtomic(conn, previousUserId, previousAmount);
}

        // 6. Thao tác 3: Lưu lịch sử bid
        Bid newBid = new Bid(userDAO.getUserById(conn, currentUserId), bidAmount);
        bidDAO.addBid(conn, sessionId, newBid);

        // 7. Chốt sổ
        conn.commit();
        System.out.println("Đặt giá thành công!");
        return true;

    } catch (Exception e) {
        if (conn != null) {
            try { conn.rollback(); } 
            catch (SQLException ex) { ex.printStackTrace();
                System.out.println("❌ Lỗi hệ thống: Không có kết nối."); }
             }
        System.out.println("❌ Lỗi hệ thống: Đã hoàn tác giao dịch bảo vệ tài sản.");
        e.printStackTrace(); 
        return false;
    } finally {
        if (conn != null) {
            try { 
                conn.setAutoCommit(true);
                conn.close(); 
            } catch (SQLException ex) { ex.printStackTrace(); System.out.println("❌ Lỗi hệ thống: Không thể đóng kết nối."); }
        }
    }
}
}
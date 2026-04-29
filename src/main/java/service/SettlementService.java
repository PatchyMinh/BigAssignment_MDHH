package service;

import java.sql.Connection;
import java.sql.SQLException;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.ItemDAO;
import dao.ItemDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.AuctionSession;
import model.Bid;
import utils.DBConnection;

public class SettlementService {
    private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();
    private BidDAO bidDAO = new BidDAOImpl();
    private UserDAO userDAO = new UserDAOImpl();
    private ItemDAO itemDAO = new ItemDAOImpl(); // Cần gọi ItemDAO để đổi chủ
    private DBConnection dbConnection = new DBConnection();

    /**
     * Hàm xử lý kết thúc phiên đấu giá
     */
    public boolean settleAuction(String sessionId) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction, không ghi cứng từ đầu, để có thể rollback nếu có lỗi

            // 1. Kiểm tra thông tin phiên
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null || session.getStatus() != AuctionSession.Status.OPEN) {
                System.out.println("Lỗi: Phiên đấu giá không tồn tại hoặc đã bị đóng!");
                return false;
            }

            // 2. Lấy người trả giá cao nhất
            Bid winningBid = bidDAO.getHighestBid(conn, sessionId);

            if (winningBid != null) {
                // ==========================================
                // KỊCH BẢN 1: CÓ NGƯỜI THẮNG CUỘC
                // ==========================================
                int buyerId = winningBid.getBidder().getID();
                int sellerId = session.getSeller().getID();
                double finalPrice = winningBid.getAmount();
                userDAO.deductFrozenMoneyAtomic(conn, buyerId, finalPrice);
                userDAO.addMoneyAtomic(conn, sellerId, finalPrice);
                itemDAO.updateItemOwner(conn, session.getItem().getItemID(), buyerId);

                System.out.println("Đấu giá thành công! Hàng đã về tay người mua ID: " + buyerId);
            } else {
                // ==========================================
                // KỊCH BẢN 2: PHIÊN Ế (KHÔNG CÓ AI ĐẶT GIÁ)
                // ==========================================
                System.out.println("Phiên đấu giá kết thúc. Không có ai đặt giá.");
            }

            sessionDAO.updateSessionStatusAtomic(conn, sessionId, AuctionSession.Status.CLOSED);

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Lỗi hệ thống: Đã hoàn tác toàn bộ quá trình chốt đơn.");
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true);
                    conn.close(); 
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}
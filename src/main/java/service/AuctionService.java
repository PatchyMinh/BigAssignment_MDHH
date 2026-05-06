package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.AuctionSessionDAO;
import dao.AuctionSessionDAOImpl;
import dao.BidDAO;
import dao.BidDAOImpl;
import dao.UserDAO;
import dao.UserDAOImpl;
import model.AuctionSession;
import model.Bid;
import model.User;
import utils.DBConnection;

public class AuctionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    
    final private UserDAO userDAO;
    final private BidDAO bidDAO;
    final private AuctionSessionDAO sessionDAO = new AuctionSessionDAOImpl();

    public AuctionService() {
        this.userDAO = new UserDAOImpl();
        this.bidDAO = new BidDAOImpl();
    }

    public List<AuctionSession> getAllSessions() {
        return sessionDAO.getAllSessions();
    }

    public AuctionSession getSessionById(String sessionId) {
        return sessionDAO.getSessionById(sessionId);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public synchronized boolean placeBid(int currentUserId, String sessionId, double bidAmount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            AuctionSession session = sessionDAO.getSessionById(conn, sessionId);
            if (session == null) {
                logger.warn("Phiên đấu giá {} không tồn tại", sessionId);
                return false;
            }

            if (session.getStatus() != AuctionSession.Status.OPEN) {
                logger.warn("Phiên đấu giá {} chưa mở hoặc đã đóng", sessionId);
                return false;
            }

            Bid highestBid = bidDAO.getHighestBid(conn, sessionId);
            
            double minValidBid;
            if (highestBid == null) {
                minValidBid = session.getStartingPrice();
            } else {
                minValidBid = highestBid.getAmount() + session.getIncrementStep();
            }

            if (bidAmount < minValidBid) {
                logger.warn("Giá đặt {} không hợp lệ cho session {}. Phải >= {}", bidAmount, sessionId, minValidBid);
                conn.rollback();
                return false;
            }

            boolean isDeducted = userDAO.freezeMoneyAtomic(conn, currentUserId, bidAmount);
            if (!isDeducted) {
                logger.warn("Số dư không đủ để đặt giá {} cho user {} trong session {}", bidAmount, currentUserId, sessionId);
                conn.rollback();
                return false;
            }

            if (highestBid != null) {
                int previousUserId = highestBid.getBidder().getID();
                double previousAmount = highestBid.getAmount();
                userDAO.refundMoneyAtomic(conn, previousUserId, previousAmount);
            }

            Bid newBid = new Bid(userDAO.getUserById(conn, currentUserId), bidAmount);
            bidDAO.addBid(conn, sessionId, newBid);

            conn.commit();
            logger.info("Đặt giá thành công: user {}, session {}, amount {}", currentUserId, sessionId, bidAmount);
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Rollback transaction cho session {} do lỗi", sessionId);
                } catch (SQLException ex) {
                    logger.error("Lỗi khi rollback: {}", ex.getMessage(), ex);
                }
            }
            logger.error("Lỗi hệ thống khi đặt giá: {}", e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Lỗi khi đóng kết nối: {}", e.getMessage(), e);
                }
            }
        }
    }
}
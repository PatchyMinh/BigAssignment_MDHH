package dao;

import model.AuctionSession;
import model.Bid;
import model.User;
import utils.DBConnection;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionSessionDAOImpl implements AuctionSessionDAO {

    private UserDAO userDAO = new UserDAOImpl();
    private BidDAO bidDAO = new BidDAOImpl(); // Khởi tạo BidDAO để tái sử dụng

    @Override
    public boolean createSession(AuctionSession session, int itemId) {
        String sql = "INSERT INTO auction_sessions (session_id, owner_id, item_id, starting_price, step_price, duration_days, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, session.getSessionID());
            pstmt.setInt(2, session.getSeller().getID());
            pstmt.setInt(3, itemId);
            pstmt.setDouble(4, session.getStartingPrice());
            pstmt.setDouble(5, session.getIncrementStep());

            int durationDays = (int) Duration.between(session.getStartTime(), session.getEndTime()).toDays();
            pstmt.setInt(6, durationDays > 0 ? durationDays : 3);
            pstmt.setString(7, session.status.name());
            pstmt.setTimestamp(8, Timestamp.valueOf(session.getStartTime()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi khi tạo phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public AuctionSession getSessionById(String sessionId) {
        String sql = "SELECT * FROM auction_sessions WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSession(rs); // Tách logic map data ra hàm riêng cho sạch code
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AuctionSession> getAllSessions() {
        List<AuctionSession> sessionList = new ArrayList<>();
        String sql = "SELECT * FROM auction_sessions";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                sessionList.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy danh sách phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
        }
        return sessionList;
    }

    @Override
    public boolean updateSessionStatus(String sessionId, AuctionSession.Status status) {
        String sql = "UPDATE auction_sessions SET status = ? WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setString(2, sessionId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi khi cập nhật trạng thái phiên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Hàm phụ trợ giúp chuyển đổi ResultSet thành Object
    private AuctionSession mapResultSetToSession(ResultSet rs) throws SQLException {
        String sessionId = rs.getString("session_id");
        int ownerId = rs.getInt("owner_id");
        User seller = userDAO.getUserById(ownerId);
        double startingPrice = rs.getDouble("starting_price");
        double stepPrice = rs.getDouble("step_price");
        int durationDays = rs.getInt("duration_days");
        AuctionSession.Status status = AuctionSession.Status.valueOf(rs.getString("status").toUpperCase());

        LocalDateTime startTime = rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(durationDays);

        // Lấy dữ liệu Bid để tính giá hiện tại và người đang trả giá cao nhất
        double currentPrice = startingPrice;
        User highestBidder = null;
        List<Bid> bids = bidDAO.getBidsBySession(sessionId);
        if (bids != null && !bids.isEmpty()) {
            Bid highestBid = bids.get(0);
            currentPrice = highestBid.getAmount();
            highestBidder = highestBid.getBidder();
        }

        // Truyền vào Constructor đầy đủ
        return new AuctionSession(sessionId, seller, startingPrice, stepPrice,
                currentPrice, highestBidder, startTime, endTime, status);
    }
}
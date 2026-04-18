package dao;

import model.AuctionSession;
import model.Bid;
import model.User;
import utils.DBConnection;

import java.sql.*;
import java.time.Duration;
import java.util.List;

public class AuctionSessionDAOImpl implements AuctionSessionDAO {

    private UserDAO userDAO = new UserDAOImpl();

    @Override
    public boolean createSession(AuctionSession session, int itemId) {
        // Câu lệnh SQL chuẩn theo Database: dùng owner_id, step_price, duration_days
        String sql = "INSERT INTO auction_sessions (session_id, owner_id, item_id, starting_price, step_price, duration_days, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, session.getSessionID());
            pstmt.setInt(2, session.getSeller().getID());
            pstmt.setInt(3, itemId);
            pstmt.setDouble(4, session.getStartingPrice());
            pstmt.setDouble(5, session.getIncrementStep());

            // Tự động tính số ngày (duration_days) từ startTime và endTime
            int durationDays = (int) Duration.between(session.getStartTime(), session.getEndTime()).toDays();
            pstmt.setInt(6, durationDays > 0 ? durationDays : 3); // Nếu lỗi tính toán thì mặc định 3 ngày

            pstmt.setString(7, session.status.name()); // "PENDING", "OPEN"...

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tạo phiên đấu giá: " + e.getMessage());
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
                    // 1. Lấy thông tin người bán
                    int ownerId = rs.getInt("owner_id");
                    User seller = userDAO.getUserById(ownerId);

                    double startingPrice = rs.getDouble("starting_price");
                    double stepPrice = rs.getDouble("step_price");
                    int durationDays = rs.getInt("duration_days");

                    // 2. Khởi tạo đối tượng Session
                    AuctionSession session = new AuctionSession(seller, sessionId, startingPrice, stepPrice, durationDays);
                    session.status = AuctionSession.Status.valueOf(rs.getString("status"));

                    // 3. Phục hồi thời gian startTime và endTime từ created_at trong DB
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        session.setStartTime(createdAt.toLocalDateTime());
                        session.setEndTime(session.getStartTime().plusDays(durationDays));
                    }

                    // 4. Lấy Giá hiện tại và Người giá cao nhất TỪ BẢNG BIDS
                    BidDAO bidDAO = new BidDAOImpl();
                    List<Bid> bids = bidDAO.getBidsBySession(sessionId); // Hàm này đã ORDER BY amount DESC rồi

                    if (bids != null && !bids.isEmpty()) {
                        Bid highestBid = bids.get(0); // Lượt bid đầu tiên là cao nhất
                        session.setCurrentPrice(highestBid.getAmount());
                        session.setHighestBidder(highestBid.getBidder());
                    } else {
                        // Nếu chưa ai đặt giá thì giá hiện tại bằng giá khởi điểm
                        session.setCurrentPrice(startingPrice);
                    }

                    return session;
                }
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
        }
        return false;
    }
}
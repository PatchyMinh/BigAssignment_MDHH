package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import model.AuctionSession;
import model.Bid;
import model.Items;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.AuctionServer;
import utils.DBConnection;

public class AuctionSessionDAOImpl implements AuctionSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(AuctionServer.class);
    private UserDAO userDAO = new UserDAOImpl();
    private DBConnection dbConnection = new DBConnection();
    private ItemDAO itemDAO = new ItemDAOImpl();
    private BidDAO bidDAO = new BidDAOImpl(); // Khai báo thêm BidDAO ở đây để tái sử dụng

    // =========================================================================
    // 1. TẠO PHIÊN ĐẤU GIÁ
    // =========================================================================
    
    // Bản dùng trong Transaction
    @Override
    public boolean createSession(Connection conn, AuctionSession session, int itemId) throws SQLException {
        String sql = "INSERT INTO auction_sessions (session_id, owner_id, item_id, starting_price, step_price, duration_days, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        }
    }

    @Override
    public boolean createSession(AuctionSession session, int itemId) {
        try (Connection conn = dbConnection.getConnection()) {
            return createSession(conn, session, itemId);
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tạo phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // 2. LẤY THÔNG TIN MỘT PHIÊN ĐẤU GIÁ
    // =========================================================================
    @Override
    public AuctionSession getSessionById(Connection conn, String sessionId) throws SQLException {
        String sql = "SELECT * FROM auction_sessions WHERE session_id = ? FOR UPDATE"; 
        // Khóa dòng để đảm bảo tính nhất quán khi đọc dữ liệu trong transaction

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int ownerId = rs.getInt("owner_id");
                    User seller = userDAO.getUserById(conn, ownerId);
                    
                    Items item = itemDAO.getItemById(conn, rs.getInt("item_id")); 
                    
                    double startingPrice = rs.getDouble("starting_price");
                    double stepPrice = rs.getDouble("step_price");
                    int durationDays = rs.getInt("duration_days");

                    // 2. Khởi tạo đối tượng Session
                    AuctionSession session = new AuctionSession(seller, item, startingPrice, stepPrice, durationDays);
                    session.status = AuctionSession.Status.valueOf(rs.getString("status"));

                    // 3. Phục hồi thời gian startTime và endTime từ created_at trong DB
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        session.setStartTime(createdAt.toLocalDateTime());
                        session.setEndTime(session.getStartTime().plusDays(durationDays));
                    }

                    // 4. Lấy Giá hiện tại và Người giá cao nhất TỪ BẢNG BIDS
                    List<Bid> bids = bidDAO.getBidsBySession(conn, sessionId); 

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

    // Bản gọi lẹ (Bổ sung thêm)
    @Override
    public AuctionSession getSessionById(String sessionId) {
        try (Connection conn = dbConnection.getConnection()) {
            return getSessionById(conn, sessionId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<AuctionSession> getAllSessions() {
        List<AuctionSession> list = new ArrayList<>();
        String sql = "SELECT session_id FROM auction_sessions"; // Lấy các ID hiện có

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Tận dụng hàm getSessionById để lấy full thông tin từng phiên
                AuctionSession session = getSessionById(rs.getString("session_id"));
                if (session != null) {
                    list.add(session);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // 3. CẬP NHẬT TRẠNG THÁI PHIÊN
    // =========================================================================
    
    // Bản dùng trong Transaction (Code cũ của bạn)
    @Override
    public boolean updateSessionStatusAtomic(Connection conn, String sessionId, AuctionSession.Status status) throws SQLException {
        String sql = "UPDATE auction_sessions SET status = ? WHERE session_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, sessionId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    // Bản gọi lẹ (Bổ sung thêm)
    @Override
    public boolean updateSessionStatusAtomic(String sessionId, AuctionSession.Status status) {
        try (Connection conn = dbConnection.getConnection()) {
            return updateSessionStatusAtomic(conn, sessionId, status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
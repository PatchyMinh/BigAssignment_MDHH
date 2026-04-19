package dao;

import model.Bid;
import model.User;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAOImpl implements BidDAO {
    private UserDAO userDAO = new UserDAOImpl();
    private DBConnection dbConnection = new DBConnection();

    @Override
    public boolean addBid(String sessionId, Bid bid) {
        // Đổi bidder_id thành user_id
        String sql = "INSERT INTO bids (session_id, user_id, amount, bid_time) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sessionId);
            pstmt.setInt(2, bid.getBidder().getID());
            pstmt.setDouble(3, bid.getAmount());
            pstmt.setTimestamp(4, Timestamp.valueOf(bid.getTime()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Lỗi khi lưu lượt đặt giá: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Bid> getBidsBySession(String sessionId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE session_id = ? ORDER BY amount DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sessionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Đổi bidder_id thành user_id
                    int userId = rs.getInt("user_id");
                    User bidder = userDAO.getUserById(userId);

                    double amount = rs.getDouble("amount");
                    Timestamp bidTime = rs.getTimestamp("bid_time");

                    Bid bid = new Bid(bidder, amount);
                    bid.setTime(bidTime.toLocalDateTime());

                    bids.add(bid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bids;
    }
    @Override
    public Bid getHighestBid(Connection conn, String sessionId) throws SQLException {
        // Sắp xếp giảm dần theo số tiền và lấy 1 kết quả đầu tiên
        String sql = "SELECT * FROM bids WHERE session_id = ? ORDER BY amount DESC LIMIT 1";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Khởi tạo User (chỉ cần nhét ID để Service biết là ai)
                    User bidder = new User();
                    bidder.setID(rs.getInt("user_id"));
                    
                    // Trả về đối tượng Bid
                    Bid highestBid = new Bid(bidder, rs.getDouble("amount"));
                    // Nếu bảng bids của bạn có cột id hoặc timestamp, set thêm vào đây
                    return highestBid;
                }
            }
        }
        return null; // Trả về null nếu phiên này chưa có ai đặt giá
    }
}
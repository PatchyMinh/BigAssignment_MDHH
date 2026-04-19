package dao;

import java.sql.Connection;
import java.sql.SQLException;

import model.AuctionSession;

public interface AuctionSessionDAO {
    // 1. Tạo phiên đấu giá mới lưu vào Database
    boolean createSession(AuctionSession session, int itemId);

    // 2. Lấy thông tin một phiên đấu giá dựa vào ID
    AuctionSession getSessionById(String sessionId);

    // 3. Cập nhật trạng thái phiên
    boolean updateSessionStatusAtomic(Connection conn, String sessionId, AuctionSession.Status status) throws SQLException;
}

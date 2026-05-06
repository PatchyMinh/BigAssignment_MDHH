package dao;

import model.AuctionSession;
import java.util.List;

public interface AuctionSessionDAO {
    // 1. Tạo phiên đấu giá mới lưu vào Database
    boolean createSession(AuctionSession session, int itemId);

    // 2. Lấy thông tin một phiên đấu giá dựa vào ID
    AuctionSession getSessionById(String sessionId);

    // 3. Cập nhật trạng thái phiên
    boolean updateSessionStatus(String sessionId, AuctionSession.Status status);

    // Bổ sung lấy danh sách phục vụ hiển thị
    List<AuctionSession> getAllSessions();
}

package dao;

import model.Bid;
import java.util.List;

public interface BidDAO {
    // Lưu lượt đặt giá mới vào database
    boolean addBid(String sessionId, Bid bid);

    // Lấy danh sách lịch sử đặt giá của một phiên cụ thể, sắp xếp từ cao xuống thấp
    List<Bid> getBidsBySession(String sessionId);
}
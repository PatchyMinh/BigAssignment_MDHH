package server;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import service.AuctionService;
import com.google.gson.Gson;
import dto.BidRequest;
import service.SettlementService;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionServer extends WebSocketServer {
    // Khởi tạo service của bạn để gọi các hàm xử lý logic
    private final SettlementService settlementService;
    private final Gson gson = new Gson();
    private final AuctionService auctionService;
    private final Map<String, Set<WebSocket>> sessionSubscribers = new ConcurrentHashMap<>();
    private AuctionFeedServer feedServer;

    public AuctionServer(int port) {
        super(new InetSocketAddress(port));
        this.settlementService = new SettlementService();
        this.auctionService = new AuctionService();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("🟢 Có người dùng mới kết nối: " + webSocket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int i, String s, boolean b) {
        System.out.println("🔴 Người dùng đã ngắt kết nối: " + conn.getRemoteSocketAddress());
        for (Set<WebSocket> subscribers : sessionSubscribers.values()) {
            subscribers.remove(conn);
        }
    }
    private void processBid(WebSocket conn, BidRequest request) {
        // 1. Gọi service xử lý logic đấu giá
        boolean isSuccess = auctionService.placeBid(
                request.getUserId(),
                request.getSessionId(),
                request.getBidAmount()
        );

        if (isSuccess) {
            System.out.println("✅ Đặt giá thành công!");

            // 2. Tạo JSON thông báo giá mới
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", "NEW_BID");
            responseData.put("sessionId", request.getSessionId());
            responseData.put("newPrice", request.getBidAmount());
            String jsonResponse = gson.toJson(responseData);

            // 3. Lọc và gửi thông báo CHỈ cho những người đang xem phiên này
            feedServer.notifyObservers(request.getSessionId(), jsonResponse);

        } else {
            // Gửi lỗi cho riêng người vừa đặt giá
            conn.send("{\"type\": \"ERROR\", \"message\": \"Đặt giá thất bại! Số dư không đủ hoặc giá không hợp lệ.\"}");
        }
    }
    private void processSettlement(WebSocket conn, String sessionId) {
        // 1. Gọi service xử lý logic kết thúc phiên
        boolean isSuccess = settlementService.settleAuction(sessionId);

        if (isSuccess) {
            System.out.println("✅ Đã chốt phiên đấu giá thành công: " + sessionId);

            // 2. Tạo JSON thông báo phiên đã đóng
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", "SESSION_CLOSED");
            responseData.put("sessionId", sessionId);
            responseData.put("message", "Phiên đấu giá đã kết thúc. Cảm ơn bạn đã tham gia!");

            String jsonResponse = gson.toJson(responseData);

            // 3. Phát thanh thông báo cho những người đang xem phiên này biết
            if (feedServer != null) {
                feedServer.notifyObservers(sessionId, jsonResponse);
            }

        } else {
            // Gửi lỗi cho người vừa ra lệnh đóng phiên (có thể là admin)
            conn.send("{\"type\": \"ERROR\", \"message\": \"Lỗi: Không thể kết thúc phiên đấu giá!\"}");
        }
    }
    @Override
    public void onMessage(WebSocket webSocket, String message) {
        System.out.println("📩 Nhận được tin nhắn từ client: " + message);
        try {
            // 1. Phân tích chuỗi JSON thành JsonObject tổng quát
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();

            // 2. Lấy trường "type"
            if (!jsonObject.has("type")) return;
            String type = jsonObject.get("type").getAsString();

            // 3. Rẽ nhánh logic
            switch (type) {
                case "JOIN":
                    String joinSessionId = jsonObject.get("sessionId").getAsString();
                    sessionSubscribers.computeIfAbsent(joinSessionId, k -> ConcurrentHashMap.newKeySet()).add(webSocket);

                    System.out.println("👤 Người dùng " + webSocket.getRemoteSocketAddress() + " đã bắt đầu xem phiên: " + joinSessionId);
                    break;

                case "BID":
                    // Chuyển phần còn lại thành BidRequest như cũ
                    BidRequest request = gson.fromJson(jsonObject, BidRequest.class);
                    processBid(webSocket, request); // Chuyển logic đặt giá vào hàm riêng cho gọn
                    break;
                case "SETTLE" :
                    String settleSessionId = jsonObject.get("sessionId").getAsString();
                    processSettlement(webSocket, settleSessionId);
                    break;
                default:
                    System.out.println("Không nhận diện được loại tin nhắn: " + type);
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi xử lý tin nhắn: " + e.getMessage());
        }
    }


    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.err.println("❌ Đã xảy ra lỗi trên kết nối: " + webSocket.getRemoteSocketAddress());
        e.printStackTrace();
    }

    @Override
    public void onStart() {
        this.feedServer = new AuctionFeedServer();
        System.out.println("🚀 Auction Server đã khởi động thành công trên port: " + getPort());
    }

}

package networking;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import service.AuctionService;
import model.AuctionSession;
import model.User;

/**
 * AuctionServer - Lớp vỏ HTTP bọc bên ngoài AuctionService.
 * Nhiệm vụ: Khởi tạo server, lắng nghe yêu cầu từ client,
 * gọi logic nghiệp vụ (AuctionService) và trả kết quả về.
 */
public class AuctionServer {
    // HÀM PHỤ TRỢ GỬI PHẢN HỒI VỀ CLIENT
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
    // Đối tượng nghiệp vụ cốt lõi mà bạn đã xây dựng
    private static AuctionService auctionService;

    public static void main(String[] args) throws Exception {
        // 1. Khởi tạo service (có thể truyền các DAO thật vào)
        auctionService = new AuctionService(); // đã có constructor mặc định khởi tạo DAO bên trong

        // 2. Tạo HTTP server lắng nghe trên cổng 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 3. Định nghĩa context "/bid" – nơi nhận lệnh đặt giá
        server.createContext("/bid", AuctionServer::handleBid);
        // AuctionServer::handleBid là method reference (tham chiếu phương thức), sẽ được gọi khi có request tới /bid
        // thay thế cho HttpHandler thông thường để code gọn hơn
        server.createContext("/sessions", AuctionServer::handleSessions);

        server.createContext("/users", AuctionServer::handleUsers);

        // 4. Thêm context "/status" để kiểm tra server đang sống
        server.createContext("/status", (exchange) -> {
            sendResponse(exchange, "Auction Server is running!", 200);
        });

        // 5. Khởi động server và in thông báo ra console
        server.setExecutor(null);
        server.start();
        System.out.println("Auction Server đang chạy tại http://localhost:8080");
    }
    //  GET /sessions và GET /sessions/{id}
    private static void handleSessions(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, "Method Not Allowed", 405);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {
            // Case 1: GET /sessions (Lấy toàn bộ danh sách)
            if (pathParts.length == 2) {
                List<AuctionSession> list = auctionService.getAllSessions();
                sendResponse(exchange, "Tìm thấy " + list.size() + " phiên đấu giá đang diễn ra.", 200);
            }
            // Case 2: GET /sessions/{id} (Chi tiết một phiên)
            else if (pathParts.length == 3) {
                String sessionId = pathParts[2];
                AuctionSession session = auctionService.getSessionById(sessionId);
                if (session != null) {
                    sendResponse(exchange, "Phiên: " + session.getSessionID() + " | Giá khởi điểm: " + session.getStartingPrice() + " | Trạng thái: " + session.status, 200);
                } else {
                    sendResponse(exchange, "Không tìm thấy phiên đấu giá id: " + sessionId, 404);
                }
            }
        } catch (Exception e) {
            sendResponse(exchange, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

    // GET /users/{id} (Lấy số dư, thông tin user)
    private static void handleUsers(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, "Method Not Allowed", 405);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {
            if (pathParts.length == 3) {
                int userId = Integer.parseInt(pathParts[2]);
                User user = auctionService.getUserById(userId);
                if (user != null) {
                    sendResponse(exchange, "User: " + user.getUsername() + " | Số dư: " + user.getBalance() + " | Tiền đóng băng: " + user.getFrozenBalance(), 200);
                } else {
                    sendResponse(exchange, "Không tìm thấy người dùng ID: " + userId, 404);
                }
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, "ID người dùng phải là số!", 400);
        } catch (Exception e) {
            sendResponse(exchange, "Error: " + e.getMessage(), 500);
        }
    }
    /**
     * Xử lý yêu cầu POST tới /bid.
     * Body request có dạng: sessionId=xxx&userId=1&amount=xxx
     * Trả về plain text hoặc JSON thông báo kết quả.
     */
    private static void handleBid(HttpExchange exchange) throws IOException {
        // - Kiểm tra phương thức HTTP (chỉ chấp nhận POST)
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, "Method Not Allowed", 405);
                return; // NGẮT LUỒNG XỬ LÝ NẾU SAI PHƯƠNG THỨC
            }

            // - Đọc body request, phân tách tham số
            try{
                String requestBody;
                try (var is = exchange.getRequestBody()) {
                    requestBody = new String(is.readAllBytes());
                }
                String[] params = requestBody.split("&");

                String sessionID = params[0].split("=")[1]; // sessionId=xxx
                int userId = Integer.parseInt(params[1].split("=")[1]); // userId=yyy
                double amount = Double.parseDouble(params[2].split("=")[1]); // amount=zzz
                boolean success = auctionService.placeBid(userId, sessionID, amount);
            // - Ghi kết quả vào response (200 nếu thành công, 400 nếu lỗi)
                if (success) {
                    sendResponse(exchange, "Bid placed successfully!", 200);
                } else {
                    sendResponse(exchange, "Failed to place bid. Check your balance or bid amount.", 400);
                }
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                sendResponse(exchange, "Bad Request: Invalid parameters format.", 400);
            } catch (Exception e){
                sendResponse(exchange, "Internal Server Error: " + e.getMessage(), 500);
            }
        }
    }
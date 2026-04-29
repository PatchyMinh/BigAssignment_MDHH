package networking;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import service.AuctionService;

/**
 * AuctionServer - Lớp vỏ HTTP bọc bên ngoài AuctionService.
 * Nhiệm vụ: Khởi tạo server, lắng nghe yêu cầu từ client,
 * gọi logic nghiệp vụ (AuctionService) và trả kết quả về.
 */
public class AuctionServer {

    // Đối tượng nghiệp vụ cốt lõi mà bạn đã xây dựng
    private static AuctionService auctionService;

    public static void main(String[] args) throws Exception {
        // 1. Khởi tạo service (có thể truyền các DAO thật vào)
        auctionService = new AuctionService(); // đã có constructor mặc định khởi tạo DAO bên trong

        // 2. Tạo HTTP server lắng nghe trên cổng 8080
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8080), 0);

        // 3. Định nghĩa context "/bid" – nơi nhận lệnh đặt giá
        server.createContext("/bid", AuctionServer::handleBid);
        // AuctionServer::handleBid là method reference (tham chiếu phương thức), sẽ được gọi khi có request tới /bid
        // thay thế cho HttpHandler thông thường để code gọn hơn

        // 4. Thêm context "/status" để kiểm tra server đang sống
        server.createContext("/status", (httpExchange) -> {
            String response = "Auction Server is running!";
            httpExchange.sendResponseHeaders(200, response.length());
            try (var os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // 5. Khởi động server và in thông báo ra console
        server.start();
        System.out.println("Auction Server đang chạy tại http://localhost:8080");
    }

    /**
     * Xử lý yêu cầu POST tới /bid.
     * Body request có dạng: sessionId=xxx&userId=1&amount=xxx
     * Trả về plain text hoặc JSON thông báo kết quả.
     */
    private static void handleBid(HttpExchange exchange) {
        String sessionID;
        int userId;
        double amount;
        // - Kiểm tra phương thức HTTP (chỉ chấp nhận POST)
            if (!"POST".equals(exchange.getRequestMethod())) {
                String response = "Method Not Allowed";
                try {
                exchange.sendResponseHeaders(405, response.length());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // - Đọc body request, phân tách tham số
                String requestBody;
                try (var is = exchange.getRequestBody()) {
                    requestBody = new String(is.readAllBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            String[] params = requestBody.split("&");
            try {
            sessionID = params[0].split("=")[1]; // sessionId=xxx
            userId = Integer.parseInt(params[1].split("=")[1]); // userId=yyy
            amount = Double.parseDouble(params[2].split("=")[1]); // amount=zzz
            boolean success = auctionService.placeBid(userId, sessionID, amount);
            // - Ghi kết quả vào response (200 nếu thành công, 400 nếu lỗi)
            if (success) {
                String successResponse = "Bid placed successfully!";
                try {
                    exchange.sendResponseHeaders(200, successResponse.length());
                    try (var os = exchange.getResponseBody()) {
                        os.write(successResponse.getBytes());
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } 
            else {
                String bidFailedResponse = "Failed to place bid. Check your input or server logs.";
                try {
                    exchange.sendResponseHeaders(400, bidFailedResponse.length());
                    try (var os = exchange.getResponseBody()) {
                        os.write(bidFailedResponse.getBytes());
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
            }
            catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                System.out.println("❌ Lỗi: Tham số không hợp lệ. Body phải có dạng sessionId=xxx&userId=yyy&amount=zzz");
                String wrongArgumentResponse = "Bad Request: Invalid parameters.";
                try {
                    exchange.sendResponseHeaders(400, wrongArgumentResponse.length());
                    try (var os = exchange.getResponseBody()) {
                        os.write(wrongArgumentResponse.getBytes());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
package networking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

/**
 * AuctionClient - Giao diện dòng lệnh cho người tham gia đấu giá.
 * Kết nối tới AuctionServer qua HTTP, nhập lệnh từ bàn phím.
 */
public class AuctionClient {

    private static final String SERVER_URL = "http://localhost:8080"; // chạy localhost trước

    // Công cụ nhập liệu và gửi request
    private static final Scanner scanner = new Scanner(System.in);
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) {
        // 1. In lời chào và hướng dẫn sử dụng
        System.out.println("CHÀO MỪNG ĐẾN VỚI SÀN ĐẤU GIÁ TRỰC TUYẾN");
        System.out.println("Các lệnh: bid <sessionId> <giá> | exit");

        // 2. Vòng lặp chính: đọc lệnh người dùng và xử lý
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;
            if (input.startsWith("bid")) {
                // Phân tách input và gọi sendBidRequest(...)
                String[] parts = input.split("\\s+");
                if (parts.length == 4) {
                    String sessionId = parts[1];
                    int userId = Integer.parseInt(parts[2]);
                    double amount = Double.parseDouble(parts[3]);
                    sendBidRequest(sessionId, userId, amount);
                } else {
                    System.out.println("Sai định dạng lệnh. Sử dụng: bid <sessionId> <userId> <amount>");
                }
            }
     }

        System.out.println("Cảm ơn bạn đã tham gia. Tạm biệt!");
    }

    /**
     * Gửi yêu cầu đặt giá lên server qua HTTP POST.
     * @param sessionId Mã phiên đấu giá
     * @param amount    Số tiền muốn đặt
     */
    private static void sendBidRequest(String sessionId, int userId, double amount) {
        // - Tạo URI: SERVER_URL + "/bid"
        URI uri = URI.create(SERVER_URL + "/bid"); // đã có Serialization nên không cần thêm tham số vào URI nữa, sẽ gửi qua body
        // - Tạo body request dạng: "sessionId=xxx&userId=yyy&amount=zzz"
        String requestBody = "sessionId=" + sessionId + "&userId=" + userId + "&amount=" + amount;
        // - Tạo HttpRequest với phương thức POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        // - Gửi request đồng bộ và nhận response
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // - In kết quả ra console
            System.out.println("Server phản hồi: " + response.body());
        } catch (Exception e) {
            System.out.println("Lỗi khi kết nối tới server: " + e.getMessage());
        }
    }
}
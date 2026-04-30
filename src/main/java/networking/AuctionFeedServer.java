package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionFeedServer {
    public static final int PORT = 8081; // Cổng riêng cho kênh thông báo

    // Lưu trữ các phòng đấu giá: sessionId -> danh sách ClientHandler đang theo dõi
    // Dùng ConcurrentHashMap để an toàn luồng khi nhiều client join/rời cùng lúc
    private static final Map<String, List<ClientHandler>> rooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Khởi động Auction Feed Server trên cổng " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đã sẵn sàng, chờ kết nối...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Mỗi client được phục vụ trong một thread riêng
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi server socket: " + e.getMessage());
        }
    }

    /**
     * Gửi thông báo đến tất cả client đang theo dõi một phiên đấu giá.
     * Phương thức static để AuctionApiController có thể gọi dễ dàng.
     */
    public static synchronized void broadcast(String sessionId, String message) {
        List<ClientHandler> clients = rooms.get(sessionId);
        if (clients != null) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * ClientHandler – Runnable quản lý kết nối socket của một client.
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String sessionId; // Phòng mà client này đã join

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = writer;
                // Giao thức: nếu đã Bid hoặc wishlist thì tự động Join, không cần Join phòng lẻ.
                String line = in.readLine();
                if (line != null && line.startsWith("JOIN ")) {
                    sessionId = line.substring(5).trim();
                    // Đăng ký client vào phòng
                    rooms.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(this);
                    sendMessage("Bạn đã tham gia phòng đấu giá: " + sessionId);
                    System.out.println("Client đã JOIN phòng " + sessionId + " từ " + socket.getInetAddress());
                } else {
                    sendMessage("Lỗi: Bạn phải gửi lệnh JOIN <sessionId> để tham gia phòng.");
                    return;
                }

                // Giữ kết nối mở để nhận thêm lệnh (nếu có) hoặc chỉ đơn giản là giữ luồng sống
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("LEAVE")) {
                        break; // Thoát vòng, sẽ rời phòng và đóng kết nối
                    }
                    else if (line.equalsIgnoreCase("PING")) {
                        sendMessage("PONG");
                    }
                }
            } catch (IOException e) {
                System.err.println("Lỗi kết nối client: " + e.getMessage());
            } finally {
                // Khi client ngắt kết nối, xóa khỏi phòng
                if (sessionId != null) {
                    List<ClientHandler> clients = rooms.get(sessionId);
                    if (clients != null) {
                        clients.remove(this);
                        if (clients.isEmpty()) {
                            rooms.remove(sessionId); // Dọn dẹp phòng trống
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /** Tiện ích gửi tin nhắn an toàn */
        private void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
    }
}
// package main.java;

import java.time.*;
import java.util.*;

public class AuctionSession {
    private User seller;
    final private String sessionID;
    final private double startingPrice;
    final private double incrementStep;
    private double currentPrice;
    private User highestBidder = null;
    private ArrayList<Bid> bidHistory = new ArrayList<Bid>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public enum Status { PENDING, OPEN, CLOSED, CANCELLED }; // một nhóm các hằng số
    public Status status;

    public String getSessionID(){
         return this.sessionID;
    }
    public AuctionSession(User seller, String sessionID, double startingPrice, double incrementStep, int openDays){
        this.seller = seller;
        this.sessionID = sessionID;
        this.startingPrice = startingPrice;
        this.incrementStep = incrementStep;
        this.startTime = LocalDateTime.now();
        this.endTime = startTime.plusDays(openDays);
        this.status = Status.PENDING;
        if (this.seller != null) {
            this.seller.addCreatedSessions(this); // thêm phiên đấu giá vào lịch sử của người bán
        }
    }
    public AuctionSession(User seller, String sessionID, double startingPrice){
        this(seller, sessionID, startingPrice, 0.1, 3);
    }
    public boolean isValidBid(double amount){
        if (this.status != Status.OPEN) {
            System.out.println("Phiên đấu giá chưa mở hoặc đã kết thúc!");
            return false;
        }
        if (amount >= currentPrice + incrementStep) {
            return true;
        }
        else {
            return false;
        }
    }
    public synchronized boolean placeBid(User user, double amount){
        if (!isValidBid(amount)){
            return false; // bid sai luật thì không cho bid
        }
        if (amount > user.getBalance()){
            System.out.println("Tài khoản của " + user.getUsername() + " không đủ tiền để thực hiện lượt đấu giá.");
            return false; // không đủ tiền thì không cho bid
        }
        this.currentPrice = amount;
        this.highestBidder = user;

        Bid newBid = new Bid(user, amount);
        bidHistory.add(newBid); // lưu lịch sử bid
        if (!user.getJoinedAuctionSessions().contains(this)) {
            user.addJoinedSessions(this); // thêm phiên đấu giá vào lịch sử của người bid
        }
        System.out.println("Người dùng " + user.getUsername() + " đã đấu giá thành công!");
        return true;
    }
    public void startSession(int openDays) {
        this.status = Status.OPEN;
        this.currentPrice = startingPrice; 
        this.startTime = LocalDateTime.now(); // Lấy giờ bấm nút
        this.endTime = this.startTime.plusDays(openDays); // Tính giờ đóng cửa
        System.out.println("Phiên đấu giá đã CHÍNH THỨC BẮT ĐẦU! Giá khởi điểm: " + startingPrice);
        System.out.println("Kết thúc vào: " + this.endTime);
    }
    public void endSession() {
        this.status = Status.CLOSED;
        this.endTime = LocalDateTime.now();

        System.out.println("\n=== PHIÊN ĐẤU GIÁ KẾT THÚC ===");
        if (highestBidder != null) {
            System.out.println("Người chiến thắng: " + highestBidder.getUsername() + " với giá " + currentPrice);
            // Gợi ý cho sau này: Tại đây bạn có thể gọi Transaction để trừ tiền người thắng
            // và cộng tiền cho seller.
        } else {
            System.out.println("Không có ai tham gia trả giá. Vật phẩm chưa được bán!");
        }
    }
}
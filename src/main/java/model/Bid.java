package model;

import java.time.LocalDateTime;

public class Bid {
        private User bidder;
        private double amount;
        private LocalDateTime time;

        public Bid(User bidder, double amount) {
            this.bidder = bidder;
            this.amount = amount;
            this.time = LocalDateTime.now();
        }
        
        public double getAmount() { return amount; }
        public User getBidder() { return bidder; }
        public LocalDateTime getTime() { return time; }

        // Thêm setTime để gán thời gian từ DB
        public void setTime(LocalDateTime time) {
            this.time = time;
        }
}
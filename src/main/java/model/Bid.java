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

        // Constructor đầy đủ cho DAO khi lấy từ Database
        public Bid(User bidder, double amount, LocalDateTime time) {
            this.bidder = bidder;
            this.amount = amount;
            this.time = time;
        }
        
        public double getAmount() { return amount; }
        public User getBidder() { return bidder; }
        public LocalDateTime getTime() { return time; }
}
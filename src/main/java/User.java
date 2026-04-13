// package main.java;
import java.util.*;
import java.security.SecureRandom;
public class User {
    protected String realName;
    protected String username;
    protected String id;
    protected String email;
    private String password;
    private String phoneNumber;
    private double balance = 0;
    private List<AuctionSession> myCreatedAuctions;
    private List<AuctionSession> myJoinedAuctions;
    protected User(){};
    public User(String realName, String username, String email, String password, String phoneNumber){
        this.realName = realName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.id = genID();
        this.myCreatedAuctions = new ArrayList<>();
        this.myJoinedAuctions = new ArrayList<>();
    }
    public String getRealName(){
        return this.realName;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPhoneNumber(){
        return this.phoneNumber;
    }
    public double getBalance(){
        return this.balance;
    }
    public List<AuctionSession> getJoinedAuctionSessions(){
         return this.myJoinedAuctions;
    }
    public List<AuctionSession> getCreatedAuctionSessions(){
         return this.myCreatedAuctions;
    }
    public void addCreatedSessions(AuctionSession session){
        this.myCreatedAuctions.add(session);
    }
    public void addJoinedSessions(AuctionSession session){
        this.myJoinedAuctions.add(session);
    }
    public void deposit(double amount){
        balance += amount;
    }
    public void withdraw(double amount){
        balance -= amount;
    }
    public void setRealName(String newRealName){
        this.realName = newRealName;
        System.out.println("Name changed successfully.");
    }
    public void setUsername(String newUsername){
        this.username = newUsername;
        System.out.println("Username changed successfully.");
    }
    public void setPassword(String newPassword){ // yêu cầu phải nhập SĐT để xác thực
        this.password = newPassword;
    }
    private static String genID(){ // gen xâu 8 chữ số
        SecureRandom secureRandom = new SecureRandom();
        int randomNumber = secureRandom.nextInt(100000000);
        return String.format("%08d", randomNumber);
    }
    
}
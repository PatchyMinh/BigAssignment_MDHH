package model;
import java.util.*;
public class User {
    protected String realName;
    protected String username;
    protected int id;
    protected String email;
    protected Role role;
    private String password;
    private String phoneNumber;
    private double balance = 0;
    private double frozenBalance = 0;
    private List<AuctionSession> myCreatedAuctions = new ArrayList<>();
    private List<AuctionSession> myJoinedAuctions = new ArrayList<>();
    public enum Role {USER, ADMIN};
    public User(){};
    public User(String realName, String username, String email, String password, String phoneNumber){
        this.role = Role.USER;
        this.realName = realName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.myCreatedAuctions = new ArrayList<>();
        this.myJoinedAuctions = new ArrayList<>();
    }
    //Bổ sung Getter
    public String getPassword(){ return this.password; }
    public String getEmail(){ return this.email; }

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
    public double getFrozenBalance(){
        return this.frozenBalance;
    }
    public int getID(){
        return this.id;
    }
    public Role getRole(){
        return this.role;
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
    public void setID(int id){
        this.id = id;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void setFrozenBalance(double frozenBalance) {
        this.frozenBalance = frozenBalance;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    // Bổ sung Setter phục vụ Register
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
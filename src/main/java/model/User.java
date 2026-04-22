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
    private List<AuctionSession> myCreatedAuctions;
    private List<AuctionSession> myJoinedAuctions;
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
    // Constructor đầy đủ cho DAO khi lấy từ Database
    public User(int id, String username, String password, String realName, String email,
                String phoneNumber, Role role, double balance, double frozenBalance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.balance = balance;
        this.frozenBalance = frozenBalance;
    }

    public String getRealName(){
        return this.realName;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    public String getEmail(){
        return this.email;
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
        if (this.myJoinedAuctions == null) {
            this.myJoinedAuctions = new ArrayList<>();
        }
         return this.myJoinedAuctions;
    }
    public List<AuctionSession> getCreatedAuctionSessions(){
        if (this.myCreatedAuctions == null){
            this.myCreatedAuctions = new ArrayList<>();
        }
         return this.myCreatedAuctions;
    }
    public void addCreatedSessions(AuctionSession session){
        if (this.myCreatedAuctions == null) {
            this.myCreatedAuctions = new ArrayList<>();
        }
        this.myCreatedAuctions.add(session);
    }
    public void addJoinedSessions(AuctionSession session){
        if (this.myJoinedAuctions == null) {
            this.myJoinedAuctions = new ArrayList<>();
        }
        this.myJoinedAuctions.add(session);
    }
    public void deposit(double amount){
        balance += amount;
    }
    public void withdraw(double amount){
        balance -= amount;
    }
    public void setID(int id){
        this.id = id;
    }
}
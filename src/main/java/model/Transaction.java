package model;

public class Transaction {
    public void deposit(User user, double amount){
        if (amount > 0) {
        user.deposit(amount);
        System.out.printf("Nạp thành công %.2f vào tài khoản", amount);
        }
        else {
            System.err.println("Không thể thực hiện giao dịch!");
        }
    }
    public void withdraw(User user, double amount){
        if (amount > 0 && user.getBalance() >= amount) {
            user.withdraw(amount);
            System.out.printf("Rút thành công %.2f ra khỏi tài khoản", amount);
        }
        else {
        System.err.println("Không thể thực hiện giao dịch!");
        }
    }
}

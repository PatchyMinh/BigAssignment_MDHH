package model;
//Lớp trừu tượng đại diện cho một món hàng cơ bản trong hệ thống đấu giá.

public abstract class Items {
    //Tích hợp tự động sinh UUID hoặc lấy ID từ Database sau.
    protected int itemID;
    protected User owner;
    protected double startingPrice;
    protected String description;
    

    public Items(User owner, double startingPrice, String description) {
        //this.itemID = UUID.randomUUID().toString();
        this.owner = owner;
        this.startingPrice = startingPrice;
        this.description = description;
    }

    // Thêm Getter và Setter cho itemID
    public int getItemID() { return itemID; }
    public void setItemID(int itemID) { this.itemID = itemID; }

    public User getOwner() {
        return owner;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public abstract String showInfo();
}

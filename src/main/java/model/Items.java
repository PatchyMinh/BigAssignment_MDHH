package model;
//Lớp trừu tượng đại diện cho một món hàng cơ bản trong hệ thống đấu giá.

public abstract class Items {
    //Tích hợp tự động sinh UUID hoặc lấy ID từ Database sau.
    protected int itemID;
    protected String owner;
    protected double startingPrice;
    protected String description;


    public Items(int itemID, String owner, double startingPrice, String description) {
        this.itemID = itemID;
        this.owner = owner;
        this.startingPrice = startingPrice;
        this.description = description;
    }

    public void setItemID(int itemID) { this.itemID = itemID; }
    public int getItemID() { return itemID; }
    public String getOwner() {
        return owner;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public String getDescription(){
        return description;
    }

    public abstract String showInfo();
}

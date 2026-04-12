//Lớp trừu tượng đại diện cho một món hàng cơ bản trong hệ thống đấu giá.

abstract class Items {
    //Tích hợp tự động sinh UUID hoặc lấy ID từ Database sau.
    //protected String itemID;
    protected String owner;
    protected double startingPrice;
    protected String description;


    public Items(String owner, double startingPrice, String description) {
        //this.itemID = UUID.randomUUID().toString();
        this.owner = owner;
        this.startingPrice = startingPrice;
        this.description = description;
    }

    /*
    public String getItemID(){ return itemID; }
     */
    public String getOwner() {
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

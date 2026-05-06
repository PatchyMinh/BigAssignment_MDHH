package model;
public class Electronics extends Items{
    private final int warranty;
    private final String brand;

    public Electronics(int itemID, String owner, double startingPrice, String description, int warranty, String brand) {
        super(itemID, owner, startingPrice, description);
        this.warranty = warranty;
        this.brand = brand;
    }

    public int getWarranty() {
        return warranty;
    }
    public String getBrand() {
        return brand;
    }

    @Override
    public String showInfo() {
        return "Current Item: \nType: Electronic\n Owner: " + getOwner() + "\nBrand: " + getBrand() + "\nWarranty Period: " + getWarranty()+ "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}

public class Electronics extends Items{
    private int warranty;
    private String brand;

    public Electronics(String owner, double startingPrice, String description, int warranty, String brand) {
        super(owner, startingPrice, description);
        this.warranty = warranty;
        this.brand = brand;
    }

    public int getWarranty() {
        return warranty;
    }
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setWarranty(int warranty) {
        this.warranty = warranty;
    }

    @Override
    public String showInfo() {
        return "Current Item: \nType: Electronic\n Owner: " + getOwner() + "\nBrand: " + getBrand() + "\nWarranty Period: " + getWarranty()+ "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}

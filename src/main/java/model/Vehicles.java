package model;
public class Vehicles extends Items{
    private String brand;
    private int mileage;
    private String vehicleID;

    public Vehicles(User owner, double startingPrice, String description, String brand, int mileage, String vehicleID) {
        super(owner, startingPrice, description);
        this.brand = brand;
        this.mileage = mileage;
        this.vehicleID = vehicleID;
    }

    public String getBrand() {
        return brand;
    }
    public String getVehicleID() {
        return vehicleID;
    }
    public int getMileage() {
        return mileage;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    @Override
    public String showInfo() {
        return "Current Item: \nType: Vehicle\n Owner: " + getOwner() + "\nBrand: " + getBrand() + "\nLicense Plate: " + getVehicleID() + "\nMileage: " + getMileage() + "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}

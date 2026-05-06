package model;

import java.io.IOException;

public class Vehicles extends Items{
    private final String brand;
    private final int mileage;
    private final String vehicleID;

    public Vehicles(int itemID, String owner, double startingPrice, String description, String brand, int mileage, String vehicleID){
        super(itemID, owner, startingPrice, description);
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

    @Override
    public String showInfo() {
        return "Current Item: \nType: Vehicle\n Owner: " + getOwner() + "\nBrand: " + getBrand() + "\nLicense Plate: " + getVehicleID() + "\nMileage: " + getMileage() + "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}

// ItemsAttributes chứa tất cả các thuộc tính đầu vào để tạo Items.
// Hỗ trợ khắc phục việc tạo nhiều tham số trong Factory.
// Nhược điểm hiện tại: Có thể tạo ra nhiều giá trị null với các thuộc tính không tương thích.
// Nên refactor sang Builder Pattern (Creational Pattern) để tối ưu việc khởi tạo.

import java.time.LocalDate;

public class ItemsAttributes {
    // Thuoc tinh chung
    private String owner;
    private double startingPrice;
    private String description;

    // Thuoc tinh rieng cho Arts
    private String artistName;
    private LocalDate releaseDate;

    // Thuoc tinh rieng cho Electronics / Vehicles
    private int warranty;
    private String brand;
    private int mileage;
    private String vehicleID;

    public String getOwner(){ return this.owner; }
    public double getStartingPrice(){ return this.startingPrice; }
    public String getDescription(){ return this.description; }
    public String getArtistName(){ return this.artistName; }
    public LocalDate getReleaseDate(){ return this.releaseDate; }
    public int getWarranty(){ return this.warranty; }
    public String getBrand(){ return this.brand; }
    public int getMileage(){ return this.mileage; }
    public String getVehicleID(){ return this.vehicleID; }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setWarranty(int warranty) {
        this.warranty = warranty;
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
}
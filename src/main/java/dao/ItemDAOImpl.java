package dao;

import model.*;
import utils.DBConnection;
import java.sql.*;

public class ItemDAOImpl implements ItemDAO {
    @Override
    public void addItem(Items item) {
        String sql = "INSERT INTO items (item_type, owner, starting_price, description, " +
                "artist_name, release_date, warranty, brand, mileage, vehicle_id_plate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(2, item.getOwner());
            ps.setDouble(3, item.getStartingPrice());
            ps.setString(4, item.getDescription());

            if (item instanceof Arts) {
                Arts art = (Arts) item;
                ps.setString(1, "Arts");
                ps.setString(5, art.getArtistName());
                ps.setDate(6, java.sql.Date.valueOf(art.getReleaseDate()));
                ps.setNull(7, java.sql.Types.INTEGER); // Sửa thành INTEGER
                ps.setNull(8, java.sql.Types.VARCHAR);
                ps.setNull(9, java.sql.Types.INTEGER); // Sửa thành INTEGER
                ps.setNull(10, java.sql.Types.VARCHAR);

            } else if (item instanceof Electronics) {
                Electronics elec = (Electronics) item;
                ps.setString(1, "Electronics");
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.DATE);
                ps.setInt(7, elec.getWarranty());      // Dùng setInt cho warranty
                ps.setString(8, elec.getBrand());
                ps.setNull(9, java.sql.Types.INTEGER); // Sửa thành INTEGER
                ps.setNull(10, java.sql.Types.VARCHAR);

            } else if (item instanceof Vehicles) {
                Vehicles v = (Vehicles) item;
                ps.setString(1, "Vehicles");
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.DATE);
                ps.setNull(7, java.sql.Types.INTEGER); // Sửa thành INTEGER
                ps.setString(8, v.getBrand());
                ps.setInt(9, v.getMileage());          // Dùng setInt cho mileage
                ps.setString(10, v.getVehicleID());
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setItemID(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Items getItemById(int id) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Items item = null;
                    String type = rs.getString("item_type");
                    String owner = rs.getString("owner");
                    double startingPrice = rs.getDouble("starting_price");
                    String desc = rs.getString("description");

                    if ("Arts".equals(type)) {
                        String artist = rs.getString("artist_name");
                        Date releaseDate = rs.getDate("release_date");
                        item = new Arts(owner, startingPrice, desc, artist, releaseDate != null ? releaseDate.toLocalDate() : null);

                    } else if ("Electronics".equals(type)) {
                        int warranty = rs.getInt("warranty"); // Dùng getInt
                        String brand = rs.getString("brand");
                        item = new Electronics(owner, startingPrice, desc, warranty, brand);

                    } else if ("Vehicles".equals(type)) {
                        String brand = rs.getString("brand");
                        int mileage = rs.getInt("mileage");   // Dùng getInt
                        String vehicleId = rs.getString("vehicle_id_plate");
                        item = new Vehicles(owner, startingPrice, desc, brand, mileage, vehicleId);
                    }

                    if (item != null) {
                        item.setItemID(id);
                    }
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
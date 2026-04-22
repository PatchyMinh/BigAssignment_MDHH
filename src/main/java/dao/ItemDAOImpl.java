package dao;

import model.*;
import utils.DBConnection;
import java.sql.*;

public class ItemDAOImpl implements ItemDAO {
    DBConnection dbConnection = new DBConnection();
    @Override
    public void addItem(Items item) {
        String sql = "INSERT INTO items (item_type, owner, starting_price, description, " +
                "artist_name, release_date, warranty, brand, mileage, vehicle_id_plate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(2, item.getOwnerName());
            ps.setDouble(3, item.getStartingPrice());
            ps.setString(4, item.getDescription());

            if (item instanceof Arts) {
                Arts art = (Arts) item;
                ps.setString(1, "Arts");
                ps.setString(5, art.getArtistName());
                ps.setDate(6, java.sql.Date.valueOf(art.getReleaseDate()));
                ps.setNull(7, java.sql.Types.INTEGER);
                ps.setNull(8, java.sql.Types.VARCHAR);
                ps.setNull(9, java.sql.Types.INTEGER);
                ps.setNull(10, java.sql.Types.VARCHAR);

            } else if (item instanceof Electronics) {
                Electronics elec = (Electronics) item;
                ps.setString(1, "Electronics");
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.DATE);
                ps.setInt(7, elec.getWarranty());
                ps.setString(8, elec.getBrand());
                ps.setNull(9, java.sql.Types.INTEGER);
                ps.setNull(10, java.sql.Types.VARCHAR);

            } else if (item instanceof Vehicles) {
                Vehicles veh = (Vehicles) item;
                ps.setString(1, "Vehicles");
                ps.setNull(5, java.sql.Types.VARCHAR);
                ps.setNull(6, java.sql.Types.DATE);
                ps.setNull(7, java.sql.Types.INTEGER);
                ps.setString(8, veh.getBrand());
                ps.setInt(9, veh.getMileage());
                ps.setString(10, veh.getVehicleID());
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
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("item_type");
                    String owner = rs.getString("owner");
                    double startingPrice = rs.getDouble("starting_price");
                    String desc = rs.getString("description");

                    // Trực tiếp return đối tượng hoàn chỉnh qua Constructor
                    if ("Arts".equals(type)) {
                        return new Arts(id, owner, startingPrice, desc,
                                rs.getString("artist_name"),
                                rs.getDate("release_date").toLocalDate());
                    } else if ("Electronics".equals(type)) {
                        return new Electronics(id, owner, startingPrice, desc,
                                rs.getInt("warranty"),
                                rs.getString("brand"));
                    } else if ("Vehicles".equals(type)) {
                        return new Vehicles(id, owner, startingPrice, desc,
                                rs.getString("brand"),
                                rs.getInt("mileage"),
                                rs.getString("vehicle_id_plate"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public boolean updateItemOwner(Connection conn, int itemId, int newOwnerId) throws SQLException {
        // Thủ thuật: Dùng truy vấn lồng (SELECT) để lấy username của người thắng (dựa vào ID)
        // và gán thẳng vào cột 'owner' của món hàng.
        String sql = "UPDATE items SET owner = (SELECT username FROM users WHERE id = ?) WHERE item_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newOwnerId); // ID của người thắng (Buyer)
            pstmt.setInt(2, itemId);     // ID của món hàng
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}
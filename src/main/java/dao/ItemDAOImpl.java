package dao;

import model.*;
import utils.DBConnection;
import java.sql.*;

public class ItemDAOImpl implements ItemDAO {
    private DBConnection dbConnection = new DBConnection();
    @Override
    public void addItem(Items item) {
        String sql = "INSERT INTO items (item_type, owner, starting_price, description, " +
                "artist_name, release_date, warranty, brand, mileage, vehicle_id_plate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(2, Integer.toString(item.getOwner().getID()));
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
        // Dùng JOIN để lấy luôn toàn bộ thông tin của User làm chủ món đồ
        String sql = "SELECT i.*, u.id AS user_id, u.username, u.password, u.real_name, u.email, u.phone_number, u.role, u.balance " +
                     "FROM items i " +
                     "JOIN users u ON i.owner = u.username " + // Khớp cột owner của items với username của users
                     "WHERE i.item_id = ?";
                     
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. TẠO ĐỐI TƯỢNG USER HOÀN CHỈNH TỪ KẾT QUẢ SQL
                    User fullOwner = new User();
                    fullOwner.setID(rs.getInt("user_id"));
                    fullOwner.setUsername(rs.getString("username"));
                    fullOwner.setPassword(rs.getString("password"));
                    fullOwner.setRealName(rs.getString("real_name"));
                    fullOwner.setEmail(rs.getString("email"));
                    fullOwner.setPhoneNumber(rs.getString("phone_number"));
                    // Giả sử Role là Enum, cần convert từ String
                    fullOwner.setRole(User.Role.valueOf(rs.getString("role"))); 
                    fullOwner.setBalance(rs.getDouble("balance"));

                    // 2. KHỞI TẠO ITEM VÀ TRUYỀN USER HOÀN CHỈNH VÀO CONSTRUCTOR
                    Items item = null;
                    String type = rs.getString("item_type");
                    double startingPrice = rs.getDouble("starting_price");
                    String desc = rs.getString("description");

                    if ("Arts".equals(type)) {
                        String artist = rs.getString("artist_name");
                        Date releaseDate = rs.getDate("release_date");
                        // Truyền fullOwner (đối tượng User) vào đây thay vì String
                        item = new Arts(fullOwner, startingPrice, desc, artist, releaseDate != null ? releaseDate.toLocalDate() : null);

                    } else if ("Electronics".equals(type)) {
                        int warranty = rs.getInt("warranty");
                        String brand = rs.getString("brand");
                        item = new Electronics(fullOwner, startingPrice, desc, warranty, brand);

                    } else if ("Vehicles".equals(type)) {
                        String brand = rs.getString("brand");
                        int mileage = rs.getInt("mileage");
                        String vehicleId = rs.getString("vehicle_id_plate");
                        item = new Vehicles(fullOwner, startingPrice, desc, brand, mileage, vehicleId);
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
    /**
     * Cập nhật chủ sở hữu của món đồ sau khi đấu giá thành công.
     */
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
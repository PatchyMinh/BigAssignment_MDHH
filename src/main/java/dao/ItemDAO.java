package dao;

import java.sql.Connection;
import java.sql.SQLException;

import model.Items;

public interface ItemDAO {
    void addItem(Items item);

    Items getItemById(int id);

    boolean updateItemOwner(Connection conn, int itemId, int newOwnerId) throws SQLException;
}
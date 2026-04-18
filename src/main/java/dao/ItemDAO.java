package dao;

import model.Items;

public interface ItemDAO {
    void addItem(Items item);

    Items getItemById(int id);


}
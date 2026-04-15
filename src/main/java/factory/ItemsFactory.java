package factory;

import model.Items;
import model.ItemsAttributes;

public interface ItemsFactory{
    Items createItems(ItemsAttributes request);
}

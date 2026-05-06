package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import model.Items;

public class ItemCardController{

    @FXML
    private Label descLabel;

    @FXML
    private ImageView imgItem;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblItemID;

    @FXML
    private Label lblOwner;

    @FXML
    private Label lblType;

    public void setItemData(Items item) {
        lblItemID.setText("ID: " + item.getItemID());
        lblCurrentPrice.setText("Giá hiện tại: " + item.getStartingPrice() + " VND");
        lblType.setText("Phân loại: " + item.getClass().getSimpleName());
        lblOwner.setText("Người sở hữu: " + item.getOwner());
    }
}

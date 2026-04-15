package com.example.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Button btnIn;

    @FXML
    private Button btnUp;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUser;

    @FXML
    void onhandleIn(ActionEvent event) {
        String username = txtUser.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Alert al1 = new Alert(Alert.AlertType.ERROR);
            al1.setContentText("Vui lòng nhập đầy đủ thông tin!");
            al1.show();
            return;
        }

        System.out.println("Tên đăng nhập: " + username);
        System.out.println("Mật khẩu: " + password);
    }

    @FXML
    void onhandleUp(ActionEvent event) {
        Alert al2 = new Alert(Alert.AlertType.INFORMATION);
        al2.setTitle("Thông báo");
        al2.setContentText("Đang phát triển");
        al2.showAndWait();
    }

}

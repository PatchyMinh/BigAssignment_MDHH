package vn.hnue.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Header - Màu xanh đặc trưng HNUE
        VBox header = new VBox();
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #1a5f7a;"); // Màu xanh đậm portal

        Label titleLabel = new Label("CỔNG ĐĂNG KÝ HỌC PHẦN | HNUE");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 22));
        header.getChildren().add(titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // 2. Form Login (Giả lập phần root của React)
        VBox loginCard = new VBox(15);
        loginCard.setPadding(new Insets(30));
        loginCard.setMaxWidth(400);
        loginCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label loginTitle = new Label("ĐĂNG NHẬP HỆ THỐNG");
        loginTitle.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
        loginTitle.setTextFill(Color.web("#333"));

        TextField txtUser = new TextField();
        txtUser.setPromptText("Mã sinh viên");
        txtUser.setPrefHeight(40);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Mật khẩu");
        txtPass.setPrefHeight(40);

        Button btnLogin = new Button("ĐĂNG NHẬP");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(40);
        btnLogin.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Hyperlink lnkForgot = new Hyperlink("Quên mật khẩu?");

        loginCard.getChildren().addAll(loginTitle, txtUser, txtPass, btnLogin, lnkForgot);
        loginCard.setAlignment(Pos.CENTER);

        // 3. Layout chính
        BorderPane root = new BorderPane();
        root.setTop(header);

        StackPane centerContainer = new StackPane(loginCard);
        centerContainer.setStyle("-fx-background-color: #f4f7f9;"); // Màu nền nhạt
        root.setCenter(centerContainer);

        // Footer
        HBox footer = new HBox();
        footer.setPadding(new Insets(10));
        footer.setAlignment(Pos.CENTER);
        footer.getChildren().add(new Label("© 2026 Trường Đại học Sư phạm Hà Nội"));
        root.setBottom(footer);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("HNUE Registration Portal");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
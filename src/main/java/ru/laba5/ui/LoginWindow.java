package ru.laba5.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.laba5.users.AuthService;

public class LoginWindow extends Application {
    private AuthService authService;

    @Override
    public void start(Stage primaryStage) {
        authService = new AuthService();
        primaryStage.setTitle("Авторизация");

        Label loginLabel = new Label("Логин:");
        TextField loginField = new TextField();
        Label passLabel = new Label("Пароль:");
        PasswordField passField = new PasswordField();
        Button loginBtn = new Button("Войти");
        Button regBtn = new Button("Регистрация");
        Label messageLabel = new Label();

        loginBtn.setOnAction(e -> {
            String login = loginField.getText();
            String pass = passField.getText();
            if (authService.login(login, pass)) {
                MainApp.showMainWindow(authService, primaryStage);
            } else {
                messageLabel.setText("Неверный логин или пароль");
            }
        });

        regBtn.setOnAction(e -> {
            String login = loginField.getText();
            String pass = passField.getText();
            if (authService.register(login, pass)) {
                messageLabel.setText("Регистрация успешна. Теперь войдите.");
            } else {
                messageLabel.setText("Ошибка регистрации (логин занят или неверный формат)");
            }
        });

        VBox root = new VBox(10, loginLabel, loginField, passLabel, passField, loginBtn, regBtn, messageLabel);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
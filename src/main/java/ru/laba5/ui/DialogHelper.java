package ru.laba5.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class DialogHelper {

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Optional<String> showInputDialog(String title, String header, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        return dialog.showAndWait();
    }

    public static Optional<String> showChoiceDialog(String title, String header, String[] choices) {
        if (choices == null || choices.length == 0) {
            return Optional.empty();
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices[0], choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    public static String showUserInputDialog() {
        TextInputDialog dialog = new TextInputDialog("SYSTEM");
        dialog.setTitle("Авторизация");
        dialog.setHeaderText("Добро пожаловать в лабораторную систему");
        dialog.setContentText("Введите имя оператора:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse("SYSTEM");
    }
}
package ru.laba5.ui;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

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

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static String showUserInputDialog() {
        TextInputDialog dialog = new TextInputDialog("SYSTEM");
        dialog.setTitle("Login");
        dialog.setHeaderText("Welcome to Laboratory Management System");
        dialog.setContentText("Enter operator name:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse("SYSTEM");
    }

    public static Optional<String> showInputDialog(String title, String header, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        return dialog.showAndWait();
    }

    public static Optional<String> showChoiceDialog(String title, String header, String[] choices) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices[0], choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    public static double showNumberInputDialog(String title, String header, String prompt) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(prompt);

            Optional<String> result = dialog.showAndWait();

            if (!result.isPresent()) {
                throw new RuntimeException("Operation cancelled by user");
            }

            String input = result.get().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid input");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid number (e.g., 7.12, 12.4, 0.5)");
                alert.showAndWait();
            }
        }
    }

    public static String showNonEmptyInputDialog(String title, String header, String prompt) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(prompt);

            Optional<String> result = dialog.showAndWait();

            if (!result.isPresent()) {
                throw new RuntimeException("Operation cancelled by user");
            }

            String input = result.get().trim();
            if (!input.isEmpty()) {
                return input;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid input");
                alert.setHeaderText(null);
                alert.setContentText("This field cannot be empty. Please enter a value.");
                alert.showAndWait();
            }
        }
    }

    public static String showRequiredChoiceDialog(String title, String header, String[] choices) {
        while (true) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices[0], choices);
            dialog.setTitle(title);
            dialog.setHeaderText(header);

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                return result.get();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Selection required");
                alert.setHeaderText(null);
                alert.setContentText("Please select an option.");
                alert.showAndWait();
            }
        }
    }

    public static Optional<Pair<String, String>> showTwoFieldDialog(String title, String header,
                                                                    String field1Label, String field2Label) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField field1 = new TextField();
        TextField field2 = new TextField();

        grid.add(new Label(field1Label + ":"), 0, 0);
        grid.add(field1, 1, 0);
        grid.add(new Label(field2Label + ":"), 0, 1);
        grid.add(field2, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Pair<>(field1.getText(), field2.getText());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
package ru.laba5.ui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.users.AuthService;

public class MainApp {

    public static void showMainWindow(AuthService authService, Stage primaryStage) {
        if (authService == null || !authService.isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }

        ExperimentManager experimentManager = new ExperimentManager(authService);
        RunManager runManager = new RunManager(authService, experimentManager);
        RunResultManager resultManager = new RunResultManager(authService, runManager, experimentManager);

        String currentUser = authService.getCurrentUsername();

        ExperimentTab experimentTab = new ExperimentTab(experimentManager, runManager, resultManager, currentUser);
        RunTab runTab = new RunTab(experimentManager, runManager, resultManager, currentUser);
        ResultTab resultTab = new ResultTab(experimentManager, runManager, resultManager, currentUser);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createTab("📊 Эксперименты", experimentTab),
                createTab("🏃 Запуски", runTab),
                createTab("📈 Результаты", resultTab)
        );

        VBox root = new VBox(createMenuBar(experimentManager, runManager, resultManager, experimentTab, runTab, resultTab),
                tabPane, createStatusBar(currentUser));
        root.setPrefSize(1200, 700);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Лабораторная система - " + currentUser);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private static MenuBar createMenuBar(ExperimentManager em, RunManager rm, RunResultManager rrm,
                                         ExperimentTab et, RunTab rt, ResultTab rst) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("Файл");
        MenuItem refreshItem = new MenuItem("Обновить всё");
        refreshItem.setOnAction(e -> {
            em.reload();
            rm.reload();
            rrm.reload();
            et.refreshData();
            rt.refreshData();
            rst.refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены из БД");
        });
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(refreshItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private static Label createStatusBar(String currentUser) {
        Label statusLabel = new Label("База данных: PostgreSQL | Пользователь: " + currentUser);
        statusLabel.setStyle("-fx-padding: 5; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        return statusLabel;
    }
}
package ru.laba5.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

public class MainApp extends Application {

    private static ExperimentManager experimentManager;
    private static RunManager runManager;
    private static RunResultManager resultManager;
    private static CsvStorage storage;
    private static ExperimentTab experimentTab;
    private static RunTab runTab;
    private static ResultTab resultTab;

    static {
        experimentManager = new ExperimentManager();
        runManager = new RunManager(experimentManager);
        resultManager = new RunResultManager(runManager);
        storage = new CsvStorage();
    }

    public static void loadData() {
        try {
            if (!storage.hasDataFiles()) {
                System.out.println("Файлы не найдены");
                return;
            }

            CsvStorage.DataContainer data = storage.load();

            experimentManager.clear();
            runManager.clear();
            resultManager.clear();

            data.experiments.values().forEach(experimentManager::add);
            data.runs.values().forEach(runManager::add);
            data.results.values().forEach(resultManager::add);

            experimentManager.syncIdGenerator();
            runManager.syncIdGenerator();
            resultManager.syncIdGenerator();

            System.out.println("Загружено: " + data.experiments.size() + " экспериментов");

            refreshAllTabs();

        } catch (Exception e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
            DialogHelper.showError("Ошибка загрузки", e.getMessage());
        }
    }

    public static void saveData() {
        try {
            storage.save(experimentManager, runManager, resultManager);
            System.out.println("Данные сохранены");
            DialogHelper.showInfo("Сохранение", "Данные сохранены успешно");
        } catch (Exception e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
            DialogHelper.showError("Ошибка сохранения", e.getMessage());
        }
    }

    public static void refreshAllTabs() {
        if (experimentTab != null) experimentTab.refreshData();
        if (runTab != null) runTab.refreshData();
        if (resultTab != null) resultTab.refreshData();
    }

    public static void reloadData() {
        System.out.println("Перезагрузка...");
        loadData();
    }

    @Override
    public void start(Stage primaryStage) {
        loadData();
        String currentUser = DialogHelper.showUserInputDialog();
        if (currentUser == null || currentUser.trim().isEmpty()) {
            currentUser = "SYSTEM";
        }

        experimentTab = new ExperimentTab(experimentManager, runManager, resultManager, currentUser);
        runTab = new RunTab(experimentManager, runManager, resultManager, currentUser);
        resultTab = new ResultTab(experimentManager, runManager, resultManager, currentUser);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createTab("📊 Эксперименты", experimentTab),
                createTab("🏃 Запуски", runTab),
                createTab("📈 Результаты", resultTab)
        );

        VBox root = new VBox(createMenuBar(), tabPane, createStatusBar(currentUser));
        root.setPrefSize(1200, 700);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Лабораторная система - " + currentUser);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> saveData());
        primaryStage.show();
    }

    private Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("Файл");

        MenuItem saveItem = new MenuItem("Сохранить");
        saveItem.setOnAction(e -> saveData());

        MenuItem refreshItem = new MenuItem("Обновить всё");
        refreshItem.setOnAction(e -> reloadData());

        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(saveItem, refreshItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);

        return menuBar;
    }

    private Label createStatusBar(String currentUser) {
        Label statusLabel = new Label("Файлы: data_experiments.csv, data_runs.csv, data_results.csv | Пользователь: " + currentUser);
        statusLabel.setStyle("-fx-padding: 5; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        return statusLabel;
    }
}
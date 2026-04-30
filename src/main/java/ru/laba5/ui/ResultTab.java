package ru.laba5.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.List;
import java.util.Optional;

public class ResultTab extends VBox {
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final String currentUser;

    private TableView<RunResult> tableView;
    private ObservableList<RunResult> data;
    private ComboBox<Run> runFilter;

    public ResultTab(ExperimentManager experimentManager,
                     RunManager runManager,
                     RunResultManager resultManager,
                     String currentUser) {
        this.runManager = runManager;
        this.resultManager = resultManager;
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setSpacing(10);
        setPadding(new Insets(10));

        HBox filterBox = createFilterBox();
        tableView = createTableView();
        HBox buttonBar = createButtonBar();

        getChildren().addAll(filterBox, tableView, buttonBar);

        refreshFilters();
        refreshData();
    }

    private HBox createFilterBox() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(5));

        Label filterLabel = new Label("Фильтр по Run:");
        runFilter = new ComboBox<>();
        runFilter.setPromptText("Все Run");
        runFilter.setOnAction(e -> refreshData());

        Button clearFilterBtn = new Button("Очистить");
        clearFilterBtn.setOnAction(e -> {
            runFilter.getSelectionModel().clearSelection();
            refreshData();
        });

        Button refreshRunsBtn = new Button("Обновить список Run");
        refreshRunsBtn.setOnAction(e -> refreshFilters());

        filterBox.getChildren().addAll(filterLabel, runFilter, clearFilterBtn, refreshRunsBtn);
        return filterBox;
    }

    private TableView<RunResult> createTableView() {
        TableView<RunResult> tv = new TableView<>();

        TableColumn<RunResult, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<RunResult, Long> runIdCol = new TableColumn<>("Run ID");
        runIdCol.setCellValueFactory(new PropertyValueFactory<>("runId"));
        runIdCol.setPrefWidth(70);

        TableColumn<RunResult, String> paramCol = new TableColumn<>("Параметр");
        paramCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getParam().name()));
        paramCol.setPrefWidth(120);

        TableColumn<RunResult, Double> valueCol = new TableColumn<>("Значение");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(100);

        TableColumn<RunResult, String> unitCol = new TableColumn<>("Единицы");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(80);

        TableColumn<RunResult, String> commentCol = new TableColumn<>("Комментарий");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setPrefWidth(250);

        TableColumn<RunResult, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        ownerCol.setPrefWidth(100);

        TableColumn<RunResult, String> createdAtCol = new TableColumn<>("Создан");
        createdAtCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt().toString().substring(0, 19)));
        createdAtCol.setPrefWidth(170);

        tv.getColumns().addAll(idCol, runIdCol, paramCol, valueCol, unitCol, commentCol, ownerCol, createdAtCol);
        return tv;
    }

    private HBox createButtonBar() {
        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> {
            MainApp.reloadData();
            refreshFilters();
            refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены");
        });

        Button addBtn = new Button("Добавить результат");
        addBtn.setOnAction(e -> showAddDialog());

        Button saveBtn = new Button("Сохранить");
        saveBtn.setOnAction(e -> MainApp.saveData());

        return new HBox(10, refreshBtn, addBtn, saveBtn);
    }

    public void refreshFilters() {
        runFilter.getItems().clear();
        runFilter.getItems().addAll(runManager.getAll());

        runFilter.setCellFactory(lv -> new ListCell<Run>() {
            @Override
            protected void updateItem(Run run, boolean empty) {
                super.updateItem(run, empty);
                setText(run == null ? "" : run.getId() + " - " + run.getName());
            }
        });
        runFilter.setButtonCell(new ListCell<Run>() {
            @Override
            protected void updateItem(Run run, boolean empty) {
                super.updateItem(run, empty);
                setText(run == null ? "" : run.getId() + " - " + run.getName());
            }
        });
    }

    public void refreshData() {
        List<RunResult> results;
        Run selectedRun = runFilter.getValue();

        if (selectedRun != null) {
            results = resultManager.getByRun(selectedRun.getId());
        } else {
            results = resultManager.getAll();
        }

        data = FXCollections.observableArrayList(results);
        tableView.setItems(data);
    }

    private void showAddDialog() {
        if (runManager.getAll().isEmpty()) {
            DialogHelper.showError("Ошибка", "Сначала создайте Run");
            return;
        }

        try {
            Run run = askForRun();
            MeasurementParam param = askForParam();

            double value = DialogHelper.showNumberInputDialog(
                    "Добавление результата",
                    "Значение для " + param.name(),
                    "Значение:");

            String unit = DialogHelper.showNonEmptyInputDialog(
                    "Добавление результата",
                    "Единицы измерения для " + param.name(),
                    "Единицы (pH, mg/L, mS/cm):");

            Optional<String> commentResult = DialogHelper.showInputDialog(
                    "Добавление результата",
                    "Комментарий (необязательно)",
                    "Комментарий:");

            String comment = commentResult.orElse("");

            long id = resultManager.getNextId();
            RunResult result = new RunResult(id, run.getId(), param, value, unit, comment, currentUser);
            resultManager.add(result);

            refreshData();
            refreshFilters();
            MainApp.saveData();

            DialogHelper.showInfo("Успех", "Результат создан с ID: " + id);

        } catch (RuntimeException e) {
            System.out.println("Операция отменена");
        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }

    private Run askForRun() {
        while (true) {
            Dialog<Run> dialog = new Dialog<>();
            dialog.setTitle("Выбор Run");
            dialog.setHeaderText("Выберите Run");

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

            ComboBox<Run> runCombo = new ComboBox<>();
            runCombo.getItems().addAll(runManager.getAll());

            runCombo.setCellFactory(lv -> new ListCell<Run>() {
                @Override
                protected void updateItem(Run run, boolean empty) {
                    super.updateItem(run, empty);
                    setText(run == null ? "" : run.getId() + " - " + run.getName());
                }
            });
            runCombo.setButtonCell(new ListCell<Run>() {
                @Override
                protected void updateItem(Run run, boolean empty) {
                    super.updateItem(run, empty);
                    setText(run == null ? "" : run.getId() + " - " + run.getName());
                }
            });

            dialog.getDialogPane().setContent(runCombo);
            dialog.setResultConverter(dialogButton -> dialogButton == okButton ? runCombo.getValue() : null);

            Optional<Run> result = dialog.showAndWait();
            if (result.isPresent()) {
                return result.get();
            }

            DialogHelper.showError("Ошибка", "Необходимо выбрать Run");
        }
    }

    private MeasurementParam askForParam() {
        String[] params = {"PH", "CONDUCTIVITY", "NITRATE"};
        String paramStr = DialogHelper.showRequiredChoiceDialog(
                "Выбор параметра",
                "Выберите параметр измерения:",
                params);
        return MeasurementParam.valueOf(paramStr);
    }
}
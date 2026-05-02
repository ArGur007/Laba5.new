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
import ru.laba5.domain.Units;
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

        Label filterLabel = new Label("Фильтр по запуску:");
        runFilter = new ComboBox<>();
        runFilter.setPromptText("Все запуски");
        runFilter.setOnAction(e -> refreshData());

        Button clearFilterBtn = new Button("Очистить");
        clearFilterBtn.setOnAction(e -> {
            runFilter.getSelectionModel().clearSelection();
            refreshData();
        });

        Button refreshRunsBtn = new Button("Обновить список запусков");
        refreshRunsBtn.setOnAction(e -> refreshFilters());

        filterBox.getChildren().addAll(filterLabel, runFilter, clearFilterBtn, refreshRunsBtn);
        return filterBox;
    }

    private TableView<RunResult> createTableView() {
        TableView<RunResult> tv = new TableView<>();

        TableColumn<RunResult, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<RunResult, Long> runIdCol = new TableColumn<>("ID запуска");
        runIdCol.setCellValueFactory(new PropertyValueFactory<>("runId"));
        runIdCol.setPrefWidth(80);

        TableColumn<RunResult, String> paramCol = new TableColumn<>("Параметр");
        paramCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getParam().name()));
        paramCol.setPrefWidth(120);

        TableColumn<RunResult, Double> valueCol = new TableColumn<>("Значение");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(100);

        TableColumn<RunResult, String> unitCol = new TableColumn<>("Единицы");
        unitCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUnit()));
        unitCol.setPrefWidth(100);

        TableColumn<RunResult, String> commentCol = new TableColumn<>("Комментарий");
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setPrefWidth(300);

        tv.getColumns().addAll(idCol, runIdCol, paramCol, valueCol, unitCol, commentCol);
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
            DialogHelper.showError("Ошибка", "Сначала создайте запуск");
            return;
        }

        try {
            Optional<Run> runResult = askForRun();
            if (!runResult.isPresent()) {
                return;
            }

            Run run = runResult.get();

            Optional<MeasurementParam> paramResult = askForParam();
            if (!paramResult.isPresent()) {
                return;
            }

            MeasurementParam param = paramResult.get();

            Optional<Double> valueResult = askForValue(param);
            if (!valueResult.isPresent()) {
                return;
            }

            double value = valueResult.get();

            Optional<Units> unitResult = askForUnit();
            if (!unitResult.isPresent()) {
                return;
            }

            Units unit = unitResult.get();

            Optional<String> commentResult = DialogHelper.showInputDialog(
                    "Добавление результата",
                    "Введите комментарий (необязательно)",
                    "Комментарий:");

            String comment = "";
            if (commentResult.isPresent()) {
                comment = commentResult.get().trim();
            }

            long id = resultManager.getNextId();
            RunResult result = new RunResult(id, run.getId(), param, value, unit.name(), comment, currentUser);
            resultManager.add(result);

            refreshData();
            refreshFilters();
            MainApp.saveData();

            DialogHelper.showInfo("Успех", "Результат создан с ID: " + id);

        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }

    private Optional<Run> askForRun() {
        Dialog<Run> dialog = new Dialog<>();
        dialog.setTitle("Выбор запуска");
        dialog.setHeaderText("Выберите запуск");

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
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return runCombo.getValue();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Optional<MeasurementParam> askForParam() {
        String[] params = {"PH", "CONDUCTIVITY", "NITRATE"};

        ChoiceDialog<String> dialog = new ChoiceDialog<>(params[0], params);
        dialog.setTitle("Выбор параметра");
        dialog.setHeaderText("Выберите параметр измерения");
        dialog.setContentText("Параметр:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            return Optional.of(MeasurementParam.valueOf(result.get()));
        }

        return Optional.empty();
    }

    private Optional<Double> askForValue(MeasurementParam param) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ввод значения");
            dialog.setHeaderText("Значение для " + param.name());
            dialog.setContentText("Значение:");

            Optional<String> result = dialog.showAndWait();

            if (!result.isPresent()) {
                return Optional.empty();
            }

            try {
                double value = Double.parseDouble(result.get().trim());
                return Optional.of(value);
            } catch (NumberFormatException e) {
                DialogHelper.showError("Ошибка", "Введите корректное число");
            }
        }
    }

    private Optional<Units> askForUnit() {
        String[] units = {"MOL_L", "MMOL_L", "MOL_ML", "MMOL_ML", "UNITLESS", "SIEMENS"};

        ChoiceDialog<String> dialog = new ChoiceDialog<>(units[0], units);
        dialog.setTitle("Выбор единиц измерения");
        dialog.setHeaderText("Выберите единицы измерения");
        dialog.setContentText("Единицы:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            return Optional.of(Units.valueOf(result.get()));
        }

        return Optional.empty();
    }
}
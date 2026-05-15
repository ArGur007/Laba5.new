package ru.laba5.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.laba5.domain.Experiment;
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
    private final ExperimentManager experimentManager;
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
        this.experimentManager = experimentManager;
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

        Button refreshBtn = new Button("Обновить списки");
        refreshBtn.setOnAction(e -> {
            runManager.reload();
            resultManager.reload();
            refreshFilters();
            refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены из БД");
        });

        filterBox.getChildren().addAll(filterLabel, runFilter, clearFilterBtn, refreshBtn);
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

        // Колонка владельца (через эксперимент)
        TableColumn<RunResult, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(cellData -> {
            long runId = cellData.getValue().getRunId();
            Run run = runManager.findById(runId);
            if (run != null) {
                Experiment exp = experimentManager.findById(run.getExperimentId());
                if (exp != null) {
                    return new SimpleStringProperty(exp.getOwnerUsername());
                }
            }
            return new SimpleStringProperty("?");
        });
        ownerCol.setPrefWidth(100);

        tv.getColumns().addAll(idCol, runIdCol, paramCol, valueCol, unitCol, commentCol, ownerCol);
        return tv;
    }

    private HBox createButtonBar() {
        Button refreshBtn = new Button("Обновить таблицу");
        refreshBtn.setOnAction(e -> {
            resultManager.reload();
            refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены");
        });

        Button addBtn = new Button("Добавить результат");
        addBtn.setOnAction(e -> showAddDialog());

        Button deleteBtn = new Button("Удалить результат");
        deleteBtn.setOnAction(e -> showDeleteDialog());

        return new HBox(10, refreshBtn, addBtn, deleteBtn);
    }

    public void refreshFilters() {
        runFilter.getItems().clear();
        // Показываем только те запуски, к которым у пользователя есть доступ (владелец эксперимента)
        List<Run> allowedRuns = runManager.getAll().stream()
                .filter(run -> {
                    Experiment exp = experimentManager.findById(run.getExperimentId());
                    return exp != null && exp.getOwnerUsername().equals(currentUser);
                })
                .toList();
        runFilter.getItems().addAll(allowedRuns);

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
            if (!runResult.isPresent()) return;

            Run run = runResult.get();

            // Проверка прав: пользователь должен быть владельцем эксперимента
            Experiment exp = experimentManager.findById(run.getExperimentId());
            if (exp == null || !exp.getOwnerUsername().equals(currentUser)) {
                DialogHelper.showError("Ошибка прав", "Вы не можете добавить результат к этому запуску");
                return;
            }

            Optional<MeasurementParam> paramResult = askForParam();
            if (!paramResult.isPresent()) return;
            MeasurementParam param = paramResult.get();

            Optional<Double> valueResult = askForValue(param);
            if (!valueResult.isPresent()) return;
            double value = valueResult.get();

            Optional<Units> unitResult = askForUnit();
            if (!unitResult.isPresent()) return;
            Units unit = unitResult.get();

            Optional<String> commentResult = DialogHelper.showInputDialog(
                    "Добавление результата",
                    "Введите комментарий (необязательно)",
                    "Комментарий:");
            String comment = commentResult.orElse("").trim();

            // Создаём результат с временным ID = 0
            RunResult result = new RunResult(run.getId(), param, value, unit.name(), comment, currentUser);
            resultManager.add(result); // БД сгенерирует ID и обновит объект

            refreshData();
            refreshFilters();
            DialogHelper.showInfo("Успех", "Результат создан, ID = " + result.getId());

        } catch (SecurityException e) {
            DialogHelper.showError("Ошибка прав", e.getMessage());
        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }

    private void showDeleteDialog() {
        RunResult selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Ошибка", "Выберите результат для удаления");
            return;
        }

        // Проверяем права через эксперимент
        Run run = runManager.findById(selected.getRunId());
        if (run == null) {
            DialogHelper.showError("Ошибка", "Запуск не найден");
            return;
        }
        Experiment exp = experimentManager.findById(run.getExperimentId());
        if (exp == null || !exp.getOwnerUsername().equals(currentUser)) {
            DialogHelper.showError("Ошибка прав", "У вас нет прав на удаление этого результата");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить результат #" + selected.getId() + "?");
        confirm.setContentText("Это действие нельзя отменить.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                resultManager.remove(selected.getId());
                refreshData();
                DialogHelper.showInfo("Успех", "Результат удалён");
            } catch (Exception e) {
                DialogHelper.showError("Ошибка", e.getMessage());
            }
        }
    }

    private Optional<Run> askForRun() {
        Dialog<Run> dialog = new Dialog<>();
        dialog.setTitle("Выбор запуска");
        dialog.setHeaderText("Выберите запуск");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        ComboBox<Run> runCombo = new ComboBox<>();
        // Показываем только запуски, доступные пользователю (владельцу эксперимента)
        List<Run> allowedRuns = runManager.getAll().stream()
                .filter(run -> {
                    Experiment exp = experimentManager.findById(run.getExperimentId());
                    return exp != null && exp.getOwnerUsername().equals(currentUser);
                })
                .toList();
        runCombo.getItems().addAll(allowedRuns);

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
            if (dialogButton == okButton) return runCombo.getValue();
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
        return result.map(MeasurementParam::valueOf);
    }

    private Optional<Double> askForValue(MeasurementParam param) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Ввод значения");
            dialog.setHeaderText("Значение для " + param.name());
            dialog.setContentText("Значение:");
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) return Optional.empty();
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
        return result.map(Units::valueOf);
    }
}
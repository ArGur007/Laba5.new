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
import ru.laba5.domain.Run;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.List;
import java.util.Optional;

public class RunTab extends VBox {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final String currentUser;

    private TableView<Run> tableView;
    private ObservableList<Run> data;
    private ComboBox<Experiment> experimentFilter;

    public RunTab(ExperimentManager expMgr, RunManager runMgr,
                  RunResultManager resMgr, String currentUser) {
        this.experimentManager = expMgr;
        this.runManager = runMgr;
        this.resultManager = resMgr;
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setSpacing(10);
        setPadding(new Insets(10));

        // Фильтр
        HBox filterBox = createFilterBox();

        // Таблица
        tableView = new TableView<>();
        setupTableColumns();

        // Кнопки
        HBox buttonBar = createButtonBar();

        getChildren().addAll(filterBox, tableView, buttonBar);

        refreshFilters();
        refreshData();
    }

    private HBox createFilterBox() {
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(5));

        Label filterLabel = new Label("Фильтр по эксперименту:");
        experimentFilter = new ComboBox<>();
        experimentFilter.setPromptText("Все эксперименты");
        experimentFilter.setOnAction(e -> refreshData());

        Button clearFilterBtn = new Button("Очистить");
        clearFilterBtn.setOnAction(e -> {
            experimentFilter.getSelectionModel().clearSelection();
            refreshData();
        });

        filterBox.getChildren().addAll(filterLabel, experimentFilter, clearFilterBtn);
        return filterBox;
    }

    private void setupTableColumns() {
        TableColumn<Run, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Run, Long> expIdCol = new TableColumn<>("Exp ID");
        expIdCol.setCellValueFactory(new PropertyValueFactory<>("experimentId"));
        expIdCol.setPrefWidth(70);

        TableColumn<Run, String> nameCol = new TableColumn<>("Название Run");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Run, String> operatorCol = new TableColumn<>("Оператор");
        operatorCol.setCellValueFactory(new PropertyValueFactory<>("operatorName"));
        operatorCol.setPrefWidth(120);

        TableColumn<Run, String> createdAtCol = new TableColumn<>("Создан");
        createdAtCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt().toString().substring(0, 19)));
        createdAtCol.setPrefWidth(170);

        tableView.getColumns().addAll(idCol, expIdCol, nameCol, operatorCol, createdAtCol);
    }

    private HBox createButtonBar() {
        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> {
            MainApp.reloadData();
            refreshFilters();
            refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены");
        });

        Button addBtn = new Button("Добавить Run");
        addBtn.setOnAction(e -> showAddDialog());

        Button showBtn = new Button("Детали");
        showBtn.setOnAction(e -> showRunDetails());

        Button saveBtn = new Button("Сохранить");
        saveBtn.setOnAction(e -> MainApp.saveData());

        return new HBox(10, refreshBtn, addBtn, showBtn, saveBtn);
    }

    public void refreshFilters() {
        experimentFilter.getItems().clear();
        experimentFilter.getItems().addAll(experimentManager.getAll());

        experimentFilter.setCellFactory(lv -> new ListCell<Experiment>() {
            @Override
            protected void updateItem(Experiment exp, boolean empty) {
                super.updateItem(exp, empty);
                setText(exp == null ? "" : exp.getId() + " - " + exp.getName());
            }
        });
        experimentFilter.setButtonCell(new ListCell<Experiment>() {
            @Override
            protected void updateItem(Experiment exp, boolean empty) {
                super.updateItem(exp, empty);
                setText(exp == null ? "" : exp.getId() + " - " + exp.getName());
            }
        });
    }

    public void refreshData() {
        List<Run> runs;
        Experiment selectedExp = experimentFilter.getValue();

        if (selectedExp != null) {
            runs = runManager.getByExperiment(selectedExp.getId());
        } else {
            runs = runManager.getAll();
        }

        data = FXCollections.observableArrayList(runs);
        tableView.setItems(data);
    }

    private void showAddDialog() {
        if (experimentManager.getAll().isEmpty()) {
            DialogHelper.showError("Ошибка", "Сначала создайте эксперимент");
            return;
        }

        try {
            Experiment experiment = askForExperiment();
            String name = DialogHelper.showNonEmptyInputDialog(
                    "Добавление Run",
                    "Название Run для эксперимента #" + experiment.getId(),
                    "Название:");

            long id = runManager.getNextId();
            Run run = new Run(id, experiment.getId(), name, currentUser);
            runManager.add(run);

            refreshFilters();
            refreshData();
            MainApp.saveData();

            DialogHelper.showInfo("Успех", "Run создан с ID: " + id);

        } catch (RuntimeException e) {
            System.out.println("Операция отменена");
        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }

    private Experiment askForExperiment() {
        while (true) {
            Dialog<Experiment> dialog = new Dialog<>();
            dialog.setTitle("Выбор эксперимента");
            dialog.setHeaderText("Выберите эксперимент");

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

            ComboBox<Experiment> expCombo = new ComboBox<>();
            expCombo.getItems().addAll(experimentManager.getAll());

            expCombo.setCellFactory(lv -> new ListCell<Experiment>() {
                @Override
                protected void updateItem(Experiment exp, boolean empty) {
                    super.updateItem(exp, empty);
                    setText(exp == null ? "" : exp.getId() + " - " + exp.getName());
                }
            });
            expCombo.setButtonCell(new ListCell<Experiment>() {
                @Override
                protected void updateItem(Experiment exp, boolean empty) {
                    super.updateItem(exp, empty);
                    setText(exp == null ? "" : exp.getId() + " - " + exp.getName());
                }
            });

            dialog.getDialogPane().setContent(expCombo);
            dialog.setResultConverter(dialogButton -> dialogButton == okButton ? expCombo.getValue() : null);

            Optional<Experiment> result = dialog.showAndWait();
            if (result.isPresent()) {
                return result.get();
            }

            DialogHelper.showError("Ошибка", "Необходимо выбрать эксперимент");
        }
    }

    private void showRunDetails() {
        Run selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Ошибка", "Выберите Run");
            return;
        }

        int resultsCount = resultManager.getByRun(selected.getId()).size();

        String details = String.format("""
                Run #%d
                Название: %s
                Эксперимент ID: %d
                Оператор: %s
                Создан: %s
                Результатов: %d
                """,
                selected.getId(),
                selected.getName(),
                selected.getExperimentId(),
                selected.getOperatorName(),
                selected.getCreatedAt().toString().substring(0, 19),
                resultsCount);

        DialogHelper.showInfo("Детали Run", details);
    }
}
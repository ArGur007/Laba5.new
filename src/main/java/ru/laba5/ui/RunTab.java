package ru.laba5.ui;

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

        HBox filterBox = createFilterBox();
        tableView = new TableView<>();
        setupTableColumns();
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

        Button refreshRunsBtn = new Button("Обновить список");
        refreshRunsBtn.setOnAction(e -> {
            experimentManager.reload();
            runManager.reload();
            refreshFilters();
            refreshData();
        });

        filterBox.getChildren().addAll(filterLabel, experimentFilter, clearFilterBtn, refreshRunsBtn);
        return filterBox;
    }

    private void setupTableColumns() {
        TableColumn<Run, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Run, Long> expIdCol = new TableColumn<>("ID эксперимента");
        expIdCol.setCellValueFactory(new PropertyValueFactory<>("experimentId"));
        expIdCol.setPrefWidth(100);

        TableColumn<Run, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Run, String> operatorCol = new TableColumn<>("Оператор");
        operatorCol.setCellValueFactory(new PropertyValueFactory<>("operatorName"));
        operatorCol.setPrefWidth(120);

        // Добавляем колонку владельца (через эксперимент)
        TableColumn<Run, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(cellData -> {
            Experiment exp = experimentManager.findById(cellData.getValue().getExperimentId());
            return new javafx.beans.property.SimpleStringProperty(exp != null ? exp.getOwnerUsername() : "?");
        });
        ownerCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, expIdCol, nameCol, operatorCol, ownerCol);
    }

    private HBox createButtonBar() {
        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> {
            experimentManager.reload();
            runManager.reload();
            refreshFilters();
            refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены из БД");
        });

        Button addBtn = new Button("Добавить запуск");
        addBtn.setOnAction(e -> showAddDialog());

        Button deleteBtn = new Button("Удалить");
        deleteBtn.setOnAction(e -> showDeleteDialog());

        return new HBox(10, refreshBtn, addBtn, deleteBtn);
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
            Optional<Experiment> expResult = askForExperiment();
            if (!expResult.isPresent()) return;

            Experiment experiment = expResult.get();

            if (!experiment.getOwnerUsername().equals(currentUser)) {
                DialogHelper.showError("Ошибка прав", "Вы не можете добавить запуск к чужому эксперименту");
                return;
            }

            Optional<String> nameResult = DialogHelper.showInputDialog(
                    "Добавление запуска",
                    "Название запуска для эксперимента #" + experiment.getId(),
                    "Название:");

            if (!nameResult.isPresent()) return;

            String name = nameResult.get().trim();
            if (name.isEmpty()) {
                DialogHelper.showError("Ошибка", "Название не может быть пустым");
                return;
            }

            // Создаём Run с временным ID 0
            Run run = new Run(experiment.getId(), name, currentUser);
            runManager.add(run); // БД генерирует ID и обновляет объект run

            refreshFilters();
            refreshData();
            DialogHelper.showInfo("Успех", "Запуск создан, ID = " + run.getId());

        } catch (SecurityException e) {
            DialogHelper.showError("Ошибка прав", e.getMessage());
        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }


    private void showDeleteDialog() {
        Run selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Ошибка", "Выберите запуск для удаления");
            return;
        }

        // Проверяем, владелец ли текущий пользователь (через эксперимент)
        Experiment exp = experimentManager.findById(selected.getExperimentId());
        if (exp == null) {
            DialogHelper.showError("Ошибка", "Эксперимент не найден");
            return;
        }
        if (!exp.getOwnerUsername().equals(currentUser)) {
            DialogHelper.showError("Ошибка прав", "У вас нет прав на удаление этого запуска");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить запуск #" + selected.getId() + "?");
        confirm.setContentText("Будут также удалены все результаты этого запуска.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                runManager.remove(selected.getId());
                refreshData();
                DialogHelper.showInfo("Успех", "Запуск удалён");
            } catch (Exception e) {
                DialogHelper.showError("Ошибка", e.getMessage());
            }
        }
    }

    private Optional<Experiment> askForExperiment() {
        Dialog<Experiment> dialog = new Dialog<>();
        dialog.setTitle("Выбор эксперимента");
        dialog.setHeaderText("Выберите эксперимент");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        ComboBox<Experiment> expCombo = new ComboBox<>();
        // Показываем только эксперименты текущего пользователя (или все, если нужно)
        expCombo.getItems().addAll(experimentManager.getByOwner(currentUser));

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
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) return expCombo.getValue();
            return null;
        });

        return dialog.showAndWait();
    }
}
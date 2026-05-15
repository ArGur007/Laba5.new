package ru.laba5.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.laba5.domain.Experiment;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.Optional;

public class ExperimentTab extends VBox {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final String currentUser;

    private TableView<Experiment> tableView;
    private ObservableList<Experiment> data;

    public ExperimentTab(ExperimentManager expMgr, RunManager runMgr,
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

        tableView = new TableView<>();
        setupTableColumns();

        HBox buttonBar = createButtonBar();

        getChildren().addAll(tableView, buttonBar);
        refreshData();
    }

    private void setupTableColumns() {
        TableColumn<Experiment, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Experiment, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Experiment, String> descCol = new TableColumn<>("Описание");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(350);

        TableColumn<Experiment, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        ownerCol.setPrefWidth(100);

        tableView.getColumns().addAll(idCol, nameCol, descCol, ownerCol);
    }

    private HBox createButtonBar() {
        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> {
            experimentManager.reload();
            refreshData();
            DialogHelper.showInfo("Обновление", "Данные обновлены из БД");
        });

        Button addBtn = new Button("Добавить эксперимент");
        addBtn.setOnAction(e -> showCreateDialog());

        Button updateBtn = new Button("Редактировать");
        updateBtn.setOnAction(e -> showUpdateDialog());

        return new HBox(10, refreshBtn, addBtn, updateBtn);
    }

    public void refreshData() {
        data = FXCollections.observableArrayList(experimentManager.getAll());
        tableView.setItems(data);
    }

    private void showCreateDialog() {
        try {
            Optional<String> nameResult = DialogHelper.showInputDialog(
                    "Создание эксперимента",
                    "Введите название эксперимента",
                    "Название:");

            if (!nameResult.isPresent()) return;

            String name = nameResult.get().trim();
            if (name.isEmpty()) {
                DialogHelper.showError("Ошибка", "Название не может быть пустым");
                return;
            }

            Optional<String> descResult = DialogHelper.showInputDialog(
                    "Создание эксперимента",
                    "Введите описание (необязательно)",
                    "Описание:");

            String description = descResult.map(String::trim).orElse("");

            // Создаём временный объект без ID (ID присвоит БД)
            Experiment exp = new Experiment( name, description, currentUser);
            experimentManager.add(exp); // внутри менеджера ID проставится автоматически

            refreshData();
            DialogHelper.showInfo("Успех", "Эксперимент создан, ID = " + exp.getId());

        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }

    private void showUpdateDialog() {
        Experiment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Ошибка", "Выберите эксперимент");
            return;
        }

        if (!selected.getOwnerUsername().equals(currentUser)) {
            DialogHelper.showError("Ошибка", "У вас нет прав на изменение этого эксперимента");
            return;
        }

        try {
            String[] choices = {"название", "описание"};
            Optional<String> fieldResult = DialogHelper.showChoiceDialog(
                    "Редактирование эксперимента",
                    "Выберите поле для изменения:",
                    choices);

            if (!fieldResult.isPresent()) return;

            String field = fieldResult.get();
            Experiment updated = null;

            if (field.equals("название")) {
                Optional<String> nameResult = DialogHelper.showInputDialog(
                        "Редактирование эксперимента",
                        "Новое название для эксперимента #" + selected.getId(),
                        "Название:");

                if (!nameResult.isPresent()) return;

                String newName = nameResult.get().trim();
                if (newName.isEmpty()) {
                    DialogHelper.showError("Ошибка", "Название не может быть пустым");
                    return;
                }
                updated = selected.updateName(newName);

            } else if (field.equals("описание")) {
                Optional<String> descResult = DialogHelper.showInputDialog(
                        "Редактирование эксперимента",
                        "Новое описание для эксперимента #" + selected.getId(),
                        "Описание:");

                if (!descResult.isPresent()) return;

                String newDesc = descResult.get().trim();
                updated = selected.updateDescription(newDesc);
            }

            if (updated != null) {
                experimentManager.update(updated); // теперь без currentUser
                refreshData();
                DialogHelper.showInfo("Успех", "Эксперимент обновлён");
            }

        } catch (SecurityException e) {
            DialogHelper.showError("Ошибка прав", e.getMessage());
        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }
}
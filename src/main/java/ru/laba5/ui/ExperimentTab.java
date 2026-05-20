package ru.laba5.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.laba5.db.dao.HistoryDAO;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.HistoryRecord;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableRow;

import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        Button deleteBtn = new Button("Удалить");
        deleteBtn.setOnAction(e -> showDeleteDialog());

        Button historyBtn = new Button("История");
        historyBtn.setOnAction(e -> showHistoryDialog());

        return new HBox(10, refreshBtn, addBtn, updateBtn, deleteBtn, historyBtn);
    }

    public void refreshData() {
        data = FXCollections.observableArrayList(experimentManager.getAll());
        tableView.setItems(data);
        tableView.refresh();
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
                experimentManager.update(updated);
                experimentManager.reload();
                refreshData();
                DialogHelper.showInfo("Успех", "Эксперимент обновлён");
            }

        } catch (SecurityException e) {
            DialogHelper.showError("Ошибка прав", e.getMessage());
        } catch (Exception e) {
            DialogHelper.showError("Ошибка", e.getMessage());
        }
    }

    private void showDeleteDialog() {
        Experiment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Ошибка", "Выберите эксперимент для удаления");
            return;
        }

        if (!selected.getOwnerUsername().equals(currentUser)) {
            DialogHelper.showError("Ошибка прав", "У вас нет прав на удаление этого эксперимента");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удалить эксперимент #" + selected.getId() + "?");
        confirm.setContentText("Будут также удалены все запуски и результаты этого эксперимента.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                experimentManager.remove(selected.getId()); // менеджер сам проверит права и удалит из БД
                refreshData();
                DialogHelper.showInfo("Успех", "Эксперимент удалён");
            } catch (Exception e) {
                DialogHelper.showError("Ошибка", e.getMessage());
            }
        }
    }
    private void showHistoryDialog() {
        Experiment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Ошибка", "Выберите эксперимент для просмотра истории");
            return;
        }

        try {
            List<HistoryRecord> history = HistoryDAO.getHistoryForExperiment(selected.getId());
            if (history.isEmpty()) {
                DialogHelper.showInfo("История", "История изменений пуста");
                return;
            }

            Stage historyStage = new Stage();
            historyStage.setTitle("История эксперимента #" + selected.getId() + " - " + selected.getName());

            TableView<HistoryRecord> table = new TableView<>();

            // Колонка: тип сущности
            TableColumn<HistoryRecord, String> entityCol = new TableColumn<>("Сущность");
            entityCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEntityType()));

            // Колонка: ID сущности
            TableColumn<HistoryRecord, String> entityIdCol = new TableColumn<>("ID сущности");
            entityIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getEntityId())));

            // Колонка: поле
            TableColumn<HistoryRecord, String> fieldCol = new TableColumn<>("Поле");
            fieldCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFieldName()));

            // Колонка: старое значение
            TableColumn<HistoryRecord, String> oldCol = new TableColumn<>("Старое значение");
            oldCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOldValue() != null ? cellData.getValue().getOldValue() : ""));

            // Колонка: новое значение
            TableColumn<HistoryRecord, String> newCol = new TableColumn<>("Новое значение");
            newCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNewValue() != null ? cellData.getValue().getNewValue() : ""));

            // Колонка: дата (отформатированная)
            TableColumn<HistoryRecord, String> dateCol = new TableColumn<>("Дата изменения");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    .withLocale(java.util.Locale.getDefault());
            dateCol.setCellValueFactory(cellData -> {
                Instant instant = cellData.getValue().getChangedAt();
                String formatted = formatter.format(instant.atZone(java.time.ZoneId.systemDefault()));
                return new SimpleStringProperty(formatted);
            });

            // Колонка: кем изменено
            TableColumn<HistoryRecord, String> userCol = new TableColumn<>("Кем изменено");
            userCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getChangedBy()));

            table.getColumns().addAll(entityCol, entityIdCol, fieldCol, oldCol, newCol, dateCol, userCol);
            ObservableList<HistoryRecord> data = FXCollections.observableArrayList(history);
            table.setItems(data);

            // Зелёная подсветка последней записи
            table.setRowFactory(tv -> new TableRow<HistoryRecord>() {
                @Override
                protected void updateItem(HistoryRecord item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                    } else {
                        boolean isDeletion = "deletion".equals(item.getFieldName());
                        boolean isLast = (history.indexOf(item) == history.size() - 1);

                        if (isDeletion) {
                            setStyle("-fx-background-color: #ffcccc;"); // красный
                        } else if (isLast) {
                            setStyle("-fx-background-color: lightgreen;"); // зелёный
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            VBox vbox = new VBox(table);
            vbox.setPadding(new Insets(10));
            Scene scene = new Scene(vbox, 900, 500);
            historyStage.setScene(scene);
            historyStage.show();

        } catch (SQLException e) {
            DialogHelper.showError("Ошибка", "Не удалось загрузить историю: " + e.getMessage());
        }
    }
}
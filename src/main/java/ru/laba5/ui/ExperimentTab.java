package ru.laba5.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.util.List;
import java.util.Map;
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

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> {
            System.out.println("Refresh нажат - перезагружаем данные из файла...");
            MainApp.reloadData();
            refreshData();
            DialogHelper.showInfo("Refresh", "Данные обновлены из файла");
        });

        Button createBtn = new Button("Create Experiment");
        createBtn.setOnAction(e -> showCreateDialog());

        Button updateBtn = new Button("Update");
        updateBtn.setOnAction(e -> showUpdateDialog());

        Button showBtn = new Button("Show Details");
        showBtn.setOnAction(e -> showExperimentDetails());

        Button summaryBtn = new Button("Summary");
        summaryBtn.setOnAction(e -> showSummary());

        Button saveBtn = new Button("Save to CSV");
        saveBtn.setOnAction(e -> saveData());

        HBox buttonBar = new HBox(10, refreshBtn, createBtn, updateBtn, showBtn, summaryBtn, saveBtn);

        getChildren().addAll(tableView, buttonBar);
        refreshData();
    }

    private void setupTableColumns() {
        TableColumn<Experiment, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<Experiment, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Experiment, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(350);

        TableColumn<Experiment, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));
        ownerCol.setPrefWidth(100);

        TableColumn<Experiment, String> createdAtCol = new TableColumn<>("Created");
        createdAtCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt().toString().substring(0, 19)));
        createdAtCol.setPrefWidth(150);

        tableView.getColumns().addAll(idCol, nameCol, descCol, ownerCol, createdAtCol);
    }

    void refreshData() {
        data = FXCollections.observableArrayList(experimentManager.getAll());
        tableView.setItems(data);
        System.out.println("Таблица экспериментов обновлена, записей: " + data.size());
    }

    private void showCreateDialog() {
        try {
            String name = DialogHelper.showNonEmptyInputDialog(
                    "Create Experiment",
                    "Enter experiment name:",
                    "Name:");

            Optional<String> descResult = DialogHelper.showInputDialog(
                    "Create Experiment",
                    "Enter description (optional):",
                    "Description:");

            String description = descResult.orElse("");

            long id = experimentManager.getNextId();
            Experiment exp = new Experiment(id, name, description, currentUser);
            experimentManager.add(exp);
            refreshData();
            MainApp.saveData();  // Сохраняем в файл
            DialogHelper.showInfo("Success", "Experiment created with ID: " + id);

        } catch (RuntimeException e) {
            System.out.println("Operation cancelled by user");
        } catch (Exception e) {
            DialogHelper.showError("Error", e.getMessage());
        }
    }

    private void showUpdateDialog() {
        Experiment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Error", "Please select an experiment to update");
            return;
        }

        try {
            String[] choices = {"name", "description"};
            String field = DialogHelper.showRequiredChoiceDialog(
                    "Update Experiment",
                    "Select field to update:",
                    choices);

            String newValue;
            if (field.equals("name")) {
                newValue = DialogHelper.showNonEmptyInputDialog(
                        "Update Experiment",
                        "New name for experiment #" + selected.getId() + ":",
                        "Name:");
            } else {
                Optional<String> result = DialogHelper.showInputDialog(
                        "Update Experiment",
                        "New description for experiment #" + selected.getId() + ":",
                        "Description:");
                newValue = result.orElse("");
            }

            Experiment updated;
            if (field.equals("name")) {
                updated = selected.updateName(newValue);
            } else {
                updated = selected.updateDescription(newValue);
            }

            experimentManager.update(updated);
            refreshData();
            MainApp.saveData();
            DialogHelper.showInfo("Success", "Experiment updated");

        } catch (RuntimeException e) {
            System.out.println("Operation cancelled by user");
        } catch (Exception e) {
            DialogHelper.showError("Error", e.getMessage());
        }
    }

    private void showExperimentDetails() {
        Experiment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Error", "Please select an experiment");
            return;
        }

        List<ru.laba5.domain.Run> runs = runManager.getByExperiment(selected.getId());

        StringBuilder details = new StringBuilder();
        details.append("Experiment #").append(selected.getId()).append("\n");
        details.append("Name: ").append(selected.getName()).append("\n");
        details.append("Description: ").append(selected.getDescription()).append("\n");
        details.append("Owner: ").append(selected.getOwnerUsername()).append("\n");
        details.append("Created: ").append(selected.getCreatedAt()).append("\n");
        details.append("Updated: ").append(selected.getUpdatedAt()).append("\n");
        details.append("Runs: ").append(runs.size()).append("\n");

        DialogHelper.showInfo("Experiment Details", details.toString());
    }

    private void showSummary() {
        Experiment selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogHelper.showError("Error", "Please select an experiment");
            return;
        }

        Map<MeasurementParam, RunResultManager.Summary> stats =
                resultManager.getSummaryByExperiment(selected.getId());

        if (stats.isEmpty()) {
            DialogHelper.showInfo("Summary", "No data available for experiment #" + selected.getId());
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Summary for Experiment: ").append(selected.getName()).append("\n\n");

        for (Map.Entry<MeasurementParam, RunResultManager.Summary> entry : stats.entrySet()) {
            sb.append(entry.getKey()).append(":\n");
            sb.append("  Count: ").append(entry.getValue().getCount()).append("\n");
            sb.append("  Min: ").append(String.format("%.2f", entry.getValue().getMin())).append("\n");
            sb.append("  Max: ").append(String.format("%.2f", entry.getValue().getMax())).append("\n");
            sb.append("  Avg: ").append(String.format("%.2f", entry.getValue().getAvg())).append("\n\n");
        }

        DialogHelper.showInfo("Experiment Summary", sb.toString());
    }

    private void saveData() {
        MainApp.saveData();
    }
}
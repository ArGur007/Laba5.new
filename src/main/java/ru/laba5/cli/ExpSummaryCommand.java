package ru.laba5.cli;

import ru.laba5.domain.MeasurementParam;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunResultManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExpSummaryCommand implements Command {
    private final ExperimentManager experimentManager;
    private final RunResultManager resultManager;
    private final Scanner scanner;

    public ExpSummaryCommand(ExperimentManager experimentManager, RunResultManager resultManager, Scanner scanner) {
        this.experimentManager = experimentManager;
        this.resultManager = resultManager;
        this.scanner = scanner;
    }

    @Override
    public void execute(List<String> args) {
        System.out.print("Введите experiment_id: ");
        String expIdInput = scanner.nextLine().trim();
        long expId;
        try {
            expId = Long.parseLong(expIdInput);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: experiment_id должен быть числом");
            return;
        }

        if (experimentManager.findById(expId) == null) {
            System.out.println("Эксперимент не найден");
            return;
        }

        Map<MeasurementParam, RunResultManager.Summary> summary = resultManager.getSummaryByExperiment(expId);
        if (summary.isEmpty()) {
            System.out.println("Нет данных для анализа");
            return;
        }

        System.out.println("Сводка по эксперименту #" + expId + ":");
        for (Map.Entry<MeasurementParam, RunResultManager.Summary> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
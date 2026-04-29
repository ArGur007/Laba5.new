package ru.laba5.cli;

import ru.laba5.domain.Run;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;

import java.util.List;
import java.util.Scanner;

public class RunListCommand implements Command {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final Scanner scanner;

    public RunListCommand(ExperimentManager experimentManager, RunManager runManager, Scanner scanner) {
        this.experimentManager = experimentManager;
        this.runManager = runManager;
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

        List<Run> runs = runManager.getByExperiment(expId);
        if (runs.isEmpty()) {
            System.out.println("Запусков для данного эксперимента нет");
            return;
        }

        // Обработка флага --last N (опционально)
        // Здесь можно добавить парсинг args
        System.out.println("\nID  Run name            Operator   Time");
        System.out.println("-".repeat(45));
        for (Run r : runs) {
            System.out.printf("%-3d %-19s %-10s %s\n",
                    r.getId(),
                    r.getName(),
                    r.getOperatorName(),
                    r.getCreatedAt().toString().replace("T", " ").substring(0, 16));
        }
    }
}
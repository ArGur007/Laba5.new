package ru.laba5.cli;

import ru.laba5.domain.Experiment;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;

import java.util.List;
import java.util.Scanner;

public class ExpShowCommand implements Command {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final Scanner scanner;

    public ExpShowCommand(ExperimentManager experimentManager, RunManager runManager, Scanner scanner) {
        this.experimentManager = experimentManager;
        this.runManager = runManager;
        this.scanner = scanner;
    }

    @Override
    public void execute(List<String> args) {
        System.out.print("Введите ID эксперимента: ");
        String input = scanner.nextLine().trim();
        try {
            long id = Long.parseLong(input);
            Experiment exp = experimentManager.findById(id);
            if (exp == null) {
                System.out.println("Эксперимент с ID " + id + " не найден");
                return;
            }
            int runsCount = runManager.getByExperiment(id).size();
            System.out.println("Experiment #" + exp.getId());
            System.out.println("name: " + exp.getName());
            System.out.println("description: " + exp.getDescription());
            System.out.println("owner: " + exp.getOwnerUsername());
            System.out.println("created: " + exp.getCreatedAt());
            System.out.println("runs: " + runsCount);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом");
        }
    }
}
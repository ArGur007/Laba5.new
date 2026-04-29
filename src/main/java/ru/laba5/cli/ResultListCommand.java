package ru.laba5.cli;

import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.List;
import java.util.Scanner;

public class ResultListCommand implements Command {
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final Scanner scanner;

    public ResultListCommand(RunManager runManager, RunResultManager resultManager, Scanner scanner) {
        this.runManager = runManager;
        this.resultManager = resultManager;
        this.scanner = scanner;
    }

    @Override
    public void execute(List<String> args) {
        System.out.print("Введите run_id: ");
        String runIdInput = scanner.nextLine().trim();
        long runId;
        try {
            runId = Long.parseLong(runIdInput);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: run_id должен быть числом");
            return;
        }

        Run run = runManager.findById(runId);
        if (run == null) {
            System.out.println("Запуск #" + runId + " не найден");
            return;
        }

        List<RunResult> results = resultManager.getByRun(runId);

        if (results.isEmpty()) {
            System.out.println("Результатов нет");
            return;
        }

        System.out.printf("%-5s | %-12s | %-10s | %-6s | %s\n",
                "ID", "Параметр", "Значение", "Unit", "Комментарий");
        System.out.println("-".repeat(60));
        for (RunResult res : results) {
            System.out.printf("%-5d | %-12s | %-10.2f | %-6s | %s\n",
                    res.getId(),
                    res.getParam().name(),
                    res.getValue(),
                    res.getUnit(),
                    res.getComment());
        }
    }
}
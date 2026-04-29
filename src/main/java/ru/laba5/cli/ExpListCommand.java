package ru.laba5.cli;

import ru.laba5.domain.Experiment;
import ru.laba5.service.ExperimentManager;

import java.util.List;

public class ExpListCommand implements Command {
    private final ExperimentManager experimentManager;
    private final String currentOwner;

    public ExpListCommand(ExperimentManager experimentManager, String currentOwner) {
        this.experimentManager = experimentManager;
        this.currentOwner = currentOwner;
    }

    @Override
    public void execute(List<String> args) {
        List<Experiment> list;
        if (args.contains("--mine")) {
            list = experimentManager.getByOwner(currentOwner);
            System.out.println("\n--- Ваши эксперименты (оператор: " + currentOwner + ") ---");
        } else {
            list = experimentManager.getAll();
            System.out.println("\n--- Список всех экспериментов ---");
        }

        if (list.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }

        System.out.printf("%-5s | %-30s | %-15s | %-10s\n", "ID", "Название", "Создан", "Владелец");
        System.out.println("-".repeat(70));
        for (Experiment e : list) {
            System.out.printf("%-5d | %-30s | %-15s | %-10s\n",
                    e.getId(),
                    e.getName(),
                    e.getCreatedAt().toString().substring(0, 10),
                    e.getOwnerUsername());
        }
    }
}
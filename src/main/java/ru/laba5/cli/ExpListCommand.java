package ru.laba5.cli;

import ru.laba5.domain.Experiment;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;

import java.util.List;

public class ExpListCommand implements Command {
    private final ExperimentManager experimentManager;
    private final AuthService authService;

    public ExpListCommand(ExperimentManager experimentManager, AuthService authService) {
        this.experimentManager = experimentManager;
        this.authService = authService;
    }

    @Override
    public void execute(List<String> args) {
        List<Experiment> list;

        if (args.contains("--mine")) {
            if (!authService.isAuthenticated()) {
                System.out.println("Ошибка: нужно войти в систему для просмотра своих экспериментов");
                return;
            }
            list = experimentManager.getByOwner(authService.getCurrentUsername());
            System.out.println("\n--- Ваши эксперименты (владелец: " + authService.getCurrentUsername() + ") ---");
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
            String name = e.getName();
            if (name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            System.out.printf("%-5d | %-30s | %-15s | %-10s\n",
                    e.getId(),
                    name,
                    e.getCreatedAt().toString().substring(0, 10),
                    e.getOwnerUsername());
        }
    }
}
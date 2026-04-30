package ru.laba5;

import ru.laba5.cli.CommandRegistry;
import ru.laba5.cli.HelpCommand;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.util.ArrayList;
import java.util.Scanner;

public class Mcxain {

    public static void main(String[] args) {
        ExperimentManager experimentManager = new ExperimentManager();
        RunManager runManager = new RunManager(experimentManager);
        RunResultManager resultManager = new RunResultManager(runManager);
        CsvStorage storage = new CsvStorage();

        // Загрузка данных при старте
        loadInitialData(experimentManager, runManager, resultManager, storage);

        Scanner scanner = new Scanner(System.in);
        String owner = promptUser(scanner);

        CommandRegistry registry = new CommandRegistry(
                experimentManager, runManager, resultManager,
                scanner, owner, storage
        );

        printWelcome(owner);
        new HelpCommand().execute(new ArrayList<>());

        // Главный цикл
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Завершение работы.");
                break;
            }

            registry.execute(input);
        }

        scanner.close();
    }

    private static void loadInitialData(ExperimentManager experimentManager,
                                        RunManager runManager,
                                        RunResultManager resultManager,
                                        CsvStorage storage) {
        try {
            if (storage.hasDataFiles()) {
                CsvStorage.DataContainer data = storage.load();

                experimentManager.clear();
                runManager.clear();
                resultManager.clear();

                data.experiments.values().forEach(experimentManager::add);
                data.runs.values().forEach(runManager::add);
                data.results.values().forEach(resultManager::add);

                experimentManager.syncIdGenerator();
                runManager.syncIdGenerator();
                resultManager.syncIdGenerator();

                System.out.println("Загружено: " + data.experiments.size() + " экспериментов, " +
                        data.runs.size() + " запусков, " + data.results.size() + " результатов");
            } else {
                System.out.println("Файлы не найдены. Начинаем с пустыми данными.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    private static String promptUser(Scanner scanner) {
        System.out.print("Введите имя оператора (Enter для SYSTEM): ");
        String owner = scanner.nextLine().trim();
        return owner.isEmpty() ? "SYSTEM" : owner;
    }

    private static void printWelcome(String owner) {
        System.out.println("=".repeat(60));
        System.out.println("  ИНФОХИМИЯ: СИСТЕМА УПРАВЛЕНИЯ ЭКСПЕРИМЕНТАМИ");
        System.out.println("=".repeat(60));
        System.out.println("Здравствуйте, " + owner + "!");
    }
}
package ru.laba5;

import ru.laba5.cli.CommandRegistry;
import ru.laba5.cli.HelpCommand;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Mcxain {
    public static void main(String[] args) {
        ExperimentManager experimentManager = new ExperimentManager();
        RunManager runManager = new RunManager(experimentManager);
        RunResultManager resultManager = new RunResultManager(runManager);

        // Автоматическая загрузка, если указан аргумент --load
        if (args.length >= 2 && args[0].equals("--load")) {
            Path loadPath = Paths.get(args[1]);
            try {
                CsvStorage tempStorage = new CsvStorage();
                CsvStorage.DataContainer data = tempStorage.load(loadPath);
                experimentManager.clear();
                runManager.clear();
                resultManager.clear();
                data.experiments.values().forEach(experimentManager::add);
                data.runs.values().forEach(runManager::add);
                data.results.values().forEach(resultManager::add);
                experimentManager.syncIdGenerator();
                runManager.syncIdGenerator();
                resultManager.syncIdGenerator();
                System.out.println("Автоматическая загрузка из " + loadPath);
            } catch (Exception e) {
                System.out.println("Не удалось загрузить файл: " + e.getMessage());
                System.out.println("Продолжаем с пустыми данными.");
            }
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("=".repeat(60));
        System.out.println("  ИНФОХИМИЯ: СИСТЕМА УПРАВЛЕНИЯ ЭКСПЕРИМЕНТАМИ");
        System.out.println("=".repeat(60));

        System.out.print("Введите имя оператора (Enter для SYSTEM): ");
        String owner = scanner.nextLine().trim();
        if (owner.isEmpty()) {
            owner = "SYSTEM";
        }

        // Создаём экземпляр CsvStorage для работы команд save/load
        CsvStorage storage = new CsvStorage();
        CommandRegistry registry = new CommandRegistry(experimentManager, runManager, resultManager, scanner, owner, storage);

        System.out.println("\nЗдравствуйте, " + owner + "!");
        new HelpCommand().execute(new ArrayList<>());

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Завершение работы. Все данные сохранены в памяти.");
                break;
            }

            registry.execute(input);
        }

        scanner.close();
    }
}
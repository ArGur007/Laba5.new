package ru.laba5;

import ru.laba5.cli.CommandRegistry;
import ru.laba5.cli.HelpCommand;
import ru.laba5.db.DatabaseConnection;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Mcxain {

    public static void main(String[] args) {
        // Проверка подключения к БД
        try {
            DatabaseConnection.testConnection();
            System.out.println("Подключение к PostgreSQL установлено.");
        } catch (SQLException e) {
            System.err.println("Критическая ошибка: не удалось подключиться к базе данных.");
            System.err.println("Причина: " + e.getMessage());
            System.err.println("Проверьте, запущен ли PostgreSQL и корректны ли настройки в database.properties");
            System.exit(1);
        }

        AuthService authService = new AuthService();
        ExperimentManager experimentManager = new ExperimentManager(authService);
        RunManager runManager = new RunManager(authService, experimentManager);
        RunResultManager resultManager = new RunResultManager(authService, runManager, experimentManager);

        Scanner scanner = new Scanner(System.in);
        CommandRegistry registry = new CommandRegistry(
                experimentManager, runManager, resultManager,
                scanner, authService
        );

        System.out.println("=".repeat(60));
        System.out.println("  ИНФОХИМИЯ: СИСТЕМА УПРАВЛЕНИЯ ЭКСПЕРИМЕНТАМИ (PostgreSQL)");
        System.out.println("=".repeat(60));

        new HelpCommand().execute(new ArrayList<>());

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
}
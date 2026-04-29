package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.util.*;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final Scanner scanner;
    private final String currentUser;
    private final CsvStorage storage;

    public CommandRegistry(ExperimentManager experimentManager,
                           RunManager runManager,
                           RunResultManager resultManager,
                           Scanner scanner,
                           String currentUser,
                           CsvStorage storage) {
        this.experimentManager = experimentManager;
        this.runManager = runManager;
        this.resultManager = resultManager;
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.storage = storage;
        registerCommands();
    }

    private void registerCommands() {
        InputReader reader = new InputReader(scanner);

        commands.put("help", new HelpCommand());
        commands.put("exp_create", new ExpCreateCommand(experimentManager, reader, currentUser));
        commands.put("exp_list", new ExpListCommand(experimentManager, currentUser));
        commands.put("exp_show", new ExpShowCommand(experimentManager, runManager, scanner));
        commands.put("exp_update", new ExpUpdateCommand(experimentManager, reader, currentUser));
        commands.put("run_add", new RunCreateCommand(experimentManager, runManager, reader, currentUser));
        commands.put("run_list", new RunListCommand(experimentManager, runManager, scanner));
        commands.put("run_show", new RunShowCommand(runManager, resultManager, scanner));
        commands.put("res_add", new ResultAddCommand(runManager, resultManager, reader, currentUser));
        commands.put("res_list", new ResultListCommand(runManager, resultManager, scanner));
        commands.put("exp_summary", new ExpSummaryCommand(experimentManager, resultManager, scanner));
        commands.put("save", new SaveCommand(experimentManager, runManager, resultManager, storage));
        commands.put("load", new LoadCommand(experimentManager, runManager, resultManager, storage));
    }

    public void execute(String input) {
        if (input == null || input.trim().isEmpty()) return;

        String[] parts = input.trim().split("\\s+", 2);
        String cmdName = parts[0].toLowerCase();

        List<String> args = new ArrayList<>();
        if (parts.length > 1) {
            args = Arrays.asList(parts[1].split("\\s+"));
        }

        Command cmd = commands.get(cmdName);
        if (cmd != null) {
            try {
                cmd.execute(args);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("Причина: " + e.getCause().getMessage());
                }
            }
        } else {
            System.out.println("Неизвестная команда: " + cmdName);
            System.out.println("Введите 'help' для списка доступных команд.");
        }
    }
}
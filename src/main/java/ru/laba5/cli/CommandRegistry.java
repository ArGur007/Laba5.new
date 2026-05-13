package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.util.*;

public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();
    private final Scanner scanner;
    private final InputReader reader;
    private final AuthService authService;

    public CommandRegistry(ExperimentManager experimentManager,
                           RunManager runManager,
                           RunResultManager resultManager,
                           Scanner scanner,
                           AuthService authService,
                           CsvStorage storage) {
        this.scanner = scanner;
        this.reader = new InputReader(scanner);
        this.authService = authService;
        registerCommands(experimentManager, runManager, resultManager, storage);
    }

    private void registerCommands(ExperimentManager experimentManager,
                                  RunManager runManager,
                                  RunResultManager resultManager,
                                  CsvStorage storage) {

        commands.put("help", new HelpCommand());
        commands.put("whoami", new WhoamiCommand(authService));
        commands.put("logout", new LogoutCommand(authService));
        commands.put("register", new RegisterCommand(authService, reader));
        commands.put("login", new LoginCommand(authService, reader));

        commands.put("exp_create", new ExpCreateCommand(experimentManager, authService, reader));
        commands.put("exp_list", new ExpListCommand(experimentManager, authService));
        commands.put("exp_show", new ExpShowCommand(experimentManager, runManager, scanner));
        commands.put("exp_update", new ExpUpdateCommand(experimentManager, authService, reader));

        commands.put("run_add", new RunCreateCommand(experimentManager, runManager, authService, reader));
        commands.put("run_list", new RunListCommand(experimentManager, runManager, scanner));
        commands.put("run_show", new RunShowCommand(runManager, resultManager, scanner));

        commands.put("res_add", new ResultAddCommand(runManager, resultManager, authService, reader));
        commands.put("res_list", new ResultListCommand(runManager, resultManager, scanner));
        commands.put("exp_summary", new ExpSummaryCommand(experimentManager, resultManager, scanner));

        commands.put("save", new SaveCommand(experimentManager, runManager, resultManager, storage));
        commands.put("load", new LoadCommand(experimentManager, runManager, resultManager, storage));
        commands.put("exp_remove", new ExpRemoveCommand(experimentManager, authService, reader));
        commands.put("run_remove", new RunRemoveCommand(runManager, authService, reader));
        commands.put("res_remove", new ResultRemoveCommand(resultManager, runManager, authService, reader));
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
            }
        } else {
            System.out.println("Неизвестная команда: " + cmdName);
            System.out.println("Введите 'help' для списка команд");
        }
    }
}